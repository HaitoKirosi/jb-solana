package com.github.haitokirosi.jbsolana.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.VisibleForTesting
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class SolanaCliService(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(SolanaCliService::class.java)
        private const val TIMEOUT_MS = 3000

        fun getInstance(project: Project): SolanaCliService = project.getService(SolanaCliService::class.java)
    }

    @VisibleForTesting
    internal var processRunner: (GeneralCommandLine) -> ProcessOutput = { cmd ->
        CapturingProcessHandler(cmd).runProcess(TIMEOUT_MS)
    }

    fun getActiveCluster(): String = try {
        val commandLine = GeneralCommandLine("solana", "config", "get")
            .withCharset(StandardCharsets.UTF_8)

        val output = processRunner(commandLine)

        if (output.exitCode == 0) parseClusterUrl(output.stdout) else "Solana CLI not found"
    } catch (e: Exception) {
        LOG.warn("Failed to retrieve Solana config", e)
        "Error"
    }

    private fun parseClusterUrl(stdout: String): String {
        val url = stdout.lines()
            .find { it.trim().startsWith("RPC URL:") }
            ?.substringAfter(":")
            ?.trim()
            ?: return "Unknown"

        return when {
            url.contains("devnet") -> "Devnet"
            url.contains("testnet") -> "Testnet"
            url.contains("mainnet") -> "Mainnet"
            url.contains("localhost") || url.contains("127.0.0.1") -> "Localhost"
            else -> "Custom"
        }
    }
}
