package mekong.ditagis.com.qlts.utities

import android.graphics.Bitmap
import java.text.SimpleDateFormat

/**
 * Created by ThanLe on 3/1/2018.
 */

class Constant internal constructor() {
    object AppID {
        const val TIEN_GIANG = "7F385322-BC7C-EB11-80D2-E88E1868CDFA"
        const val VINH_LONG = "F21FBD2D-BC7C-EB11-80D2-E88E1868CDFA"
        val LIST = arrayOf(TIEN_GIANG, VINH_LONG)
    }

    object LAYER_ID {
        const val SU_CO = "DiemSuCo"
        const val BASEMAP = "BASEMAP"
    }

    object RequestCode {
        const val LOGIN = 0
        const val CAMERA = 1
        const val SHOW_CAPTURE = 2
        const val PERMISSION = 3
        const val SEARCH = 4
        const val BASEMAP = 5
        const val RLAYER = 6
        const val ADD = 7
        const val ADD_FEATURE_ATTACHMENT = 8
        const val LIST_TASK = 9
        const val UPDATE = 10
        const val UPDATE_ATTACHMENT = 11
        const val PICK_PHOTO = 12
        const val NOTIFICATION = 100
        const val REQUEST_ID_UPDATE_ATTACHMENT = 50
    }

    object Field {
        const val OBJECTID = "OBJECTID"
        const val CREATED_USER = "created_user"
        const val CREATED_DATE = "created_date"
        const val LAST_EDITED_USER = "last_edited_user"
        const val LAST_EDITED_DATE = "last_edited_date"
        val NONE_UPDATE_FIELDS = arrayOf(CREATED_DATE, CREATED_USER, LAST_EDITED_DATE, LAST_EDITED_USER, OBJECTID)
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
        const val LOGIN = "$SERVER_API/Auth/Login"
        const val LAYER_INFO = "$SERVER_API/auth/layerinfos"
        const val CAPABILITIES = "$SERVER_API/auth/capabilities"
        const val APP_INFO = "$SERVER_API/auth/appinfo/"
    }

    object CompressFormat {
        val JPEG = Bitmap.CompressFormat.JPEG
        val PNG = Bitmap.CompressFormat.PNG

        val TYPE_UPDATE = JPEG

        val TYPE_COMPRESS = PNG
    }

    object Message {
        const val UNDEFINED = "Lỗi chưa xác định"
    }

    object Request {
        val LOGIN = 0
        val QUERY = 1
        val PERMISSION = 2
        val CAMERA = 3
        val SHOW_CAPTURE = 4
    }

    object HTTPRequest {
        const val GET_METHOD = "GET"
        const val POST_METHOD = "POST"
        const val AUTHORIZATION = "Authorization"
    }

    companion object {
        const val EMPTY = ""

        val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy")
        val OBJECTID = "OBJECTID"
        val IDSU_CO = "IDSuCo"

        //        val SERVER = "http://vwa.ditagis.com"
//        val SERVER = "http://vwaco.com.vn:9092"
        const val SERVER_API = "http://171.244.32.245:100"


        private var mInstance: Constant? = null

        val instance: Constant
            get() {
                if (mInstance == null)
                    mInstance = Constant()
                return mInstance!!
            }
    }
}
