package com.example.happyplaces.utils

import java.io.File
import java.net.URI

class Utils {
    companion object {
        fun removeImageFromUri(filePath: String) {
            val file = File(URI.create(filePath))
            file.delete()
        }
    }
}