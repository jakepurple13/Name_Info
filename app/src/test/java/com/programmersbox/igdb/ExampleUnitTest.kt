package com.programmersbox.igdb

import com.programmersbox.igdb.models.BehindTheName
import org.junit.Test

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

        BehindTheName.buildService().getNameFacts("Jacob").subscribe { println(it) }

        /*val f: MutableList<Int> = fixedListOf(5, 1, 2, 3, 4, 5).apply { removeFrom = FixedListLocation.START }

        for(i in 0..10) {
            f+=i
            println(f)
        }*/


    }
}