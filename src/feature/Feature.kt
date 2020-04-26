package fr.spoutnik87.feature


interface Feature {
    suspend fun start()
    suspend fun stop()
}
