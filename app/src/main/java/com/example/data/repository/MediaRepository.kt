package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.room.Room
import com.example.data.api.StremioApiClient
import com.example.data.api.TmdbApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaRepository(private val context: Context) {

    private val apiClient = StremioApiClient()
    private val tmdbApiClient = TmdbApiClient()

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nuvio_media.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    private val prefs = context.getSharedPreferences("nuvio_prefs", Context.MODE_PRIVATE)

    private val _serverState = MutableStateFlow(
        ServerConnectionState(
            serverUrl = prefs.getString("server_url", "https://p01--telegram-stremio--czrdx5625lgp.code.run") ?: "https://p01--telegram-stremio--czrdx5625lgp.code.run",
            token = prefs.getString("token", "O6zE4PZv1RHKMxWc0ZU5SkLgdObmoJgh") ?: "O6zE4PZv1RHKMxWc0ZU5SkLgdObmoJgh",
            username = prefs.getString("username", "Harsh") ?: "Harsh",
            password = prefs.getString("password", "Nokia@2134") ?: "Nokia@2134",
            activeTokenName = prefs.getString("token_name", "Harsh🎯") ?: "Harsh🎯",
            activeProfileName = "Harsh",
            profileLogs = listOf("Session initialized for Harsh", "Connected to Server CDN"),
            isConnected = true
        )
    )
    val serverState: StateFlow<ServerConnectionState> = _serverState.asStateFlow()

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val url = _serverState.value.serverUrl
                val user = _serverState.value.username
                val pass = _serverState.value.password
                val res = apiClient.loginAndGetTokens(url, user, pass)
                val tokens = res.getOrNull() ?: emptyList()
                if (tokens.isNotEmpty()) {
                    _serverState.update {
                        it.copy(
                            availableTokens = tokens,
                            profileLogs = _serverState.value.profileLogs + "Synced ${tokens.size} tokens from server host."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaRepository", "Error syncing initial server tokens", e)
            }
        }
    }

    val watchlist: Flow<List<WatchlistEntity>> by lazy { db.watchlistDao().getAll() }
    val continueWatching: Flow<List<WatchHistoryEntity>> by lazy { db.watchHistoryDao().getContinueWatching() }
    val watchHistory: Flow<List<WatchHistoryEntity>> by lazy { db.watchHistoryDao().getAll() }

    fun isInWatchlist(id: String): Flow<Boolean> = db.watchlistDao().isInWatchlist(id)

    suspend fun toggleWatchlist(item: MediaItem, isAdded: Boolean) {
        if (isAdded) {
            db.watchlistDao().deleteById(item.id)
        } else {
            db.watchlistDao().insert(
                WatchlistEntity(
                    id = item.id,
                    title = item.name,
                    type = item.type,
                    poster = item.poster,
                    backdrop = item.background,
                    year = item.year ?: item.releaseInfo,
                    rating = item.imdbRating,
                    genres = item.genres?.joinToString(", ")
                )
            )
        }
    }

    suspend fun saveWatchProgress(
        mediaId: String,
        title: String,
        type: String,
        poster: String?,
        backdrop: String?,
        season: Int?,
        episode: Int?,
        episodeTitle: String?,
        streamUrl: String?,
        streamTitle: String?,
        positionMs: Long,
        durationMs: Long
    ) {
        val uniqueId = if (type == "series" && season != null && episode != null) {
            "series_${mediaId}_${season}_$episode"
        } else {
            "movie_$mediaId"
        }

        db.watchHistoryDao().insertOrUpdate(
            WatchHistoryEntity(
                id = uniqueId,
                mediaId = mediaId,
                title = title,
                type = type,
                poster = poster,
                backdrop = backdrop,
                season = season,
                episode = episode,
                episodeTitle = episodeTitle,
                streamUrl = streamUrl,
                streamTitle = streamTitle,
                positionMs = positionMs,
                durationMs = durationMs,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getLastWatchPosition(mediaId: String, season: Int? = null, episode: Int? = null): Long {
        val isEpisodeRequested = season != null && episode != null
        val uniqueId = if (isEpisodeRequested) {
            "series_${mediaId}_${season}_$episode"
        } else {
            "movie_$mediaId"
        }
        
        val history = db.watchHistoryDao().getById(uniqueId)
        if (history != null) return history.positionMs
        
        // If a specific episode was requested but not found in history, start from beginning (0)
        // rather than inheriting the position of the previously watched episode from getLatestByMediaId.
        if (isEpisodeRequested) return 0L
        
        // For movies or general series resume, use the latest entry
        val latestHistory = db.watchHistoryDao().getLatestByMediaId(mediaId)
        return latestHistory?.positionMs ?: 0L
    }

    suspend fun deleteFromHistory(id: String) {
        db.watchHistoryDao().deleteById(id)
    }

    suspend fun clearAllHistory() {
        db.watchHistoryDao().clearAll()
    }

    suspend fun connectServer(serverUrl: String, user: String, pass: String): Result<List<TokenItem>> {
        _serverState.update { it.copy(isChecking = true, errorMessage = null) }
        val result = apiClient.loginAndGetTokens(serverUrl, user, pass)
        result.onSuccess { tokens ->
            val chosenToken = tokens.firstOrNull { it.token == _serverState.value.token }
                ?: tokens.firstOrNull()

            val tokenStr = chosenToken?.token ?: _serverState.value.token
            val tokenName = chosenToken?.user_name ?: "Server Addon"

            prefs.edit {
                putString("server_url", serverUrl)
                putString("username", user)
                putString("password", pass)
                putString("token", tokenStr)
                putString("token_name", tokenName)
            }

            _serverState.update {
                it.copy(
                    serverUrl = serverUrl,
                    username = user,
                    password = pass,
                    token = tokenStr,
                    activeTokenName = tokenName,
                    availableTokens = tokens,
                    isConnected = true,
                    isChecking = false,
                    errorMessage = null
                )
            }
        }.onFailure { err ->
            _serverState.update {
                it.copy(
                    isChecking = false,
                    errorMessage = err.message ?: "Authentication failed"
                )
            }
        }
        return result
    }

    fun selectToken(tokenItem: TokenItem) {
        prefs.edit {
            putString("token", tokenItem.token)
            putString("token_name", tokenItem.user_name ?: "Token")
        }

        val logs = _serverState.value.profileLogs + "Selected server token: ${tokenItem.user_name ?: "Token"}"
        _serverState.update {
            it.copy(
                token = tokenItem.token,
                activeTokenName = tokenItem.user_name ?: "Token",
                profileLogs = logs
            )
        }
    }

    suspend fun createNewProfile(userName: String, customTokenStr: String? = null) {
        // No-op or simplified
    }

    fun switchProfile(profileId: String) {
        // No-op or simplified
    }

    private val catalogCache = java.util.concurrent.ConcurrentHashMap<String, List<MediaItem>>()

    private fun sanitizeMediaItem(item: MediaItem): MediaItem {
        val poster = if (item.poster.isNullOrBlank() || item.poster.contains("poster-default")) {
            if (item.id.startsWith("tt")) "https://images.metahub.space/poster/medium/${item.id}/img"
            else item.poster
        } else item.poster

        val rating = if (item.imdbRating.isNullOrBlank() || item.imdbRating == "0.0" || item.imdbRating == "0") {
             null
        } else item.imdbRating

        return item.copy(poster = poster, imdbRating = rating)
    }

    suspend fun fetchLatestMovies(genre: String? = null): List<MediaItem> {
        val key = "latest_movies_${genre ?: "all"}"
        val st = _serverState.value
        val res = apiClient.fetchCatalog(st.serverUrl, st.token, "movie", "latest_movies", genre = genre)
        val list = res.getOrElse { catalogCache[key] ?: getFallbackMovies() }
        val sanitized = list.map { sanitizeMediaItem(it) }
        if (sanitized.isNotEmpty()) catalogCache[key] = sanitized
        return sanitized
    }

    suspend fun fetchPopularMovies(genre: String? = null): List<MediaItem> {
        val key = "top_movies_${genre ?: "all"}"
        val st = _serverState.value
        val res = apiClient.fetchCatalog(st.serverUrl, st.token, "movie", "top_movies", genre = genre)
        val list = res.getOrElse { catalogCache[key] ?: getFallbackMovies() }
        val sanitized = list.map { sanitizeMediaItem(it) }
        if (sanitized.isNotEmpty()) catalogCache[key] = sanitized
        return sanitized
    }

    suspend fun fetchLatestSeries(genre: String? = null): List<MediaItem> {
        val key = "latest_series_${genre ?: "all"}"
        val st = _serverState.value
        val res = apiClient.fetchCatalog(st.serverUrl, st.token, "series", "latest_series", genre = genre)
        val list = res.getOrElse { catalogCache[key] ?: getFallbackSeries() }
        val sanitized = list.map { sanitizeMediaItem(it) }
        if (sanitized.isNotEmpty()) catalogCache[key] = sanitized
        return sanitized
    }

    suspend fun fetchPopularSeries(genre: String? = null): List<MediaItem> {
        val key = "top_series_${genre ?: "all"}"
        val st = _serverState.value
        val res = apiClient.fetchCatalog(st.serverUrl, st.token, "series", "top_series", genre = genre)
        val list = res.getOrElse { catalogCache[key] ?: getFallbackSeries() }
        val sanitized = list.map { sanitizeMediaItem(it) }
        if (sanitized.isNotEmpty()) catalogCache[key] = sanitized
        return sanitized
    }

    suspend fun search(query: String, type: String = "all"): List<MediaItem> {
        if (query.isBlank()) return emptyList()
        val st = _serverState.value
        val results = mutableListOf<MediaItem>()

        if (type == "all" || type == "movie") {
            val movieRes = apiClient.fetchCatalog(st.serverUrl, st.token, "movie", "top_movies", search = query)
            results.addAll(movieRes.getOrDefault(emptyList()).map { sanitizeMediaItem(it) })
        }
        if (type == "all" || type == "series") {
            val seriesRes = apiClient.fetchCatalog(st.serverUrl, st.token, "series", "top_series", search = query)
            results.addAll(seriesRes.getOrDefault(emptyList()).map { sanitizeMediaItem(it) })
        }

        // If remote search returned empty or error, filter combined fallback
        if (results.isEmpty()) {
            val all = getFallbackMovies() + getFallbackSeries()
            val filtered = all.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.genres?.any { g -> g.contains(query, ignoreCase = true) } == true)
            }
            results.addAll(filtered)
        }

        return results.distinctBy { it.id }
    }

    suspend fun getMeta(id: String, type: String): Result<MetaDetail> {
        val st = _serverState.value
        val tgMetaRes = apiClient.fetchMeta(st.serverUrl, st.token, type, id)

        // Fetch TMDB data as primary source for info if it's an IMDB ID
        val tmdbData = if (id.startsWith("tt")) tmdbApiClient.getInfoByImdbId(id) else null

        if (type.equals("series", ignoreCase = true)) {
            val cinemetaRes = apiClient.fetchCinemetaMeta(type, id)
            val cinemetaMeta = cinemetaRes.getOrNull()
            val tgMeta = tgMetaRes.getOrNull()

            if (cinemetaMeta != null || tgMeta != null || tmdbData != null) {
                val baseMeta = tgMeta ?: cinemetaMeta ?: MetaDetail(id = id, type = type, name = "Unknown")
                val tgVideos = tgMeta?.videos ?: emptyList()
                val cineVideos = cinemetaMeta?.videos ?: emptyList()

                // Identify Server combined files (Full season batch files)
                val combinedVideos = tgVideos.mapNotNull { v ->
                    val title = (v.title ?: v.name ?: "").trim()
                    val isCombined = (v.season == 0) || ((v.episode ?: 0) >= 1000) ||
                            title.contains("Combined", ignoreCase = true) ||
                            title.contains("Batch", ignoreCase = true) ||
                            title.contains("Complete", ignoreCase = true)

                    if (isCombined) {
                        val epNum = v.episode ?: 0
                        val seasonNum = when {
                            epNum in 1001..1099 -> epNum - 1000
                            v.season != null && v.season > 0 -> v.season
                            title.contains("Season 1", ignoreCase = true) || title.contains("S01", ignoreCase = true) || title.contains("S1", ignoreCase = true) -> 1
                            title.contains("Season 2", ignoreCase = true) || title.contains("S02", ignoreCase = true) || title.contains("S2", ignoreCase = true) -> 2
                            title.contains("Season 3", ignoreCase = true) || title.contains("S03", ignoreCase = true) || title.contains("S3", ignoreCase = true) -> 3
                            title.contains("Season 4", ignoreCase = true) || title.contains("S04", ignoreCase = true) || title.contains("S4", ignoreCase = true) -> 4
                            title.contains("Season 5", ignoreCase = true) || title.contains("S05", ignoreCase = true) || title.contains("S5", ignoreCase = true) -> 5
                            else -> 1
                        }
                        v.copy(
                            season = seasonNum,
                            isCombined = true,
                            title = if (title.isNotBlank()) title else "Season $seasonNum Combined (Full Season)"
                        )
                    } else null
                }

                // Standard individual episodes (from Cinemeta or standard Server)
                val serverEpisodes = tgVideos.filter { v ->
                    val title = (v.title ?: v.name ?: "").trim()
                    !(v.isCombined || v.season == 0 || (v.episode ?: 0) >= 1000 ||
                            title.contains("Combined", ignoreCase = true) ||
                            title.contains("Batch", ignoreCase = true) ||
                            title.contains("Complete", ignoreCase = true))
                }
                val serverIds = serverEpisodes.map { it.id }.toSet()

                val standardEpisodes = if (cineVideos.isNotEmpty()) {
                    val cineEpisodesMapped = cineVideos.map { cv -> 
                        val serverMatch = serverEpisodes.firstOrNull { 
                            it.id == cv.id || (it.season == cv.season && it.episode == cv.episode)
                        }
                        val hasServer = serverMatch != null || serverIds.contains(cv.id)
                        val effectiveId = serverMatch?.id ?: cv.id
                        cv.copy(
                            id = effectiveId,
                            isCombined = false, 
                            hasServerFile = hasServer
                        ) 
                    }
                    // Also include episodes from server that are NOT in Cinemeta's list
                    val cineIds = cineVideos.map { it.id }.toSet()
                    val extraFromServer = serverEpisodes.filter { !cineIds.contains(it.id) }
                        .map { it.copy(isCombined = false, hasServerFile = true) }
                    
                    cineEpisodesMapped + extraFromServer
                } else {
                    serverEpisodes.map { it.copy(isCombined = false, hasServerFile = true) }
                }

                val mergedVideos = (standardEpisodes + combinedVideos).distinctBy { it.id }

                val merged = baseMeta.copy(
                    description = if (tmdbData is TmdbTvDetail) tmdbData.overview ?: baseMeta.description else baseMeta.description?.ifBlank { cinemetaMeta?.description } ?: cinemetaMeta?.description,
                    genres = if (tmdbData is TmdbTvDetail) tmdbData.genres?.map { it.name } ?: baseMeta.genres else if (!baseMeta.genres.isNullOrEmpty()) baseMeta.genres else cinemetaMeta?.genres,
                    cast = if (tmdbData is TmdbTvDetail) tmdbData.credits?.cast?.map { "${it.name}|${it.character ?: ""}|${it.profileUrl ?: ""}" } ?: baseMeta.cast else baseMeta.cast ?: cinemetaMeta?.cast,
                    background = if (tmdbData is TmdbTvDetail) tmdbData.backdropUrl ?: baseMeta.background else baseMeta.background,
                    poster = if (tmdbData is TmdbTvDetail) tmdbData.posterUrl ?: baseMeta.poster else if (baseMeta.poster.isNullOrBlank() && id.startsWith("tt")) "https://images.metahub.space/poster/medium/$id/img" else baseMeta.poster,
                    imdbRating = if (tmdbData is TmdbTvDetail) String.format(java.util.Locale.US, "%.1f", tmdbData.voteAverage ?: 0.0) else baseMeta.imdbRating,
                    videos = mergedVideos
                )
                return Result.success(merged)
            }
        } else {
            // For Movie
            tgMetaRes.onSuccess { meta ->
                if (tmdbData is TmdbMovieDetail) {
                    val merged = meta.copy(
                        description = tmdbData.overview ?: meta.description,
                        imdbRating = String.format(java.util.Locale.US, "%.1f", tmdbData.voteAverage ?: 0.0),
                        poster = tmdbData.posterUrl ?: meta.poster,
                        cast = tmdbData.credits?.cast?.map { "${it.name}|${it.character ?: ""}|${it.profileUrl ?: ""}" } ?: meta.cast,
                        background = tmdbData.backdropUrl ?: meta.background,
                        genres = tmdbData.genres?.map { it.name } ?: meta.genres,
                        runtime = tmdbData.runtime?.let { "${it} min" } ?: meta.runtime
                    )
                    return Result.success(merged)
                }
                // Even if TMDB fails, ensure poster is not broken
                if (meta.poster.isNullOrBlank() && id.startsWith("tt")) {
                    return Result.success(meta.copy(poster = "https://images.metahub.space/poster/medium/$id/img"))
                }
            }
        }
        return tgMetaRes
    }

    suspend fun getStreams(id: String, type: String): Result<List<StreamItem>> {
        val st = _serverState.value
        val directResult = apiClient.fetchStreams(st.serverUrl, st.token, type, id)
        val streams = directResult.getOrDefault(emptyList())

        // If a series episode has no separate split streams on Server,
        // automatically fallback to the Season's Combined file stream so the user can stream immediately!
        if (streams.isEmpty() && type.equals("series", ignoreCase = true) && id.contains(":")) {
            val parts = id.split(":")
            if (parts.size >= 3) {
                val seriesId = parts[0]
                val seasonNum = parts[1].toIntOrNull() ?: 1
                val epNum = parts[2].toIntOrNull() ?: 1

                val fallbackCandidates = listOf(
                    "$seriesId:0:${1000 + seasonNum}",
                    "$seriesId:0:$seasonNum",
                    "$seriesId:$seasonNum:0",
                    "$seriesId:$seasonNum:$epNum"
                )
                for (candId in fallbackCandidates) {
                    if (candId == id) continue
                    val fallbackStreamsRes = apiClient.fetchStreams(st.serverUrl, st.token, type, candId)
                    val fallbackStreams = fallbackStreamsRes.getOrDefault(emptyList())
                    if (fallbackStreams.isNotEmpty()) {
                        val markedStreams = fallbackStreams.map { s ->
                            s.copy(
                                name = if (s.name != null) "${s.name} (S$seasonNum Stream)" else "Season $seasonNum Stream"
                            )
                        }
                        return Result.success(markedStreams)
                    }
                }
            }
        }

        return directResult
    }

    fun getFallbackMovies(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "tt32023291",
                type = "movie",
                name = "Main Ladega",
                poster = "https://btttr.cc/poster-qa/imdb/poster-default/tt32023291.jpg",
                background = "https://image.tmdb.org/t/p/original/yXvVIZCIitmt8SeGAoSuEn8Z6Mi.jpg",
                year = "2024",
                imdbRating = "6.4",
                genres = listOf("Drama", "Action"),
                description = "A young boy escapes his troubled home, finding solace in boxing at an army hostel, where he channels his pain into strength to protect his family."
            ),
            MediaItem(
                id = "tt22084616",
                type = "movie",
                name = "Spider-Man: Brand New Day",
                poster = "https://btttr.cc/poster-qa/imdb/poster-default/tt22084616.jpg",
                background = "https://image.tmdb.org/t/p/original/eSvYccJm5ejJXFtJTqM2dIqrhK7.jpg",
                year = "2026",
                imdbRating = "8.0",
                genres = listOf("Action", "Adventure", "Sci-Fi"),
                description = "Fighting crime full-time as Spider-Man in a world that doesn't remember him, Peter Parker faces a shocking new threat."
            )
        )
    }

    fun getFallbackSeries(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "tt11407524",
                type = "series",
                name = "Hostel Daze",
                poster = "https://btttr.cc/poster-qa/imdb/poster-default/tt11407524.jpg",
                background = "https://images.metahub.space/background/medium/tt11407524/img",
                year = "2019",
                imdbRating = "8.5",
                genres = listOf("Comedy", "Drama"),
                description = "Through the adventures of four friends 'Hostel Daze' captures what goes inside an engineering hostel in India."
            ),
            MediaItem(
                id = "tt35870921",
                type = "series",
                name = "Raakh",
                poster = "https://btttr.cc/poster-qa/imdb/poster-default/tt35870921.jpg",
                background = "https://images.metahub.space/background/medium/tt35870921/img",
                year = "2026",
                imdbRating = "7.6",
                genres = listOf("Crime", "Drama"),
                description = "When two teenagers vanish, a close-knit family is shattered and the city is left on edge."
            )
        )
    }
}
