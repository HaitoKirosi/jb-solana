package com.github.haitokirosi.jbsolana.runconfig.ui

import com.github.haitokirosi.jbsolana.runconfig.model.ExecutableType
import com.github.haitokirosi.jbsolana.runconfig.run.SolanaRunConfiguration
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class SolanaSettingsEditor : SettingsEditor<SolanaRunConfiguration>() {
    private lateinit var panel: DialogPanel
    private var argsTextField: JBTextField? = null
    private var currentConfiguration: SolanaRunConfiguration? = null

    private var executableType = ExecutableType.CARGO
    private var customPath = ""
    private var workingDir = ""
    private var sourceFile = ""

    private var cargoCommand = "run"
    private var cargoArgs = ""

    private var rustcArgs = ""
    private var customArgs = ""

    private var activeArgs: String
        get() = argsTextField?.text ?: ""
        set(value) { argsTextField?.text = value }

    override fun createEditor(): JComponent {
        panel = panel {
            lateinit var customExecutableRb: Cell<JBRadioButton>
            lateinit var rustcRb: Cell<JBRadioButton>
            lateinit var cargoRb: Cell<JBRadioButton>

            buttonsGroup("Executable Type") {
                row {
                    cargoRb = radioButton("Cargo (in PATH)", ExecutableType.CARGO)
                        .actionListener { _, _ -> onTypeChanged(ExecutableType.CARGO) }
                }
                row {
                    rustcRb = radioButton("RustC (in PATH)", ExecutableType.RUSTC)
                        .actionListener { _, _ -> onTypeChanged(ExecutableType.RUSTC) }
                }
                row {
                    customExecutableRb = radioButton("Custom Executable", ExecutableType.CUSTOM)
                        .actionListener { _, _ -> onTypeChanged(ExecutableType.CUSTOM) }
                }
            }.bind(::executableType)

            row("Source File (.rs):") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileDescriptor("rs").withTitle("Select Rust Source File")
                ).bindText(::sourceFile)
                    .align(AlignX.FILL)
            }.visibleIf(rustcRb.selected)

            row("Custom Executable Path:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor().withTitle("Select Executable")
                ).bindText(::customPath)
                    .align(AlignX.FILL)
            }.visibleIf(customExecutableRb.selected)

            row("Working Directory:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Select Working Directory")
                ).bindText(::workingDir)
                    .align(AlignX.FILL)
            }.visibleIf(cargoRb.selected)

            row("Command:") {
                textField()
                    .bindText(::cargoCommand)
                    .align(AlignX.FILL)
                    .comment("e.g. 'run', 'test', 'build'")
            }.visibleIf(cargoRb.selected)

            row("Program Arguments:") {
                textField()
                    .applyToComponent { argsTextField = this }
                    .bindText(
                        getter = { getCurrentArgsForType(executableType) },
                        setter = { setCurrentArgsForType(executableType, it) }
                    )
                    .align(AlignX.FILL)
            }
        }
        return panel
    }

    private fun onTypeChanged(newType: ExecutableType) {
        if (newType == executableType) return

        setCurrentArgsForType(executableType, activeArgs)

        executableType = newType

        currentConfiguration?.executableType = newType

        activeArgs = getCurrentArgsForType(newType)

        fireEditorStateChanged()
    }

    private fun getCurrentArgsForType(type: ExecutableType): String {
        return when (type) {
            ExecutableType.CARGO -> cargoArgs
            ExecutableType.RUSTC -> rustcArgs
            ExecutableType.CUSTOM -> customArgs
        }
    }

    private fun setCurrentArgsForType(type: ExecutableType, value: String) {
        when (type) {
            ExecutableType.CARGO -> cargoArgs = value
            ExecutableType.RUSTC -> rustcArgs = value
            ExecutableType.CUSTOM -> customArgs = value
        }
    }

    override fun resetEditorFrom(s: SolanaRunConfiguration) {
        currentConfiguration = s
        executableType = s.executableType
        customPath = s.customExecutablePath ?: ""
        workingDir = s.workingDirectory ?: ""
        sourceFile = s.sourceFile ?: ""

        cargoCommand = s.cargoCommand ?: "run"
        cargoArgs = s.cargoArgs ?: ""
        rustcArgs = s.rustcArgs ?: ""
        customArgs = s.customArgs ?: ""

        panel.reset()
    }

    override fun applyEditorTo(s: SolanaRunConfiguration) {
        panel.apply()

        s.executableType = executableType
        s.customExecutablePath = customPath
        s.workingDirectory = workingDir
        s.sourceFile = sourceFile

        setCurrentArgsForType(executableType, activeArgs)

        s.cargoCommand = cargoCommand
        s.cargoArgs = cargoArgs
        s.rustcArgs = rustcArgs
        s.customArgs = customArgs

        if (executableType == ExecutableType.CUSTOM) {
            s.programArguments = activeArgs
        }
    }
}