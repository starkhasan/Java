package com.example.musicservice.activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicservice.R
import com.example.musicservice.adapter.MusicAdapter
import com.example.musicservice.response.AudioModel
import com.example.musicservice.util.MusicService
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
class MainActivity : AppCompatActivity(),MusicService.Callbacks {
    var mService: MusicService? = null
    var mBound = false
    var isStart = false
    val PERMISSION_CODE = 1
    var musicPosition = 0
    var musicAdapter: MusicAdapter?=null
    val mediaList = ArrayList<AudioModel>()
    var isLoop = false
    var isDirect = false
    private val listener = MyBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        getPermission()
        LocalBroadcastManager.getInstance(this).registerReceiver(listener, IntentFilter("FINISHED"))
        ivPlay.setOnClickListener {
            if(mBound){
                if(isStart){
                    ivPlay.setImageResource(R.drawable.icon_play)
                    isStart = false
                    mService!!.mediaPlayer!!.pause()
                }else{
                    ivPlay.setImageResource(R.drawable.icon_pausee)
                    isStart = true
                    mService!!.mediaPlayer!!.start()
                }
            }else{
                musicPosition = 0
                musicIntent(0)
            }
        }

        ivNext.setOnClickListener {
            if(mBound){
                if(mService!!.mediaPlayer!=null){
                    mService!!.mediaPlayer!!.release()
                }
                if(musicPosition+1 < mediaList.size){
                    musicPosition = musicPosition + 1
                    musicIntent(musicPosition)
                }else{
                    musicPosition = 0
                    musicIntent(musicPosition)
                }
            }else{
                musicPosition = 0
                musicIntent(0)
            }
        }

        ivPrev.setOnClickListener {
            if(mBound){
                if(mService!!.mediaPlayer!=null){
                    mService!!.mediaPlayer!!.release()
                }
                if(musicPosition-1 < 0){
                    musicPosition = mediaList.size-1
                    musicIntent(musicPosition)
                }else{
                    musicPosition = musicPosition - 1
                    musicIntent(musicPosition)
                }
            }else{
                musicPosition = 0
                musicIntent(0)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(mBound) {
                    if (mService!!.mediaPlayer != null && mService!!.mediaPlayer!!.isPlaying()) {
                        mService!!.mediaPlayer!!.seekTo(seekBar!!.progress);
                    }
                }else{
                    seekBar?.setProgress(0)
                    val snackbar = Snackbar.make(coordinatorLayout,"Please Start Music",Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
            }
        })

        ivRepeat.setOnClickListener{
            if(mBound){
                if(isLoop){
                    isLoop = false
                    ivRepeat.setImageResource(R.drawable.icon_no_repeat)
                    mService!!.mediaPlayer!!.isLooping = false
                }else{
                    isLoop = true
                    ivRepeat.setImageResource(R.drawable.icon_repeat)
                    mService!!.mediaPlayer!!.isLooping = true
                }
            }else{
                if(isDirect){
                    isDirect = false
                    isLoop = false
                    ivRepeat.setImageResource(R.drawable.icon_no_repeat)
                }else {
                    isDirect = true
                    isLoop = true
                    ivRepeat.setImageResource(R.drawable.icon_repeat)
                }
            }
        }

        ivVideo.setOnClickListener {
            startActivity(Intent(this,VideoPlayerActivity::class.java))
            val intent = Intent(this, MusicService::class.java)
            try {
                unbindService(mConnection)
                stopService(intent)
                mBound = false
                ivPlay.setImageResource(R.drawable.icon_play)
                for(i in 0..mediaList.size-1){
                    mediaList[i].isPlaying = false
                }
                musicAdapter!!.notifyDataSetChanged()
                seekBar.setProgress(0)
                tvStartTime.text = "0:00"
                tvtotalTime.text = "0:00"
            }catch (e:Exception){}
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listener)
        super.onDestroy()
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent){
            when (intent.action) {
                "FINISHED" -> {
                    if (mService!!.mediaPlayer != null) {
                        mService!!.mediaPlayer!!.release()
                    }
                    if (musicPosition + 1 < mediaList.size) {
                        musicPosition = musicPosition + 1
                        musicIntent(musicPosition)
                    } else {
                        musicPosition = 0
                        musicIntent(musicPosition)
                    }
                }
                else -> Toast.makeText(context, "Couldn't Found", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity@this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getMediaFile()
        }else{
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMediaFile()
                } else {
                    Toast.makeText(applicationContext, "Grant Permission for better Result", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getMediaFile(){
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = applicationContext.contentResolver.query(uri, null, null, null, null)
        mediaList.clear()
        while (cursor!!.moveToNext()){
            val title: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val artist: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val duration: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            val url: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            val modelAudio = AudioModel(title, url, duration, artist,false)
            mediaList.add(modelAudio)
        }

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMusic.layoutManager = linearLayoutManager
        musicAdapter = MusicAdapter(this, mediaList){ position: Int, uri: String, flag: String ->
            if(flag == "Start"){
                musicPosition = position
                if(!isStart){
                    musicIntent(position)
                }else{
                    if(mService!!.mediaPlayer!=null){
                        mService!!.mediaPlayer!!.release()
                        musicIntent(position)
                    }
                }
            }
        }
        rvMusic.adapter = musicAdapter
    }

    val mConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            mService = binder.getService()
            mService!!.registerClient(this@MainActivity)
            if(!mBound){
                mBound = true
                mService!!.doUpdateClient()
            }
            if(isDirect) {
                isDirect = false
                mService!!.mediaPlayer!!.isLooping = true
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(applicationContext,"Service UnRegistered",Toast.LENGTH_SHORT).show()
            mBound = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    fun musicIntent(position: Int){
        if(isLoop && !isDirect){
            isLoop = false
            ivRepeat.setImageResource(R.drawable.icon_no_repeat)
        }
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("Uri", mediaList[position].uri)
        intent.putExtra("position",musicPosition)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
        ivPlay.setImageResource(R.drawable.icon_pausee)
        isStart = true
        for(i in 0..mediaList.size-1){
            if(i==position)
                mediaList[i].isPlaying = true
            else
                mediaList[i].isPlaying = false
        }
        musicAdapter!!.notifyDataSetChanged()
    }

    override fun updateClient() {
        if(mBound){
            val thread = Thread{
                var currentPosition = mService!!.mediaPlayer!!.currentPosition
                val total = mService!!.mediaPlayer!!.duration
                while(currentPosition<total){
                    try {
                        Thread.sleep(1000)
                        currentPosition = mService!!.mediaPlayer!!.currentPosition
                        //println("$currentPosition has run.")
                        setText(tvStartTime, currentPosition)
                    }catch (e: Exception){
                        return@Thread
                    }
                }
            }
            thread.start()
        }
    }

    private fun setText(text: TextView, mills: Int) {
        val value = milliSecondsToTimer(mills.toLong())
        runOnUiThread {
            seekBar.max = mService!!.mediaPlayer!!.duration
            seekBar.setProgress(mService!!.mediaPlayer!!.currentPosition)
            tvtotalTime.text = milliSecondsToTimer(mService!!.mediaPlayer!!.duration.toLong())
            text.text = value
        }
    }


    fun milliSecondsToTimer(milliseconds: Long): String? {
        var finalTimerString = ""
        var secondsString = ""
        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"
        return finalTimerString
    }

    /*
    For Stoping the Service but the onDestroy() method will be called from the Service Class
    Intent myService = new Intent(MainActivity.this, BackgroundSoundService.class);
    stopService(myService);
    */
}