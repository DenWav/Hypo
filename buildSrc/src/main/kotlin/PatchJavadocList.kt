import javax.inject.Inject
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class PatchJavadocList : DefaultTask() {

    @get:InputDirectory
    abstract val input: DirectoryProperty

    @get:Input
    abstract val patches: ListProperty<HypoPatchSpec>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun run() {
        fs.sync {
            from(input)
            into(output)
        }

        val outputDir = output.get().asFile.toPath()
        patches.get().forEach { patch ->
            val lib = patch.library.get()

            val packageListFile = outputDir.resolve("${lib.module.group}/${lib.module.name}/${lib.versionConstraint}/package-list")
            val elementListFile = packageListFile.resolveSibling("element-list")

            if (elementListFile.isRegularFile()) {
                val originalText = elementListFile.readText()
                elementListFile.bufferedWriter().use { writer ->
                    writer.appendLine("module:${patch.name}")
                    writer.append(originalText)
                }
            } else {
                elementListFile.bufferedWriter().use { writer ->
                    writer.appendLine("module:${patch.name}")

                    packageListFile.bufferedReader().use { reader ->
                        reader.copyTo(writer)
                    }
                }
            }

            packageListFile.deleteIfExists()
        }
    }
}
