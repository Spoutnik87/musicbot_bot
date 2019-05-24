package fr.spoutnik87.bot

interface QueueListener {
    /**
     * Called when a content is added to the queue.
     */
    suspend fun onAddContent(content: Content)

    /**
     * Called when a content is removed from the queue
     */
    suspend fun onRemoveContent(content: Content)

    /**
     * Called when the queue is cleared.
     */
    suspend fun onClear()
}