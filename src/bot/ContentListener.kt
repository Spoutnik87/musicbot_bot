package fr.spoutnik87.bot

interface ContentListener {
    /**
     * Fired when a track is started.
     */
    suspend fun onContentStart()

    /**
     * Fired when a track is manually stopped
     */
    suspend fun onContentStop()

    /**
     * Fired when a track is automatically ended.
     */
    suspend fun onContentEnd()

    /**
     * Fired when a track is loaded.
     */
    suspend fun onContentLoad(content: Content)
}