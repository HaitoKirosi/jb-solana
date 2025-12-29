package com.github.haitokirosi.jbsolana.runconfig

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationFactory
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationType
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunProfileState
import com.intellij.execution.ExecutionException
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File
import java.nio.file.Files

class SolanaRunConfigurationTest : BasePlatformTestCase() {

    private lateinit var factory: SolanaConfigurationFactory
    private lateinit var type: SolanaConfigurationType

    override fun setUp() {
        super.setUp()
        type = SolanaConfigurationType()
        factory = SolanaConfigurationFactory(type)
    }

    fun `test cargo command generation`() {
        val config = SolanaRunConfiguration(project, factory, "TestConfig")
        config.executableType = ExecutableType.CARGO
        config.cargoCommand = "build"
        config.cargoArgs = "--release"
        config.workingDirectory = "/tmp"

        val executor = DefaultRunExecutor.getRunExecutorInstance()
        val environment = ExecutionEnvironmentBuilder.create(executor, config).build()
        val state = SolanaRunProfileState(environment, config)

        val commandLine = state.createCommandLine()

        assertEquals("cargo", commandLine.exePath)
        assertEquals("/tmp", commandLine.workDirectory.path)
        val args = commandLine.parametersList.list

        assertTrue(args.contains("build"))
        assertTrue(args.contains("--"))
        assertTrue(args.contains("--release"))
        assertEquals("build", args[0])
        assertEquals("--", args[1])
        assertEquals("--release", args[2])
    }

    fun `test custom executable generation`() {
        val config = SolanaRunConfiguration(project, factory, "CustomConfig")
        config.executableType = ExecutableType.CUSTOM
        val customPath = if (SystemInfo.isWindows) "C:\\bin\\custom.exe" else "/bin/custom"
        config.customExecutablePath = customPath
        config.programArguments = "arg1 arg2"

        val executor = DefaultRunExecutor.getRunExecutorInstance()
        val environment = ExecutionEnvironmentBuilder.create(executor, config).build()
        val state = SolanaRunProfileState(environment, config)

        val commandLine = state.createCommandLine()

        assertEquals(customPath, commandLine.exePath)
        val args = commandLine.parametersList.list
        assertEquals(2, args.size)
        assertEquals("arg1", args[0])
        assertEquals("arg2", args[1])
    }

    fun `test rustc command generation`() {
        val tempDir = Files.createTempDirectory("solana_test").toFile()
        val sourceFile = File(tempDir, "main.rs")
        sourceFile.writeText("fn main() {}")

        val binaryName = if (SystemInfo.isWindows) "main.exe" else "main"
        val binaryFile = File(tempDir, binaryName)
        binaryFile.writeText("dummy binary")

        try {
            val config = SolanaRunConfiguration(project, factory, "RustCConfig")
            config.executableType = ExecutableType.RUSTC
            config.sourceFile = sourceFile.absolutePath
            config.workingDirectory = tempDir.absolutePath
            config.rustcArgs = "--test"

            val executor = DefaultRunExecutor.getRunExecutorInstance()
            val environment = ExecutionEnvironmentBuilder.create(executor, config).build()
            val state = SolanaRunProfileState(environment, config)

            val commandLine = state.createCommandLine()

            assertEquals(binaryFile.absolutePath, commandLine.exePath)

            val args = commandLine.parametersList.list
            assertEquals(1, args.size)
            assertEquals("--test", args[0])

        } finally {
            tempDir.deleteRecursively()
        }
    }

    fun `test missing source file throws exception`() {
        val config = SolanaRunConfiguration(project, factory, "ErrorConfig")
        config.executableType = ExecutableType.RUSTC
        config.sourceFile = ""

        val executor = DefaultRunExecutor.getRunExecutorInstance()
        val environment = ExecutionEnvironmentBuilder.create(executor, config).build()
        val state = SolanaRunProfileState(environment, config)

        try {
            state.createCommandLine()
            fail("Should throw ExecutionException")
        } catch (e: ExecutionException) {
            assertTrue(e.message!!.contains("No source file specified"))
        }
    }

    fun `test configuration serialization`() {
        val config = SolanaRunConfiguration(project, factory, "PersistConfig")
        config.executableType = ExecutableType.CARGO
        config.cargoCommand = "deploy"
        config.cargoArgs = "--lib"

        val element = org.jdom.Element("configuration")
        config.writeExternal(element)

        val loadedConfig = SolanaRunConfiguration(project, factory, "LoadedConfig")
        loadedConfig.readExternal(element)

        assertEquals(ExecutableType.CARGO, loadedConfig.executableType)
        assertEquals("deploy", loadedConfig.cargoCommand)
        assertEquals("--lib", loadedConfig.cargoArgs)
    }
}
