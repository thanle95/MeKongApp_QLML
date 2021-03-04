package mekong.ditagis.com.qlts.entities

import android.graphics.Bitmap
import com.esri.arcgisruntime.data.Attachment

class DAttachment {
    var attachment: Attachment? = null
    var image: Bitmap? = null
    var name: String
    var title: String

    constructor(name: String, title: String) {
        this.name = name
        this.title = title
    }

    constructor(attachment: Attachment?, image: Bitmap?, name: String, title: String) {
        this.attachment = attachment
        this.image = image
        this.name = name
        this.title = title
    }

}
