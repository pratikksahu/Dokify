package com.pratiksahu.dokify.model

import android.net.Uri


data class DocInfo(
    val imageUri: Uri,
    val imageName: String = " ",
    val imageSize: String = " "
)