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
        const val DEFAULT_MASTER_TOKEN = "O6zE4PZv1RHKMxWc0ZU5SkLgdObmoJgh"
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
                "$cleanBase/stremio/$token/catalog/$type/$catalogId.json"
            } else {
                "$cleanBase/stremio/$token/catalog/$type/$catalogId/${extraParts.joinToString("&")}.json"
            }

            Log.d("StremioApiClient", "Fetching catalog: $url")
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && token != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Catalog token $token returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchCatalog(serverUrl, DEFAULT_MASTER_TOKEN, type, catalogId, genre, search, skip)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = catalogAdapter.fromJson(body)
            Result.success(parsed?.metas ?: emptyList())
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
        try {
            val cleanBase = serverUrl.trimEnd('/')
            val encodedId = id.replace(":", "%3A")
            val url = "$cleanBase/stremio/$token/meta/$type/$encodedId.json"

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && token != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Meta token $token returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchMeta(serverUrl, DEFAULT_MASTER_TOKEN, type, id)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = metaAdapter.fromJson(body)
            val meta = parsed?.meta
            if (meta != null) {
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
        try {
            val cleanBase = serverUrl.trimEnd('/')
            val encodedId = id.replace(":", "%3A")
            val url = "$cleanBase/stremio/$token/stream/$type/$encodedId.json"

            Log.d("StremioApiClient", "Fetching primary streams: $url")
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body.isNullOrBlank()) {
                if ((response.code == 401 || response.code == 403) && token != DEFAULT_MASTER_TOKEN) {
                    Log.w("StremioApiClient", "Stream token $token returned HTTP ${response.code}, retrying with DEFAULT_MASTER_TOKEN")
                    return@withContext fetchStreams(serverUrl, DEFAULT_MASTER_TOKEN, type, id)
                }
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val parsed = streamAdapter.fromJson(body)
            val streams = (parsed?.streams ?: emptyList()).filter { it.isPlayable }
            Result.success(streams)
        } catch (e: Exception) {
            Log.e("StremioApiClient", "Primary stream error", e)
            Result.failure(e)
        }
    }
}
