package com.leewilson.movienights.ui.main.feed

import android.text.Html
import android.text.Spannable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.leewilson.movienights.R
import com.leewilson.movienights.databinding.ItemMovienightBinding
import com.leewilson.movienights.model.MovieNight
import com.leewilson.movienights.util.toFormattedString
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class FeedAdapter(
    differCallback: DiffUtil.ItemCallback<MovieNight>
) : PagingDataAdapter<MovieNight, FeedAdapter.MovieNightViewHolder>(differCallback) {

    private val TAG = "FeedAdapter"

    inner class MovieNightViewHolder(
        val binding: ItemMovienightBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieNight: MovieNight) {

            Log.d(TAG, "bind: $movieNight")
            Picasso.get()
                .load(movieNight.imageUrl)
                .placeholder(R.drawable.movie_placeholder)
                .error(R.drawable.movie_placeholder)
                .fit()
                .centerCrop()
                .into(binding.poster)

            val context = binding.root.context
            val hostingMsg = context.resources.getString(R.string.movie_night_hosting, movieNight.hostName)
            val formattedHostingMsg = Html.fromHtml(hostingMsg)
            binding.tvPersonHosting.setText(formattedHostingMsg)
            binding.movieName.text = movieNight.movieName

            val numGuestsString = if (movieNight.guestUids.size == 1)
                context.resources.getString(R.string.movie_night_num_guests_singular)
                else context.resources.getString(R.string.movie_night_num_guests_plural, movieNight.guestUids.size)
            binding.numberGuestsTv.text = numGuestsString

            movieNight.dateOfEvent?.let { timeStamp ->
                Log.d(TAG, "timestamp: $timeStamp")
                val date = Date(timeStamp.seconds * 1000)
                binding.dateTv.text = date.toFormattedString()
            }
        }
    }

    override fun onBindViewHolder(holder: MovieNightViewHolder, position: Int) {
        getItem(position)?.let { movieNight ->
            holder.bind(movieNight)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieNightViewHolder {
        val binding = ItemMovienightBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MovieNightViewHolder(binding)
    }
}