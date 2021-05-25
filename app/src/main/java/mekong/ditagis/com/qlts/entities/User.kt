package mekong.ditagis.com.qlts.entities

class User(
        val username: String?,
        val displayName: String?,
        var roleId: String?,
        var accessToken: String?,
        var capabilities: Array<String>?,
        var capability: String?
)