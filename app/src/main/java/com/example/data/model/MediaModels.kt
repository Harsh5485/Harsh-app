package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaItem(
    val id: String = "",
    val type: String = "movie",
    val name: String = "",
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val year: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: String? = null,
    val genres: List<String>? = null,
    val description: String? = null,
    val cast: List<String>? = null,
    val runtime: String? = null,
    val moviedb_id: Long? = null
)

@JsonClass(generateAdapter = true)
data class CatalogResponse(
    val metas: List<MediaItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoEpisode(
    val id: String = "",
    val name: String? = null,
    val title: String? = null,
    val season: Int? = 1,
    val episode: Int? = 1,
    val overview: String? = null,
    val thumbnail: String? = null,
    val released: String? = null,
    val isCombined: Boolean = false,
    val sizeText: String? = null
) {
    val displayTitle: String
        get() = when {
            isCombined -> title ?: name ?: "Season $season Combined (Full Season)"
            !title.isNullOrBlank() -> title
            !name.isNullOrBlank() -> name
            else -> "Episode $episode"
        }

    val displaySeason: Int
        get() = season ?: 1
}

@JsonClass(generateAdapter = true)
data class MetaDetail(
    val id: String = "",
    val type: String = "movie",
    val name: String = "",
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val year: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: String? = null,
    val genres: List<String>? = null,
    val description: String? = null,
    val cast: List<String>? = null,
    val runtime: String? = null,
    val videos: List<VideoEpisode>? = null
)

@JsonClass(generateAdapter = true)
data class MetaResponse(
    val meta: MetaDetail? = null
)

@JsonClass(generateAdapter = true)
data class StreamItem(
    val name: String? = null,
    val title: String? = null,
    val url: String? = null,
    val size_bytes: Long? = null,
    val episode_start: Int? = null,
    val name_key: String? = null,
    val externalUrl: String? = null,
    val description: String? = null
) {
    val isPlayable: Boolean
        get() = !url.isNullOrBlank()

    val parsedSizeBytes: Long
        get() {
            val bytes = size_bytes ?: 0L
            if (bytes > 0L) return bytes
            val t = title ?: ""
            val match = Regex("""(?:[💾📦💽📂📁💿]\s*)?([0-9]{1,4}(?:\.[0-9]{1,3})?)\s*(GB|MB|GiB|MiB)\b""", RegexOption.IGNORE_CASE).find(t)
            if (match != null) {
                val num = match.groupValues[1].toDoubleOrNull() ?: 0.0
                val unit = match.groupValues[2].uppercase()
                return if (unit.startsWith("G")) {
                    (num * 1024.0 * 1024.0 * 1024.0).toLong()
                } else {
                    (num * 1024.0 * 1024.0).toLong()
                }
            }
            return 0L
        }

    val displayQuality: String
        get() {
            val n = (name ?: "").lowercase()
            val t = (title ?: "").lowercase()
            val combined = "$n $t"

            // 1. Direct 4K / 2160p checks
            if (combined.contains("2160p") || combined.contains("2160i") || n.contains("2160p")) return "4K UHD"

            // Check if downscaled 4k (DS4K)
            val isDs4k = combined.contains("ds4k") || combined.contains("ds 4k") || combined.contains("downscale")

            // 2. 1080p check: If it has 1080p / 1080i / FHD (even if DS4K is mentioned, it is a 1080p encode)
            if (combined.contains("1080p") || combined.contains("1080i") || combined.contains("fhd")) return "1080p"

            // 3. Standalone 4K UHD check (only if not downscaled)
            if (!isDs4k && (Regex("""\b4k\b""", RegexOption.IGNORE_CASE).containsMatchIn(combined) || combined.contains("uhd"))) {
                return "4K UHD"
            }

            // 4. 720p check
            if (combined.contains("720p") || combined.contains("720i") || combined.contains("hdrip")) return "720p"

            // 5. 480p / SD check
            if (combined.contains("480p") || combined.contains("576p") || combined.contains("dvdrip") || combined.contains("sd")) return "480p"

            // 6. Size based fallback
            val bytes = parsedSizeBytes
            return when {
                bytes > 12_000_000_000L -> "4K UHD"
                bytes > 1_200_000_000L -> "1080p"
                bytes > 600_000_000L -> "720p"
                combined.contains("hd") -> "HD"
                else -> "HD"
            }
        }

    val displaySource: String
        get() {
            val n = name ?: ""
            return if (n.isNotBlank()) n.replace("Telegram", "Server").replace("TG", "Server").trim() else "Server Stream"
        }

    val displayVideoCodec: String
        get() {
            val t = (title ?: "").lowercase()
            return when {
                t.contains("x265") || t.contains("hevc") || t.contains("h.265") || t.contains("h265") -> "HEVC (H.265)"
                t.contains("x264") || t.contains("avc") || t.contains("h.264") || t.contains("h264") -> "AVC (H.264)"
                t.contains("av1") || t.contains("av01") -> "AV1"
                t.contains("vp9") -> "VP9"
                else -> "H.264"
            }
        }

    val displayAudio: String
        get() {
            val t = (title ?: "").lowercase()
            return when {
                t.contains("atmos") -> "Dolby Atmos"
                t.contains("ddp5.1") || t.contains("ddp 5.1") || t.contains("dd+ 5.1") || t.contains("e-ac3") || t.contains("eac3") -> "Dolby DDP 5.1"
                t.contains("dd5.1") || t.contains("dd 5.1") || t.contains("ac3 5.1") || t.contains("dolby digital 5.1") || t.contains("dolby digital") -> "Dolby 5.1"
                t.contains("7.1") -> "7.1 Surround"
                t.contains("he-aac 5.1") || t.contains("aac 5.1") -> "AAC 5.1"
                t.contains("aac") -> "Stereo AAC"
                t.contains("dts-hd") -> "DTS-HD"
                t.contains("dts") -> "DTS Audio"
                else -> "Multi-Audio"
            }
        }

    val displayHdr: String
        get() {
            val t = (title ?: "").lowercase()
            return when {
                t.contains("dv") || t.contains("dolby vision") -> "Dolby Vision"
                t.contains("hdr10+") -> "HDR10+"
                t.contains("hdr") -> "HDR"
                t.contains("10bit") || t.contains("10-bit") -> "10-bit"
                else -> "SDR"
            }
        }

    val displayRip: String
        get() {
            val t = (title ?: "").lowercase()
            return when {
                t.contains("remux") -> "REMUX"
                t.contains("bluray") || t.contains("blu-ray") || t.contains("bdrip") -> "Blu-ray"
                t.contains("webrip") -> "WEBRip"
                t.contains("web-dl") || t.contains("webdl") -> "WEB-DL"
                t.contains("hdtv") -> "HDTV"
                else -> "WEB"
            }
        }

    val formattedSize: String
        get() {
            val t = title ?: ""
            // 1. Emoji indicators: 💾, 📦, 💽, 📂, 📁, 💿
            val emojiMatch = Regex("""[💾📦💽📂📁💿]\s*([0-9.]+\s*(?:GB|MB|KB|GiB|MiB|KiB))""", RegexOption.IGNORE_CASE).find(t)
            if (emojiMatch != null) {
                return emojiMatch.groupValues[1].trim()
                    .replace("GiB", "GB", ignoreCase = true)
                    .replace("MiB", "MB", ignoreCase = true)
            }

            // 2. Direct size mention in title: e.g. "3.40 GB", "79.96GB"
            val textMatch = Regex("""\b([0-9]{1,4}(?:\.[0-9]{1,3})?)\s*(GB|MB|GiB|MiB)\b""", RegexOption.IGNORE_CASE).find(t)
            if (textMatch != null) {
                val num = textMatch.groupValues[1]
                val unit = textMatch.groupValues[2].uppercase().replace("GIB", "GB").replace("MIB", "MB")
                return "$num $unit"
            }

            // 3. Use size_bytes if available and positive
            val bytes = size_bytes ?: 0L
            if (bytes > 0L) {
                val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                return if (gb >= 1.0) {
                    String.format(java.util.Locale.US, "%.2f GB", gb)
                } else {
                    val mb = bytes.toDouble() / (1024.0 * 1024.0)
                    String.format(java.util.Locale.US, "%.0f MB", mb)
                }
            }

            return ""
        }

    val displayFileName: String
        get() {
            val t = title ?: ""
            val firstLine = t.lines().firstOrNull { it.isNotBlank() } ?: (name ?: "Stream")
            return firstLine.replace("📁", "").trim()
        }
}

@JsonClass(generateAdapter = true)
data class StreamResponse(
    val streams: List<StreamItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class TokenItem(
    val token: String = "",
    val user_id: String? = null,
    val user_name: String? = null,
    val lifetime: Boolean? = true,
    val never_expires: Boolean? = true,
    val daily_limit_gb: Double? = 0.0,
    val monthly_limit_gb: Double? = 0.0,
    val daily_bytes: Long? = 0L,
    val monthly_bytes: Long? = 0L,
    val addon_url: String? = null,
    val is_expired: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class TokensResponse(
    val tokens: List<TokenItem>? = emptyList(),
    val subscription: Boolean? = false
)

// TMDB Related Models
@JsonClass(generateAdapter = true)
data class TmdbCast(
    val id: Int,
    val name: String,
    val character: String?,
    @field:Json(name = "profile_path") val profilePath: String?
) {
    val profileUrl: String? get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    val cast: List<TmdbCast>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbImage(
    @field:Json(name = "file_path") val filePath: String?
) {
    val url: String? get() = filePath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbImagesResponse(
    val backdrops: List<TmdbImage>? = emptyList(),
    val posters: List<TmdbImage>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbExternalIds(
    val imdb_id: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetail(
    val id: Int,
    val title: String?,
    val overview: String?,
    @field:Json(name = "poster_path") val posterPath: String?,
    @field:Json(name = "backdrop_path") val backdropPath: String?,
    @field:Json(name = "vote_average") val voteAverage: Double?,
    @field:Json(name = "release_date") val releaseDate: String?,
    val runtime: Int?,
    val genres: List<TmdbGenre>? = emptyList(),
    val credits: TmdbCredits? = null,
    val images: TmdbImagesResponse? = null
) {
    val posterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbGenre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbFindResponse(
    val movie_results: List<TmdbMovieDetail>? = emptyList(),
    val tv_results: List<TmdbTvDetail>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetail(
    val id: Int,
    val name: String?,
    val overview: String?,
    @field:Json(name = "poster_path") val posterPath: String?,
    @field:Json(name = "backdrop_path") val backdropPath: String?,
    @field:Json(name = "vote_average") val voteAverage: Double?,
    @field:Json(name = "first_air_date") val firstAirDate: String?,
    val genres: List<TmdbGenre>? = emptyList(),
    val credits: TmdbCredits? = null,
    val images: TmdbImagesResponse? = null
) {
    val posterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val backdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
}

data class ServerConnectionState(
    val serverUrl: String = "https://p01--telegram-stremio--czrdx5625lgp.code.run",
    val token: String = "O6zE4PZv1RHKMxWc0ZU5SkLgdObmoJgh",
    val username: String = "Harsh",
    val password: String = "Nokia@2134",
    val isConnected: Boolean = true,
    val activeTokenName: String = "Harsh🎯",
    val activeProfileName: String = "Harsh",
    val profileLogs: List<String> = emptyList(),
    val availableTokens: List<TokenItem> = emptyList(),
    val monthlyDataFormatted: String = "498.3 GB",
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)
