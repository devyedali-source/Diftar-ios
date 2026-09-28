@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.android.gms.common.api

open class ApiException(val statusCode: Int, val statusMessage: String? = null) : Exception("$statusCode: ${statusMessage ?: ""}") {
    val status: Status get() = Status(statusCode, statusMessage)
}

class Status(val statusCode: Int, val statusMessage: String?)

class Scope(val scopeUri: String) {
    override fun equals(other: Any?) = other is Scope && other.scopeUri == scopeUri
    override fun hashCode() = scopeUri.hashCode()
    override fun toString() = scopeUri
}

object CommonStatusCodes {
    const val SUCCESS = 0
    const val NETWORK_ERROR = 7
    const val CANCELED = 16
    const val SIGN_IN_REQUIRED = 4
}
