package dev.asodesu.teamsilly.config

object TeamApi : Config("teamapi") {
    val authKey by value("authKey", "")
    val disabled by value("disabled", true)
}