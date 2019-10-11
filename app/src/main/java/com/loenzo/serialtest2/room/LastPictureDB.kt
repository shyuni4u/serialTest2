package com.loenzo.serialtest2.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.loenzo.serialtest2.util.ioThread

@Database(entities = [LastPicture::class], version = 1)
abstract class LastPictureDB: RoomDatabase() {
    abstract fun lastPictureDao(): LastPictureDao

    companion object {
        private var INSTANCE: LastPictureDB? = null

        fun getInstance(context: Context, title: String = "TEMP"): LastPictureDB? {
            if (INSTANCE == null) {
                synchronized(LastPictureDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, LastPictureDB::class.java, "category.db")
                        .addCallback(object: Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                ioThread {
                                    getInstance(context)?.lastPictureDao()?.insert(LastPicture(title))
                                }
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}