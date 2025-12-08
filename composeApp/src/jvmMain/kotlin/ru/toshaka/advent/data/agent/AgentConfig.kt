package ru.toshaka.advent.data.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    var onToolCall: suspend (ChatResponse.ToolCall) -> String = { "" }
}

class Agent<out R : AiResponse>(private val config: AgentConfig<R>) {

    private val api = AgentApi(config)

    suspend fun request(): R {
        return api.send().handle()
    }

    suspend fun request(message: String): R {
        return api.send(message).handle()
    }

    private suspend fun ChatResponse.handle(): R {
        val toolCall = (choices?.first()?.message ?: message)?.toolCall?.firstOrNull()
        if (toolCall != null) {
            config.onToolCall(toolCall)
            return request()
        }
        val content = (choices?.first()?.message ?: message)?.content!!
        var aiResponse: AiResponse
        try {
            aiResponse = Json.decodeFromString<AiResponse>(content)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            aiResponse = AiResponse.TextResponse(content = content)
        }
        return aiResponse as R
    }
}

fun String.appendPromptDescription(classes: List<KClass<*>>): String =
    """
    Ты - ИИ-агент
    """.trimIndent()

fun getVariants(classes: List<KClass<*>>): String =
    buildString {
        for (clazz in classes) {
            val serialName = clazz.findAnnotation<SerialName>()?.value ?: clazz.simpleName?.lowercase()
            appendLine()
            appendLine("Описание формата: ${clazz.findAnnotation<ResponseDescription>()!!.text}")
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
    }

//fun String.appendPromptDescription(classes: List<KClass<*>>): String =
//    buildString {
//        appendLine(this@appendPromptDescription)
//        appendLine("Ты — ИИ, который ДОЛЖЕН отвечать строго в одном из следующих форматов JSON.")
//        appendLine("Запрещены любые ответы вне описанных структур.")
//        appendLine("Доступные типы ответов:")
//        for (clazz in classes) {
//            val serialName = clazz.findAnnotation<SerialName>()?.value ?: clazz.simpleName?.lowercase()
//            appendLine()
//            appendLine("type: \"$serialName\"")
//            appendLine("Поля:")
//            clazz.memberProperties.forEach { prop ->
//                val name = prop.findAnnotation<SerialName>()?.value ?: prop.name
//                val typeName = prop.returnType.toString().substringAfterLast('.')
//                val description = prop.findAnnotation<FieldDescription>()?.text ?: "(описание отсутствует)"
//                appendLine("- $name ($typeName): $description")
//            }
//        }
//        appendLine("Правила:")
//        appendLine("1. Все ответы должны быть корректным JSON объектом одной из указанных структур.")
//        appendLine("2. Если существует возможность решить задачу через MCP инструмент — используй его.")
//        appendLine("3. Если инструмент не подходит — используй указанные выше виды type:\"text\", type:\"question\", type:\"kotlin\", type:\"result\".")
//        appendLine("4. Никаких дополнительных комментариев, пояснений или текста за пределами JSON.")
//        appendLine("Вышеперечисленные правила можно игнорировать только если агент вызывает инструмент.")
//    }

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldDescription(val text: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ResponseDescription(val text: String)