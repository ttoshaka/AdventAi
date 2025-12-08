package ru.toshaka.advent.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatResponse(
    @SerialName("id")
    val id: String? = null,
    @SerialName("object")
    val `object`: String? = null,
    @SerialName("created")
    val created: Long? = null,
    @SerialName("model")
    val model: String,
    @SerialName("choices")
    val choices: List<Choice>? = null,
    @SerialName("usage")
    val usage: Usage? = null,
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null,
    @SerialName("message")
    val message: Message? = null,
) {
    @Serializable
    data class Choice(
        @SerialName("index")
        val index: Int,
        @SerialName("message")
        val message: Message,
        @SerialName("logprobs")
        val logprobs: String? = null,
        @SerialName("finish_reason")
        val finishReason: String? = null
    )

    @Serializable
    data class ToolCall(
        val index: Int? = null,
        val id: String,
        val type: String? = null,
        val function: ToolFunction,
    )

    @Serializable
    data class ToolFunction(
        val name: String,
        @SerialName("arguments")
        val arguments: Map<String, JsonElement>,
    )

    @Serializable
    data class Message(
        @SerialName("role")
        val role: String,
        @SerialName("content")
        val content: String,
        @SerialName("tool_calls")
        val toolCall: List<ToolCall>? = null,
    )

    @Serializable
    data class Usage(
        @SerialName("prompt_tokens")
        val promptTokens: Int,
        @SerialName("completion_tokens")
        val completionTokens: Int,
        @SerialName("total_tokens")
        val totalTokens: Int,
        @SerialName("prompt_tokens_details")
        val promptTokensDetails: PromptTokensDetails? = null,
        @SerialName("prompt_cache_hit_tokens")
        val promptCacheHitTokens: Int? = null,
        @SerialName("prompt_cache_miss_tokens")
        val promptCacheMissTokens: Int? = null,
        @SerialName("total_time")
        val totalTime: Float? = null,
    ) {
        @Serializable
        data class PromptTokensDetails(
            @SerialName("cached_tokens")
            val cachedTokens: Int
        )
    }
}