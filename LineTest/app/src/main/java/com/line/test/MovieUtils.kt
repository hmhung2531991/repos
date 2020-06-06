package com.line.test

import org.json.JSONObject

object MovieUtils {

    private val VIDEO_DATA: String = "{\n" +
            "    \"title\" : \"Civil War\",\n" +
            "    \"image\" : [\n" +
            "        \"http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg?type=m665_443_2\",\n" +
            "        \"http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2\",\n" +
            "        \"https://i.annihil.us/u/prod/marvel/images/OpenGraph-TW-1200x630.jpg\",\n" +
            "        \"https://upload.wikimedia.org/wikipedia/en/8/85/Captain_Marvel_poster.jpg\",\n" +
            "        \"https://cdn.mos.cms.futurecdn.net/PBpaPfht3TSS2rSg5ezHE.jpg\",\n" +
            "        \"https://pbs.twimg.com/media/EUs-b_vUUAALces.jpg\",\n" +
            "        \"https://thetvtraveler.com/wp-content/uploads/2019/03/endgame.jpg\",\n" +
            "        \"https://i.ytimg.com/vi/RHbHfVjj4n4/maxresdefault.jpg\",\n" +
            "        \"https://upload.wikimedia.org/wikipedia/en/5/5f/Marvel_Cinematic_Universe_Infinity_Saga_Artwork.jpeg\",\n" +
            "        \"https://media.gizmodo.co.uk/wp-content/uploads/2017/07/n3e3sabil5wmpigkddmr.png\",\n" +
            "        \"https://i2-prod.mirror.co.uk/incoming/article807990.ece/ALTERNATES/s615/AVENGERS%20620x396.jpg\",\n" +
            "        \"https://i.ytimg.com/vi/BsprbcuVgSU/maxresdefault.jpg\",\n" +
            "        \"https://i.ytimg.com/vi/1PDz-fsp7HQ/hqdefault.jpg\",\n" +
            "        \"https://img.youtube.com/vi/WRDb9QHxlqw/hqdefault.jpg\",\n" +
            "        \"https://images-na.ssl-images-amazon.com/images/I/61S00RQMfrL._AC_SL1200_.jpg\",\n" +
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