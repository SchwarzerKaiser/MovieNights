package com.leewilson.movienights.repository.auth

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.leewilson.movienights.model.UserProperties
import com.leewilson.movienights.persistence.UserPropertiesDao
import com.leewilson.movienights.repository.User
import com.leewilson.movienights.ui.auth.state.AuthViewState
import com.leewilson.movienights.util.Constants
import com.leewilson.movienights.util.DataState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDbRef: DatabaseReference,
    private val authDao: UserPropertiesDao,
    private val sharedPreferences: SharedPreferences,
    private val sharedPreferencesEditor: SharedPreferences.Editor
) {

    private val TAG = "AuthRepository"

    suspend fun loginUserIfExisting(): DataState<AuthViewState> {

        val email = sharedPreferences.getString(Constants.PREVIOUS_AUTH_USER, null)
            // User not in SharedPref, so return null (will not be reacted to)
            ?: return DataState.data<AuthViewState>(null)

        val userProperties = authDao.searchByEmail(email)
            // This shouldn't ever happen, unless there's some database config error
            ?: return DataState.error<AuthViewState>("Database error! Please reinstall.")

        return login(email, userProperties.password)
    }

    suspend fun loginWithCredentials(email: String?, password: String?): DataState<AuthViewState> {
        if(email.isNullOrBlank() || password.isNullOrBlank()) {
            return DataState.error<AuthViewState>(
                Constants.MISSING_FIELDS
            )
        }
        return login(email, password)
    }

    private suspend fun login(email: String, password: String): DataState<AuthViewState> {
        try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            storeUserLocally(authResult.user!!, password)

            return DataState.data<AuthViewState>(
                data = AuthViewState(
                    authResult.user?.uid
                )
            )
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e(TAG, "loginUserIfExisting: FirebaseAuthInvalidUserException: ", e)
            return DataState.error<AuthViewState>(
                Constants.MISSING_USER
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "loginUserIfExisting: FirebaseAuthInvalidCredentialsException: ", e)
            return DataState.error<AuthViewState>(
                Constants.INCORRECT_PASSWORD
            )
        }
    }

    suspend fun register(
        name: String?, email: String?,
        password: String?, confirmPassword: String?
    ): DataState<AuthViewState> {

        if (name.isNullOrBlank() ||
            email.isNullOrBlank() ||
            password.isNullOrBlank() ||
            confirmPassword.isNullOrBlank())
            return DataState.error(Constants.MISSING_FIELDS)

        if (password != confirmPassword)
            return DataState.error(Constants.PASSWORDS_DO_NOT_MATCH)

        try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            authResult.user?.let { user ->
                storeUserLocally(user, password)
                createDbUser(name, user)
                return@register DataState.data(
                    null,
                    AuthViewState(user.uid)
                )
            }
        } catch (e: FirebaseAuthWeakPasswordException) {
            return DataState.error(e.reason.toString())
        } catch (e: FirebaseAuthUserCollisionException) {
            return DataState.error(e.message.toString())
        }

        // user is null. unknown error
        return DataState.error("Something went wrong.")
    }

    private fun createDbUser(name: String, user: FirebaseUser) {
        firebaseDbRef
            .child("users")
            .child(user.uid)
            .setValue(
                User(
                    name = name,
                    uid = user.uid
                )
            )
    }

    private fun storeUserLocally(user: FirebaseUser, password: String) {
        sharedPreferencesEditor.putString(Constants.PREVIOUS_AUTH_USER, user.email)
        sharedPreferencesEditor.apply()
        authDao.insertAndReplace(
            UserProperties(
                0,
                user.email!!,
                password
            )
        )
    }
}
