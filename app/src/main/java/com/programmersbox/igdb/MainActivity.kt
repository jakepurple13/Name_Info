package com.programmersbox.igdb

import android.Manifest
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.programmersbox.gsonutils.sharedPrefNotNullObjectDelegate
import com.programmersbox.helpfulutils.*
import com.programmersbox.igdb.models.Ify
import com.programmersbox.igdb.models.IfyInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private val ifyService by lazy { Ify() }
    private val adapter by lazy { CountryAdapter(this) }
    private val uiSubject = PublishSubject.create<IfyInfo>()
    private var cachedInfo by sharedPrefNotNullObjectDelegate(mutableListOf<IfyInfo>())
    private val fixedCacheList by lazy { FixedList(10, c = cachedInfo) }
    private val recentAdapter by lazy { RecentAdapter(this, uiSubject) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                recentAdapter.addItem(it)
                if (it !in fixedCacheList) {
                    fixedCacheList.add(0, it)
                    cachedInfo = fixedCacheList.distinctBy(IfyInfo::name).toMutableList()
                }
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