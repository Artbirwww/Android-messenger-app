package com.artbirwww.messenger.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val photoURL: String = "",
    val birthday: String = "",
    val bio: String = "",
    val gender: String = "unspecified",
    val createdAt: Long = 0L
)
