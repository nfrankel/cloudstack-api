package ch.frankel.cloudstack.plugin

import org.jsoup.nodes.Element

class GeneratedParameter(element: Element) {

    val name: String by lazy {
        element.select("td")[0].text()
    }

    val description: String by lazy {
        element.select("td")[1].text()
    }

    val required: Boolean by lazy {
        element.select("td")[2].text().toBoolean()
    }

    override fun toString(): String {
        return "parameter(name=$name,required=$required)"
    }
}