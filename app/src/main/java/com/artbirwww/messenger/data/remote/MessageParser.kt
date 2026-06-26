package com.artbirwww.messenger.data.remote

object MessageParser {
    // Логика из messageParser.js для детекции markdown/ссылок/медиа в сообщениях
    fun parse(text: String): ParsedMessage {
        val urlRegex = "(https?://[^\\s]+)".toRegex()
        val match = urlRegex.find(text)
        return if (match != null) {
            val url = match.value
            when {
                url.contains(".mp4") || url.contains(".mov") -> ParsedMessage.Video(url)
                url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png") || url.contains(".gif") -> ParsedMessage.Image(url)
                else -> ParsedMessage.Link(text, url)
            }
        } else {
            ParsedMessage.Text(text)
        }
    }
}

sealed class ParsedMessage {
    data class Text(val content: String) : ParsedMessage()
    data class Image(val url: String) : ParsedMessage()
    data class Video(val url: String) : ParsedMessage()
    data class Link(val content: String, val url: String) : ParsedMessage()
}