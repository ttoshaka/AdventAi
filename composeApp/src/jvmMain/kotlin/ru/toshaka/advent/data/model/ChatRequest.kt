package ru.toshaka.advent.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ChatRequest(
    @SerialName("messages")
    val messages: List<ChatMessage>,
    @SerialName("model")
    val model: String,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Int = 0,
    @SerialName("max_tokens")
    val maxTokens: Int = 12000,
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
    val temperature: Float = 0.2f,
    @SerialName("top_p")
    val topP: Int = 1,
    @SerialName("tools")
    val tools: List<Tool>? = null,
    @SerialName("tool_choice")
    val toolChoice: String = "auto",
    @SerialName("logprobs")
    val logprobs: Boolean = false,
    @SerialName("top_logprobs")
    val topLogprobs: String? = null,
) {

    @Serializable
    data class ChatMessage(
        @SerialName("content")
        val content: String?,
        @SerialName("role")
        val role: String,
        @SerialName("tool_call_id")
        val toolCallId: String? = null,
        @SerialName("tool_calls")
        val toolCalls: List<ChatResponse.ToolCall>? = null,
    )

    @Serializable
    data class ResponseFormat(
        @SerialName("type")
        val type: String
    )


    @Serializable
    data class Tool(
        val type: String,
        val function: ToolFunction
    )

    @Serializable
    data class ToolFunction(
        val name: String,
        val description: String?,
        val parameters: ToolParameter
    )

    @Serializable
    data class ToolParameter(
        val type: String,
        val properties: JsonObject,
        val required: List<String>?
    )
}