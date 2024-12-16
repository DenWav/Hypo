import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class DownloadJavadocListFiles : DefaultTask() {

    @get:Input
    abstract val dependencies: ListProperty<MinimalExternalModuleDependency>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun run() {
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

        val outDir = output.get().asFile.toPath()
        val base = "https://static.javadoc.io"
        dependencies.get().forEach { m ->
            val types = listOf("element", "package")
            var response: HttpResponse<*>? = null
            for (type in types) {
                val filePath = "${m.module.group}/${m.module.name}/${m.versionConstraint}/$type-list"
                val url = "$base/$filePath"
                val outFile = outDir.resolve(filePath)
                outFile.parent.createDirectories()

                val request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build()
                response = client.send(request, HttpResponse.BodyHandlers.ofFile(outFile))
                if (response.statusCode() == 200) {
                    break
                }

                outFile.deleteIfExists()
            }
            if (response == null || response.statusCode() != 200) {
                throw Exception("Failed: $response")
            }
        }
    }
}
