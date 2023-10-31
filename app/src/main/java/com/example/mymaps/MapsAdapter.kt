package com.example.mymaps

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymaps.models.UserMap

private const val TAG = "MapsAdapter"
class MapsAdapter(
    val context: Context,
    val userMaps: List<UserMap>,
    val onClickListener: OnClickListener,
    val onLongClickListener: OnLongClickListener,
    val optionsMenuClickListener: OptionsMenuClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnClickListener {
        fun onItemClick(position: Int)
    }

    interface OnLongClickListener {
        fun onItemLongClick(position: Int)
    }

    interface OptionsMenuClickListener {
        fun onOptionsMenuClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_map, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = userMaps.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val userMap = userMaps[position]

        holder.itemView.setOnClickListener {
            Log.i(TAG, "Tapped on $position")
            onClickListener.onItemClick(holder.adapterPosition)

            // Why can't we use position here?
            // When you use local variables like position, the variable must be final and its value cannot be changed. When we call notifyDataSetChanged the position of holder will be changed but the position variable used in the listener function remains the same as it was previously. This is why we should use holder.adapterPosition to get the accurate position when data is changed
        }

        holder.itemView.setOnLongClickListener {
            onLongClickListener.onItemLongClick(holder.adapterPosition)
            return@setOnLongClickListener true
        }

        val textViewOptionsMenu = holder.itemView.findViewById<TextView>(R.id.tvOptionsMenu)
        textViewOptionsMenu.setOnClickListener {
            optionsMenuClickListener.onOptionsMenuClick(holder.adapterPosition)
        }

        val textViewTitle = holder.itemView.findViewById<TextView>(R.id.tvMapTitle)
        val textViewNumMarkers = holder.itemView.findViewById<TextView>(R.id.tvMapNumMarkers)
        textViewTitle.text = userMap.title
        val numMarkers = userMap.places.size
        textViewNumMarkers.text = "$numMarkers ${if (numMarkers > 1) "markers" else "marker"}"
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}
