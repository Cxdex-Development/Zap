package de.theskyscout.zap.screens.popups

import android.widget.Button
import android.widget.EditText
import de.theskyscout.findex.screens.PopUp
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.signIn.GoogleAuthClient
import kotlinx.coroutines.runBlocking

class RegisterPopup(activity: CodexActivity) : PopUp(
    activity,
    R.layout.popup_register,
    cancelable = false
) {
    init {
        val btnRegister = dialog.findViewById<Button>(R.id.btnRegister)
        val inputUsername = dialog.findViewById<EditText>(R.id.etRegisterUsername)
        val inputBio = dialog.findViewById<EditText>(R.id.etRegisterBio)

        btnRegister.setOnClickListener {
            val username = inputUsername.text.toString()
            var bio = inputBio.text.toString()
            if (username.isNotEmpty() && bio.isNotEmpty()) {
                val user = User().apply {
                    name = username
                    owner_id = GoogleAuthClient.user?.uid ?: ""
                    email = GoogleAuthClient.user?.email ?: ""
                    this.bio = bio
                }
                runBlocking {
                    activity.dbMain.addUser(user)
                }
                activity.swapToActivity(MainActivity::class.java)
                dialog.dismiss()
            }
        }

    }
}