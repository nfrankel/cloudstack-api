package ch.frankel.cloudstack.plugin

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class GeneratedCategory(private val element: Element) {

    val name: String by lazy {
        element.select("h5").text()
    }

    val commands: List<GeneratedCommand> by lazy {
        element.select("a")
                .map { it.attr("abs:href") to it.text().endsWith("(A)") }
                .map { Jsoup.connect(it.first).get() to it.second }
                .map { GeneratedCommand(it.first, it.second) }
    }
}