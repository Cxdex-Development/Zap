package de.theskyscout.zap.activities

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.material.imageview.ShapeableImageView
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.databinding.ActivityLoginBinding
import de.theskyscout.zap.screens.popups.RegisterPopup
import de.theskyscout.zap.utils.Constants
import kotlinx.coroutines.runBlocking

class LoginActivity : CodexActivity() {

    //Screen elements
    private lateinit var btnGoogle: ShapeableImageView
    private lateinit var btnLogin: Button
    private lateinit var registerPopup: RegisterPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("WEB_CLIENT_ID", Constants.WEB_CLIENT_ID)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_login)

        registerPopup = RegisterPopup(this)

        //Initialize screen elements
        btnGoogle = findViewById(R.id.ivLoginGoogle)
        btnLogin = findViewById(R.id.btnLoginLogin)

        btnLogin.setOnClickListener {
            signInWithGoogle()
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != Constants.GOOGLE_SIGN_IN_REQUEST_CODE) return
        googleAuthClient.login(data) { b, e ->
            runBlocking {
                if (b) {
                    //Successful Sign In
                    Log.d("GoogleSignIn", "onActivityResult: Successful Sign In")
                    if (dbMain.getCurrentUser() != null) {
                        swapToActivity(MainActivity::class.java)
                    } else {
                        registerPopup.show()
                    }
                } else {
                    // Failed Sign In
                    Log.d("GoogleSignIn", "onActivityResult: Failed Sign In")
                    e?.printStackTrace()
                }
            }
        }
    }

    private fun signInWithGoogle() {
        googleAuthClient.onTapClient.beginSignIn(
            BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(Constants.WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
        ).addOnSuccessListener { result ->
            try {
                activity.startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    Constants.GOOGLE_SIGN_IN_REQUEST_CODE,
                    Intent(),
                    0,
                    0,
                    0
                )
            } catch (e: IntentSender.SendIntentException) {
                Log.e("GoogleSignIn", "Error signing in with Google", e)
            }
        }.addOnFailureListener { e ->
            Log.e("GoogleSignIn", "Error signing in with Google", e)
        }
    }
}