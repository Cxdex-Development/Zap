package de.theskyscout.zap.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.gms.auth.api.identity.Identity
import de.theskyscout.findex.database.MongoDB
import de.theskyscout.zap.signIn.GoogleAuthClient

open class CodexActivity : AppCompatActivity() {

    lateinit var binding: ViewBinding
    lateinit var activity: CodexActivity
    lateinit var googleAuthClient: GoogleAuthClient
    val database = MongoDB


    fun <T : CodexActivity> swapToActivity(activity: Class<T>) {
        this.activity.finish()
        this.activity.startActivity(Intent(this.activity, activity))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        googleAuthClient = GoogleAuthClient(Identity.getSignInClient(this), this)
        super.onCreate(savedInstanceState)
    }
}