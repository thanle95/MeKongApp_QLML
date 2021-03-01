package vinhlong.ditagis.com.qlts.utities

import android.graphics.Bitmap
import vinhlong.ditagis.com.qlts.adapter.SettingsAdapter
import java.text.SimpleDateFormat

/**
 * Created by ThanLe on 3/1/2018.
 */

class Constant internal constructor() {
    val settingsItems: Array<SettingsAdapter.Item>? = null
    var API_LOGIN: String

    var DISPLAY_NAME: String

    var LAYER_INFO: String

    var IS_ACCESS: String

    init {
        API_LOGIN = "$SERVER_API/Login"
    }

    init {
        DISPLAY_NAME = "$SERVER_API/Account/Profile"
    }

    init {
        LAYER_INFO = "$SERVER_API/Account/LayerInfo"
    }

    init {
        IS_ACCESS = "$SERVER_API/Account/IsAccess/m_qlts"
    }
    object PreferenceKey {
        const val USERNAME = "username"
        const val PASSWORD = "pasword"
        const val DISPLAY_NAME = "displayname"
        const val TOKEN = "token"
    }

    object AttachmentName {
        val ADD = "img_%s_%d.png"
        val UPDATE = "img_update_%s_%d.png"
    }
    object URL_API {
        val CHECK_VERSION = "$SERVER/versioning/QLTS?version=%s"
        val ADD_FEATURE = "$SERVER_API/QuanLySuCo/TiepNhanSuCo/%s"
        val LOGIN = "$SERVER_API/Login"
        val PROFILE = "$SERVER_API/Account/Profile"
        val GENERATE_ID_SUCO = "$SERVER_API/QuanLySuCo/GenerateIDSuCo"
        val LAYER_INFO = "$SERVER_API/Account/layerinfo"
        val CHANGE_PASSWORD = "$SERVER_API/Account/changepass"
        val COMPLETE = "$SERVER_API/quanlysuco/xacnhanhoanthanhnhanvien?id=%s"
        val IS_ACCESS = "$SERVER_API/Account/IsAccess/m_qlsc"
        val GENERATE_ID_SUCOTHONGTIN = "$SERVER_API/QuanLySuCo/GenerateIDSuCoThongTin/"


    }
    object CompressFormat {
        val JPEG = Bitmap.CompressFormat.JPEG
        val PNG = Bitmap.CompressFormat.PNG

        val TYPE_UPDATE = JPEG

    }

    object Request {
        val LOGIN = 0
        val QUERY = 1
        val PERMISSION = 2
        val CAMERA = 3
        val SHOW_CAPTURE = 4
    }
    object HTTPRequest {
        val POST_METHOD = "POST"
    }

    companion object {
        val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy")
        val OBJECTID = "OBJECTID"
        val IDSU_CO = "IDSuCo"
        //        val SERVER = "http://vwa.ditagis.com"
        val SERVER = "http://vwaco.com.vn:9092"
        val SERVER_API = "$SERVER/api"


        private var mInstance: Constant? = null

        val instance: Constant
            get() {
                if (mInstance == null)
                    mInstance = Constant()
                return mInstance!!
            }
    }
}
