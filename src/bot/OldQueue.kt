package fr.spoutnik87.bot

class OldQueue(
    val server: OldServer
) {
    private val queue = ArrayList<Content>()

    var currentlyPlaying: Content? = null
        private set

    fun addContent(content: Content) {
        this.queue.add(content)
    }

    fun getAllContents(): List<Content> = this.queue

    fun getContent(id: String): Content? {
        return this.queue.find { it.id == id }
    }

    fun getContent(index: Int): Content? {
        return if (index >= 0 && index < this.queue.size) {
            this.queue[index]
        } else {
            null
        }
    }

    fun contains(id: String): Boolean = queue.any { it.id == id }

    fun remove(id: String): Boolean = queue.removeIf { it.id == id }

    fun next(): Content? {
        if (queue.isEmpty()) {
            return null
        }
        val content = queue[0]
        queue.removeAt(0)
        this.currentlyPlaying = content
        content.startTime = System.currentTimeMillis()
        return content
    }

    fun stop() {
        this.currentlyPlaying = null
    }

    fun clear() {
        this.currentlyPlaying = null
        this.queue.clear()
    }
}