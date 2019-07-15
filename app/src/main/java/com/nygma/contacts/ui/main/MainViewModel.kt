package com.nygma.contacts.ui.main

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource

//add feature of checking if the database is dirty
class ContactsViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    lateinit var contactsList: LiveData<PagedList<Contact>>

    fun loadContacts() {
        val config = PagedList.Config.Builder()
            .setPageSize(20)
            .setEnablePlaceholders(false)
            .build()
        contactsList = LivePagedListBuilder(
            ContactsDataSourceFactory(contentResolver), config
        ).build()
    }
}

class ContactsDataSourceFactory(private val contentResolver: ContentResolver) :
    DataSource.Factory<Int, Contact>() {

    override fun create(): DataSource<Int, Contact> {
        return ContactsDataSource(contentResolver)
    }
}

class ContactsDataSource(private val cr: ContentResolver) :
    PositionalDataSource<Contact>() {

    companion object {
        private val PROJECTION = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Contact>) {
        callback.onResult(getContacts(params.requestedLoadSize, params.requestedStartPosition), 0)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Contact>) {
        callback.onResult(getContacts(params.loadSize, params.startPosition))
    }

    private fun getContacts(limit: Int, offset: Int): MutableList<Contact> {
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            PROJECTION,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY +
                    " ASC LIMIT " + limit + " OFFSET " + offset
        )

        cur.moveToFirst()
        val contacts: MutableList<Contact> = mutableListOf()
        while (cur.moveToNext()) {
            val id = cur.getString(cur.getColumnIndex(PROJECTION[0]))
            val lookupKey = cur.getString(cur.getColumnIndex(PROJECTION[1]))
            val name = cur.getString(cur.getColumnIndex(PROJECTION[2]))
            contacts.add(Contact(id.toLong(), lookupKey, name))
        }
        cur.close()

        return contacts
    }
}

data class Contact(
    val id: Long,
    val lookupKey: String,
    val name: String
)