package tpcreative.co.qrscanner.common.entities

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Ignore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.HistoryEntityModel
import tpcreative.co.qrscanner.model.SaveEntityModel
import java.util.*

@Database(entities = [HistoryEntity::class, SaveEntity::class], version = 4, exportSchema = false)
abstract class InstanceGenerator : RoomDatabase() {
    @Ignore
    abstract fun historyDao(): HistoryDao?
    @Ignore
    abstract fun saveDao(): SaveDao?
    fun onInsert(cTalkManager: HistoryEntityModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance.historyDao().insert(HistoryEntity(cTalkManager))
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
    }

    fun getHistoryList(): MutableList<HistoryEntityModel?>? {
        try {
            val mValue = instance.historyDao().loadAll()
            val mList: MutableList<HistoryEntityModel?> = ArrayList()
            for (index in mValue) {
                val item = HistoryEntityModel(index)
                if (item.uuId == null) {
                    item.uuId = Utils.getUUId()
                    SQLiteHelper.getInstance().onUpdate(item)
                }
                mList.add(item)
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, e.message)
        }
        return null
    }

    fun getHistoryList(isSync: Boolean): MutableList<HistoryEntityModel?>? {
        try {
            val mValue = instance.historyDao().loadAll(isSync)
            val mList: MutableList<HistoryEntityModel?> = ArrayList()
            for (index in mValue) {
                val item = HistoryEntityModel(index)
                if (item.uuId == null) {
                    item.uuId = Utils.getUUId()
                    SQLiteHelper.getInstance().onUpdate(item)
                }
                mList.add(item)
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, e.message)
        }
        return null
    }

    fun onDelete(entity: HistoryEntityModel?): Boolean {
        try {
            instance.historyDao().delete(HistoryEntity(entity))
            return true
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }

    fun onDeleteHistorySpecific(uuId: String?): Boolean {
        try {
            instance.historyDao().deleteSpecific(uuId)
            return true
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }

    fun onInsert(cTalkManager: SaveEntityModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance.saveDao().insert(SaveEntity(cTalkManager))
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
    }

    fun onUpdate(cTalkManager: SaveEntityModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance.saveDao().update(SaveEntity(cTalkManager))
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
    }

    fun onUpdate(cTalkManager: HistoryEntityModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance.historyDao().update(HistoryEntity(cTalkManager))
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
    }

    fun getSaveList(): MutableList<SaveEntityModel?>? {
        try {
            val mValue = instance.saveDao().loadAll()
            val mList: MutableList<SaveEntityModel?> = ArrayList()
            for (index in mValue) {
                val item = SaveEntityModel(index)
                if (item.uuId == null) {
                    item.uuId = Utils.getUUId()
                    SQLiteHelper.getInstance().onUpdate(item)
                }
                mList.add(item)
            }
            return mList
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return null
    }

    fun getSaveList(isSynced: Boolean): MutableList<SaveEntityModel?>? {
        try {
            val mValue = instance.saveDao().loadAll(isSynced)
            val mList: MutableList<SaveEntityModel?> = ArrayList()
            for (index in mValue) {
                val item = SaveEntityModel(index)
                if (item.uuId == null) {
                    item.uuId = Utils.getUUId()
                    SQLiteHelper.getInstance().onUpdate(item)
                }
                mList.add(item)
            }
            return mList
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return null
    }

    fun getItemByHistory(contentUnique: String?): HistoryEntityModel? {
        try {
            val mResult = instance.historyDao().loadItem(contentUnique)
            if (mResult != null) {
                return HistoryEntityModel(mResult)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return null
    }

    fun getItemBySave(contentUnique: String?): SaveEntityModel? {
        try {
            val mResult = instance.saveDao().loadItem(contentUnique)
            if (mResult != null) {
                return SaveEntityModel(mResult)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return null
    }

    fun onDelete(entity: SaveEntityModel?): Boolean {
        try {
            instance.saveDao().delete(SaveEntity(entity))
            return true
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }

    fun onDeleteSaveSpecific(uuId: String?): Boolean {
        try {
            instance.saveDao().deleteSpecific(uuId)
            return true
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }

    companion object {
        @Ignore
        private var instance: InstanceGenerator? = null

        @Ignore
        val TAG = InstanceGenerator::class.java.simpleName
        val MIGRATION_1_2: Migration? = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase?) {
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'barcodeFormat' TEXT")
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'favorite' INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'updatedDateTime' TEXT")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'barcodeFormat' TEXT")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'favorite' INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'updatedDateTime' TEXT")
            }
        }
        val MIGRATION_2_3: Migration? = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase?) {
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'contentUnique' TEXT")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'contentUnique' TEXT")
            }
        }
        val MIGRATION_3_4: Migration? = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase?) {
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'isSynced' INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE 'save' ADD COLUMN  'uuId' TEXT")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'isSynced' INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE 'history' ADD COLUMN  'uuId' TEXT")
            }
        }

        fun getInstance(context: Context?): InstanceGenerator? {
            if (instance == null) {
                instance = Room.databaseBuilder(context,
                        InstanceGenerator::class.java, context.getString(R.string.database_name))
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .allowMainThreadQueries()
                        .build()
            }
            return instance
        }
    }
}