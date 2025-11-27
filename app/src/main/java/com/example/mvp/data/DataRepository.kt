package com.example.mvp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")

class DataRepository(private val context: Context) {
    private val gson = Gson()
    
    // Keys for storing data per user
    private fun ticketsKey(userEmail: String) = stringPreferencesKey("tickets_$userEmail")
    private fun jobsKey(userEmail: String) = stringPreferencesKey("jobs_$userEmail")
    private val currentUserKey = stringPreferencesKey("current_user")
    private val registeredUsersKey = stringPreferencesKey("registered_users")
    
    // Save current user
    suspend fun saveCurrentUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[currentUserKey] = gson.toJson(user)
        }
    }
    
    // Get current user
    suspend fun getCurrentUser(): User? {
        val userJson = context.dataStore.data.first()[currentUserKey] ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveRegisteredUser(credential: LocalCredential) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[registeredUsersKey]
            val type = object : TypeToken<List<LocalCredential>>() {}.type
            val currentList = try {
                gson.fromJson<List<LocalCredential>>(currentJson, type)?.toMutableList()
            } catch (e: Exception) {
                null
            } ?: mutableListOf()

            val existingIndex = currentList.indexOfFirst { it.email.equals(credential.email, ignoreCase = true) }
            if (existingIndex >= 0) {
                currentList[existingIndex] = credential
            } else {
                currentList.add(credential)
            }

            preferences[registeredUsersKey] = gson.toJson(currentList)
        }
    }

    suspend fun getRegisteredUser(email: String): LocalCredential? {
        val usersJson = context.dataStore.data.first()[registeredUsersKey] ?: return null
        return try {
            val listType = object : TypeToken<List<LocalCredential>>() {}.type
            val users = gson.fromJson<List<LocalCredential>>(usersJson, listType) ?: return null
            users.firstOrNull { it.email.equals(email, ignoreCase = true) }
        } catch (e: Exception) {
            null
        }
    }
    
    // Save tickets for a specific user
    // For landlords: saves all tickets
    // For tenants: saves only their own tickets
    suspend fun saveTickets(userEmail: String, tickets: List<Ticket>) {
        context.dataStore.edit { preferences ->
            // Get all existing tickets from all users
            val allExistingTickets = mutableMapOf<String, Ticket>()
            
            preferences.asMap().forEach { (key, value) ->
                if (key.name.startsWith("tickets_")) {
                    try {
                        val listType = object : TypeToken<List<Ticket>>() {}.type
                        val userTickets = gson.fromJson<List<Ticket>>(value as String, listType)
                        userTickets?.forEach { ticket ->
                            allExistingTickets[ticket.id] = ticket
                        }
                    } catch (e: Exception) {
                        // Skip invalid entries
                    }
                }
            }
            
            // Update with new tickets (new tickets override old ones with same ID)
            tickets.forEach { ticket ->
                allExistingTickets[ticket.id] = ticket
            }
            
            // Save back to the user's key (for landlords, this contains all tickets)
            preferences[ticketsKey(userEmail)] = gson.toJson(allExistingTickets.values.toList())
        }
    }
    
    // Get tickets for a specific user
    @Suppress("UNUSED")
    suspend fun getTickets(userEmail: String): List<Ticket> {
        val ticketsJson = context.dataStore.data.first()[ticketsKey(userEmail)] ?: return emptyList()
        return try {
            val listType = object : TypeToken<List<Ticket>>() {}.type
            gson.fromJson(ticketsJson, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Save jobs for a specific user (contractor)
    suspend fun saveJobs(userEmail: String, jobs: List<Job>) {
        context.dataStore.edit { preferences ->
            preferences[jobsKey(userEmail)] = gson.toJson(jobs)
        }
    }
    
    // Get jobs for a specific user (contractor)
    @Suppress("UNUSED")
    suspend fun getJobs(userEmail: String): List<Job> {
        val jobsJson = context.dataStore.data.first()[jobsKey(userEmail)] ?: return emptyList()
        return try {
            val listType = object : TypeToken<List<Job>>() {}.type
            gson.fromJson(jobsJson, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get all tickets across all users (for landlords to see all tickets)
    // Deduplicates by ticket ID
    suspend fun getAllTickets(): List<Ticket> {
        val allTicketsMap = mutableMapOf<String, Ticket>()
        val preferences = context.dataStore.data.first()
        
        preferences.asMap().forEach { (key, value) ->
            if (key.name.startsWith("tickets_")) {
                try {
                    val listType = object : TypeToken<List<Ticket>>() {}.type
                    val tickets = gson.fromJson<List<Ticket>>(value as String, listType)
                    tickets?.forEach { ticket ->
                        // Keep the most recent version of each ticket (by ID)
                        allTicketsMap[ticket.id] = ticket
                    }
                } catch (e: Exception) {
                    // Skip invalid entries
                }
            }
        }
        
        return allTicketsMap.values.toList()
    }
    
    // Get all jobs across all users (for contractors to see available jobs)
    suspend fun getAllJobs(): List<Job> {
        val allJobs = mutableListOf<Job>()
        val preferences = context.dataStore.data.first()
        
        preferences.asMap().forEach { (key, value) ->
            if (key.name.startsWith("jobs_")) {
                try {
                    val listType = object : TypeToken<List<Job>>() {}.type
                    val jobs = gson.fromJson<List<Job>>(value as String, listType)
                    allJobs.addAll(jobs ?: emptyList())
                } catch (e: Exception) {
                    // Skip invalid entries
                }
            }
        }
        
        return allJobs
    }
    
    // Properties
    private fun propertiesKey(userEmail: String) = stringPreferencesKey("properties_$userEmail")
    
    suspend fun saveProperties(userEmail: String, properties: List<Property>) {
        context.dataStore.edit { preferences ->
            preferences[propertiesKey(userEmail)] = gson.toJson(properties)
        }
    }
    
    suspend fun getProperties(userEmail: String): List<Property> {
        val propertiesJson = context.dataStore.data.first()[propertiesKey(userEmail)] ?: return emptyList()
        return try {
            val listType = object : TypeToken<List<Property>>() {}.type
            gson.fromJson(propertiesJson, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Maintenance Reminders
    private fun remindersKey(userEmail: String) = stringPreferencesKey("reminders_$userEmail")
    
    suspend fun saveMaintenanceReminders(userEmail: String, reminders: List<MaintenanceReminder>) {
        context.dataStore.edit { preferences ->
            preferences[remindersKey(userEmail)] = gson.toJson(reminders)
        }
    }
    
    suspend fun getMaintenanceReminders(userEmail: String): List<MaintenanceReminder> {
        val remindersJson = context.dataStore.data.first()[remindersKey(userEmail)] ?: return emptyList()
        return try {
            val listType = object : TypeToken<List<MaintenanceReminder>>() {}.type
            gson.fromJson(remindersJson, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Clear user data (on logout)
    @Suppress("UNUSED")
    suspend fun clearUserData(userEmail: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(ticketsKey(userEmail))
            preferences.remove(jobsKey(userEmail))
            preferences.remove(propertiesKey(userEmail))
            preferences.remove(remindersKey(userEmail))
        }
    }
}

