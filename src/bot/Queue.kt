package fr.spoutnik87.bot

class Queue(
    val server: Server
) {
    private val queue = ArrayList<Content>()

    var currentlyPlaying: Content? = null
        private set

    fun addContent(content: Content) {
        this.queue.add(content)
    }

    fun getAllContents(): List<Content> = this.queue

    fun getContent(id: String): Content? {
        return this.queue.findLast { it.id == id }
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
        return content
    }

    fun clear() {
        this.currentlyPlaying = null
        this.queue.clear()
    }
}