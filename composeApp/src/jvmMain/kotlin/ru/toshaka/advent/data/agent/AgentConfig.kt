package ru.toshaka.advent.data.agent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import ru.toshaka.advent.data.AgentApi
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class AgentConfig<R : AiResponse> {

    var name: String = UUID.randomUUID().toString()

    var temperature: Float = 1f

    var model: String = ""

    var baseUrl: String = ""

    var key: String = ""

    var systemPrompt: String = ""
        get() = field.appendPromptDescription(outputFormats)

    var inputFormats: KClass<out AiResponse>? = null

    var outputFormats: List<KClass<*>> = listOf(AiResponse.TextResponse::class)

    var isReceiveUserMessage: Boolean = false

    var history: () -> List<Pair<String, String>> = { emptyList() }

    lateinit var onAiResponse: (R, AgentReponseDebugInfo) -> Unit

    var tools: (String, Map<String, Any?>) -> String = { q, w -> "" }
}

class Agent<R : AiResponse>(private val config: AgentConfig<R>) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val api = AgentApi(config)

    val name: String = config.name
    val isReceiveUserMessage = config.isReceiveUserMessage
    var receiveType: KClass<out AiResponse>? = config.inputFormats

    operator fun invoke(message: String, force: Boolean = false) = scope.launch {
        val response = api(message, config.history(), force)
        val mes = Json.decodeFromString<AiResponse>(response.choices.first().message.content) as R
        val usage = response.usage!!
        config.onAiResponse(
            mes, AgentReponseDebugInfo(
                promptToken = usage.promptTokens,
                completionToken = usage.completionTokens,
                totalToken = usage.totalTokens,
                agentName = name,
            )
        )
        if (mes is AiResponse.ToolCall) {
            val toolResponse = config.tools(mes.name, Json.parseToJsonElement(mes.args).jsonObject)
            send("Результат работы инструмента - $toolResponse")
        }
    }

    private fun send(message: String) {
        invoke(message, true)
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
        appendLine("Поддерживаемые типы ответов:")
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