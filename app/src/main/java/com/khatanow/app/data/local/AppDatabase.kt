package com.khatanow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.khatanow.app.data.local.dao.CustomerDao
import com.khatanow.app.data.local.dao.DeviceDao
import com.khatanow.app.data.local.dao.ProductDao
import com.khatanow.app.data.local.dao.TransactionDao
import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.DeviceEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.data.local.entities.TransactionEntity

@Database(
    entities = [
        CustomerEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        DeviceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khata_now_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
