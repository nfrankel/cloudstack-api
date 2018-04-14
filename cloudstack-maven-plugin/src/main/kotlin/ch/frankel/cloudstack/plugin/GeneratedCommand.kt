package ch.frankel.cloudstack.plugin

import org.jsoup.nodes.Element

class GeneratedCommand(private val element: Element, val asynchronous: Boolean) {

    val name: String by lazy {
        element.select("h1").text()
    }

    val parameters: List<GeneratedParameter> by lazy {
        element.select("table.apitable")[0]
                .select("tr:not(.hed)")
                .map { GeneratedParameter(it) }
    }

    override fun toString(): String {
        return "command(name=$name,asynchronous=$asynchronous)"
    }
}