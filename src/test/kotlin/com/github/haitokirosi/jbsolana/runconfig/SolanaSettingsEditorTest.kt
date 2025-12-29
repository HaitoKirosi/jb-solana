package com.github.haitokirosi.jbsolana.runconfig

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationFactory
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationType
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.github.haitokirosi.jbsolana.runconfig.ui.SolanaSettingsEditor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SolanaSettingsEditorTest : BasePlatformTestCase() {

    private lateinit var factory: SolanaConfigurationFactory
    private lateinit var type: SolanaConfigurationType
    private lateinit var editor: SolanaSettingsEditor

    override fun setUp() {
        super.setUp()
        type = SolanaConfigurationType()
        factory = SolanaConfigurationFactory(type)
        editor = SolanaSettingsEditor()
        editor.component
    }

    fun `test reset from configuration`() {
        val config = SolanaRunConfiguration(project, factory, "TestConfig")
        config.executableType = ExecutableType.CARGO
        config.cargoCommand = "test"
        config.cargoArgs = "--lib"
        config.workingDirectory = "/tmp"

        editor.resetFrom(config)

        val newConfig = SolanaRunConfiguration(project, factory, "NewConfig")
        editor.applyTo(newConfig)

        assertEquals(ExecutableType.CARGO, newConfig.executableType)
        assertEquals("test", newConfig.cargoCommand)
        assertEquals("--lib", newConfig.cargoArgs)
        assertEquals("/tmp", newConfig.workingDirectory)
    }

    fun `test apply to configuration`() {
        val config = SolanaRunConfiguration(project, factory, "TestConfig")

        config.executableType = ExecutableType.CUSTOM
        config.customExecutablePath = "/bin/custom"
        config.programArguments = "-v"
        config.customArgs = "-v"

        editor.resetFrom(config)

        val targetConfig = SolanaRunConfiguration(project, factory, "Target")
        editor.applyTo(targetConfig)

        assertEquals(ExecutableType.CUSTOM, targetConfig.executableType)
        assertEquals("/bin/custom", targetConfig.customExecutablePath)
        assertEquals("-v", targetConfig.programArguments)
    }
}
