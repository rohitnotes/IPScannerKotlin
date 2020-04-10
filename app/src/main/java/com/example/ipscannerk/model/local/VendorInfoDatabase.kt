package com.example.ipscannerk.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [VendorInfo::class], version = 1, exportSchema = false)
abstract class VendorInfoDatabase : RoomDatabase() {
    abstract fun vendorInfoDAO(): VendorInfoDAO?

    companion object {
        @Volatile
        private var INSTANCE: VendorInfoDatabase? = null

        @Synchronized
        fun getInstance(context: Context): VendorInfoDatabase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder<VendorInfoDatabase>(
                    context.applicationContext,
                    VendorInfoDatabase::class.java,
                    "kip_scanner"
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE
        }

    }

}