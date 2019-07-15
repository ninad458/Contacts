package com.nygma.contacts.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.nygma.contacts.R
import kotlinx.android.synthetic.main.detail_activity.*
import kotlinx.android.synthetic.main.item_phone_number.view.*

class DetailActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DetailViewModel(application.contentResolver) as T
            }
        }).get(DetailViewModel::class.java)
    }

    companion object {
        private const val ID = "id"
        fun Context.startContactDetails(id: Long) {
            startActivity(Intent(this, DetailActivity::class.java).apply {
                putExtra(ID, id)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_activity)
        getData()
    }

    private fun getData() {
        val id = intent.getLongExtra(ID, -1)
        viewModel.loadDetails(id)
        viewModel.getName().observe(this, Observer {
            txt_name.text = it
        })
        viewModel.getPhoneNumbers().observe(this, Observer {
            list_numbers.adapter = PhoneNumberAdapter(it)
        })
        viewModel.getDetails().observe(this, Observer {
            txt_details.text = it
        })
        viewModel.getPhoto().observe(this, Observer {
            img_profile.visibility = View.VISIBLE
            img_profile.setImageBitmap(it)
        })
    }

    class PhoneNumberAdapter(private val data: List<String>) :
        RecyclerView.Adapter<PhoneViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PhoneViewHolder(parent)

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
            holder.setPhoneNumber(data[position])
        }
    }

    class PhoneViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(itemView.context)
            .inflate(R.layout.item_phone_number, itemView, false)
    ) {

        fun setPhoneNumber(phoneNumber: String) {
            itemView.txt_phone_number.text = phoneNumber
            itemView.act_call.setOnClickListener {
                val uri = "tel:" + phoneNumber.trim()
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(uri)
                it.context.startActivity(intent)
            }
            itemView.act_message.setOnClickListener {
                val sendIntent = Intent(Intent.ACTION_VIEW)
                sendIntent.data = Uri.parse("sms:$phoneNumber")
                it.context.startActivity(sendIntent)
            }
        }
    }
}