package com.example.secureapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.content.ContextCompat

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val bundle = intent.extras
        val pdus = bundle?.get("pdus") as? Array<*>
        val format = bundle?.getString("format")

        pdus?.forEach { pdu ->
            val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)
            val sender = sms.displayOriginatingAddress
            val message = sms.displayMessageBody.trim()

            Log.d("SmsReceiver", "SMS from $sender: $message")

            when (message.lowercase()) {
                "#getlocation" -> {
                    val serviceIntent = Intent(context, LocationService::class.java).apply {
                        putExtra(LocationService.EXTRA_PHONE_NUMBER, sender)
                    }
                    startServiceCompat(context, serviceIntent)
                }

                "#lock" -> {
                    // Directly call LockHelper
                    LockHelper.lockNow(context)
                }
                "#freeze" -> {
                    if (!FreezeManager.isFrozen) {
                        val intent = Intent(context, FreezeControlActivity::class.java).apply {
                            putExtra(FreezeControlActivity.EXTRA_ACTION, FreezeControlActivity.ACTION_FREEZE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }

                "#unfreeze" -> {
                    if (FreezeManager.isFrozen) {
                        val intent = Intent(context, FreezeControlActivity::class.java).apply {
                            putExtra(FreezeControlActivity.EXTRA_ACTION, FreezeControlActivity.ACTION_UNFREEZE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }

                "#alarm" -> {
                    val alarmIntent = Intent(context, AlarmService::class.java).apply {
                        putExtra("sender", sender)
                    }
                    startServiceCompat(context, alarmIntent)
                }
                "#stopalarm" -> {
                    val stopAlarmIntent = Intent(context, AlarmService::class.java).apply {
                        putExtra("sender", sender)
                        action = AlarmService.ACTION_STOP
                    }
                    startServiceCompat(context, stopAlarmIntent)
                }
                else -> {
                    Log.d("SmsReceiver", "No matching command")
                }
            }
        }
    }

    private fun startServiceCompat(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
