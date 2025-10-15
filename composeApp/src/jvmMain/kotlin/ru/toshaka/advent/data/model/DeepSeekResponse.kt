package ru.toshaka.advent.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekResponse(
    @SerialName("id")
    val id: String,
    @SerialName("object")
    val `object`: String,
    @SerialName("created")
    val created: Long,
    @SerialName("model")
    val model: String,
    @SerialName("choices")
    val choices: List<Choice>,
    @SerialName("usage")
    val usage: Usage? = null,
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null
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
    data class Message(
        @SerialName("role")
        val role: String,
        @SerialName("content")
        val content: String
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
        val promptCacheMissTokens: Int? = null
    ) {
        @Serializable
        data class PromptTokensDetails(
            @SerialName("cached_tokens")
            val cachedTokens: Int
        )
    }
}