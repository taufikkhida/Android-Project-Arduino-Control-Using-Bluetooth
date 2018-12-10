package com.blajarbanyak.eepisky.arduinocontrol

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList

class SetupActivity : AppCompatActivity() {
    private var bluetoothObj: BluetoothAdapter? = null
    private var pairedDevices: Set<BluetoothDevice>? = null
    private var deviceList: ListView? = null

    private val itemList_Click = AdapterView.OnItemClickListener { parent, view, position, id ->
        val info = (view as TextView).text.toString()
        val address = info.substring(info.length - 17)
        val intent = Intent(this@SetupActivity, MainActivity::class.java)
        intent.putExtra(EXTRA_ADDRESS, address)
        intent.putExtra(EXTRA_NAME, info)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        bluetoothObj = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothObj == null) {
            Toast.makeText(applicationContext, "Bluetooth Device Not Available", Toast.LENGTH_LONG)
            finish()
        } else {
            if (bluetoothObj!!.isEnabled) {

            } else {
                val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(turnBTon, 1)
            }
        }

        deviceList = findViewById<View>(R.id.deviceList) as ListView
    }

    fun btnRefresh_Click(view: View) {
        pairedDeviceList()
    }

    private fun pairedDeviceList() {
        pairedDevices = bluetoothObj!!.bondedDevices
        val list = ArrayList<String>()

        if (pairedDevices!!.size > 0) {
            for (bt in pairedDevices!!) {
                list.add(bt.name + "\n" + bt.address)
            }
        } else {
            Toast.makeText(applicationContext, "No Paired Device Found.", Toast.LENGTH_LONG)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        deviceList!!.adapter = adapter
        deviceList!!.onItemClickListener = itemList_Click
    }

    companion object {
        val EXTRA_ADDRESS = "com.blajarbanyak.android.SetupActivity.extra.REPLY"
        val EXTRA_NAME = "com.blajarbanyak.android.SetupActivity.extra.NAME"
    }
}
