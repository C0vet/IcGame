package com.example.icgame

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val POST_URL = "https://foolstack.ru/ic-students/send-data/"
    private const val GET_URL = "https://foolstack.ru/ic-students/get-data/"

    private const val USER_NAME = "testName"
    private const val PROJECT_NAME = "testProject"

    private val client = HttpClient(CIO) {
        // Таймауты если сервер не отвечает
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 5000
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        // Отключает проверку сертификата (помогает при работе с IP)
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(pChain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(pChain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            }
        }
    }

    // Получить результаты (GET)
    suspend fun getResults(): ResultsResponse {
        return client.get(GET_URL) {
            parameter("userName", USER_NAME)
            parameter("project", PROJECT_NAME)
        }.body()
    }

    // Отправить результат (POST). Возвращает true, если сервер принял
    suspend fun sendResult(score: Int): Boolean {
        // Тело запроса как из постмана project, userName, userResult
        val json = """{"project":"$PROJECT_NAME","userName":"$USER_NAME","userResult":$score}"""

        println("API SEND JSON: $json")

        val response = client.post(POST_URL) {
            setBody(TextContent(json, ContentType.Application.Json))
        }

        // Logcat (поиск апи пост) что ответил сервер
        println("API POST: ${response.status} ${response.bodyAsText().replace("\n", " ")}")
        return response.status.isSuccess()
    }
}