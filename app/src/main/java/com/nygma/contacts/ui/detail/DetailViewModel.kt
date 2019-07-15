package com.nygma.contacts.ui.detail

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException


class DetailViewModel(private val cr: ContentResolver) : ViewModel() {

    private val details = MutableLiveData<String>().apply { value = "" }
    private val name = MutableLiveData<String>()
    private val phoneNumbers =
        MutableLiveData<MutableList<String>>().apply { value = mutableListOf() }
    private val photo = MutableLiveData<Bitmap>()

    fun getDetails(): LiveData<String> = details

    fun getPhoneNumbers(): LiveData<List<String>> = phoneNumbers as LiveData<List<String>>

    fun getPhoto(): LiveData<Bitmap> = photo

    fun getName(): LiveData<String> = name

    fun loadDetails(id: Long) {
        val id = id.toString()

        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            ContactsContract.Contacts._ID + " = ?",
            arrayOf(id),
            null
        )
        cur.moveToFirst()
        val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
        this.name.value = name
        cur.close()

        val pCur = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(id), null
        )
        while (pCur.moveToNext()) {
            val phone = pCur.getString(
                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )
            if (phone != null)
                phoneNumbers.value?.add(phone)
        }
        pCur.close()

        val emailCur = cr.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(id), null
        )
        while (emailCur.moveToNext()) {
            val email = emailCur.getString(
                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
            )
            val emailType = emailCur.getString(
                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
            )

            if (email != null)
                details.value = "${details.value} $email $emailType"

        }
        emailCur.close()

        try {
            val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                cr, ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
            )

            if (inputStream != null) {
                val photo = BitmapFactory.decodeStream(inputStream)
                this.photo.value = photo
                inputStream.close()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}