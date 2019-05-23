package fr.spoutnik87.bot

class Queue(
    private val listener: QueueListener
) {

    private val queue = ArrayList<Content>()

    suspend fun addContent(content: Content) {
        this.queue.add(content)
        listener.onAddContent(content)
    }

    fun getAllContents(): List<Content> = this.queue

    fun getContent(id: String): Content? {
        return this.queue.find { it.id == id }
    }

    suspend fun clearQueue() {
        this.queue.clear()
        listener.onClearQueue()
    }

    suspend fun next(): Content? {
        if (queue.isEmpty()) {
            return null
        }
        val content = queue[0]
        queue.removeAt(0)
        listener.onRemoveContent(content)
        return content
    }

    fun removeContent(uid: String) {
        this.queue.removeIf { it.uid == uid }
    }
}