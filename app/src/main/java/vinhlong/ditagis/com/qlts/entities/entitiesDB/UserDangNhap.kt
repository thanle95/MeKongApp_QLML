package vinhlong.ditagis.com.qlts.entities.entitiesDB

class UserDangNhap private constructor() {
    var user: User? = null

    companion object {

        var instance: UserDangNhap? = null
            get() {
                if (field == null) {
                    this.instance = UserDangNhap()
                }
                return field
            }
    }
}
