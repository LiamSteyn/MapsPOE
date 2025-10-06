package com.varsity.mapspoe.data.local.dao.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StoreEntity::class, ReviewEntity::class],
    version = 2, // bump if you just added googlePlaceId
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDAO
    abstract fun reviewDao(): ReviewDAO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mapspoe.db"
                )
                    .addCallback(SeedCallback())
                    .fallbackToDestructiveMigration() // fine for Part-1
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // actual insert done via seedDatabase() after instance exists
        }
    }
}

/** One-time seed; safe to call multiple times */
fun seedDatabase(db: AppDatabase) {
    val stores = listOf(
        StoreEntity("st_1", "Green Leaf Dispensary", -33.9249, 18.4241, "123 Long St, Cape Town", "+27 21 000 0001", "09:00-20:00", 4.5, 128, null),
        StoreEntity("st_2", "Table Mountain Wellness", -33.9628, 18.4098, "456 Kloof Nek Rd, Cape Town", "+27 21 000 0002", "10:00-18:00", 4.2, 74, null),
        StoreEntity("st_3", "Sea Point Botanicals", -33.9200, 18.3870, "789 Main Rd, Sea Point", "+27 21 000 0003", "08:00-22:00", 4.8, 203, null)
    )
    val now = System.currentTimeMillis()
    val reviews = listOf(
        ReviewEntity("rv_1", "st_1", "Zanele", 5, "Friendly staff, fast service.", now - 86_400_000L),
        ReviewEntity("rv_2", "st_1", "Kyle", 4, "Good quality, slightly pricey.", now - 172_800_000L),
        ReviewEntity("rv_3", "st_2", "Amahle", 5, "Great selection!", now - 259_200_000L),
        ReviewEntity("rv_4", "st_2", "Leo", 3, "Okay experience.", now - 345_600_000L),
        ReviewEntity("rv_5", "st_3", "Naledi", 5, "Best in the area.", now - 432_000_000L),
        ReviewEntity("rv_6", "st_3", "Josh", 4, "Nice deals on weekends.", now - 518_400_000L)
    )

    CoroutineScope(Dispatchers.IO).launch {
        db.storeDao().upsertAll(stores)
        db.reviewDao().upsertAll(reviews)
    }
}
