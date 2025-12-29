package com.github.haitokirosi.jbsolana.runconfig.task

import com.intellij.execution.BeforeRunTask
import com.intellij.openapi.util.Key

class SolanaBuildTask : BeforeRunTask<SolanaBuildTask>(KEY) {
    companion object {
        val KEY = Key.create<SolanaBuildTask>("SolanaBuildTask")
    }
}
