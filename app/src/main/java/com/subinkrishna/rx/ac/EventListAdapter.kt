package com.subinkrishna.rx.ac

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventListAdapter: RecyclerView.Adapter<EventItemViewHolder>() {

  private val events = mutableListOf<Event>()

  fun add(event: String, type: EventType) {
    events.add(events.size, Event(event, type))
    notifyItemInserted(events.size)
  }

  fun clear() {
    events.clear()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
    return EventItemViewHolder.create(parent)
  }

  override fun getItemCount(): Int = events.size

  override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
    holder.bind(events[position])
  }
}

class EventItemViewHolder(val v: TextView): RecyclerView.ViewHolder(v) {

  companion object {
    fun create(parent: ViewGroup): EventItemViewHolder {
      val v = LayoutInflater.from(parent.context)
          .inflate(R.layout.item, parent, false)
      return EventItemViewHolder(v as TextView)
    }
  }

  fun bind(event: Event) {
    val color = if (event.type == EventType.Request) Color.BLACK else Color.BLUE
    v.text = event.label
    v.setTextColor(color)
  }
}

enum class EventType { Request, Response }
data class Event(val label: String, val type: EventType)