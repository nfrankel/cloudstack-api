package com.exoscale.api.generate

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File


@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES)
class CloudStackGenerateMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project.build.directory}", readonly = true)
    private val buildDirectory: File? = null

    override fun execute() {
        GenerateApi().generate(buildDirectory!!)
    }
}