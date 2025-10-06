package com.varsity.mapspoe.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppScopes {
    // One IO scope for the whole app. No Activity/Fragment leaks.
    val io: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
}