package com.blocknos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blocknos.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceCallLogEntry(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val duration: Long
)

class DeviceCallLogAdapter(
    private val onBlock: (DeviceCallLogEntry) -> Unit
) : ListAdapter<DeviceCallLogEntry, DeviceCallLogAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

        private val DIFF = object : DiffUtil.ItemCallback<DeviceCallLogEntry>() {
            override fun areItemsTheSame(a: DeviceCallLogEntry, b: DeviceCallLogEntry) =
                a.number == b.number && a.date == b.date
            override fun areContentsTheSame(a: DeviceCallLogEntry, b: DeviceCallLogEntry) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textDeviceName)
        val textNumber: TextView = view.findViewById(R.id.textDeviceNumber)
        val textInfo: TextView = view.findViewById(R.id.textDeviceInfo)
        val buttonBlock: MaterialButton = view.findViewById(R.id.buttonBlockFromLog)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_call_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        if (item.name != null && item.name != item.number) {
            holder.textName.text = item.name
            holder.textName.visibility = View.VISIBLE
            holder.textNumber.text = item.number
        } else {
            holder.textName.visibility = View.GONE
            holder.textNumber.text = item.number
        }

        val typeLabel = when (item.type) {
            android.provider.CallLog.Calls.INCOMING_TYPE -> "Incoming"
            android.provider.CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            android.provider.CallLog.Calls.MISSED_TYPE -> "Missed"
            android.provider.CallLog.Calls.REJECTED_TYPE -> "Rejected"
            android.provider.CallLog.Calls.BLOCKED_TYPE -> "Blocked"
            else -> "Unknown"
        }
        val dateStr = DATE_FORMAT.format(Date(item.date))
        holder.textInfo.text = "$typeLabel \u2022 $dateStr"

        holder.buttonBlock.setOnClickListener { onBlock(item) }
    }
}
