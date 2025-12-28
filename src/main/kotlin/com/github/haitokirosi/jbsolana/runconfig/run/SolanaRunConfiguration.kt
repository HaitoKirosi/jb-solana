package com.github.haitokirosi.jbsolana.runconfig.run

import com.github.haitokirosi.jbsolana.runconfig.build.SolanaBuildTask
import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.model.SolanaRunConfigurationOptions
import com.github.haitokirosi.jbsolana.runconfig.ui.SolanaSettingsEditor
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.util.ArrayList

class SolanaRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<SolanaRunConfigurationOptions>(project, factory, name), LocatableConfiguration {

    var executableType: ExecutableType
        get() = options.executableType
        set(value) { options.executableType = value }

    var customExecutablePath: String?
        get() = options.customExecutablePath
        set(value) { options.customExecutablePath = value }

    var programArguments: String?
        get() = options.programArguments
        set(value) { options.programArguments = value }

    var cargoArgs: String?
        get() = options.cargoArgs
        set(value) { options.cargoArgs = value }

    var cargoCommand: String?
        get() = options.cargoCommand
        set(value) { options.cargoCommand = value }

    var rustcArgs: String?
        get() = options.rustcArgs
        set(value) { options.rustcArgs = value }

    var customArgs: String?
        get() = options.customArgs
        set(value) { options.customArgs = value }

    var workingDirectory: String?
        get() = options.workingDirectory
        set(value) { options.workingDirectory = value }

    var sourceFile: String?
        get() = options.sourceFile
        set(value) { options.sourceFile = value }

    override fun getOptions(): SolanaRunConfigurationOptions {
        return super.getOptions() as SolanaRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return SolanaSettingsEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return SolanaRunProfileState(environment, this)
    }

    override fun suggestedName(): String? {
        return if (name == "Unnamed" || name.isBlank()) "Run" else null
    }

    override fun isGeneratedName(): Boolean {
        return name == "Run" || name == "Unnamed"
    }

    override fun setBeforeRunTasks(value: List<BeforeRunTask<*>>) {
        if (executableType == ExecutableType.RUSTC) {
            val tasks = value.toMutableList()
            if (ensureBuildTaskExists(tasks)) {
                super<RunConfigurationBase>.setBeforeRunTasks(tasks)
                return
            }
        }
        super<RunConfigurationBase>.setBeforeRunTasks(value)
    }

    /**
     * Adds the SolanaBuildTask to the list if it's missing.
     * @return true if the list was modified
     */
    private fun ensureBuildTaskExists(tasks: MutableList<BeforeRunTask<*>>): Boolean {
        if (tasks.any { it.providerId == SolanaBuildTask.KEY }) return false

        val taskProvider = BeforeRunTaskProvider.getProvider(project, SolanaBuildTask.KEY) ?: return false
        val task = taskProvider.createTask(this) ?: return false

        task.isEnabled = true
        tasks.add(task)
        return true
    }
}