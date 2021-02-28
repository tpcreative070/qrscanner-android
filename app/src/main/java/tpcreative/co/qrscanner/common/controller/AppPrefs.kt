package co.tpcreative.supersafe.common.controller
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AppPrefs {
    const val key_password_default = "tpcreative.co"
    lateinit var encryptedPrefs: Prefs
    lateinit var prefs: Prefs
    fun initEncryptedPrefs(context: Context) {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS+key_password_default)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        encryptedPrefs =
                Prefs(
                        "${context.packageName}_ENCRYPTED_PREFS",
                        context,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
    }
    fun initPrefs(context: Context) {
        prefs = Prefs("${context}_PREFS", context)
    }
}