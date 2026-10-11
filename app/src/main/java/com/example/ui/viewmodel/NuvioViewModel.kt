package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.DownloadTask
import com.example.data.model.MediaItem
import com.example.data.model.MetaDetail
import com.example.data.model.ServerConnectionState
import com.example.data.model.StreamItem
import com.example.data.model.TokenItem
import com.example.data.model.VideoEpisode
import com.example.data.repository.DownloadRepository
import com.example.data.repository.MediaRepository
import com.example.util.DeviceCodecHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val heroItems: List<MediaItem> = emptyList(),
    val latestMovies: List<MediaItem> = emptyList(),
    val popularMovies: List<MediaItem> = emptyList(),
    val latestSeries: List<MediaItem> = emptyList(),
    val popularSeries: List<MediaItem> = emptyList(),
    val selectedGenre: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SearchUiState(
    val query: String = "",
    val typeFilter: String = "all", // "all", "movie", "series"
    val results: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false
)

private fun isCombinedStreamItem(stream: StreamItem): Boolean {
    val title = (stream.title ?: stream.name ?: "").trim()
    val fn = stream.displayFileName
    return title.contains("COMBINED", ignoreCase = true) ||
           title.contains("Batch", ignoreCase = true) ||
           title.contains("Full Season", ignoreCase = true) ||
           title.contains("Complete", ignoreCase = true) ||
           title.contains("[E01-", ignoreCase = true) ||
           title.contains("E01-E", ignoreCase = true) ||
           fn.contains("COMBINED", ignoreCase = true) ||
           fn.contains("[E01-", ignoreCase = true) ||
           fn.contains("Full Season", ignoreCase = true)
}

data class DetailUiState(
    val mediaItem: MediaItem? = null,
    val metaDetail: MetaDetail? = null,
    val seasons: List<Int> = emptyList(),
    val selectedSeason: Int = 1,
    val seasonEpisodes: List<VideoEpisode> = emptyList(),
    val combinedFiles: List<VideoEpisode> = emptyList(),
    val selectedEpisode: VideoEpisode? = null,
    val isViewingCombinedTab: Boolean = false,
    val isSeasonCombinedOnly: Boolean = false,
    val streams: List<StreamItem> = emptyList(),
    val episodeStreams: List<StreamItem> = emptyList(),
    val isLoadingMeta: Boolean = false,
    val isLoadingStreams: Boolean = false,
    val isLoadingEpisodeStreams: Boolean = false,
    val isInWatchlist: Boolean = false,
    val streamErrorMessage: String? = null
)

data class PlayerPlaybackState(
    val mediaId: String = "",
    val title: String = "",
    val type: String = "movie",
    val poster: String? = null,
    val backdrop: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val episodeTitle: String? = null,
    val streamItem: StreamItem? = null,
    val availableStreams: List<StreamItem> = emptyList(),
    val initialPositionMs: Long = 0L,
    val nextEpisode: VideoEpisode? = null
)

class NuvioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val downloadRepository = DownloadRepository(application)

    val downloads: StateFlow<List<DownloadTask>> = downloadRepository.downloads
    val downloadPath: StateFlow<String> = downloadRepository.customPath

    val serverState: StateFlow<ServerConnectionState> = repository.serverState

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val continueWatching: StateFlow<List<WatchHistoryEntity>> = repository.continueWatching.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _playerPlayback = MutableStateFlow<PlayerPlaybackState?>(null)
    val playerPlayback: StateFlow<PlayerPlaybackState?> = _playerPlayback.asStateFlow()

    init {
        // Pre-seed home catalog from fallback/cache so the app opens instantly with zero blank buffering
        val initialM = repository.getFallbackMovies()
        val initialS = repository.getFallbackSeries()
        val initialHero = (initialM + initialS).shuffled().take(5)
        _homeState.update {
            it.copy(
                heroItems = initialHero,
                latestMovies = initialM,
                popularMovies = initialM,
                latestSeries = initialS,
                popularSeries = initialS,
                isLoading = false
            )
        }
        loadHomeData()
    }

    fun loadHomeData(genre: String? = _homeState.value.selectedGenre) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = it.latestMovies.isEmpty(), selectedGenre = genre, errorMessage = null) }
            try {
                val latestMDef = async { repository.fetchLatestMovies(genre) }
                val popularMDef = async { repository.fetchPopularMovies(genre) }
                val latestSDef = async { repository.fetchLatestSeries(genre) }
                val popularSDef = async { repository.fetchPopularSeries(genre) }

                val latestM = latestMDef.await()
                val popularM = popularMDef.await()
                val latestS = latestSDef.await()
                val popularS = popularSDef.await()

                val heroList = (popularM + popularS).shuffled().take(5)

                _homeState.update {
                    it.copy(
                        heroItems = if (heroList.isNotEmpty()) heroList else latestM.take(3),
                        latestMovies = latestM,
                        popularMovies = popularM,
                        latestSeries = latestS,
                        popularSeries = popularS,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun selectGenre(genre: String?) {
        val newGenre = if (_homeState.value.selectedGenre == genre) null else genre
        loadHomeData(newGenre)
    }

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(query = query) }
        performSearch()
    }

    fun updateSearchType(type: String) {
        _searchState.update { it.copy(typeFilter = type) }
        performSearch()
    }

    private fun performSearch() {
        val query = _searchState.value.query
        val type = _searchState.value.typeFilter
        if (query.isBlank()) {
            _searchState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        viewModelScope.launch {
            _searchState.update { it.copy(isSearching = true) }
            val res = repository.search(query, type)
            _searchState.update { it.copy(results = res, isSearching = false) }
        }
    }

    fun openMediaDetail(item: MediaItem) {
        viewModelScope.launch {
            _detailState.update {
                DetailUiState(
                    mediaItem = item,
                    isLoadingMeta = true,
                    isLoadingStreams = true
                )
            }

            // Watchlist check
            repository.isInWatchlist(item.id).collect { inWatchlist ->
                _detailState.update { it.copy(isInWatchlist = inWatchlist) }
            }
        }

        viewModelScope.launch {
            val metaResult = repository.getMeta(item.id, item.type)
            val detail = metaResult.getOrNull()

            val videos = detail?.videos ?: emptyList()
            val combinedList = videos.filter { it.isCombined }
            val standardList = videos.filter { !it.isCombined }

            val seasons = if (standardList.isNotEmpty()) {
                standardList.mapNotNull { it.season }.filter { it >= 0 }.distinct().sorted()
            } else if (videos.isNotEmpty()) {
                videos.mapNotNull { it.season }.filter { it >= 0 }.distinct().sorted()
            } else {
                emptyList()
            }
            
            // Filter seasons that have actual episodes available on server
            val activeSeasons = seasons.filter { s -> 
                videos.any { it.season == s && it.hasServerFile }
            }

            val defaultSeason = activeSeasons.firstOrNull() ?: 1
            val singleEps = standardList.filter { it.season == defaultSeason }
            val seasonEpisodes = if (singleEps.isNotEmpty()) singleEps else emptyList()
            val initialEpisode = singleEps.firstOrNull { it.hasServerFile }
                ?: combinedList.firstOrNull { it.season == defaultSeason }
                ?: singleEps.firstOrNull()
                ?: videos.firstOrNull { it.season == defaultSeason }

            val isInitialCombined = combinedList.isNotEmpty() && standardList.isEmpty()

            _detailState.update {
                it.copy(
                    metaDetail = detail,
                    seasons = activeSeasons,
                    selectedSeason = defaultSeason,
                    seasonEpisodes = seasonEpisodes,
                    combinedFiles = combinedList.filter { it.season == defaultSeason },
                    selectedEpisode = initialEpisode,
                    isViewingCombinedTab = false,
                    isSeasonCombinedOnly = isInitialCombined,
                    isLoadingMeta = false
                )
            }

            // Load streams for movie or first episode / combined file of series
            val targetStreamId = if (item.type == "series" && initialEpisode != null) {
                initialEpisode.id
            } else {
                item.id
            }

            loadStreams(item.type, targetStreamId)
        }
    }

    fun selectSeason(season: Int) {
        val videos = _detailState.value.metaDetail?.videos ?: emptyList()
        val standardList = videos.filter { !it.isCombined }
        val combinedList = videos.filter { it.isCombined }
        
        val singleEps = standardList.filter { it.season == season }
        val combinedEps = combinedList.filter { it.season == season }
        
        val epToSelect = singleEps.firstOrNull()
            ?: combinedEps.firstOrNull()
            ?: videos.firstOrNull { it.season == season }

        _detailState.update {
            it.copy(
                selectedSeason = season,
                seasonEpisodes = singleEps,
                combinedFiles = combinedEps,
                selectedEpisode = epToSelect,
                isViewingCombinedTab = false
            )
        }
        if (epToSelect != null) {
            loadStreams("series", epToSelect.id)
        }
    }

    fun selectEpisode(episode: VideoEpisode) {
        _detailState.update {
            it.copy(selectedEpisode = episode)
        }
        loadStreams("series", episode.id)
    }

    fun playEpisodeDirectly(item: MediaItem, episode: VideoEpisode) {
        viewModelScope.launch {
            _detailState.update { it.copy(selectedEpisode = episode, isLoadingStreams = true) }
            val streams = repository.getStreams(episode.id, "series").getOrDefault(emptyList())
            _detailState.update { it.copy(streams = streams, isLoadingStreams = false) }
            val firstStream = DeviceCodecHelper.selectBestCompatibleStream(streams, getApplication()) ?: streams.firstOrNull()
            if (firstStream != null) {
                playStream(
                    item = item,
                    streamItem = firstStream,
                    availableStreams = streams,
                    season = episode.season,
                    episode = episode.episode,
                    episodeTitle = episode.title
                )
            }
        }
    }

    fun selectCombinedTab(viewingCombined: Boolean) {
        _detailState.update {
            it.copy(isViewingCombinedTab = viewingCombined)
        }
        if (viewingCombined) {
            val firstCombined = _detailState.value.combinedFiles.firstOrNull()
            if (firstCombined != null) {
                _detailState.update { it.copy(selectedEpisode = firstCombined) }
                loadStreams("series", firstCombined.id)
            }
        }
    }

    fun loadStreams(type: String, id: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _detailState.update { it.copy(isLoadingStreams = true, streamErrorMessage = null) }
            }
            val result = repository.getStreams(id, type)
            result.onSuccess { list ->
                val isOnlyCombined = if (type.equals("series", ignoreCase = true) && list.isNotEmpty()) {
                    list.all { isCombinedStreamItem(it) }
                } else {
                    _detailState.value.isSeasonCombinedOnly
                }
                _detailState.update {
                    it.copy(
                        streams = list,
                        isSeasonCombinedOnly = isOnlyCombined,
                        isLoadingStreams = false,
                        streamErrorMessage = if (list.isEmpty()) "No streams found for this item." else null
                    )
                }
            }.onFailure { err ->
                _detailState.update {
                    it.copy(
                        streams = emptyList(),
                        isLoadingStreams = false,
                        streamErrorMessage = err.message ?: "Could not load streams"
                    )
                }
            }
        }
    }

    fun toggleWatchlist() {
        val item = _detailState.value.mediaItem ?: return
        val current = _detailState.value.isInWatchlist
        viewModelScope.launch {
            repository.toggleWatchlist(item, current)
            _detailState.update { it.copy(isInWatchlist = !current) }
        }
    }

    fun loadEpisodeStreams(video: VideoEpisode) {
        viewModelScope.launch {
            _detailState.update { it.copy(selectedEpisode = video, isLoadingEpisodeStreams = true, episodeStreams = emptyList()) }
            val result = repository.getStreams(video.id, "series")
            result.onSuccess { list ->
                _detailState.update { it.copy(episodeStreams = list, isLoadingEpisodeStreams = false) }
            }.onFailure {
                _detailState.update { it.copy(isLoadingEpisodeStreams = false) }
            }
        }
    }

    private fun getNextEpisode(current: VideoEpisode?): VideoEpisode? {
        if (current == null) return null
        val videos = _detailState.value.metaDetail?.videos ?: return null
        
        // Prefer episodes that actually have server files
        val serverList = videos.filter { !it.isCombined && it.hasServerFile }
        val listToUse = if (serverList.isNotEmpty()) serverList else videos.filter { !it.isCombined }
        
        var index = listToUse.indexOfFirst { it.id == current.id }
        if (index == -1 && current.season != null && current.episode != null) {
            index = listToUse.indexOfFirst { it.season == current.season && it.episode == current.episode }
        }
        if (index != -1 && index < listToUse.size - 1) {
            return listToUse[index + 1]
        }
        // Fallback by sequential episode number
        if (current.season != null && current.episode != null) {
            val nextInSameSeason = listToUse.firstOrNull { it.season == current.season && (it.episode ?: 0) == (current.episode ?: 0) + 1 }
            if (nextInSameSeason != null) return nextInSameSeason
            val nextInNextSeason = listToUse.firstOrNull { (it.season ?: 0) == (current.season ?: 0) + 1 && (it.episode ?: 0) == 1 }
            if (nextInNextSeason != null) return nextInNextSeason
        }
        return null
    }

    fun playStream(
        item: MediaItem,
        streamItem: StreamItem,
        availableStreams: List<StreamItem> = emptyList(),
        season: Int? = null,
        episode: Int? = null,
        episodeTitle: String? = null,
        videoEpisode: VideoEpisode? = null
    ) {
        viewModelScope.launch {
            val lastPos = repository.getLastWatchPosition(item.id, season, episode)
            val streamsToPass = if (availableStreams.isNotEmpty()) availableStreams else _detailState.value.streams
            
            val nextEp = videoEpisode ?: _detailState.value.selectedEpisode
            val next = getNextEpisode(nextEp)

            _playerPlayback.value = PlayerPlaybackState(
                mediaId = item.id,
                title = item.name,
                type = item.type,
                poster = item.poster,
                backdrop = item.background,
                season = season,
                episode = episode,
                episodeTitle = episodeTitle,
                streamItem = streamItem,
                availableStreams = streamsToPass,
                initialPositionMs = lastPos,
                nextEpisode = next
            )
        }
    }

    fun playNextEpisode() {
        val current = _playerPlayback.value ?: return
        val next = current.nextEpisode ?: return
        val mediaItem = _detailState.value.mediaItem ?: return
        
        viewModelScope.launch {
            // We don't set to null here to prevent orientation glitch/rotation back-and-forth
            // The PlayerScreen will handle the transition once the next episode's stream is loaded
            
            // Auto-load streams for next episode and play first one
            val targetId = if (next.id.isNotBlank() && next.id.contains(":")) {
                next.id
            } else {
                "${mediaItem.id}:${next.season ?: 1}:${next.episode ?: 1}"
            }
            var streams = repository.getStreams(targetId, "series").getOrDefault(emptyList())
            
            // If primary targetId returned empty, also try direct next.id if different
            if (streams.isEmpty() && next.id.isNotBlank() && next.id != targetId) {
                streams = repository.getStreams(next.id, "series").getOrDefault(emptyList())
            }
            
            val first = DeviceCodecHelper.selectBestCompatibleStream(streams, getApplication()) ?: streams.firstOrNull()
            if (first != null) {
                _detailState.update { it.copy(selectedEpisode = next) }
                playStream(
                    item = mediaItem,
                    streamItem = first,
                    availableStreams = streams,
                    season = next.season,
                    episode = next.episode,
                    episodeTitle = next.title,
                    videoEpisode = next
                )
            } else {
                // If no separate stream found on server, fallback to current available streams (for season pack files)
                val fallbackStream = DeviceCodecHelper.selectBestCompatibleStream(current.availableStreams, getApplication()) ?: current.availableStreams.firstOrNull()
                if (fallbackStream != null) {
                    _detailState.update { it.copy(selectedEpisode = next) }
                    playStream(
                        item = mediaItem,
                        streamItem = fallbackStream,
                        availableStreams = current.availableStreams,
                        season = next.season,
                        episode = next.episode,
                        episodeTitle = next.title,
                        videoEpisode = next
                    )
                } else {
                    // If truly no streams, close player and notify user
                    _playerPlayback.value = null
                    android.widget.Toast.makeText(getApplication(), "No streams found for next episode", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun streamNow(item: MediaItem) {
        viewModelScope.launch {
            openMediaDetail(item)
            val targetId = if (item.type == "series") {
                _detailState.value.selectedEpisode?.id ?: item.id
            } else item.id
            val streams = repository.getStreams(targetId, item.type).getOrDefault(emptyList())
            val bestStream = DeviceCodecHelper.selectBestCompatibleStream(streams, getApplication()) ?: streams.firstOrNull()
            if (bestStream != null) {
                playStream(
                    item = item,
                    streamItem = bestStream,
                    availableStreams = streams,
                    season = _detailState.value.selectedEpisode?.season,
                    episode = _detailState.value.selectedEpisode?.episode,
                    episodeTitle = _detailState.value.selectedEpisode?.title
                )
            }
        }
    }

    fun switchPlayerStream(streamItem: StreamItem) {
        _playerPlayback.update { current ->
            current?.copy(streamItem = streamItem)
        }
    }

    fun resumeHistoryItem(historyItem: WatchHistoryEntity) {
        viewModelScope.launch {
            val dummyItem = MediaItem(
                id = historyItem.mediaId,
                type = historyItem.type,
                name = historyItem.title,
                poster = historyItem.poster,
                background = historyItem.backdrop
            )

            val stream = StreamItem(
                name = "Server Stream",
                title = historyItem.streamTitle ?: historyItem.title,
                url = historyItem.streamUrl
            )

            _playerPlayback.value = PlayerPlaybackState(
                mediaId = historyItem.mediaId,
                title = historyItem.title,
                type = historyItem.type,
                poster = historyItem.poster,
                backdrop = historyItem.backdrop,
                season = historyItem.season,
                episode = historyItem.episode,
                episodeTitle = historyItem.episodeTitle,
                streamItem = stream,
                initialPositionMs = historyItem.positionMs
            )
        }
    }

    fun closePlayer() {
        _playerPlayback.value = null
    }

    fun saveProgress(positionMs: Long, durationMs: Long) {
        val current = _playerPlayback.value ?: return
        viewModelScope.launch {
            repository.saveWatchProgress(
                mediaId = current.mediaId,
                title = current.title,
                type = current.type,
                poster = current.poster,
                backdrop = current.backdrop,
                season = current.season,
                episode = current.episode,
                episodeTitle = current.episodeTitle,
                streamUrl = current.streamItem?.url,
                streamTitle = current.streamItem?.title,
                positionMs = positionMs,
                durationMs = durationMs
            )
        }
    }

    fun deleteHistory(id: String) {
        viewModelScope.launch {
            repository.deleteFromHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun connectServer(url: String, user: String, pass: String) {
        viewModelScope.launch {
            repository.connectServer(url, user, pass)
            loadHomeData()
        }
    }

    fun selectToken(token: TokenItem) {
        repository.selectToken(token)
        loadHomeData()
    }

    fun createNewProfile(name: String, customToken: String? = null) {
        viewModelScope.launch {
            repository.createNewProfile(name, customToken)
            loadHomeData()
        }
    }

    fun switchProfile(profileId: String) {
        repository.switchProfile(profileId)
        loadHomeData()
    }

    fun downloadStream(
        item: MediaItem,
        stream: StreamItem,
        season: Int? = null,
        episode: Int? = null,
        episodeTitle: String? = null
    ) {
        val displayTitle = if (season != null && episode != null) {
            "${item.name} S${season}E${episode}"
        } else {
            item.name
        }
        val subtitle = episodeTitle ?: stream.displayFileName
        val streamUrl = stream.url ?: return
        downloadRepository.startDownload(
            mediaId = item.id,
            title = displayTitle,
            subtitle = subtitle,
            poster = item.poster ?: item.background,
            streamUrl = streamUrl,
            quality = stream.displayQuality,
            rawFileName = stream.displayFileName
        )
    }

    fun updateDownloadPath(path: String) {
        downloadRepository.setCustomPath(path)
    }

    fun startDirectLinkDownload(url: String, title: String? = null) {
        downloadRepository.startDirectLinkDownload(url, title)
    }

    fun pauseOrResumeDownloadTask(task: DownloadTask) {
        downloadRepository.pauseOrResumeDownload(task)
    }

    fun downloadEpisodeDirectly(item: MediaItem, video: VideoEpisode) {
        viewModelScope.launch {
            val streams = _detailState.value.streams
            if (streams.isNotEmpty()) {
                val best = DeviceCodecHelper.selectBestCompatibleStream(streams, getApplication()) ?: streams.first()
                downloadStream(
                    item = item,
                    stream = best,
                    season = video.season,
                    episode = video.episode,
                    episodeTitle = video.title
                )
            } else {
                // Fetch streams first then trigger download
                _detailState.update { it.copy(isLoadingStreams = true) }
                val fetched = repository.getStreams(video.id, "series").getOrDefault(emptyList())
                _detailState.update { it.copy(isLoadingStreams = false, streams = fetched) }
                val best = DeviceCodecHelper.selectBestCompatibleStream(fetched, getApplication()) ?: fetched.firstOrNull()
                if (best != null) {
                    downloadStream(
                        item = item,
                        stream = best,
                        season = video.season,
                        episode = video.episode,
                        episodeTitle = video.title
                    )
                } else {
                    android.widget.Toast.makeText(
                        getApplication(),
                        "No downloadable stream found for this episode",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun deleteDownloadTask(downloadId: Long) {
        downloadRepository.deleteDownload(downloadId)
    }

    fun playDownloadedTask(task: DownloadTask) {
        viewModelScope.launch {
            val file = task.localFilePath?.let { java.io.File(it) }
            val playUrl = if (file != null && file.exists()) {
                android.net.Uri.fromFile(file).toString()
            } else {
                task.localFilePath ?: task.streamUrl
            }
            val dummyStream = StreamItem(
                name = task.title,
                title = task.fileName,
                url = playUrl
            )
            _playerPlayback.value = PlayerPlaybackState(
                mediaId = task.mediaId,
                title = task.title,
                type = "movie",
                poster = task.poster,
                streamItem = dummyStream,
                availableStreams = listOf(dummyStream)
            )
        }
    }
}
