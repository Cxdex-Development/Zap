package de.theskyscout.template.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.gms.auth.api.identity.Identity
import de.theskyscout.findex.database.MongoDB
import de.theskyscout.template.signIn.GoogleAuthClient

class CodexActivity : AppCompatActivity() {

    lateinit var binding: ViewBinding
    lateinit var activity: CodexActivity
    lateinit var googleAuthClient: GoogleAuthClient
    val database = MongoDB


    fun swapToActivity(activity: Class<CodexActivity>) {
        this.activity.finish()
        this.activity.startActivity(Intent(this.activity, activity))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        googleAuthClient = GoogleAuthClient(Identity.getSignInClient(this), this)
        super.onCreate(savedInstanceState)
    }
}