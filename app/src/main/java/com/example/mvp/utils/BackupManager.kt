package com.example.mvp.utils

import android.content.Context
import com.example.mvp.data.*
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    data class BackupData(
        val version: String = "1.0",
        val timestamp: String,
        val user: User?,
        val tickets: List<Ticket>,
        val jobs: List<Job>,
        val properties: List<Property>,
        val reminders: List<MaintenanceReminder>,
        val notifications: List<Notification>
    )
    
    fun createBackup(
        context: Context,
        user: User?,
        tickets: List<Ticket>,
        jobs: List<Job>,
        properties: List<Property>,
        reminders: List<MaintenanceReminder>,
        notifications: List<Notification>
    ): File? {
        return try {
            val backupData = BackupData(
                timestamp = dateFormat.format(Date()),
                user = user,
                tickets = tickets,
                jobs = jobs,
                properties = properties,
                reminders = reminders,
                notifications = notifications
            )
            
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, "home_backup_${backupData.timestamp}.json")
            FileWriter(backupFile).use { writer ->
                writer.write(gson.toJson(backupData))
            }
            
            backupFile
        } catch (e: Exception) {
            null
        }
    }
    
    fun restoreBackup(context: Context, backupFile: File): BackupData? {
        return try {
            val json = backupFile.readText()
            gson.fromJson(json, BackupData::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getBackupFiles(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists() && backupDir.isDirectory) {
            backupDir.listFiles()?.filter { it.name.endsWith(".json") }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun deleteBackup(backupFile: File): Boolean {
        return try {
            backupFile.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun exportToCloud(context: Context, backupFile: File): Boolean {
        // TODO: Implement cloud backup (Firebase Storage, Google Drive, etc.)
        return false
    }
    
    fun importFromCloud(context: Context, cloudUrl: String): File? {
        // TODO: Implement cloud restore
        return null
    }
}

