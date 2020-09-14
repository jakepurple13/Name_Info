package com.programmersbox.igdb

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.igdb.databinding.RecentListItemBinding
import com.programmersbox.igdb.models.IfyInfo
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt

class RecentAdapter(private val context: Context, private val subject: PublishSubject<IfyInfo>) : DragSwipeAdapter<IfyInfo, RecentHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
        RecentHolder(RecentListItemBinding.inflate(context.layoutInflater, parent, false))

    override fun RecentHolder.onBind(item: IfyInfo, position: Int) = bind(item, subject)

    override fun addItem(item: IfyInfo, position: Int) {
        dataList.find { it.name == item.name } ?: super.addItem(item, 0)
        while (itemCount > 10) {
            removeItem(dataList.lastIndex)
        }
    }

}

class RecentHolder(private val binding: RecentListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: IfyInfo, subject: PublishSubject<IfyInfo>) {
        binding.recentGenderChart.setProgressTextAdapter { "${it.roundToInt()}%" }
        binding.info = item
        binding.executePendingBindings()
        item.gender?.let { gender ->
            binding.recentGenderChart.setCurrentProgress(gender.probability.toDouble() * 100)
            gender.genderColor
                ?.let(binding.root.context::getColor)
                ?.let { color ->
                    binding.recentGenderChart.progressColor = color
                    binding.recentGenderChart.textColor = color
                }

            gender.genderColorInverse
                ?.let(binding.root.context::getColor)
                ?.let(binding.recentGenderChart::setDotColor)
        }
        binding.recentAge.text = binding.root.context.getString(R.string.ageItem, item.age)
        binding.root.setOnClickListener { subject.onNext(item) }
    }
}