package com.project.lagidimana.presentation

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.text.bold
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.lagidimana.R
import com.project.lagidimana.changeDateFormat
import com.project.lagidimana.databinding.ItemLocationDataBinding
import com.project.lagidimana.presentation.model.LocationLog

class LocationLogAdapter(
    private val context: Context,
    private val onMapsButtonClicked: (Double, Double) -> Unit
): RecyclerView.Adapter<LocationLogAdapter.ViewHolder>() {

    private val datas: MutableList<LocationLog> = mutableListOf()

    class ViewHolder(private val binding: ItemLocationDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, data: LocationLog, onMapsButtonClicked: (Double, Double) -> Unit) {
            with(binding){
                btnMaps.setOnClickListener {
                    onMapsButtonClicked.invoke(data.latitude, data.longitude)
                }
                tvTime.text = SpannableStringBuilder()
                    .bold {
                        append(
                            if(data.isOnline)
                                "Online, "
                            else
                                "Offline, "
                        )
                    }
                    .append(data.time.changeDateFormat())
                tvLatitude.text = context.getString(R.string.format_latitude, data.latitude)
                tvLongitude.text = context.getString(R.string.format_longitude, data.longitude)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]
        holder.bind(context, data, onMapsButtonClicked)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun setData(locationList: List<LocationLog>) {
        val diffCallback = LogDiffCallback(this.datas, locationList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.datas.clear()
        this.datas.addAll(locationList)
        diffResult.dispatchUpdatesTo(this)
    }

    class LogDiffCallback(private val oldList: List<LocationLog>, private val newList: List<LocationLog>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].time === newList[newItemPosition].time
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val (_, value, name) = oldList[oldPosition]
            val (_, value1, name1) = newList[newPosition]

            return name == name1 && value == value1
        }

        @Nullable
        override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
            return super.getChangePayload(oldPosition, newPosition)
        }
    }
}