package com.artbirwww.messenger.ui.screens.chat

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    var registeredContacts = mutableStateOf<List<User>>(emptyList())
    var isLoading = mutableStateOf(false)

    fun loadContacts(context: Context) {
        isLoading.value = true
        viewModelScope.launch {
            val devicePhones = fetchDeviceContacts(context)
            val matchedUsers = AuthRepository.getUsersByPhones(devicePhones)
            registeredContacts.value = matchedUsers
            isLoading.value = false
        }
    }

    private fun fetchDeviceContacts(context: Context): List<String> {
        val phones = mutableListOf<String>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                // Normalize: keep only digits. 
                // If it starts with 8, replace with 7 (specific to Russia/CIS if needed, but let's be general)
                var normalized = number.replace(Regex("[^\\d]"), "")
                if (normalized.startsWith("8") && normalized.length == 11) {
                    normalized = "7" + normalized.substring(1)
                }
                if (normalized.isNotEmpty()) {
                    phones.add(normalized)
                }
            }
        }
        return phones.distinct()
    }
}
