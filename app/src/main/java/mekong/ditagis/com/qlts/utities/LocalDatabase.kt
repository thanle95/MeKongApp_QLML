package mekong.ditagis.com.qlts.utities

import android.R.attr.id
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import mekong.ditagis.com.qlts.entities.DBookmark
import java.util.*


class LocalDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    val allDBookmark: List<DBookmark>
        get() {
            val dBookmarks = ArrayList<DBookmark>()
            Log.i(TAG, "LocalDatabase.getHoaDon_UnRead ... $id")
            val db = this.readableDatabase
            val cursor: Cursor
            val query = "select * from $TABLE_BOOKMARK"
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val dBookmark = DBookmark(java.lang.Double.parseDouble(cursor.getString(0)),
                            java.lang.Double.parseDouble(cursor.getString(1)), cursor.getString(2), cursor.getString(3))
                    dBookmarks.add(dBookmark)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return dBookmarks
        }

    // Tạo các bảng.
    override fun onCreate(db: SQLiteDatabase) {
        Log.i(TAG, "LocalDatabase.onCreate ... ")
        // Script tạo bảng.
        val script = ("CREATE TABLE " + TABLE_BOOKMARK + "("

                + COLUMN_BOOKMARK_LATITUDE + " TEXT,"
                + COLUMN_BOOKMARK_LONGTITUDE + " TEXT ,"
                + COLUMN_BOOKMARK_ADDRESS + " TEXT ,"
                + COLUMN_BOOKMARK_NAME + " TEXT" + ")")
        val script1 = ("CREATE TABLE " + TABLE_BASEMAP + "("

                + COLUMN_BASEMAP_ID + " TEXT,"
                + COLUMN_BASEMAP_PATH + " TEXT,"
                + COLUMN_BASEMAP_CENTER_X + " REAL,"
                + COLUMN_BASEMAP_CENTER_Y + " REAL,"
                + COLUMN_BASEMAP_MAPSCALE + " REAL " + ")")


        // Chạy lệnh tạo bảng.
        db.execSQL(script)
        db.execSQL(script1)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        Log.i(TAG, "LocalDatabase.onUpgrade ... ")

        // Hủy (drop) bảng cũ nếu nó đã tồn tại.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARK")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BASEMAP")


        // Và tạo lại.
        onCreate(db)
    }
//
//    fun create() {
//        val db = this.writableDatabase
//
//        onCreate(db)
//    }
//
//    fun Upgrade() {
//        val db = this.writableDatabase
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARK")
//
//        onCreate(db)
//    }

    fun addDBookmark(dBookMark: DBookmark): Boolean {

        Log.i(TAG, "LocalDatabase.addDBookmark ... " + dBookMark.address)

        val db = this.writableDatabase
        var value: Long = 0
        try {
            val values = ContentValues()
            values.put(COLUMN_BOOKMARK_LATITUDE, dBookMark.latitude)
            values.put(COLUMN_BOOKMARK_LONGTITUDE, dBookMark.longtiude)
            values.put(COLUMN_BOOKMARK_ADDRESS, dBookMark.address)
            values.put(COLUMN_BOOKMARK_NAME, dBookMark.name)

            value = db.insert(TABLE_BOOKMARK, null, values)

            // Đóng kết nối database.
            db.close()
        } catch (e: Exception) {
            Log.d("ERROR", e.toString())
        }

        return value > 0
    }


    fun deleteBookmark(longtitude: Double, latitude: Double): Boolean {
        Log.i(TAG, "LocalDatabase.deleteDBookmark ... ")

        val db = this.writableDatabase
        val delete = db.delete(TABLE_BOOKMARK, String.format("%s ='%s' and %s = '%s'", COLUMN_BOOKMARK_LONGTITUDE, longtitude,
                COLUMN_BOOKMARK_LATITUDE, latitude),
                arrayOf())
        db.close()
        return delete > 0
    }


    fun deleteAllBaseMap(): Boolean {
        Log.i(TAG, "LocalDatabase.deleteBasemap ... ")

        val db = this.writableDatabase
        val delete = db.delete(TABLE_BASEMAP," 1 = 1 ",
                arrayOf())
        db.close()
        return delete > 0
    }

    companion object {

        private const val TAG = "SQLite"


        // Phiên bản
        private const val DATABASE_VERSION = 2


        // Tên cơ sở dữ liệu.
        private const val DATABASE_NAME = "HoaDon_Manager"


        // Tên bảng: HoaDon.
        private const val TABLE_BOOKMARK = "HoaDon"
        // Tên bảng: Basemap.
        private const val TABLE_BASEMAP = "BaseMap"
        private const val COLUMN_BOOKMARK_LATITUDE = "BookmarkLatitude"
        private const val COLUMN_BOOKMARK_LONGTITUDE = "BookmarkLongtitude"
        private const val COLUMN_BOOKMARK_ADDRESS = "BookmarkAddress"
        private const val COLUMN_BOOKMARK_NAME = "BookmarkName"

        private const val COLUMN_BASEMAP_ID = "BaseMapID"
        private const val COLUMN_BASEMAP_PATH = "BaseMapPath"
        private const val COLUMN_BASEMAP_CENTER_X = "BaseMapCenterX"
        private const val COLUMN_BASEMAP_CENTER_Y = "BaseMapCenterY"
        private const val COLUMN_BASEMAP_MAPSCALE = "BaseMapMapScale"


        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            if (instance == null)
                instance = LocalDatabase(context.applicationContext)
            return instance as LocalDatabase
        }
    }


}