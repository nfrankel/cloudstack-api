package ch.frankel.cloudstack.plugin

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.nio.file.Paths

class GenerateAPI(private val url: String) {

    val categories: List<GeneratedCategory> by lazy {
        document.select("div.apismallbullet_box")
                .map { GeneratedCategory(it) }
    }

    private val document: Document by lazy {
        Jsoup.connect(url).get()
    }
}

fun GenerateAPI.generate(buildDirectory: File) {
    val lineSeparator = System.getProperty("line.separator")
    val subPath = Paths.get("generated-sources", "kotlin", "ch", "frankel", "cloudstack", "api")
    val directory = buildDirectory.resolve(subPath.toFile())
    if (!directory.exists()) {
        directory.mkdirs()
    }
    categories.forEach {
        val path = directory.resolve("${it.name.replace(" ", "")}Commands.kt").apply {
            if (!exists()) {
                createNewFile()
            }
        }
        val file = path.apply {
            writeText("package ch.frankel.cloudstack.api")
        }
        it.commands.forEach {
            with(file) {
                appendText(lineSeparator)
                appendText(lineSeparator)
                appendText("class ${it.name.capitalize()}(")
                it.parameters.filter { it.required }
                        .forEach {
                            appendText("val ${it.name}: String, ")
                        }
                appendText("parameters: Map<String, String> = mapOf()) : Command(\"${it.name}\", parameters)")
            }
        }
    }
}