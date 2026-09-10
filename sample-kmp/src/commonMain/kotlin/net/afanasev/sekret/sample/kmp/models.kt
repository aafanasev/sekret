package net.afanasev.sekret.sample.kmp

data class Credentials(
    val login: String,
    @Secret val password: String,
)

@Secret
data class AuthToken(
    val value: String,
    val expiresAt: Long,
)

@Suppress("ArrayInDataClass")
data class Session(
    val id: String,
    @Secret val key: ByteArray,
    val scopes: Array<String>,
)

data class PaymentCard(
    @Masked("([0-9]{4})([0-9]{8})([0-9]{4})", "****-****-****-\$3")
    val number: String,
)

data class Contact(
    @Masked("([0-9]{3})([0-9]{4})", "***-\$2")
    val phone: String?,
)
