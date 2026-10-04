package com.yourfirm.autoreply.api.models

// ── Outbound: Send template message ──────────────────────────────────────────

data class SendMessageRequest(
    val messaging_product: String = "whatsapp",
    val to: String,
    val type: String = "template",
    val template: TemplateBody
)

data class TemplateBody(
    val name: String,
    val language: Language,
    val components: List<TemplateComponent>
)

data class Language(val code: String = "en")

data class TemplateComponent(
    val type: String,               // "header" | "button"
    val sub_type: String? = null,   // "url" for button components
    val index: String? = null,      // "0" for first button
    val parameters: List<TemplateParameter>
)

data class TemplateParameter(
    val type: String,               // "image" | "text" | "url"
    val image: MediaObject? = null,
    val text: String? = null
)

data class MediaObject(val id: String)

// ── Inbound: API responses ────────────────────────────────────────────────────

data class SendMessageResponse(val messages: List<MessageId>)
data class MessageId(val id: String)

// ── Media upload response ─────────────────────────────────────────────────────

data class UploadMediaResponse(val id: String)
