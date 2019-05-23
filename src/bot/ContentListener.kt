package fr.spoutnik87.bot

interface ContentListener {
    suspend fun onContentStart()
    suspend fun onContentStop()
    suspend fun onContentLoad(content: Content)
}