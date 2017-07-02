package com.akolov.currigate

data class ThinUser(val id: String) {
    constructor () : this("")
}

data class ThickUser(val id: String, val identity: Identity?) {
    constructor () : this("", null)

}

data class Identity(val sub: String,
                    val name: String,
                    val givenName: String,
                    val familyName: String,
                    val profile: String,
                    val picture: String,
                    val email: String,
                    val gender: String,
                    val locale: String) {
    constructor () : this("", "", "", "", "", "", "", "", "")

}
