package com.artbirwww.messenger.data.remote

import android.util.Base64
import com.artbirwww.messenger.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class GitHubUploadRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: String,
    @SerializedName("branch") val branch: String = "main"
)

data class GitHubUploadResponse(
    @SerializedName("content") val content: GitHubContent
)

data class GitHubContent(
    @SerializedName("path") val path: String,
    @SerializedName("sha") val sha: String,
    @SerializedName("download_url") val downloadUrl: String
)

interface GitHubApi {
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun uploadFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") token: String,
        @Body body: GitHubUploadRequest
    ): GitHubUploadResponse
}

object GitHubService {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: GitHubApi = retrofit.create(GitHubApi::class.java)

    private fun getAuthHeader(): String = "token ${BuildConfig.GITHUB_TOKEN}"

    suspend fun uploadFile(fileBytes: ByteArray, fileName: String, type: String, userId: String, chatId: String? = null): String? {
        return try {
            val base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
            val timestamp = System.currentTimeMillis()
            val uniqueId = (0..100000).random().toString(36)
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            
            val path = when (type) {
                "avatar" -> "avatars/$userId/${timestamp}_${uniqueId}_$safeName"
                "chat" -> "chats/$chatId/${timestamp}_${uniqueId}_$safeName"
                "audio" -> "audio/$chatId/${timestamp}_${uniqueId}_$safeName"
                "video" -> "video/$chatId/${timestamp}_${uniqueId}_$safeName"
                else -> "files/$userId/${timestamp}_${uniqueId}_$safeName"
            }

            api.uploadFile(
                owner = BuildConfig.GITHUB_OWNER,
                repo = BuildConfig.GITHUB_REPO,
                path = path,
                token = getAuthHeader(),
                body = GitHubUploadRequest(
                    message = "Upload $path",
                    content = base64Content
                )
            )
            
            // Constructing raw URL similar to web version
            // https://raw.githubusercontent.com/${REPO_OWNER}/${REPO_NAME}/${BRANCH}/${filePath}
            "https://raw.githubusercontent.com/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}/main/$path"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
