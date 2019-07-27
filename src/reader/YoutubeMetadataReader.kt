package reader

data class YoutubeMetadataReader(
    val url: String,
    val publishedAt: Long,
    val channel: String,
    val title: String,
    val description: String,
    val duration: Long
)