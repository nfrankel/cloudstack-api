package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Paths

class GenerateApi {

    fun generate(buildDir: File) {
        val rootDir = prepareFilesystem(buildDir)
        val commands = getJsonCommands()
        commands.forEach {
            generateClassFile(it as JSONObject, rootDir)
        }
    }

    private fun generateClassFile(json: JSONObject, directory: File) = json
        .toSpec()
        .writeTo(directory)

    private fun getJsonCommands(): JSONArray {
        val text = javaClass.getResource("/api.json").readText(Charsets.UTF_8)
        val json = JSONObject(text)
        return json.getJSONArray("api")
    }

    private fun prepareFilesystem(buildDirectory: File): File {
        val subPath = Paths.get("generated-sources", "kotlin")
        return buildDirectory.resolve(subPath.toFile()).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
}
