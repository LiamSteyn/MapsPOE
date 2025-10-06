package com.varsity.mapspoe.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.varsity.mapspoe.data.local.dao.entity.ReviewDAO
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.data.local.dao.entity.ReviewEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*

class ReviewDaoTest {

    @get:Rule val instant = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: ReviewDAO

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.reviewDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryByStore() = runBlocking {
        val r1 = ReviewEntity("r1", "s1", "Alex", 5, "Nice", 1L)
        val r2 = ReviewEntity("r2", "s2", "Bo", 4, "Ok", 2L)

        dao.upsertAll(listOf(r1, r2))

        val s1 = dao.getByStore("s1").first()
        Assert.assertEquals(1, s1.size)
        Assert.assertEquals("r1", s1.first().id)
    }
}
