package com.architectcoders.animalcoders.data.remote.login

import arrow.core.Either
import com.architectcoders.domain.model.Failure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseLoginServiceImpl (private var auth: FirebaseAuth) : LoginService {

    override suspend fun login(username: String, password: String) : Either<Failure, FirebaseUser> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        auth.currentUser?.let{ user ->
                            continuation.resume(Either.right(user))
                        } ?:  continuation.resume(Either.left(Failure(Failure.Reason.USER_NOT_EXIST, null)))
                    } else {
                        continuation.resume(Either.left(Failure(Failure.Reason.API_ERROR, it.exception?.message)))
                    }
                }
        }

    override suspend fun register(username: String, password: String): Either<Failure, FirebaseUser> =
        suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        auth.currentUser?.let { fbUser ->
                            continuation.resume(Either.right(fbUser))
                        }?: continuation.resume(Either.left(Failure(Failure.Reason.REGISTER_ERROR)))
                    }
                }
        }
}
