package com.github.haitokirosi.jbsolana.runconfig.model

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.NotNullLazyValue

class SolanaConfigurationType : ConfigurationTypeBase(
    ID,
    "Solana Executable",
    "Run custom executables (Rust/Solana)",
    NotNullLazyValue.createValue { AllIcons.RunConfigurations.Application }
) {
    init {
        addFactory(SolanaConfigurationFactory(this))
    }

    companion object {
        const val ID = "SolanaConfigurationType"
    }
}
