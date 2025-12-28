package com.github.haitokirosi.jbsolana.runconfig.run

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.SystemInfo
import java.io.File

class SolanaRunProfileState(
    environment: ExecutionEnvironment,
    private val config: SolanaRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val workingDir = resolveWorkingDirectory()
        val commandLine = GeneralCommandLine().apply {
            exePath = resolveExecutablePath(workingDir)
            if (workingDir != null) {
                setWorkDirectory(workingDir)
            }
            addParameters(resolveProgramArguments())
        }

        return KillableColoredProcessHandler(commandLine)
    }

    private fun resolveWorkingDirectory(): String? {
        return config.workingDirectory?.takeIf { it.isNotBlank() }
            ?: environment.project.basePath
    }

    private fun resolveExecutablePath(workingDir: String?): String {
        return when (config.executableType) {
            ExecutableType.CARGO -> "cargo"
            ExecutableType.RUSTC -> resolveRustCBinaryPath(workingDir)
            ExecutableType.CUSTOM -> resolveCustomExecutablePath()
        }
    }

    private fun resolveRustCBinaryPath(workingDir: String?): String {
        val sourcePath = config.sourceFile
        if (sourcePath.isNullOrBlank()) {
            throw ExecutionException("No source file specified for RustC configuration")
        }

        val sourceFile = File(sourcePath)
        val binaryName = sourceFile.nameWithoutExtension
        val binaryFileName = if (SystemInfo.isWindows) "$binaryName.exe" else binaryName

        val binaryFile = if (workingDir != null) {
            File(workingDir, binaryFileName)
        } else {
            File(if (SystemInfo.isWindows) "$binaryName.exe" else "./$binaryName")
        }

        if (!binaryFile.exists()) {
            throw ExecutionException(
                "Binary not found at ${binaryFile.absolutePath}. " +
                        "Ensure 'Build' task is present in 'Before Launch' list."
            )
        }

        return binaryFile.absolutePath
    }

    private fun resolveCustomExecutablePath(): String {
        val path = config.customExecutablePath
        if (path.isNullOrBlank()) {
            throw ExecutionException("Custom executable path is not specified")
        }
        return path
    }

    private fun resolveProgramArguments(): List<String> {
        return when (config.executableType) {
            ExecutableType.CARGO -> {
                val command = config.cargoCommand ?: "run"
                val args = config.cargoArgs
                val params = mutableListOf<String>()

                params.addAll(command.split("\\s+".toRegex()).filter { it.isNotBlank() })

                if (!args.isNullOrBlank()) {
                    params.add("--")
                    params.addAll(args.split("\\s+".toRegex()).filter { it.isNotBlank() })
                }
                params
            }
            ExecutableType.RUSTC -> {
                config.rustcArgs?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
            }
            ExecutableType.CUSTOM -> {
                config.programArguments?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
            }
        }
    }
}