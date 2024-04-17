package de.theskyscout.template.signIn

import android.content.Intent
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import de.theskyscout.template.activities.CodexActivity

class GoogleAuthClient(
    val onTapClient: SignInClient,
    val activity: CodexActivity
) {
    val auth = Firebase.auth
    var user = auth.currentUser
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

            // Sign in with MongoDB Realm
            activity.database.login(idToken!!) { success, exception ->
                if(success) {
                    callback(true, null)
                } else {
                    callback(false, exception)
                }
            }
        } else {
            callback(false, Exception("Google ID Token is null"))
        }
    }

    fun signOut() {
        activity.database.logout()
        auth.signOut()
        user = null
    }
}