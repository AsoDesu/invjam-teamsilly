package dev.asodesu.teamsilly.game

import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.teamsilly.config.TeamApi
import java.lang.IllegalStateException
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bukkit.Bukkit

object Teams {
    private val client = OkHttpClient()
    private val JSON = Json {
        ignoreUnknownKeys = true
    }

    val players = mutableMapOf<UUID, Team>()
    var all = mutableListOf(
        Team("testteam1", 0, mutableListOf()),
        Team("testteam2", 0, mutableListOf()),
        Team("testteam3", 0, mutableListOf()),
        Team("testteam4", 0, mutableListOf()),
        Team("testteam5", 0, mutableListOf()),
        Team("testteam6", 0, mutableListOf()),
        Team("testteam7", 0, mutableListOf()),
        Team("testteam8", 0, mutableListOf()),
    )

    fun updateTeams() {
        players.clear()
        all.clear()

        if (TeamApi.disabled) {
            all = mutableListOf(
                Team("testteam1", 0, mutableListOf()),
                Team("testteam2", 0, mutableListOf()),
                Team("testteam3", 0, mutableListOf()),
                Team("testteam4", 0, mutableListOf()),
                Team("testteam5", 0, mutableListOf()),
                Team("testteam6", 0, mutableListOf()),
                Team("testteam7", 0, mutableListOf()),
                Team("testteam8", 0, mutableListOf()),
            )
            return
        }
        val request = Request.Builder()
            .url("http://radsteve.net:3000/")
            .addHeader("Authorization", "Bearer ${TeamApi.authKey}")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val string = response.body?.string() ?: throw IllegalStateException("Request failed: ${response.code}")
            all += JSON.decodeFromString<List<Team>>(string)
        }

        updatePlayers()
    }

    fun updatePlayers() {
        players.clear()

        all.forEach { team ->
            team.players.forEach { teamPlayer ->
                Teams.players[teamPlayer.uniqueId] = team
            }
        }
    }

    fun commitPoints() {
        if (TeamApi.disabled) return
        all.forEach {
            debug("Updating team score for ${it.name}")
            val request = Request.Builder()
                .url("http://radsteve.net:3000/team_score/${it.name}/${it.totalScore}")
                .addHeader("Authorization", "Bearer ${TeamApi.authKey}")
                .post("".toRequestBody())
                .build()
            client.newCall(request).execute()
        }
        debug("Updated all teams.")
    }

    @Serializable
    class Team(
        val name: String,
        @SerialName("total_score") var totalScore: Int,
        val players: List<TeamPlayer>
    ) {
        @Transient
        val uuids = players.map { it.uniqueId }

        @Transient
        val offlinePlayers = uuids.map { Bukkit.getOfflinePlayer(it) }
    }

    @Serializable
    class TeamPlayer(
        val uuid: String,
        var score: Int
    ) {
        @Transient val uniqueId = UUID.fromString(uuid)
    }
}