package com.line.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter constructor(
    private val context: Context,
    private val images: List<String>
) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {


    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.movie_video, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val url = images[position]
    }

    // total number of rows
    override fun getItemCount(): Int {
        return images.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val ivVideo: ImageView = itemView.findViewById(R.id.ivVideo)
    }

}