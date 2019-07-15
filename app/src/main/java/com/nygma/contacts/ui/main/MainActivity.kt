package com.nygma.contacts.ui.main

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nygma.contacts.R
import com.nygma.contacts.ui.detail.DetailActivity.Companion.startContactDetails
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ContactsViewModel(application.contentResolver) as T
            }
        }).get(ContactsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        contactsList.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL,
            false
        )
        showContacts()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts()
            } else {
                Toast.makeText(
                    this,
                    "Until you grant the permission, we canot display the names",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showContacts() {
        val diffCallback = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }
        }

        val adapter = ContactsAdapter(diffCallback) {
            startContactDetails(it)
        }
        contactsList.adapter = adapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            viewModel.loadContacts()
            viewModel.contactsList.observe(this, Observer {
                adapter.submitList(it)
                contactsEmpty.visibility = if (adapter.itemCount > 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            })

        }
    }

    class ContactsAdapter(
        diffCallback: DiffUtil.ItemCallback<Contact>,
        private val onClick: (Long) -> Unit
    ) :
        PagedListAdapter<Contact, ContactViewHolder>(diffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            return ContactViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_contact, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            holder.textView.text = getItem(position)?.name
            holder.itemView.setOnClickListener {
                getItem(position)?.id?.apply { onClick(this) }
            }
        }

    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.groupName)
    }
}
