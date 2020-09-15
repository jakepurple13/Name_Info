package com.programmersbox.igdb.models

import androidx.lifecycle.ViewModel
import com.programmersbox.igdb.BuildConfig
import com.programmersbox.igdb.R
import com.programmersbox.thirdpartyutils.gsonConverter
import com.programmersbox.thirdpartyutils.rx2FactorySync
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

class Ify {
    private val agify by lazy { Agify.buildService() }
    private val genderize by lazy { Genderize.buildService() }
    private val nationalize by lazy { Nationalize.buildService() }
    private val behindTheName by lazy { BehindTheName.buildService() }

    fun getIfyInfo(vararg name: String) = Observables.zip(
        agify.getList(*name),
        genderize.getList(*name),
        nationalize.getList(*name),
        behindTheName.getNameFacts(name[0])
    ) { a, g, n, b ->
        a.map { age ->
            IfyInfo(
                name = age.name,
                age = age.age,
                relatedNames = b.names,
                gender = g.find { it.name == age.name }?.let { Gender(gender = it.gender, probability = it.probability) },
                nationality = n.find { it.name == age.name }?.country.orEmpty()
            )
        }
    }

}

data class Gender(val gender: String, val probability: Float) {
    val genderColor
        get() = when (gender) {
            "male" -> R.color.maleColor
            "female" -> R.color.femaleColor
            else -> null
        }

    val genderColorInverse
        get() = when (gender) {
            "male" -> R.color.femaleColor
            "female" -> R.color.maleColor
            else -> null
        }

    fun capitalGender() = gender.capitalize(Locale.getDefault())
}

data class IfyInfo(val name: String, val age: Int, val relatedNames: List<String>?, val gender: Gender?, val nationality: List<Country>)

data class AgifyInfo(val name: String, val age: Int, val count: Int)

data class GenderizeInfo(val name: String, val gender: String, val probability: Float, val count: Int)

data class Country(val country_id: String, val probability: Float) : ViewModel() {
    val flagUrl get() = "https://www.countryflags.io/$country_id/flat/64.png"
    val countryName: String get() = Locale("", country_id).displayCountry
}

data class NationalizeInfo(val name: String, val country: List<Country>)

data class BehindTheNameInfo(val names: List<String> = emptyList())

object Agify {
    private const val baseUrl = "https://api.agify.io"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .build()
            chain.proceed(request)
        }
        .build()

    fun buildService(): AgifyService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .gsonConverter()
        .rx2FactorySync()
        .build()
        .create()

    interface AgifyService {

        @GET("?")
        fun getList(
            @Query("name[]") vararg name: String,
            @Query("country_id") country_id: String = Locale.getDefault().country
        ): Observable<List<AgifyInfo>>

    }
}

object Genderize {
    private const val baseUrl = "https://api.genderize.io/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .build()
            chain.proceed(request)
        }
        .build()

    fun buildService(): GenderizeService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .gsonConverter()
        .rx2FactorySync()
        .build()
        .create()

    interface GenderizeService {

        @GET("?")
        fun getList(
            @Query("name[]") vararg name: String,
            @Query("country_id") country_id: String = Locale.getDefault().country
        ): Observable<List<GenderizeInfo>>

    }
}

object Nationalize {
    private const val baseUrl = "https://api.nationalize.io"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .build()
            chain.proceed(request)
        }
        .build()

    fun buildService(): NationalizeService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .gsonConverter()
        .rx2FactorySync()
        .build()
        .create()

    interface NationalizeService {

        @GET("?")
        fun getList(
            @Query("name[]") vararg name: String,
            @Query("country_id") country_id: String = Locale.getDefault().country
        ): Observable<List<NationalizeInfo>>

    }
}

object BehindTheName {
    private const val baseUrl = "https://www.behindthename.com/"
    // https://www.behindthename.com/api/lookup.json?name=mary&key=#key#

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .build()
            chain.proceed(request)
        }
        .build()

    fun buildService(): BehindTheNameService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .gsonConverter()
        .rx2FactorySync()
        .build()
        .create()

    interface BehindTheNameService {
        @GET("api/related.json?")
        fun getNameFacts(@Query("name") name: String, @Query("key") key: String = BuildConfig.BehindTheNameKey): Observable<BehindTheNameInfo>
    }
}