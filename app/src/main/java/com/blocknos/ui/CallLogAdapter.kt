package com.blocknos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blocknos.R
import com.blocknos.data.BlockedCallLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallLogAdapter : ListAdapter<BlockedCallLogEntity, CallLogAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())

        private val DIFF = object : DiffUtil.ItemCallback<BlockedCallLogEntity>() {
            override fun areItemsTheSame(a: BlockedCallLogEntity, b: BlockedCallLogEntity) = a.id == b.id
            override fun areContentsTheSame(a: BlockedCallLogEntity, b: BlockedCallLogEntity) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNumber: TextView = view.findViewById(R.id.textLogNumber)
        val textReason: TextView = view.findViewById(R.id.textLogReason)
        val textTime: TextView = view.findViewById(R.id.textLogTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.textNumber.text = item.phoneNumber
        holder.textReason.text = item.reason
        holder.textTime.text = DATE_FORMAT.format(Date(item.timestamp))
    }
}
