package com.example.musicservice.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicservice.R
import com.example.musicservice.activity.MainActivity
import com.example.musicservice.response.AudioModel
import kotlinx.android.synthetic.main.row_music.view.*

class MusicAdapter(val context: MainActivity, val mediaList: ArrayList<AudioModel>,val listener: (Int, String, String) -> Unit) : RecyclerView.Adapter<MusicAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_music,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicAdapter.ViewHolder, position: Int) {
        holder.itemView.tvAlbumName.text = mediaList[position].title
        holder.itemView.llSong.setOnClickListener {
            listener(position, mediaList[position].uri, "Start")
        }
        if (mediaList[position].isPlaying){
            holder.itemView.ivMusic.setImageDrawable(context.getDrawable(R.drawable.icon_music_play))
            holder.itemView.tvAlbumName.setTextColor(Color.parseColor("#1DD05D"))
        }else {
            holder.itemView.ivMusic.setImageDrawable(context.getDrawable(R.drawable.icon_music))
            holder.itemView.tvAlbumName.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}
}