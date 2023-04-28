package com.kuss.kdrop

object Globals {
    const val remoteUrl = "http://20.205.0.103:3000/api"
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

