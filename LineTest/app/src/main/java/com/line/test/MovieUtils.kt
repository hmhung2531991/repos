package com.line.test

import org.json.JSONObject

object MovieUtils {

    private val VIDEO_DATA: String = "{\n" +
            "    \"title\" : \"Civil War\",\n" +
            "    \"image\" : [\n" +
            "        \"http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg?type=m665_443_2\",\n" +
            "        \"http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2\",\n" +
            "        \"http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg?type=m665_443_2\"\n" +
            "    ]\n" +
            "}"

    fun readMovie(): Movie {
        val videoObject = JSONObject(VIDEO_DATA)
        val title = videoObject.getString("title")
        val imageObjects = videoObject.getJSONArray("image")
        val images = arrayListOf<String>()
        for (i in 0 until imageObjects.length()) {
            val url = imageObjects.getString(i)
            images.add(url)
        }

        return Movie(title, images)
    }

}