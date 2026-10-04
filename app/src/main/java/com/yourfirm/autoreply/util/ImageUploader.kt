package com.yourfirm.autoreply.util

import android.content.Context
import android.net.Uri
import com.yourfirm.autoreply.api.MetaApiService
import com.yourfirm.autoreply.api.models.UploadMediaResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Uploads a PNG from local storage to Meta's media endpoint.
 * Returns the media ID string that goes into the template header component.
 *
 * A new Retrofit instance is used here (separate from MetaApiClient) because
 * this is a multipart upload — different OkHttp write timeout is appropriate.
 *
 * The image is read entirely into memory before upload. At max 5 MB for a
 * WhatsApp template header image this is fine; do not use for video.
 */
object ImageUploader {

    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)   // longer for multipart upload
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://graph.facebook.com/v19.0/")
        .client(uploadClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: MetaApiService = retrofit.create(MetaApiService::class.java)

    /**
     * @param imageUriString  The URI string stored in prefs (file:// or content://)
     * @return  Meta media ID — e.g. "1234567890123456"
     * @throws  Exception if the file can't be read or the upload fails
     */
    suspend fun upload(
        context: Context,
        imageUriString: String,
        phoneNumberId: String,
        accessToken: String
    ): String {
        val uri = Uri.parse(imageUriString)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image file — please re-select it in Template settings")

        val bytes = inputStream.use { it.readBytes() }

        val requestFile = bytes.toRequestBody("image/png".toMediaType())
        val filePart    = MultipartBody.Part.createFormData("file", "template.png", requestFile)
        val typePart    = "image/png".toRequestBody("text/plain".toMediaType())
        val productPart = "whatsapp".toRequestBody("text/plain".toMediaType())

        val response = service.uploadMedia(
            phoneNumberId  = phoneNumberId,
            authorization  = "Bearer $accessToken",
            file           = filePart,
            type           = typePart,
            messagingProduct = productPart
        )

        if (!response.isSuccessful) {
            val error = response.errorBody()?.string() ?: "unknown error"
            throw Exception("Image upload failed ${response.code()}: $error")
        }

        return response.body()?.id
            ?: throw Exception("Upload succeeded but no media ID returned")
    }
}
