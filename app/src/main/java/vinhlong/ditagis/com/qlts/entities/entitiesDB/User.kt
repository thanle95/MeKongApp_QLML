package vinhlong.ditagis.com.qlts.entities.entitiesDB

class User {
    var userName: String? = null
    var passWord: String? = null
    var displayName: String? = null
    var token: String? = null
    var isQuan5: Boolean = false
    var isQuan6: Boolean = false
    var isQuan8: Boolean = false
    var isQuanBinhTan: Boolean = false

    var isCreate: Boolean = false
    var isValid: Boolean = false


    constructor() {

    }

    constructor(userName: String, passWord: String, displayName: String, isQuan5: Boolean, isQuan6: Boolean, isQuan8: Boolean, isQuanBinhTan: Boolean, isCreate: Boolean, isValid: Boolean) {
        this.userName = userName
        this.passWord = passWord
        this.displayName = displayName
        this.isQuan5 = isQuan5
        this.isQuan6 = isQuan6
        this.isQuan8 = isQuan8
        this.isQuanBinhTan = isQuanBinhTan
        this.isCreate = isCreate
        this.isValid = isValid
    }
}