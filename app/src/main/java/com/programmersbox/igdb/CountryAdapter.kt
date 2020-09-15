package com.programmersbox.igdb

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.programmersbox.dragswipe.DragSwipeAdapter
import com.programmersbox.helpfulutils.layoutInflater
import com.programmersbox.helpfulutils.startDrawable
import com.programmersbox.igdb.databinding.CountryItemBinding
import com.programmersbox.igdb.models.Country
import com.programmersbox.thirdpartyutils.getPalette
import com.programmersbox.thirdpartyutils.into
import kotlin.math.roundToInt

class CountryAdapter(private val context: Context) : DragSwipeAdapter<Country, CountryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryHolder =
        CountryHolder(CountryItemBinding.inflate(context.layoutInflater, parent, false))

    override fun CountryHolder.onBind(item: Country, position: Int) = bind(item)
}

class CountryHolder(private val binding: CountryItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Country) {
        binding.countryChart.setCurrentProgress(item.probability.toDouble() * 100)
        binding.country = item
        binding.executePendingBindings()
        binding.countryChart.setProgressTextAdapter { "${it.roundToInt()}%" }
        Glide.with(binding.countryName)
            .asDrawable()
            .load(item.flagUrl)
            .into<Drawable> {
                resourceReady { image, _ ->
                    binding.countryName.startDrawable = image
                    image.getPalette().let {
                        it.vibrantSwatch?.rgb?.let { it1 -> binding.countryChart.progressColor = it1 }
                        it.dominantSwatch?.rgb?.let { it1 -> binding.countryChart.dotColor = it1 }
                    }
                }
            }
    }

}
