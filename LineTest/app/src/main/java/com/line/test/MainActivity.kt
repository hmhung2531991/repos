package com.line.test

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mVideoAsyncTask: AsyncTask<Void, Void, Movie>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvImage.layoutManager = LinearLayoutManager(this);
        initData()
    }

    private fun initData() {
        mVideoAsyncTask = VideoLoader(listener = object : VideoLoader.VideoLoaderListener {
            override fun onStarted() {
                pbcLoading.isVisible = true
            }

            override fun onCompleted(movie: Movie) {
                pbcLoading.isVisible = false

                tvTitle.text = movie.title
                val movieAdapter = MovieAdapter(this@MainActivity, movie.image)
                rvImage.adapter = movieAdapter
            }
        })
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

}
