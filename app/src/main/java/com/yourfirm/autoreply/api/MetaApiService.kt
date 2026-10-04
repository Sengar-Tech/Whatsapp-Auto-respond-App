package com.yourfirm.autoreply.api

import com.yourfirm.autoreply.api.models.SendMessageRequest
import com.yourfirm.autoreply.api.models.SendMessageResponse
import com.yourfirm.autoreply.api.models.UploadMediaResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface — each function maps to one Meta Cloud API endpoint.
 * Retrofit generates the actual HTTP implementation at runtime via reflection.
 *
 * Base URL is set in [MetaApiClient]: https://graph.facebook.com/v19.0/
 */
interface MetaApiService {

    /**
     * Send a pre-approved template message.
     * POST https://graph.facebook.com/v19.0/{phoneNumberId}/messages
     */
    @POST("{phoneNumberId}/messages")
    suspend fun sendMessage(
        @Path("phoneNumberId") phoneNumberId: String,
        @Header("Authorization") authorization: String,
        @Body body: SendMessageRequest
    ): Response<SendMessageResponse>

    /**
     * Upload a PNG image and receive a media ID back.
     * POST https://graph.facebook.com/v19.0/{phoneNumberId}/media
     */
    @Multipart
    @POST("{phoneNumberId}/media")
    suspend fun uploadMedia(
        @Path("phoneNumberId") phoneNumberId: String,
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("messaging_product") messagingProduct: RequestBody
    ): Response<UploadMediaResponse>
}
