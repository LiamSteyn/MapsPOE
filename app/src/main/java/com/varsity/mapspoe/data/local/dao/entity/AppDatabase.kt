package com.varsity.mapspoe.data.local.dao.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.varsity.mapspoe.core.AppScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [StoreEntity::class, ReviewEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDAO
    abstract fun reviewDao(): ReviewDAO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(
            context: Context,
            appScope: CoroutineScope = AppScopes.io
        ): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mapspoe.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { instance ->
                                appScope.launch {
                                    seedDatabase(instance)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

/** One-time seed; safe to call multiple times */
suspend fun seedDatabase(db: AppDatabase) {
    val stores = listOf(
        // The 5 Cape Town cannabis stores requested
        StoreEntity("st_ck","CannaKingdom",-33.917,18.470,"53 Section St, Paarden Eiland, Cape Town", null, "09:00-20:00", null, null, null),
        StoreEntity("st_baked","Baked",-33.948,18.476,"87 Durban Rd, Mowbray, Cape Town", null, "10:00-20:30", null, null, null),
        StoreEntity("st_ca","CannaAfrica",-34.001,18.468,"Penny Lane, Wynberg Upper, Cape Town", null, "10:00-19:00", null, null, null),
        StoreEntity("st_ag","Alpha Gram",-33.800,18.517,"Unit 5A Leonardo Park, Parklands / 10 Blaauberg Rd, Table View", null, "09:00-21:00", null, null, null),
        StoreEntity("st_rt","Rooftop420",-33.932,18.413,"7 Beckham St, Gardens, Cape Town", null, "08:00-22:00", null, null, null)
    )
    val now = System.currentTimeMillis()
    val reviews = listOf(
        ReviewEntity("rv_seed_1", "st_ck", "System", 5, "Welcome to CannaKingdom!", now),
        ReviewEntity("rv_seed_2", "st_baked", "System", 5, "Welcome to Baked!", now),
        ReviewEntity("rv_seed_3", "st_ca", "System", 5, "Welcome to CannaAfrica!", now),
        ReviewEntity("rv_seed_4", "st_ag", "System", 5, "Welcome to Alpha Gram!", now),
        ReviewEntity("rv_seed_5", "st_rt", "System", 5, "Welcome to Rooftop420!", now)
    )

    db.storeDao().upsertAll(stores)
    db.reviewDao().upsertAll(reviews)
}