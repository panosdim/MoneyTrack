package com.panosdim.moneytrack.rest.requests

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)