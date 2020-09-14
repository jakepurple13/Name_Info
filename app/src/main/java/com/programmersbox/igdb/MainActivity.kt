package com.programmersbox.igdb

import android.Manifest
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.*
import com.programmersbox.igdb.models.Ify
import com.programmersbox.igdb.models.IfyInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private val ifyService by lazy { Ify() }
    private val adapter by lazy { CountryAdapter(this) }
    private val uiSubject = PublishSubject.create<IfyInfo>()
    private val fixedCacheList by lazy { FixedList<IfyInfo>(10) }
    private val recentAdapter by lazy { RecentAdapter(this, uiSubject) }
    private val dataStore = createDataStore(name = defaultSharedPrefName)
    private val cachedInfoList = dataStore.data
        .map { it[preferencesKey<String>("cachedInfo")]?.fromJson<List<IfyInfo>>() }
        .filterNotNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking { dataStore.data.first()[preferencesKey<String>("cachedInfo")]?.fromJson<List<IfyInfo>>()?.let { fixedCacheList.addAll(it) } }

        cachedInfoList.collectOnUi {
            recentAdapter.setListNotify(it)
        }

        genderChart.setProgressTextAdapter { "${it.roundToInt()}%" }

        countryRv.adapter = adapter
        recentList.adapter = recentAdapter

        recentAdapter.addItems(fixedCacheList)

        enterName.editText
            ?.editorActionEvents()
            ?.filter { it.actionId() == EditorInfo.IME_ACTION_DONE }
            ?.filter { it.view().text.isNotEmpty() }
            ?.subscribe { getInfoButton.performClick() }
            ?.addTo(disposable)

        getInfoButton
            .clicks()
            .map { enterName.editText?.text?.toString() }
            .map { it.capitalize(Locale.getDefault()) }
            .distinctUntilChanged()
            .subscribe { name ->
                enterName.editText?.hideKeyboard()
                requestPermissions(Manifest.permission.INTERNET) {
                    if (it.isGranted) {
                        name?.let { it1 ->
                            infoLayout.invisible()
                            loading.visible()
                            fixedCacheList.find { info -> info.name == name }
                                ?.let(uiSubject::onNext) ?: ifyService
                                .getIfyInfo(it1)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError { t ->
                                    t.printStackTrace()
                                    Snackbar.make(
                                        recentList,
                                        "Something went wrong. Please connect to wifi or wait until tomorrow",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                .map(List<IfyInfo>::first)
                                .subscribe(uiSubject::onNext)
                                .addTo(disposable)
                        }
                    }
                }
            }
            .addTo(disposable)

        uiSubject
            .subscribe {
                if (it !in fixedCacheList) fixedCacheList.add(0, it)
                GlobalScope.launch {
                    dataStore.edit { e -> e[preferencesKey<String>("cachedInfo")] = fixedCacheList.distinctBy(IfyInfo::name).toJson() }
                }
            }
            .addTo(disposable)

        uiSubject
            .subscribe {
                infoLayout.visible()
                loading.gone()
                it.gender?.let { gender ->
                    genderName.text = gender.gender.capitalize(Locale.getDefault())
                    genderChart.setCurrentProgress(gender.probability.toDouble() * 100)
                    gender.genderColor
                        ?.let(this::getColor)
                        ?.let { it1 ->
                            genderChart.setGradient(CircularProgressIndicator.RADIAL_GRADIENT, it1)
                            genderChart.textColor = it1
                        }

                    gender.genderColorInverse
                        ?.let(this::getColor)
                        ?.let { it1 -> genderChart.dotColor = it1 }
                }

                nameInfo.text = it.name.capitalize(Locale.getDefault())
                ageInfo.text = getString(R.string.ageItem, it.age)

                adapter.setListNotify(it.nationality)

            }
            .addTo(disposable)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}