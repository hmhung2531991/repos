package com.line.test

import android.content.Context
import android.os.Environment
import java.io.File

class FileCache(context: Context) {

    //Find the dir at SDCARD to save cached images
    private val cacheDir: File = if (Environment.getExternalStorageState() ==
        Environment.MEDIA_MOUNTED
    ) {
        //if SDCARD is mounted (SDCARD is present on device and mounted)
        File(
            context.getExternalFilesDir(null), "line_test"
        )
    } else {
        // if checking on simulator the create cache dir in your application context
        context.cacheDir
    }

    init {
        if (!cacheDir.exists()) {
            // create cache dir in your application context
            cacheDir.mkdirs()
        }
    }

    fun getFile(url: String): File {
        //Identify images by hashcode or encode by URLEncoder.encode.
        val filename = url.hashCode().toString()

        return File(cacheDir, filename)
    }

    fun clear() {
        // list all files inside cache directory
        val files = cacheDir.listFiles() ?: return

        //delete all cache directory files
        for (f in files) f.delete()
    }
}