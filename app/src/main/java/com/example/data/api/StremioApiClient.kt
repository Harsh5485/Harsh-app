package com.example.data.api

import android.util.Log
import com.example.data.model.CatalogResponse
import com.example.data.model.MediaItem
import com.example.data.model.MetaDetail
import com.example.data.model.MetaResponse
import com.example.data.model.StreamItem
import com.example.data.model.StreamResponse
import com.example.data.model.TokenItem
import com.example.data.model.TokensResponse
import com.example.util.StreamUrlHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class StremioApiClient {

    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    // In-memory caching to eliminate server load and make catalog, meta, and stream fetches lightning fast
    private val catalogCache = ConcurrentHashMap<String, Pair<Long, List<MediaItem>>>()
    private val metaCache = ConcurrentHashMap<String, Pair<Long, MetaDetail>>()
    private val streamCache = ConcurrentHashMap<String, Pair<Long, List<StreamItem>>>()

    private val CATALOG_CACHE_TTL = 10 * 60 * 1000L // 10 minutes
    private val META_CACHE_TTL = 30 * 60 * 1000L    // 30 minutes
    private val STREAM_CACHE_TTL = 8 * 60 * 1000L   // 8 minutes

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val list = cookieStore.getOrPut(url.host) { mutableListOf() }
            list.removeAll { old -> cookies.any { it.name == old.name } }
            list.addAll(cookies)
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectionPool(okhttp3.ConnectionPool(10, 5, TimeUnit.MINUTES))
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val catalogAdapter = moshi.adapter(CatalogResponse::class.java)
    private val metaAdapter = moshi.adapter(MetaResponse::class.java)
    private val streamAdapter = moshi.adapter(StreamResponse::class.java)
    private val tokensAdapter = moshi.adapter(TokensResponse::class.java)

    suspend fun loginAndGetTokens(
        serverUrl: String,
        user: String,
        pass: String
    ): Result<List<TokenItem>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = serverUrl.trimEnd('/')
            val loginUrl = "$cleanBase/login"

            val formBody = FormBody.Builder()
                .add("username", user)
                .add("password", pass)
                .build()

            val loginRequest = Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build()

            val loginResponse = okHttpClient.newCall(loginRequest).execute()
            loginResponse.close()

            // Fetch tokens from /api/admin/access/tokens
            val tokensUrl = "$cleanBase/api/admin/access/tokens"
            val tokensRequest = Request.Builder()
                .url(tokensUrl)
                .get()
                .build()

            val tokensResponse = okHttpClient.newCall(tokensRequest).execute()
            val body = tokensResponse.body?.string()

            if (!tokensResponse.isSuccessful || body.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Failed to fetch tokens (HTTP ${tokensResponse.code})"))
            }

            val parsed = tokensAdapter.fromJson(body)
            val list = parsed?.tokens ?: emptyList()
            Result.success(list)
        } catch (e: Exception) {
            Log.e("StremioApiClient", "login error", e)
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_MASTER_TOKEN = StreamUrlHelper.HARSH_TOKEN
    }

    suspend fun createServerTokenOnHost(
        serverUrl: String,
        userName: String,
        adminUser: String = "Harsh",
        adminPass: String = "Nokia@2134"
    ): Result<TokenItem> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = serverUrl.trimEnd('/')
            val loginUrl = "$cleanBase/login"
            val loginFormBody = FormBody.Builder()
                .add("username", adminUser)
                .add("password", adminPass)
                .build()

            val loginRequest = Request.Builder().url(loginUrl).post(loginFormBody).build()
            val loginRes = okHttpClient.newCall(loginRequest).execute()
            if (!loginRes.isSuccessful) {
                Log.e("StremioApiClient", "Admin login failed for token creation: ${loginRes.code}")
            }
            loginRes.close()

            // 1. Post JSON payload to /api/admin/access/tokens
            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val jsonPayload = """
                {
                    "user_name": "$userName",
                    "never_expires": true,
                    "lifetime": true,
                    "daily_limit_gb": 0,
                    "monthly_limit_gb": 0
                }
            """.trimIndent()
            val jsonRequestBody = jsonPayload.toRequestBody(jsonMediaType)

            val jsonRequest = Request.Builder()
                .url("$cleanBase/api/admin/access/tokens")
                .post(jsonRequestBody)
                .build()

            val jsonRes = okHttpClient.newCall(jsonRequest).execute()
            val jsonResCode = jsonRes.code
            val jsonResBody = jsonRes.body?.string()
            jsonRes.close()
            Log.d("StremioApiClient", "JSON creation response: $jsonResCode, Body: $jsonResBody")

            // 2. Post FormBody payload as fallback if JSON creation didn't seem to work (often server accepts one or the other)
            if (jsonResCode != 200 && jsonResCode != 201) {
                val formBody = FormBody.Builder()
                    .add("user_name", userName)
                    .add("never_expires", "true")
                    .add("lifetime", "true")
                    .build()

                val formRequest = Request.Builder()
                    .url("$cleanBase/api/admin/access/tokens")
                    .post(formBody)
                    .build()

                val formRes = okHttpClient.newCall(formRequest).execute()
                Log.d("StremioApiClient", "Form creation response: ${formRes.code}")
                formRes.close()
            }

            // 3. Re-fetch all tokens from server to find the created token
            val tokensRes = loginAndGetTokens(serverUrl, adminUser, adminPass)
            val tokens = tokensRes.getOrNull() ?: emptyList()

            // Try to find the exact match for the user name
            val matchedNewToken = tokens.filter { it.user_name != null }
                .firstOrNull { it.user_name!!.equals(userName, ignoreCase = true) }
                ?: tokens.lastOrNull { it.token.isNotBlank() }

            if (matchedNewToken != null && matchedNewToken.token.isNotBlank()) {
                Log.d("StremioApiClient", "Successfully created/found token for $userName: ${matchedNewToken.token}")
                return@withContext Result.success(matchedNewToken)
            }

            Result.failure(Exception("Failed to create token on server for $userName"))
        } catch (e: Exception) {
            Log.e("StremioApiClient", "createServerTokenOnHost error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchCatalog(
        serverUrl: String,
        token: String,
        type: String,
        catalogId: String,
        genre: String? = null,
        search: String? = null,
        skip: Int = 0
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        val activeToken = if (token.isNotBlank()) token else DEFAULT_MASTER_TOKEN
        val cacheKey = "$serverUrl|$activeToken|$type|$catalogId|$genre|$search|$skip"
        val cached = catalogCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.first) < CATALOG_CACHE_TTL) {
            return@withContext Result.success(cached.second)
        }

        try {
            val cleanBase = serverUrl.trimEnd('/')
            val extraParts = mutableListOf<String>()

            if (!genre.isNullOrBlank()) {
                extraParts.add("genre=${URLEncoder.encode(genre, "UTF-8")}")
            }
            if (!search.isNullOrBlank()) {
                extraParts.add("search=${URLEncoder.encode(search, "UTF-8")}")
            }
            if (skip > 0) {
                extraParts.add("skip=$skip")
            }

            val url = if (extraParts.isEmpty()) {
                "$cleanBase/stremio/$activeToken/catalog/$type/$catalogId.json"
            } else {
                "$cleanBase/stremio/$activeToken/catalog/$type/$catalogId/${extraParts.joinToString("&")}.json"
            }

            Log.d("StremioApiClient", "Fetching catalog: $url")
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && activeToken != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Catalog token $activeToken returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchCatalog(serverUrl, DEFAULT_MASTER_TOKEN, type, catalogId, genre, search, skip)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = catalogAdapter.fromJson(body)
            val items = parsed?.metas ?: emptyList()
            catalogCache[cacheKey] = Pair(System.currentTimeMillis(), items)
            Result.success(items)
        } catch (e: Exception) {
            Log.e("StremioApiClient", "Catalog error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchMeta(
        serverUrl: String,
        token: String,
        type: String,
        id: String
    ): Result<MetaDetail> = withContext(Dispatchers.IO) {
        val activeToken = if (token.isNotBlank()) token else DEFAULT_MASTER_TOKEN
        val cacheKey = "$serverUrl|$activeToken|$type|$id"
        val cached = metaCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.first) < META_CACHE_TTL) {
            return@withContext Result.success(cached.second)
        }

        try {
            val cleanBase = serverUrl.trimEnd('/')
            val encodedId = id.replace(":", "%3A")
            val url = "$cleanBase/stremio/$activeToken/meta/$type/$encodedId.json"

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && activeToken != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Meta token $activeToken returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchMeta(serverUrl, DEFAULT_MASTER_TOKEN, type, id)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = metaAdapter.fromJson(body)
            val meta = parsed?.meta
            if (meta != null) {
                metaCache[cacheKey] = Pair(System.currentTimeMillis(), meta)
                Result.success(meta)
            } else {
                Result.failure(Exception("Meta detail was null"))
            }
        } catch (e: Exception) {
            Log.e("StremioApiClient", "Meta error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchCinemetaMeta(
        type: String,
        id: String
    ): Result<MetaDetail> = withContext(Dispatchers.IO) {
        val cacheKey = "cinemeta|$type|$id"
        val cached = metaCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.first) < META_CACHE_TTL) {
            return@withContext Result.success(cached.second)
        }

        try {
            val encodedId = id.replace(":", "%3A")
            val url = "https://v3-cinemeta.strem.io/meta/$type/$encodedId.json"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Cinemeta HTTP ${response.code}"))
            }

            val parsed = metaAdapter.fromJson(body)
            val meta = parsed?.meta
            if (meta != null) {
                metaCache[cacheKey] = Pair(System.currentTimeMillis(), meta)
                Result.success(meta)
            } else {
                Result.failure(Exception("Cinemeta meta was null"))
            }
        } catch (e: Exception) {
            Log.e("StremioApiClient", "Cinemeta error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchStreams(
        serverUrl: String,
        token: String,
        type: String,
        id: String
    ): Result<List<StreamItem>> = withContext(Dispatchers.IO) {
        val activeToken = if (token.isNotBlank()) token else DEFAULT_MASTER_TOKEN
        val cacheKey = "$serverUrl|$activeToken|$type|$id"
        val cached = streamCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.first) < STREAM_CACHE_TTL) {
            return@withContext Result.success(cached.second)
        }

        try {
            val cleanBase = serverUrl.trimEnd('/')
            val encodedId = id.replace(":", "%3A")
            val url = "$cleanBase/stremio/$activeToken/stream/$type/$encodedId.json"

            Log.d("StremioApiClient", "Fetching primary streams: $url")
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && activeToken != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Stream token $activeToken returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchStreams(serverUrl, DEFAULT_MASTER_TOKEN, type, id)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = streamAdapter.fromJson(body)
            val rawStreams = (parsed?.streams ?: emptyList()).filter { it.isPlayable }
            // Automatically proxy all stream URLs through Harsh's high-speed Cloudflare Worker proxy
            val streams = rawStreams.map { stream ->
                val proxiedUrl = StreamUrlHelper.toWorkerProxyUrl(stream.url)
                stream.copy(url = proxiedUrl)
            }
            streamCache[cacheKey] = Pair(System.currentTimeMillis(), streams)
            Result.success(streams)
        } catch (e: Exception) {
            Log.e("StremioApiClient", "Primary stream error", e)
            Result.failure(e)
        }
    }
}
