package com.github.haitokirosi.jbsolana.runconfig.task

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Paths
import javax.swing.Icon

class SolanaBuildTaskProvider : BeforeRunTaskProvider<SolanaBuildTask>() {

    companion object {
        private val LOG = Logger.getInstance(SolanaBuildTaskProvider::class.java)
        private const val RUSTC_EXECUTABLE = "rustc"
    }

    override fun getId(): Key<SolanaBuildTask> = SolanaBuildTask.KEY

    override fun getName(): String = "Build Rust File"

    override fun getIcon(): Icon = AllIcons.Actions.Compile

    override fun createTask(runConfiguration: RunConfiguration): SolanaBuildTask? {
        if (runConfiguration !is SolanaRunConfiguration) return null

        return SolanaBuildTask().apply {
            isEnabled = runConfiguration.executableType == ExecutableType.RUSTC
        }
    }

    override fun executeTask(
        context: DataContext,
        configuration: RunConfiguration,
        env: ExecutionEnvironment,
        task: SolanaBuildTask
    ): Boolean {
        if (configuration !is SolanaRunConfiguration) return true
        if (configuration.executableType != ExecutableType.RUSTC) return true

        val sourceFilePath = configuration.sourceFile
        if (sourceFilePath.isNullOrBlank()) {
            LOG.warn("Skipping build: source file is not specified")
            return false
        }

        try {
            val commandLine = createCompileCommand(configuration, sourceFilePath)
            LOG.info("Executing build command: ${commandLine.commandLineString}")

            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess()

            if (output.exitCode != 0) {
                logBuildFailure(output)
                return false
            }

            refreshFileSystem(commandLine.workDirectory, sourceFilePath)
            return true

        } catch (e: Exception) {
            LOG.error("Failed to execute RustC build task", e)
            return false
        }
    }

    internal fun createCompileCommand(
        configuration: SolanaRunConfiguration,
        sourceFilePath: String
    ): GeneralCommandLine {
        val workingDir = resolveWorkingDirectory(configuration)
        val binaryPath = resolveBinaryPath(workingDir, sourceFilePath)

        return GeneralCommandLine().apply {
            exePath = RUSTC_EXECUTABLE
            setWorkDirectory(workingDir)
            addParameter(sourceFilePath)
            addParameter("-o")
            addParameter(binaryPath)
        }
    }

    private fun resolveWorkingDirectory(configuration: SolanaRunConfiguration): File {
        return configuration.workingDirectory?.takeIf { it.isNotBlank() }?.let { File(it) }
            ?: File(configuration.project.basePath ?: System.getProperty("user.dir"))
    }

    private fun resolveBinaryPath(workingDir: File, sourceFilePath: String): String {
        val sourceFileName = File(sourceFilePath).nameWithoutExtension
        val binaryName = if (SystemInfo.isWindows) "$sourceFileName.exe" else sourceFileName
        return File(workingDir, binaryName).absolutePath
    }

    private fun logBuildFailure(output: ProcessOutput) {
        val msg = StringBuilder("RustC compilation failed with exit code ${output.exitCode}")
        if (output.stderr.isNotBlank()) {
            msg.append("\nStderr: ").append(output.stderr)
        }
        LOG.warn(msg.toString())
    }

    private fun refreshFileSystem(workingDirectory: File?, sourceFilePath: String) {
        val targetToRefresh = workingDirectory?.toPath() ?: Paths.get(sourceFilePath).parent

        ApplicationManager.getApplication().invokeLater({
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(targetToRefresh)
        }, ModalityState.defaultModalityState())
    }
}