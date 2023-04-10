package tpcreative.co.qrscanner.ui.scanner.cpp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import java.io.IOException


class BeepManager(activity: Activity) {
    private val context: Context

    init {
        activity.volumeControlStream = AudioManager.STREAM_MUSIC

        // We do not keep a reference to the Activity itself, to prevent leaks
        context = activity.applicationContext
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun playBeepSoundAndVibrate() {
        if (Utils.getBeep()) {
            playBeepSound()
        }
        if (Utils.getVibrate()) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        300,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        300,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(300)
            }
        }
    }

    private fun playBeepSound() {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
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
