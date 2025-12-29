package com.github.haitokirosi.jbsolana.runconfig

import com.github.haitokirosi.jbsolana.runconfig.task.SolanaBuildTask
import com.github.haitokirosi.jbsolana.runconfig.task.SolanaBuildTaskProvider
import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationFactory
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationType
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class SolanaBuildTaskTest : BasePlatformTestCase() {

    private lateinit var factory: SolanaConfigurationFactory
    private lateinit var type: SolanaConfigurationType
    private lateinit var provider: SolanaBuildTaskProvider

    override fun setUp() {
        super.setUp()
        type = SolanaConfigurationType()
        factory = SolanaConfigurationFactory(type)
        provider = SolanaBuildTaskProvider()
    }

    fun `test create task returns task only for solana config`() {
        val solanaConfig = SolanaRunConfiguration(project, factory, "Solana")
        solanaConfig.executableType = ExecutableType.RUSTC

        val task = provider.createTask(solanaConfig)
        assertNotNull(task)
        assertTrue(task is SolanaBuildTask)
        assertTrue(task!!.isEnabled)
    }

    fun `test compile command generation`() {
        val config = SolanaRunConfiguration(project, factory, "BuildTest")
        config.executableType = ExecutableType.RUSTC
        config.sourceFile = "/tmp/main.rs"
        config.workingDirectory = "/tmp/build"

        val commandLine = provider.createCompileCommand(config, "/tmp/main.rs")

        assertEquals("rustc", commandLine.exePath)
        assertEquals("/tmp/build", commandLine.workDirectory.path)

        val args = commandLine.parametersList.list
        assertEquals(3, args.size)
        assertEquals("/tmp/main.rs", args[0])
        assertEquals("-o", args[1])

        val expectedBinaryName = if (SystemInfo.isWindows) "main.exe" else "main"
        val expectedBinaryPath = File("/tmp/build", expectedBinaryName).absolutePath
        assertEquals(expectedBinaryPath, args[2])
    }

    fun `test compile command uses project dir if working dir empty`() {
        val config = SolanaRunConfiguration(project, factory, "BuildTestNoDir")
        config.executableType = ExecutableType.RUSTC
        config.sourceFile = "/src/main.rs"
        config.workingDirectory = ""

        val commandLine = provider.createCompileCommand(config, "/src/main.rs")

        assertEquals(project.basePath, commandLine.workDirectory.path)
    }

    fun `test compile command handles paths with spaces`() {
        val config = SolanaRunConfiguration(project, factory, "Space Test")
        config.executableType = ExecutableType.RUSTC
        val pathWithSpaces = "/home/user/My Projects/solana app"
        config.sourceFile = "$pathWithSpaces/main.rs"
        config.workingDirectory = pathWithSpaces

        val commandLine = provider.createCompileCommand(config, config.sourceFile!!)

        assertEquals(pathWithSpaces, commandLine.workDirectory.path)
        val args = commandLine.parametersList.list
        assertEquals("$pathWithSpaces/main.rs", args[0])
    }
}
