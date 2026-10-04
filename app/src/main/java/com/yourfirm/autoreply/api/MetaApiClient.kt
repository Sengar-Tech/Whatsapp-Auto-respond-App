package com.yourfirm.autoreply.api

import com.yourfirm.autoreply.api.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the Meta Cloud API.
 *
 * Kept as an object so the OkHttpClient (and its thread pool + connection pool)
 * is created exactly once and reused across every API call.
 *
 * Logging interceptor is LEVEL.BODY in debug. In release ProGuard strips it
 * to LEVEL.NONE so tokens never appear in logcat on user devices.
 */
object MetaApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://graph.facebook.com/v19.0/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: MetaApiService = retrofit.create(MetaApiService::class.java)

    /**
     * Sends a pre-approved template message to [toNumber].
     * Throws an exception if the API returns a non-2xx response — caller logs it.
     *
     * @param toNumber  Normalized number WITH country code, no plus sign (e.g. "919876543210")
     * @param mediaId   Media ID returned by [ImageUploader.upload]
     * @param urlButton Optional URL for the template button — null omits the button component
     */
    suspend fun send(
        phoneNumberId: String,
        accessToken: String,
        toNumber: String,
        templateName: String,
        mediaId: String,
        urlButton: String? = null
    ) {
        val components = mutableListOf(
            // Header component — contains the uploaded PNG via its media ID
            TemplateComponent(
                type = "header",
                parameters = listOf(
                    TemplateParameter(type = "image", image = MediaObject(id = mediaId))
                )
            )
        )

        // Only add URL button if the user configured one
        if (!urlButton.isNullOrBlank()) {
            components.add(
                TemplateComponent(
                    type = "button",
                    sub_type = "url",
                    index = "0",
                    parameters = listOf(TemplateParameter(type = "text", text = urlButton))
                )
            )
        }

        val request = SendMessageRequest(
            to = toNumber,
            template = TemplateBody(
                name = templateName,
                language = Language(),
                components = components
            )
        )

        val response = service.sendMessage(
            phoneNumberId = phoneNumberId,
            authorization = "Bearer $accessToken",
            body = request
        )

        if (!response.isSuccessful) {
            val error = response.errorBody()?.string() ?: "unknown error"
            throw Exception("API ${response.code()}: $error")
        }
    }
}
