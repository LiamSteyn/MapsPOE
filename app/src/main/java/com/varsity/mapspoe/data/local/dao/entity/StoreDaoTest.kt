package com.varsity.mapspoe.data.local.dao.entity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class StoreDaoTest {

    @get:Rule val instant = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: StoreDAO

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.storeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQuery() = runBlocking {
        val s = StoreEntity(
            id = "s1",
            name = "Test",
            lat = 0.0,
            lon = 0.0,
            address = "addr",
            phone = null,
            openHours = null,
            ratingAvg = 4.0,
            ratingCount = 10,
            googlePlaceId = null
        )
        dao.upsert(s)
        val list = dao.getStores().first()
        Assert.assertEquals(1, list.size)
        Assert.assertEquals("s1", list.first().id)
    }
}
