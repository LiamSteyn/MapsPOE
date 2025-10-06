package com.varsity.mapspoe.di

import android.content.Context

/**
 * Thin wrapper so any existing calls to ServiceLocator keep working.
 * It just makes sure the singleton DataRepository is initialized and returns it.
 */
object ServiceLocator {
    fun provideRepository(ctx: Context): com.varsity.mapspoe.data.DataRepository {
        com.varsity.mapspoe.data.DataRepository.init(ctx.applicationContext)
        return com.varsity.mapspoe.data.DataRepository
    }
    fun enableErrorSimulation(enable: Boolean) {
        com.varsity.mapspoe.data.DataRepository.enableErrorSimulation(enable)
    }
}
