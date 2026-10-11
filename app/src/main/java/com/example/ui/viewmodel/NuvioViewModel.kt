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
           fn.contains("[E01-", ignoreCase = true)
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
    val isLoadingMeta: Boolean = false,
    val isLoadingStreams: Boolean = false,
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
    val initialPositionMs: Long = 0L
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
                standardList.mapNotNull { it.season }.filter { it > 0 }.distinct().sorted()
            } else if (videos.isNotEmpty()) {
                videos.mapNotNull { it.season }.filter { it > 0 }.distinct().sorted()
            } else {
                emptyList()
            }

            val defaultSeason = seasons.firstOrNull() ?: 1
            val singleEps = standardList.filter { it.season == defaultSeason }
            val seasonEpisodes = if (singleEps.isNotEmpty()) singleEps else emptyList()
            val initialEpisode = singleEps.firstOrNull()
                ?: combinedList.firstOrNull { it.season == defaultSeason }
                ?: videos.firstOrNull { it.season == defaultSeason }

            val isInitialCombined = combinedList.isNotEmpty() && standardList.isEmpty()

            _detailState.update {
                it.copy(
                    metaDetail = detail,
                    seasons = seasons,
                    selectedSeason = defaultSeason,
                    seasonEpisodes = seasonEpisodes,
                    combinedFiles = combinedList,
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
        val singleEps = standardList.filter { it.season == season }
        val epToSelect = singleEps.firstOrNull()
            ?: videos.firstOrNull { it.season == season && it.isCombined }
            ?: videos.firstOrNull { it.season == season }

        _detailState.update {
            it.copy(
                selectedSeason = season,
                seasonEpisodes = singleEps,
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
            val firstStream = streams.firstOrNull()
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

    fun playStream(
        item: MediaItem,
        streamItem: StreamItem,
        availableStreams: List<StreamItem> = emptyList(),
        season: Int? = null,
        episode: Int? = null,
        episodeTitle: String? = null
    ) {
        viewModelScope.launch {
            val lastPos = repository.getLastWatchPosition(item.id, season, episode)
            val streamsToPass = if (availableStreams.isNotEmpty()) availableStreams else _detailState.value.streams
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
                initialPositionMs = lastPos
            )
        }
    }

    fun streamNow(item: MediaItem) {
        viewModelScope.launch {
            openMediaDetail(item)
            val targetId = if (item.type == "series") {
                _detailState.value.selectedEpisode?.id ?: item.id
            } else item.id
            val streams = repository.getStreams(targetId, item.type).getOrDefault(emptyList())
            val bestStream = streams.firstOrNull()
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
                val best = streams.first()
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
                val best = fetched.firstOrNull()
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
