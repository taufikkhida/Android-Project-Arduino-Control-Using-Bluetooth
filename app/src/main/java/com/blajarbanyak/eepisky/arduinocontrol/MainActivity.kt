package com.blajarbanyak.eepisky.arduinocontrol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.*

import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mTxtDevices: TextView? = null
    private var mControl: TableLayout? = null
    private var mSwStart: Switch? = null

    var mBtnF: Button? = null
    var mBtnL: Button? = null
    var mBtnR: Button? = null
    var mBtnB: Button? = null
    var mSb1: SeekBar? = null
    var mSb2: SeekBar? = null
    var mSb3: SeekBar? = null
    var mSb4: SeekBar? = null
    var mSb5: SeekBar? = null
    var mSb6: SeekBar? = null

    var blutoothObj: BluetoothAdapter? = null
    var btSocket: BluetoothSocket? = null
    var address: String = ""

    var mTxtLog: TextView? = null
    var isConnected = false

    val timer: Timer = Timer()
    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTxtDevices = findViewById<View>(R.id.txtDevices) as TextView
        mTxtLog = findViewById<View>(R.id.txtLog) as TextView
        mSwStart = findViewById<View>(R.id.swStart) as Switch
        mBtnB = findViewById<View>(R.id.btnBackward) as Button
        mBtnL = findViewById<View>(R.id.btnLeft) as Button
        mBtnR = findViewById<View>(R.id.btnRight) as Button
        mBtnF = findViewById<View>(R.id.btnForward) as Button

        mSb1 = findViewById<View>(R.id.sb1) as SeekBar
        mSb2 = findViewById<View>(R.id.sb2) as SeekBar
        mSb3 = findViewById<View>(R.id.sb3) as SeekBar
        mSb4 = findViewById<View>(R.id.sb4) as SeekBar
        mSb5 = findViewById<View>(R.id.sb5) as SeekBar
        mSb6 = findViewById<View>(R.id.sb6) as SeekBar

        val conn = ConnectBT(this)

        mSwStart!!.setOnCheckedChangeListener { _, isChecked ->
            if (address.isEmpty()) {
                Toast.makeText(this, "Please Connect first", Toast.LENGTH_LONG).show()
                mTxtLog!!.setText("Please Connect First")
                mSwStart!!.isChecked = false
            } else {
                if (isChecked) {
                    conn.execute()
                } else {
                    timer.cancel()
                    conn.cancel(true)
                    btSocket!!.close()
                }
            }
        }
    }

    fun openSetup(view: View) {
        val setupDevice = Intent(this, SetupActivity::class.java)
        startActivityForResult(setupDevice, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                address = data!!.getStringExtra(SetupActivity.EXTRA_ADDRESS)
                val btName = data.getStringExtra(SetupActivity.EXTRA_NAME)

                mTxtDevices!!.text = btName
            }
        }
    }

    inner class sendParam : TimerTask() {
        override fun run() {
            val param = arrayOf(
                    mBtnF?.isPressed.toString(),
                    mBtnL?.isPressed.toString(),
                    mBtnB?.isPressed.toString(),
                    mBtnR?.isPressed.toString(),
                    mSb1!!.progress.toString(),
                    mSb2!!.progress.toString(),
                    mSb3!!.progress.toString(),
                    mSb4!!.progress.toString(),
                    mSb5!!.progress.toString(),
                    mSb6!!.progress.toString()
                //"true","false","false","false","100","100","100","100","100"
            )
            val command = "COMMAND," + TextUtils.join(",", param) + ",X"
            if (btSocket != null) {
               try {
                    btSocket!!.outputStream.write(command.toByteArray())
               } catch (e: IOException) {
                    e.printStackTrace()
               }
            }
        }
    }

    class ConnectBT internal constructor(context: MainActivity) : AsyncTask<Void, Void, Void>() {
        private var connectSuccess = true
        private var activityReference: WeakReference<MainActivity> = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            activity.mTxtLog!!.setText("Connecting...")
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return null
            try {
                if (activity.btSocket == null || !activity.isConnected) {
                    activity.blutoothObj = BluetoothAdapter.getDefaultAdapter()
                    var dispositivo: BluetoothDevice? = activity.blutoothObj!!.getRemoteDevice(activity.address)
                    activity.btSocket = dispositivo!!.createInsecureRfcommSocketToServiceRecord(activity.myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    activity.btSocket!!.connect();
                }
            } catch (e: IOException) {
                connectSuccess = false
            }
            return null;
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            if (!connectSuccess) {
                activity.mTxtLog!!.setText("Conncetion Fail")
            } else {
                activity.mTxtLog!!.setText("Ready")
                activity.isConnected = true
                activity.timer.schedule(activity.sendParam(), 0, 200)
            }
        }
    }
}