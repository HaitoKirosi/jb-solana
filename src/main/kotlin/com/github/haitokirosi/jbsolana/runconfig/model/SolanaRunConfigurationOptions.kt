package com.github.haitokirosi.jbsolana.runconfig.model

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class SolanaRunConfigurationOptions : RunConfigurationOptions() {

    // Stored Properties (Persistent State)
    private val executableTypeProperty: StoredProperty<String?> =
        string(ExecutableType.CARGO.name).provideDelegate(this, "executableType")

    private val customExecutablePathProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "customExecutablePath")

    private val programArgumentsProperty: StoredProperty<String?> =
        string("run").provideDelegate(this, "programArguments")

    private val cargoArgsProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "cargoArgs")

    private val cargoCommandProperty: StoredProperty<String?> =
        string("run").provideDelegate(this, "cargoCommand")

    private val rustcArgsProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "rustcArgs")

    private val customArgsProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "customArgs")

    private val sourceFileProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "sourceFile")

    private val workingDirectoryProperty: StoredProperty<String?> =
        string("").provideDelegate(this, "workingDirectory")

    // Accessors
    var executableType: ExecutableType
        get() = ExecutableType.valueOf(executableTypeProperty.getValue(this) ?: ExecutableType.CARGO.name)
        set(value) { executableTypeProperty.setValue(this, value.name) }

    var customExecutablePath: String?
        get() = customExecutablePathProperty.getValue(this)
        set(value) { customExecutablePathProperty.setValue(this, value) }

    var programArguments: String?
        get() = programArgumentsProperty.getValue(this)
        set(value) { programArgumentsProperty.setValue(this, value) }

    var cargoArgs: String?
        get() = cargoArgsProperty.getValue(this)
        set(value) { cargoArgsProperty.setValue(this, value) }

    var cargoCommand: String?
        get() = cargoCommandProperty.getValue(this)
        set(value) { cargoCommandProperty.setValue(this, value) }

    var rustcArgs: String?
        get() = rustcArgsProperty.getValue(this)
        set(value) { rustcArgsProperty.setValue(this, value) }

    var customArgs: String?
        get() = customArgsProperty.getValue(this)
        set(value) { customArgsProperty.setValue(this, value) }

    var workingDirectory: String?
        get() = workingDirectoryProperty.getValue(this)
        set(value) { workingDirectoryProperty.setValue(this, value) }

    var sourceFile: String?
        get() = sourceFileProperty.getValue(this)
        set(value) { sourceFileProperty.setValue(this, value) }
}

enum class ExecutableType {
    RUSTC,
    CARGO,
    CUSTOM
}