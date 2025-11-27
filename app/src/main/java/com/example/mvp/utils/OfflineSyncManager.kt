package com.example.mvp.utils

import android.content.Context
import com.example.mvp.data.Ticket
import com.example.mvp.data.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SyncItem(
    val id: String,
    val type: SyncType,
    val data: Any, // Ticket, Job, etc.
    val action: SyncAction,
    val timestamp: Long,
    val retryCount: Int = 0
)

enum class SyncType {
    TICKET, JOB, MESSAGE, DOCUMENT
}

enum class SyncAction {
    CREATE, UPDATE, DELETE
}

object OfflineSyncManager {
    private val _syncQueue = MutableStateFlow<List<SyncItem>>(emptyList())
    val syncQueue: StateFlow<List<SyncItem>> = _syncQueue.asStateFlow()
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _syncInProgress = MutableStateFlow(false)
    val syncInProgress: StateFlow<Boolean> = _syncInProgress.asStateFlow()
    
    fun addToQueue(item: SyncItem) {
        val currentQueue = _syncQueue.value.toMutableList()
        // Check if item already exists (same id and type)
        val existingIndex = currentQueue.indexOfFirst { 
            it.id == item.id && it.type == item.type 
        }
        
        if (existingIndex >= 0) {
            // Replace existing item
            currentQueue[existingIndex] = item
        } else {
            // Add new item
            currentQueue.add(item)
        }
        
        _syncQueue.value = currentQueue
    }
    
    fun removeFromQueue(itemId: String, type: SyncType) {
        _syncQueue.value = _syncQueue.value.filterNot { 
            it.id == itemId && it.type == type 
        }
    }
    
    fun markAsSynced(itemId: String, type: SyncType) {
        removeFromQueue(itemId, type)
    }
    
    fun setOnlineStatus(isOnline: Boolean) {
        _isOnline.value = isOnline
    }
    
    fun setSyncInProgress(inProgress: Boolean) {
        _syncInProgress.value = inProgress
    }
    
    fun getPendingItemsCount(): Int {
        return _syncQueue.value.size
    }
    
    fun clearQueue() {
        _syncQueue.value = emptyList()
    }
    
    fun incrementRetryCount(itemId: String, type: SyncType) {
        _syncQueue.value = _syncQueue.value.map { item ->
            if (item.id == itemId && item.type == type) {
                item.copy(retryCount = item.retryCount + 1)
            } else {
                item
            }
        }
    }
    
    fun getItemsExceedingMaxRetries(maxRetries: Int = 5): List<SyncItem> {
        return _syncQueue.value.filter { it.retryCount >= maxRetries }
    }
}

