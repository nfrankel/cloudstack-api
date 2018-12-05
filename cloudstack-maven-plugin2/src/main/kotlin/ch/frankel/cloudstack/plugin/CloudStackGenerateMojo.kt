package ch.frankel.cloudstack.plugin

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File


@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES)
class CloudStackGenerateMojo : AbstractMojo() {

    @Parameter(property = "generate.version")
    private var version: String? = null

    @Parameter(property = "generate.url", defaultValue = "https://doc.internal.exoscale.ch/others/cs/TOC_User.html")
    private var url: String? = null

    @Parameter(defaultValue = "\${project.build.directory}", readonly = true)
    private val buildDirectory: File? = null

    override fun execute() {
        if (url != null) {
            GenerateAPI(url!!).generate(buildDirectory!!)
        } else {
            GenerateAPI("http://cloudstack.apache.org/api/apidocs-$version/index.html").generate(buildDirectory!!)
        }
    }
}