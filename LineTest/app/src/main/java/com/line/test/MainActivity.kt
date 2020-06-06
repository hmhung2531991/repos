package com.line.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mVideoAsyncTask: AsyncTask<Void, Void, Movie>

    private lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvImage.layoutManager = LinearLayoutManager(this)
        rvImage.addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.item_spacing)))

        PreferencesManager.initializeInstance(applicationContext)
        imageLoader = ImageLoader(this)
        if (isStoragePermissionGranted()) {
            initData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(
                "MainActivity",
                "Permission: " + permissions[0] + "was " + grantResults[0]
            )
            //resume tasks needing this permission
            initData()
        }
    }

    private fun initData() {
        mVideoAsyncTask = VideoLoader(listener = object : VideoLoader.VideoLoaderListener {
            override fun onStarted() {
                pbcLoading.isVisible = true
            }

            override fun onCompleted(movie: Movie) {
                pbcLoading.isVisible = false

                val movieAdapter = MovieAdapter(this@MainActivity, imageLoader, movie.image)
                tvTitle.text = movie.title
                rvImage.adapter = movieAdapter
            }
        })
        mVideoAsyncTask.execute()
    }

    private fun isStoragePermissionGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("MainActivity", "Permission is granted")
                true
            } else {
                Log.v("MainActivity", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v("MainActivity", "Permission is granted")
            true
        }

    private class VideoLoader(val listener: VideoLoaderListener) : AsyncTask<Void, Void, Movie>() {

        interface VideoLoaderListener {
            // callback for start
            fun onStarted()

            // callback on success
            fun onCompleted(movie: Movie)

        }

        override fun onPreExecute() {
            super.onPreExecute()
            listener.onStarted()
        }

        override fun doInBackground(vararg params: Void?): Movie {
            return MovieUtils.readMovie()
        }

        override fun onPostExecute(result: Movie) {
            super.onPostExecute(result)
            listener.onCompleted(result)
        }
    }

    override fun onStop() {
        super.onStop()

        mVideoAsyncTask.cancel(true)
    }

    override fun onDestroy() {
        super.onDestroy()

        imageLoader.cancelDownload()
    }

}
