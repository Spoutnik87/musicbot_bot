package fr.spoutnik87.bot

interface ContentPlayerListener {
    fun onContentStart(content: Content)
    fun onContentStartFailure(content: Content)
    fun onContentStop(content: Content)
    fun onContentEnd(content: Content)
    fun onContentPause(content: Content, position: Long)
    fun onContentResume(content: Content, position: Long)
    fun onContentPositionChange(content: Content, oldPosition: Long, newPosition: Long)
}