package tpcreative.co.qrscanner.common.services
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.presenter.BaseView
import tpcreative.co.qrscanner.common.presenter.PresenterService
import tpcreative.co.qrscanner.model.EnumStatus
import java.io.File


class QRScannerService : PresenterService<BaseView<*>?>(), QRScannerReceiver.ConnectivityReceiverListener {
    private val mBinder: IBinder = LocalBinder() // Binder given to clients
    private var androidReceiver: QRScannerReceiver? = null
    override fun onCreate() {
        super.onCreate()
        Utils.Log(TAG, "onCreate")
        onInitReceiver()
        QRScannerApplication.getInstance().setConnectivityListener(this)
        QRScannerApplication.getInstance().setRequestClearCacheData(false)
    }

    override fun onActionScreenOff() {

    }

    private fun onInitReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        androidReceiver = QRScannerReceiver()
        registerReceiver(androidReceiver, intentFilter)
        QRScannerApplication.getInstance().setConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        QRScannerApplication.getInstance().onDestroyAllAds()
        if (QRScannerApplication.getInstance().isRequestClearCacheData()){
            this.cacheDir.deleteRecursively()
            this.cacheDir.mkdirs()
        }
        Utils.Log(TAG, "onDestroy")
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver)
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Connected :$isConnected")
        val view: BaseView<*>? = view()
        if (view != null) {
            if (isConnected) {
                view.onSuccessful("Connected network", EnumStatus.CONNECTED)
                ServiceManager.getInstance().onCheckVersion()
            } else {
                view.onSuccessful("Disconnected network", EnumStatus.DISCONNECTED)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we get killed, after returning from here, restart
        Utils.Log(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        val extras: Bundle? = intent?.extras
        Utils.Log(TAG, "onBind")
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra")
        }
        return mBinder
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): QRScannerService? {
            // Return this instance of SignalRService so clients can call public methods
            return this@QRScannerService
        }
    }

    interface ServiceManagerSyncDataListener {
        fun onCompleted()
        fun onError()
        fun onCancel()
    }


    companion object {
        private val TAG = QRScannerService::class.java.simpleName
    }
}