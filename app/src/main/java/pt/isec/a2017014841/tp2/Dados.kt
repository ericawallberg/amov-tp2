package pt.isec.a2017014841.tp2

import com.google.firebase.auth.FirebaseAuth

internal object Dados {
    const val MIN_USERS = 3
    const val CLIENT_MODE = 1
    const val SERVER_MODE = 0
    const val RC_SIGN_IN = 0
    var mAuth: FirebaseAuth? = null

}