package fr.spoutnik87.util

import java.net.URL

class URLHelper {
    companion object {
        fun getQueryParameters(link: String): List<Pair<String, String>> {
            return try {
                getQueryParameters(URL(link))
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun getQueryParameters(link: URL): List<Pair<String, String>> {
            return link.query.split("&").mapNotNull {
                val index = it.indexOf("=")
                if (index == -1) {
                    null
                } else {
                    Pair(it.substring(0, index), it.substring(index + 1))
                }
            }
        }

        fun createSafeYoutubeLink(link: String) =
            getQueryParameters(link).firstOrNull { it.first == "v" }?.second?.let { "https://www.youtube.com/watch?v=$it" }
    }
}