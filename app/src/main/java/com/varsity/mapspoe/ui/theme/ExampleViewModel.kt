package com.varsity.mapspoe.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.varsity.mapspoe.core.DataResult
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.di.ServiceLocator
import com.varsity.mapspoe.domain.Review
import com.varsity.mapspoe.domain.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExampleViewModel(app: Application) : AndroidViewModel(app) {

    // Get the singleton repo (ServiceLocator ensures it's initialized)
    private val repo: DataRepository = ServiceLocator.provideRepository(app)

    // Room-backed stream of stores
    val stores: StateFlow<List<Store>> =
        repo.observeStores() // <- use the flow from DataRepository
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // One-shot store fetch wrapped in DataResult
    fun loadStore(storeId: String): Flow<DataResult<Store>> = flow {
        emit(repo.getStore(storeId))
    }

    // Stream reviews for the selected store
    fun observeReviews(storeId: String) {
        viewModelScope.launch {
            repo.observeReviews(storeId).collect { _reviews.value = it }
        }
    }

    // Try to refresh from the fake/remote service; UI remains stable on failure
    fun refreshReviews(storeId: String, onDone: (DataResult<Unit>) -> Unit) {
        viewModelScope.launch {
            val r = repo.refreshReviews(storeId)
            onDone(r)
        }
    }
}
