package de.theskyscout.findex.screens

import android.app.Dialog
import de.theskyscout.zap.activities.CodexActivity

interface PopUpScreen {

    val activity: CodexActivity
    val dialog: Dialog
    val layout: Int

    private val LOG_TAG: String
        get() = "POPUP_SCREEN"

    private fun getLogTag(action: String): String {
        return "$LOG_TAG:$action"
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun hide() {
        dialog.hide()
    }

}