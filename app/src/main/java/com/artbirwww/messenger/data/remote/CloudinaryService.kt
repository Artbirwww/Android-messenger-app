package com.artbirwww.messenger.data.remote

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

object CloudinaryService {
    private val client = OkHttpClient()

    // Прямой REST-запрос к Cloudinary API для выгрузки файлов без тяжелого SDK
    fun uploadMedia(fileBytes: ByteArray, fileName: String, callback: (String?) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, RequestBody.create(MultipartBody.FORM, fileBytes))
            .addFormDataPart("upload_preset", "ml_default") // Стандартный пресет Cloudinary
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/mess-e5fcf/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    // Извлекаем secure_url из JSON-ответа
                    val url = body?.substringAfter("\"secure_url\":\"")?.substringBefore("\"")
                    callback(url)
                } else {
                    callback(null)
                }
            }
        })
    }
}