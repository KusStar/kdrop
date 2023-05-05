package com.kuss.kdrop

/**
 * 包含全局属性和函数的单例对象 [Globals]
 */
object Globals {
    /**
     * 远程服务器的地址 [remoteUrl]，应在生产环境下使用
     */
    const val remoteUrl = "http://20.205.0.103:3000/api"

    /**
     * 本地服务器的地址 [localUrl]，用于开发环境下测试使用
     */
    private const val localUrl = "http://localhost:3000/api"

    /**
     * 指定底层 sockets 所绑定的端口号 [socketPort]，默认为 9999
     */
    const val socketPort = 9999

    /**
     * 存放 API 的 URL 地址 [apiUrl]，根据当前环境自动选择 [remoteUrl] 或 [localUrl]，初始值为 [getBackendUrl] 的返回值
     */
    var apiUrl: String

    /**
     * 根据调用者的 [useRelease] 值返回 [remoteUrl] 或 [localUrl]，用于获取后端 API 的 URL
     * @param useRelease 控制是否取远程服务器地址
     * @return 返回 [remoteUrl] 或 [localUrl] 其中一个 URL
     */
    fun getBackendUrl(useRelease: Boolean): String {
        return if (BuildConfig.DEBUG) { // 判断当前是否为开发环境
            if (useRelease) remoteUrl else localUrl // 如果 useRelease 参数为 true 则返回 [remoteUrl] 否则返回 [localUrl]
        } else { // 如果当前不处于开发环境则直接返 [remoteUrl]
            remoteUrl
        }
    }

    /**
     * 初始化函数 [init]，在对象创建时被调用
     */
    init {
        apiUrl = getBackendUrl(false) // 调用 [getBackendUrl] 函数获取 API 的 URL，并将返回值赋给 [apiUrl] 变量
    }
}
