package com.example.data.api

import android.util.Log
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TmdbApiClient {
    private val apiKey = "13bd31184a73665a1fe0d61cc8248d0c"
    private val readAccessToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxM2JkMzExODRhNzM2NjVhMWZlMGQ2MWNjODI0OGQwYyIsIm5iZiI6MTc4NTc4NzExMy44ODQsInN1YiI6IjZhNzBmMmU5MTRiZTZhMjBiZTZjMjFjNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.FSL6ClDyrQbadIAtOFzpaHqj9wSkf9F2FYIwSh_uyHw"
    private val baseUrl = "https://api.themoviedb.org/3"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $readAccessToken")
                .addHeader("accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val findAdapter = moshi.adapter(TmdbFindResponse::class.java)
    private val movieAdapter = moshi.adapter(TmdbMovieDetail::class.java)
    private val tvAdapter = moshi.adapter(TmdbTvDetail::class.java)

    suspend fun getInfoByImdbId(imdbId: String): Any? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/find/$imdbId?api_key=$apiKey&external_source=imdb_id"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) return@withContext null

            val findResult = findAdapter.fromJson(body)
            val movie = findResult?.movie_results?.firstOrNull()
            if (movie != null) {
                return@withContext getMovieDetails(movie.id)
            }
            val tv = findResult?.tv_results?.firstOrNull()
            if (tv != null) {
                return@withContext getTvDetails(tv.id)
            }
            null
        } catch (e: Exception) {
            Log.e("TmdbApiClient", "Error finding by IMDB ID", e)
            null
        }
    }

    suspend fun getMovieDetails(tmdbId: Int): TmdbMovieDetail? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/movie/$tmdbId?api_key=$apiKey&append_to_response=credits,images"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) return@withContext null
            movieAdapter.fromJson(body)
        } catch (e: Exception) {
            Log.e("TmdbApiClient", "Error fetching movie details", e)
            null
        }
    }

    suspend fun getTvDetails(tmdbId: Int): TmdbTvDetail? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/tv/$tmdbId?api_key=$apiKey&append_to_response=credits,images"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) return@withContext null
            tvAdapter.fromJson(body)
        } catch (e: Exception) {
            Log.e("TmdbApiClient", "Error fetching TV details", e)
            null
        }
    }
}
