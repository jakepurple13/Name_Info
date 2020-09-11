package com.programmersbox.igdb

import com.programmersbox.igdb.models.BehindTheName
import com.programmersbox.igdb.models.Ify
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        /*val service = Igdb.buildService()
        //service.getList().subscribe { println(it.take(1)) }
        val f = service.getList().blockingFirst()
        f.first().cover?.toString()?.let {
            println(it)
            service.getCovers(it).subscribe { println(it) }
        }

        service.getListWithCover().subscribe { println(it) }*/

        //Igdb.buildService().getCovers().subscribe { println(it.take(1)) }

        /*Ify().getIfyInfo("Jacob").subscribe { println(it) }

        println(Locale.getDefault().country)

        println(Locale.getAvailableLocales().sortedBy { it.displayCountry }.map { "${it.displayCountry} - ${it.country}" })*/

        BehindTheName.buildService("Jacob").subscribe { println(it) }


    }
}