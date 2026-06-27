package com.artbirwww.messenger.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class PushNotification(
    @SerializedName("to") val to: String,
    @SerializedName("notification") val notification: NotificationData,
    @SerializedName("data") val data: Map<String, String>
)

data class NotificationData(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("sound") val sound: String = "default"
)

interface FCMApi {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") serverKey: String,
        @Body notification: PushNotification
    )
}

object FCMClient {
    private const val BASE_URL = "https://fcm.googleapis.com/"
    private const val SERVER_KEY = "key=AIzaSyBeSTojkn_pj9MzgP2RFMTHSV3IIb-5rEQ"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FCMApi by lazy {
        retrofit.create(FCMApi::class.java)
    }

    suspend fun sendPush(token: String, title: String, message: String, chatId: String) {
        try {
            val push = PushNotification(
                to = token,
                notification = NotificationData(title, message),
                data = mapOf("chatId" to chatId, "type" to "chat_message")
            )
            api.sendNotification(serverKey = SERVER_KEY, notification = push)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
