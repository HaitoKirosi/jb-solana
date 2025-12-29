package com.github.haitokirosi.jbsolana.runconfig.model

import com.github.haitokirosi.jbsolana.runconfig.task.SolanaBuildTask
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class SolanaConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "SolanaConfigurationFactory"

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return SolanaRunConfiguration(project, this, "Run")
    }

    override fun getOptionsClass(): Class<out com.intellij.execution.configurations.RunConfigurationOptions>? {
        return SolanaRunConfigurationOptions::class.java
    }

    override fun configureBeforeRunTaskDefaults(
        providerID: Key<out com.intellij.execution.BeforeRunTask<*>>,
        task: com.intellij.execution.BeforeRunTask<*>
    ) {
        if (providerID == SolanaBuildTask.KEY) {
            task.isEnabled = true
        }
    }
}
