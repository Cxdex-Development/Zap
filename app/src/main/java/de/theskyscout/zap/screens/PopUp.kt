package de.theskyscout.findex.screens

import android.app.Dialog
import android.view.ViewGroup
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.CodexActivity

open class PopUp(
    final override val activity: CodexActivity,
    override val layout: Int,
    backgroundID: Int? = null,
    cancelable: Boolean? = null,
    init: PopUp.() -> Unit = {}
) : PopUpScreen {

    override val dialog: Dialog = Dialog(activity)

    init {
        dialog.setContentView(layout)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawableResource(backgroundID ?: R.drawable.dialog_background_transparent)
        dialog.setCancelable(cancelable?:true)
        init()
    }

}