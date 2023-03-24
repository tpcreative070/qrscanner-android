package tpcreative.co.qrscanner.ui.scanner.cpp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Vibrator
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import java.io.IOException

class BeepManager(activity: Activity) {
    private val context: Context

    /**
     * Call updatePrefs() after setting this.
     *
     * If the device is in silent mode, it will not beep.
     *
     * @param beepEnabled true to enable beep
     */
    var isBeepEnabled = true

    /**
     * Call updatePrefs() after setting this.
     *
     * @param vibrateEnabled true to enable vibrate
     */
    var isVibrateEnabled = false

    init {
        activity.volumeControlStream = AudioManager.STREAM_MUSIC

        // We do not keep a reference to the Activity itself, to prevent leaks
        context = activity.applicationContext
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun playBeepSoundAndVibrate() {
        if (isBeepEnabled) {
            playBeepSound()
        }
        if (isVibrateEnabled) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(VIBRATE_DURATION)
        }
    }

    fun playBeepSound() {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val initVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercent = (initVolume.toFloat() / maxVolume * 100).toInt()
        mediaPlayer.setOnCompletionListener { mp ->
            mp.stop()
            mp.release()
        }
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Utils.Log(TAG, "Failed to beep $what, $extra")
            // possibly media player error, so release and recreate
            mp.stop()
            mp.release()
            true
        }
        try {
            val file = context.resources.openRawResourceFd(R.raw.zxing_beep)
            try {
                mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
            } finally {
                file.close()
            }
            Utils.Log(TAG, "Volume $volumePercent")
            if (volumePercent > BEEP_VOLUME) {
                mediaPlayer.setVolume(volumePercent.toFloat(), volumePercent.toFloat())
            } else {
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
            }
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (ioe: IOException) {
            Utils.Log(TAG, ioe.message)
            mediaPlayer.release()
        }
    }

    companion object {
        private val TAG = BeepManager::class.java.simpleName
        private const val BEEP_VOLUME = 5f
        private const val VIBRATE_DURATION = 200L
    }
}
