package com.programmersbox.igdb.models

import com.programmersbox.thirdpartyutils.gsonConverter
import com.programmersbox.thirdpartyutils.rx2FactorySync
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

object Igdb {
    private const val baseUrl = "https://api-v3.igdb.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("user-key", "4cc7a11dbb492abfc3f0e79fc2f6c132")
                .addHeader("fields", "name")
                .build()
            chain.proceed(request)
        }
        .build()

    fun buildService(): IgdbServiceClass = IgdbServiceClass(
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .gsonConverter()
            .rx2FactorySync()
            .build()
            .create()
    )

    interface IgdbService {

        @GET("games/?fields=*")
        fun getList(): Observable<List<Game>>

        @GET("covers/{id}?fields=*")
        fun getCovers(@Path("id") gameId: String): Observable<List<Cover>>

    }

    data class Cover(
        val id: Number?,
        val game: Number?,
        val height: Number?,
        val image_id: String?,
        val url: String?,
        val width: Number?,
        val checksum: String?
    )

    data class Game(
        val id: Number?,
        val age_ratings: List<Number>?,
        val category: Number?,
        val collection: Number?,
        val cover: Number?,
        val created_at: Number?,
        val external_games: List<Number>?,
        val first_release_date: Number?,
        val genres: List<Number>?,
        val involved_companies: List<Number>?,
        val keywords: List<Number>?,
        val multiplayer_modes: List<Number>?,
        val name: String?,
        val platforms: List<Number>?,
        val popularity: Number?,
        val release_dates: List<Number>?,
        val screenshots: List<Number>?,
        val similar_games: List<Number>?,
        val slug: String?,
        val summary: String?,
        val tags: List<Number>?,
        val updated_at: Number?,
        val url: String?,
        val videos: List<Number>?,
        val websites: List<Number>?,
        val checksum: String?
    )

    class IgdbServiceClass(igdbService: IgdbService) : IgdbService by igdbService
}