package com.github.haitokirosi.jbsolana.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SolanaCliServiceTest : BasePlatformTestCase() {

    private lateinit var service: SolanaCliService
    private lateinit var originalRunner: (GeneralCommandLine) -> ProcessOutput

    override fun setUp() {
        super.setUp()
        service = SolanaCliService.getInstance(project)
        originalRunner = service.processRunner
    }

    override fun tearDown() {
        service.processRunner = originalRunner
        super.tearDown()
    }

    fun `test parsing devnet url`() {
        mockSolanaOutput("RPC URL: https://api.devnet.solana.com")
        assertEquals("Devnet", service.getActiveCluster())
    }

    fun `test parsing mainnet url`() {
        mockSolanaOutput("RPC URL: https://api.mainnet-beta.solana.com")
        assertEquals("Mainnet", service.getActiveCluster())
    }

    fun `test parsing testnet url`() {
        mockSolanaOutput("RPC URL: https://api.testnet.solana.com")
        assertEquals("Testnet", service.getActiveCluster())
    }

    fun `test parsing localhost url`() {
        mockSolanaOutput("RPC URL: http://localhost:8899")
        assertEquals("Localhost", service.getActiveCluster())
    }

    fun `test parsing custom url`() {
        val customUrl = "https://custom.rpc.com"
        mockSolanaOutput("RPC URL: $customUrl")
        assertEquals("Custom", service.getActiveCluster())
    }

    fun `test parsing unknown output`() {
        mockSolanaOutput("Some random output")
        assertEquals("Unknown", service.getActiveCluster())
    }

    fun `test handling cli error`() {
        mockSolanaOutput("", exitCode = 1)
        assertEquals("Solana CLI not found", service.getActiveCluster())
    }

    fun `test handling exception`() {
        service.processRunner = { throw RuntimeException("Execution failed") }
        assertEquals("Error", service.getActiveCluster())
    }

    private fun mockSolanaOutput(stdout: String, exitCode: Int = 0) {
        val output = ProcessOutput(stdout, "", exitCode, false, false)
        service.processRunner = { output }
    }
}
