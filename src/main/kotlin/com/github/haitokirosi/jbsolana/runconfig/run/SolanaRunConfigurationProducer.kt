package com.github.haitokirosi.jbsolana.runconfig.run

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaConfigurationType
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

class SolanaRunConfigurationProducer : LazyRunConfigurationProducer<SolanaRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return ConfigurationTypeUtil.findConfigurationType(SolanaConfigurationType.ID)!!
            .configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: SolanaRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.psiLocation?.containingFile?.virtualFile ?: return false

        if (configuration.executableType == ExecutableType.RUSTC) {
            return isRustCConfiguration(configuration, file)
        }

        if (configuration.executableType == ExecutableType.CARGO) {
            return isCargoConfiguration(configuration, file)
        }

        return false
    }

    override fun setupConfigurationFromContext(
        configuration: SolanaRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.psiLocation?.containingFile?.virtualFile ?: return false
        val cargoTomlDir = findCargoTomlDir(file)

        if (file.extension == "rs" && cargoTomlDir == null) {
            setupRustCConfiguration(configuration, file, context.project.basePath)
            return true
        }

        if (file.name == "Cargo.toml" || cargoTomlDir != null) {
            val workingDir = cargoTomlDir?.path ?: context.project.basePath
            setupCargoConfiguration(configuration, cargoTomlDir, workingDir)
            return true
        }

        return false
    }

    private fun isRustCConfiguration(configuration: SolanaRunConfiguration, file: VirtualFile): Boolean {
        return file.extension == "rs" && configuration.sourceFile == file.path
    }

    private fun isCargoConfiguration(configuration: SolanaRunConfiguration, file: VirtualFile): Boolean {
        val cargoTomlDir = findCargoTomlDir(file)
        val configDir = configuration.workingDirectory

        if (cargoTomlDir != null && configDir == cargoTomlDir.path) {
            return configuration.programArguments == "run"
        }
        return file.name == "Cargo.toml" && configuration.programArguments == "run"
    }

    private fun setupRustCConfiguration(
        configuration: SolanaRunConfiguration,
        file: VirtualFile,
        projectBasePath: String?
    ) {
        configuration.name = "Run ${file.name}"
        configuration.executableType = ExecutableType.RUSTC
        configuration.sourceFile = file.path
        configuration.customExecutablePath = null
        configuration.workingDirectory = projectBasePath
    }

    private fun setupCargoConfiguration(
        configuration: SolanaRunConfiguration,
        cargoTomlDir: VirtualFile?,
        workingDir: String?
    ) {
        val packageName = cargoTomlDir?.let { getPackageName(it) }
        val configName = if (packageName != null) "Run $packageName" else "Run Cargo"

        configuration.name = configName
        configuration.executableType = ExecutableType.CARGO
        configuration.programArguments = "run"
        configuration.customExecutablePath = null
        configuration.workingDirectory = workingDir
    }

    private fun findCargoTomlDir(file: VirtualFile): VirtualFile? {
        var current: VirtualFile? = if (file.isDirectory) file else file.parent
        while (current != null) {
            if (current.findChild("Cargo.toml") != null) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun getPackageName(dir: VirtualFile): String? {
        val cargoToml = dir.findChild("Cargo.toml") ?: return null
        return try {
            val content = String(cargoToml.contentsToByteArray())
            content.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("name") }
                ?.split("=")
                ?.getOrNull(1)
                ?.trim()?.trim('"')?.trim('\'')
        } catch (e: Exception) {
            null
        }
    }
}