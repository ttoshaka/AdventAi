package ru.toshaka.advent.data.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.AgentApi
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.data.model.ChatRequest
import ru.toshaka.advent.data.model.ChatResponse
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class AgentConfig<R : AiResponse> {

    var name: String = UUID.randomUUID().toString()

    var temperature: Float = 1f

    var maxTokens: Int = 4096

    var model: String = ""

    var baseUrl: String = ""

    var key: String = ""

    var systemPrompt: String = ""
        get() = field.appendPromptDescription(outputFormats)

    var outputFormats: List<KClass<*>> = listOf(AiResponse.TextResponse::class)

    var history: suspend () -> List<MessageEntity> = { emptyList() }

    var tools: List<ChatRequest.Tool>? = null

    var onToolCall: suspend (String, String) -> String = { _, _ -> "" }
}

class Agent<R : AiResponse>(private val config: AgentConfig<R>) {

    private val api = AgentApi(config)

    suspend fun request(): R {
        return api.send().handle()
    }

    suspend fun request(message: String): R {
        return api.send(message).handle()
    }

    private suspend fun ChatResponse.handle(): R {
        val toolCall = choices.first().message.toolCall?.firstOrNull()
        if (toolCall != null) {
            val toolResponse = config.onToolCall(toolCall.function.name, toolCall.function.arguments)
            val response = api.send(toolResponse, toolCall)
            return response.handle()
        }
        val message = Json.decodeFromString<AiResponse>(choices.first().message.content) as R
        return message
    }
}

fun <R : AiResponse> Agent(config: AgentConfig<R>.() -> Unit): Agent<R> =
    Agent(AgentConfig<R>().apply(config))

fun String.appendPromptDescription(classes: List<KClass<*>>): String =
    buildString {
        appendLine(this@appendPromptDescription)
        appendLine("Ты — AI, который всегда отвечает строго в JSON.")
        appendLine("Формат ответов должен соответствовать одной из следующих схем:")
        appendLine()
        for (clazz in classes) {
            val serialName = clazz.findAnnotation<SerialName>()?.value ?: clazz.simpleName?.lowercase()
            appendLine()
            appendLine("type: \"$serialName\"")
            appendLine("Поля:")
            clazz.memberProperties.forEach { prop ->
                val name = prop.findAnnotation<SerialName>()?.value ?: prop.name
                val typeName = prop.returnType.toString().substringAfterLast('.')
                val description = prop.findAnnotation<FieldDescription>()?.text ?: "(описание отсутствует)"
                appendLine("- $name ($typeName): $description")
            }
        }
        appendLine()
        appendLine("Отвечай только одним JSON-объектом, без текста вне фигурных скобок.")
    }

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldDescription(val text: String)