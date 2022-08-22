package io.github.emaccaull.hotmic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AudioDeviceArrayAdapter(context: Context) :
    ArrayAdapter<AudioDevice>(context, R.layout.audio_device_item) {

    private val inflater = LayoutInflater.from(context)

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.audio_device_item, parent, false)

        val device = getItem(position)
        val nameView = view.findViewById<TextView>(R.id.device_name)
        nameView.text = device!!.name

        return view
    }
}
