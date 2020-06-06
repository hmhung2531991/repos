package com.line.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ImageLoader(private val context: Context) {

    // default image show in list (Before online image download)
    private val stubId = R.drawable.image_placehoder

    // Initialize MemoryCache
    private val memoryCache = MemoryCache()
    private val fileCache = FileCache(context)

    //Create Map (collection) to store image and image url in key value pair
    private val imageViews =
        Collections.synchronizedMap(
            WeakHashMap<ImageView, String>()
        )

    // Creates a thread pool that reuses a fixed number of
    // threads operating off a shared unbounded queue.
    private val executorService =
        Executors.newFixedThreadPool(5)

    //handler to display images in UI thread
    private val handler = Handler()

    fun displayImage(
        url: String,
        imageView: ImageView,
        textView: TextView
    ) {
        //Store image and url in Map
        imageViews[imageView] = url

        //Check image is stored in MemoryCache Map or not
        val bitmap = memoryCache.get(url)
        if (bitmap != null) {
            // if image is stored in MemoryCache Map then
            // Show image in listview row
            imageView.setImageBitmap(bitmap)
        } else {
            //queue Photo to download from url
            queuePhoto(url, imageView, textView)

            //Before downloading image show default image
            imageView.setImageResource(stubId)
            textView.text = context.getString(R.string.progress_download, "0")
        }
    }

    fun clearCache() {
        //Clear cache directory downloaded images and stored data in maps
        memoryCache.clear()
        fileCache.clear()
    }

    fun cancelDownload() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
        }
    }

    private fun queuePhoto(
        url: String,
        imageView: ImageView,
        textView: TextView
    ) {
        // Store image and url in PhotoToLoad object
        val p = PhotoToLoad(url, imageView, textView)

        // pass PhotoToLoad object to PhotosLoader runnable class
        // and submit PhotosLoader runnable to executers to run runnable
        // Submits a PhotosLoader runnable task for execution
        executorService.submit(PhotosLoader(p))
    }

    //Task for the queue
    private inner class PhotoToLoad(
        var url: String,
        var imageView: ImageView,
        var textView: TextView
    )

    private inner class PhotosLoader internal constructor(var photoToLoad: PhotoToLoad) :
        Runnable {

        override fun run() {
            try {
                //Check if image already downloaded
                if (imageViewReused(photoToLoad)) return

                // download image from web url
                val bmp = getBitmap(photoToLoad.url, progressListener = { kb ->
                    val pd = ProgressDisplayer(kb, photoToLoad)

                    // Use handle to update ui form WorkerThread to MainThread
                    handler.post(pd)
                })

                bmp?.let {
                    // set image data in Memory Cache
                    memoryCache.put(photoToLoad.url, bmp)

                    if (imageViewReused(photoToLoad)) return

                    PreferencesManager.instance.setValue(photoToLoad.url, true)

                    // Get bitmap to display
                    val bd = BitmapDisplayer(bmp, photoToLoad)

                    // Causes the Runnable bd (BitmapDisplayer) to be added to the message queue.
                    // The runnable will be run on the thread to which this handler is attached.
                    // BitmapDisplayer run method will call
                    handler.post(bd)
                }
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }

    }

    private fun getBitmap(url: String, progressListener: (String) -> Unit): Bitmap? {
        val f = fileCache.getFile(url)

        if (PreferencesManager.instance.getValue(url)) {
            decodeFile(f)?.let {
                return it
            }
        }
        //from SD cache
        //if trying to decode file which not exist in cache return null
        return try {
            // Download image file from web
            var bitmap: Bitmap? = null
            val imageUrl = URL(url)
            val conn =
                imageUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength: Int = conn.contentLength

            // read content of by input stream
            val `is` = conn.inputStream

            // Constructs a new FileOutputStream that writes to file
            // if file not exist then it will create file
            val os: OutputStream = FileOutputStream(f)

            // See Utils class CopyStream method
            // It will each pixel from input stream and
            // write pixels to output stream (file)
            val bufferSize = 512
            var total: Long = 0
            val bytes = ByteArray(bufferSize)
            while (true) {
                //Read byte from input stream
                val count = `is`.read(bytes, 0, bufferSize)
                if (count == -1) break

                total += count
                Log.d("ImageLoader", "$url Total $total")
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    progressListener("$total")

                //Write byte from output stream
                os.write(bytes, 0, count)
            }

            os.close()
            conn.disconnect()

            //Now file created and going to resize file with defined height
            // Decodes image and scales it to reduce memory consumption
            bitmap = decodeFile(f)
            bitmap
        } catch (ex: Throwable) {
            ex.printStackTrace()
            if (ex is OutOfMemoryError) memoryCache.clear()
            null
        }
    }

    //Decodes image and scales it to reduce memory consumption
    private fun decodeFile(f: File): Bitmap? {
        try {
            // check file existed on sdcard
            if (!f.exists()) return null

            //Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            val stream1 = FileInputStream(f)
            BitmapFactory.decodeStream(stream1, null, o)
            stream1.close()

            //Find the correct scale value. It should be the power of 2.

            // Set width/height of recreated image
            val REQUIRED_SIZE = 85
            var widthTmp = o.outWidth
            var heightTmp = o.outHeight
            var scale = 1
            while (true) {
                if (widthTmp / 2 < REQUIRED_SIZE || heightTmp / 2 < REQUIRED_SIZE) break
                widthTmp /= 2
                heightTmp /= 2
                scale *= 2
            }

            //decode with current scale values
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val stream2 = FileInputStream(f)
            val bitmap = BitmapFactory.decodeStream(stream2, null, o2)
            stream2.close()
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun imageViewReused(photoToLoad: PhotoToLoad): Boolean {
        val tag = imageViews[photoToLoad.imageView]
        //Check url is already exist in imageViews MAP
        return tag == null || tag != photoToLoad.url
    }

    //Used to display bitmap in the UI thread
    private inner class BitmapDisplayer(var bitmap: Bitmap?, var photoToLoad: PhotoToLoad) :
        Runnable {

        override fun run() {
            if (imageViewReused(photoToLoad)) return

            // Show bitmap on UI
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap)
            else
                photoToLoad.imageView.setImageResource(stubId)

            // hide text view progress
            photoToLoad.textView.visibility = View.INVISIBLE
        }

    }

    //Used to display progress in the UI thread
    private inner class ProgressDisplayer(var kb: String, var photoToLoad: PhotoToLoad) :
        Runnable {

        override fun run() {
            // Show bitmap on UI
            photoToLoad.textView.text = context.getString(
                R.string.progress_download,
                kb
            )
        }

    }
}