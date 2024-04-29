package de.theskyscout.zap.signIn

import android.content.Intent
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.utils.Cache

class GoogleAuthClient(
    val onTapClient: SignInClient,
    val activity: CodexActivity
) {

    companion object {
        val auth = Firebase.auth
        var user = auth.currentUser
    }

    var idToken: String? = null


    fun login(data: Intent?, callback: (Boolean, Exception?) -> Unit?) {
        val googleCredentials = onTapClient.getSignInCredentialFromIntent(data)
        idToken = googleCredentials.googleIdToken
        if(idToken != null) {
            val firebaseCredentials = GoogleAuthProvider.getCredential(idToken, null)
            // Sign in with Firebase Auth
            auth.signInWithCredential(firebaseCredentials)
                .addOnCompleteListener(activity) { task ->
                    if(task.isSuccessful) {
                        user = auth.currentUser
                        callback(true, null)
                    } else {
                        callback(false, task.exception)
                    }
                }
        } else {
            callback(false, Exception("Google ID Token is null"))
        }
    }

    fun signOut() {
        auth.signOut()
        user = null
    }
}