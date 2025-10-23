package ru.toshaka.advent.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("messages")
    val messages: List<ChatMessage>,
    @SerialName("model")
    val model: String,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Int = 0,
    val maxTokens: Int = 4096,
    @SerialName("presence_penalty")
    val presencePenalty: Int = 0,
    @SerialName("response_format")
    val responseFormat: ResponseFormat = ResponseFormat("text"),
    @SerialName("stop")
    val stop: String? = null,
    @SerialName("stream")
    val stream: Boolean = false,
    @SerialName("stream_options")
    val streamOptions: String? = null,
    @SerialName("temperature")
    val temperature: Float = 1f,
    @SerialName("top_p")
    val topP: Int = 1,
    @SerialName("tools")
    val tools: String? = null,
    @SerialName("tool_choice")
    val toolChoice: String = "none",
    @SerialName("logprobs")
    val logprobs: Boolean = false,
    @SerialName("top_logprobs")
    val topLogprobs: String? = null
) {

    @Serializable
    data class ChatMessage(
        @SerialName("content")
        val content: String,

        @SerialName("role")
        val role: String
    )

    @Serializable
    data class ResponseFormat(
        @SerialName("type")
        val type: String
    )
}