package com.example.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONObject

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    LIST,
    GET,
    WRITE
}

data class FirestoreErrorInfo(
    val error: String,
    val operationType: String,
    val path: String?,
    val userId: String?,
    val isAnonymous: Boolean?,
    val email: String?,
    val emailVerified: Boolean?,
    val providerIds: List<String>
)

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?
): Nothing {
    val auth = try {
        Firebase.auth
    } catch (_: Exception) {
        null
    }
    val user = auth?.currentUser
    val providerIds = user?.providerData?.mapNotNull { it.providerId } ?: emptyList()

    val info = FirestoreErrorInfo(
        error = exception.message ?: exception.toString(),
        operationType = operationType.name.lowercase(),
        path = path,
        userId = user?.uid,
        isAnonymous = user?.isAnonymous,
        email = user?.email,
        emailVerified = user?.isEmailVerified,
        providerIds = providerIds
    )

    val json = JSONObject().apply {
        put("error", info.error)
        put("operationType", info.operationType)
        put("path", info.path ?: JSONObject.NULL)
        put("authInfo", JSONObject().apply {
            put("userId", info.userId ?: JSONObject.NULL)
            put("isAnonymous", info.isAnonymous ?: JSONObject.NULL)
            put("email", info.email ?: JSONObject.NULL)
            put("emailVerified", info.emailVerified ?: JSONObject.NULL)
            put("providerIds", org.json.JSONArray(info.providerIds))
        })
    }

    val msg = "Firestore Error: $json"
    Log.e("FirestoreError", msg, exception)
    throw RuntimeException(msg, exception)
}

fun logFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?
): String {
    return try {
        handleFirestoreError(exception, operationType, path)
    } catch (e: RuntimeException) {
        e.message ?: (exception.message ?: "Firestore operation failed")
    }
}
