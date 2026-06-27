package com.artbirwww.messenger.data.repository

import com.artbirwww.messenger.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signUp(user: User, password: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = result.user?.uid ?: return false
            val finalUser = user.copy(uid = uid, createdAt = System.currentTimeMillis())
            db.collection("users").document(uid).set(finalUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            db.collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFcmToken(uid: String): String? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("fcmToken")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(user: User): Boolean {
        return try {
            db.collection("users").document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            db.collection("users").document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return try {
            db.collection("users")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", query + "\uf8ff")
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
