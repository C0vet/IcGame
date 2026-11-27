package com.example.icgame

import kotlinx.serialization.SerialName// имя поля в JSON
import kotlinx.serialization.Serializable// класс можно превращать в JSON и обратно

@Serializable
data class GameResult(
    @SerialName("resultId") val id: Int? = null,
    @SerialName("project") val project: String? = null,
    @SerialName("userName") val userName: String? = null,   // имя игрокаесли сервер его присылает
    @SerialName("result") val score: Int,
    @SerialName("dateTime") val dateTime: Long? = null
)

@Serializable
data class ResultsResponse(
    @SerialName("results") val results: List<GameResult>,
    @SerialName("errorMsg") val errorMsg: String? = null
)

// Класс для отправки данных (вместо mapOf)была ошибка сериализации разных типов)
@Serializable
data class SendGameResultRequest(
    @SerialName("project") val project: String,
    @SerialName("result") val score: Int,
    @SerialName("userName") val userName: String
)