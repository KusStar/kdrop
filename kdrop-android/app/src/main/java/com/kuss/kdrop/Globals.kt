package com.kuss.kdrop

object Globals {
    private const val remoteUrl = "https://kdrop.uselessthing.top/api"
    private const val localUrl = "http://localhost:3000/api"

    var apiUrl: String

    fun getBackendUrl(useRelease: Boolean): String {
        return if (BuildConfig.DEBUG) {
            if (useRelease) remoteUrl else localUrl
        } else {
            remoteUrl
        }
    }

    init {
        apiUrl = getBackendUrl(false)
    }
}

