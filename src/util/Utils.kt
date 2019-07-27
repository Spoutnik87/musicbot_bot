package fr.spoutnik87.util

import org.jsoup.Jsoup
import reader.YoutubeMetadataReader
import java.text.SimpleDateFormat

class Utils {
    companion object {
        /**
         * @param duration Duration in millis
         */
        fun toDurationString(duration: Long): String {
            val value = Math.floorDiv(duration, 1000)
            val hours = Math.floorDiv(value, 3600)
            val minutes = Math.floorDiv(value % 3600, 60)
            val seconds = (value % 3600) - minutes * 60
            var hoursString = ""
            if (hours > 0) {
                hoursString = hoursString.plus(String.format("%02d", hours) + ":")
            }
            return hoursString.plus(String.format("%02d", minutes) + ":" + String.format("%02d", seconds))
        }

        /**
         * @param duration Formatted duration string
         * @return Result in milliseconds
         */
        fun fromDurationString(duration: String): Long {
            var result = 0L
            var multi = 1
            duration.split(":").reversed().forEach {
                it.toLongOrNull()?.takeIf { it in 0..59 }?.apply { result += this * multi }
                multi *= 60
            }
            return result * 1000
        }

        /**
         * @param safeYoutubeURL
         */
        fun loadMetadata(safeYoutubeURL: String): YoutubeMetadataReader? {
            return try {
                val document = Jsoup.connect(safeYoutubeURL).header("User-Agent", "Chrome").get()
                val body = document.body()
                val contents = body.getElementById("watch7-content")
                contents.getElementsByTag("meta")
                var duration: Long = 0
                try {
                    var durationText =
                        contents.getElementsByAttributeValue("itemprop", "duration").attr("content").substring(2)
                    val minutes = durationText.substringBefore("M").toInt()
                    durationText = durationText.substringAfter("M")
                    val seconds = durationText.substringBefore("S").toInt()
                    duration = (minutes * 60 + seconds) * 1000L
                } catch (e: Exception) {
                }
                var publishedAt: Long = 0
                try {
                    val format = SimpleDateFormat("YYYY-MM-dd")
                    publishedAt =
                        format.parse(contents.getElementsByAttributeValue("itemprop", "datePublished").attr("content"))
                            .time
                } catch (e: Exception) {
                }
                YoutubeMetadataReader(
                    safeYoutubeURL,
                    publishedAt,
                    body.getElementById("watch7-user-header").getElementsByClass("yt-user-info")[0].child(0).wholeText(),
                    body.getElementById("eow-title").attr("title"),
                    body.getElementById("watch-description-text").children().text(),
                    duration
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}