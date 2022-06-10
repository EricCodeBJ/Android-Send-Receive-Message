package com.kidevstudio.sendreceivemessage.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.kidevstudio.sendreceivemessage.events.NewMessageEvent;

import org.greenrobot.eventbus.EventBus;

public class SMSReceiverService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msgFrom = "";
            StringBuilder stringBuilder = new StringBuilder();

            if (bundle != null){

                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msgFrom = msgs[i].getOriginatingAddress();
                        String msgPart = msgs[i].getMessageBody();
                        stringBuilder.append(msgPart);
                    }
                    EventBus.getDefault().post(new NewMessageEvent(msgFrom, stringBuilder.toString()));

                }catch(Exception e){
                    Log.e("Exception caught",e.getMessage());
                }
            }
        }else{
            Log.e("SMS Receiver", "Intent is not sms"+intent.getAction());
        }
    }
}
