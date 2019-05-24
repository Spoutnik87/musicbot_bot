package fr.spoutnik87.bot

class Queue(
    private val listener: QueueListener
) {

    private val queue = ArrayList<Content>()
    private val queueLock = Object()

    /**
     * Add a content to the queue.
     * Fire onAddContent event.
     */
    suspend fun addContent(content: Content) {
        synchronized(queueLock) {
            this.queue.add(content)
        }
        listener.onAddContent(content)
    }

    /**
     * Get all contents from the queue.
     */
    fun getAllContents(): List<Content> = synchronized(queueLock) { this.queue }

    /**
     * Get content at @param index
     * @return Content if exist or null.
     */
    fun getContent(id: String): Content? {
        synchronized(queueLock) {
            return this.queue.find { it.id == id }
        }
    }

    /**
     * Get content at @param index
     * @return Content if exist or null.
     */
    fun getContent(index: Int): Content? {
        synchronized(queueLock) {
            if (index < this.queue.size && index < 0) {
                return null
            }
            return this.queue[index]
        }
    }

    /**
     * Clear the queue.
     * Fire onClear event.
     */
    suspend fun clear() {
        synchronized(queueLock) {
            this.queue.clear()
        }
        listener.onClear()
    }

    /**
     * Get the next content in the queue and remove it from the queue.
     * @return Next content if exist or null.
     */
    fun next(): Content? {
        var content: Content? = null
        synchronized(queueLock) {
            if (queue.isEmpty()) {
                return null
            }
            if (this.queue.size > 0) {
                content = this.queue[0]
                queue.removeAt(0)
            }
        }
        return content
    }

    /**
     * Remove a content identified by an index.
     * Fire onRemoveContent event.
     * @return Removed content or null
     */
    suspend fun removeContent(index: Int): Content? {
        var content: Content? = null
        synchronized(queueLock) {
            if (index < this.queue.size && index < 0) {
                content = this.queue[0]
                queue.removeAt(index)
            }
        }
        listener.onRemoveContent(content ?: return null)
        return content
    }

    /**
     * Remove a content identified by an unique id.
     * Fire onRemoveContent event.
     * @return Removed content or null
     */
    suspend fun removeContent(uid: String): Content? {
        var content: Content?
        synchronized(queueLock) {
            content = this.queue.find { it.uid == uid }
            this.queue.removeIf { it.uid == uid }
        }
        listener.onRemoveContent(content ?: return null)
        return content
    }
}