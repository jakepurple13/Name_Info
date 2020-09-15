package com.programmersbox.igdb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.helpfulutils.fixedListOf
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.startDrawable
import com.programmersbox.igdb.databinding.RecentListItemBinding
import com.programmersbox.igdb.models.Country
import com.programmersbox.igdb.models.IfyInfo
import com.programmersbox.thirdpartyutils.into
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToInt

class RecentAdapter(private val context: Context, private val subject: PublishSubject<IfyInfo>) :
    DragSwipeAdapter<IfyInfo, RecentHolder>(fixedListOf(10)) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
        RecentHolder(RecentListItemBinding.inflate(context.layoutInflater, parent, false))

    override fun RecentHolder.onBind(item: IfyInfo, position: Int) = bind(item, subject)

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

class NationalityAdapter(private val context: Context, list: List<Country>) :
    DragSwipeAdapter<Country, NationalityAdapter.NationalityHolder>(list.toMutableList()) {
    class NationalityHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NationalityHolder = NationalityHolder(
        TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_MaterialComponents_Subtitle1)
            textSize = 12f
            gravity = Gravity.CENTER
        }
    )

    @SuppressLint("SetTextI18n")
    override fun NationalityHolder.onBind(item: Country, position: Int) {
        (itemView as? TextView)?.let {
            it.text = "${(item.probability * 100).roundToInt()}%"
            Glide.with(it).asDrawable().load(item.flagUrl).into<Drawable> { resourceReady { image, _ -> it.startDrawable = image } }
        }
    }
}

@BindingAdapter("nationalities")
fun nationalities(view: RecyclerView, list: List<Country>) {
    view.adapter = NationalityAdapter(view.context, list)
}