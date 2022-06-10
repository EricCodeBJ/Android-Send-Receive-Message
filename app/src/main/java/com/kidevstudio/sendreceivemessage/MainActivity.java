package com.kidevstudio.sendreceivemessage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kidevstudio.sendreceivemessage.events.NewMessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText text_to_send, receiver_number;
    private AppCompatButton btn_send;
    private TextView sender, receive_sms, send_sms_statut;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS, android.Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestSmsPermission();

        findViewByIds();

        btn_send.setOnClickListener(v -> {
            if (text_to_send.getText().toString().trim().length() > 0) {
                try {
                    send_sms_statut.setTextColor(Color.parseColor("#000000"));
                    send_sms_statut.setText("Sending SMS...");
                    btn_send.setVisibility(View.INVISIBLE);
                    btn_send.setClickable(false);
                    // Send Message
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(receiver_number.getText().toString(), null, text_to_send.getText().toString(), smsDeliveryListener(),  null);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private PendingIntent smsDeliveryListener() {

        String SENT = "SMS_SENT";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                send_sms_statut.setTextColor(Color.parseColor("#FF0000"));
                btn_send.setVisibility(View.VISIBLE);
                btn_send.setClickable(true);
                int resultCode = getResultCode();
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        send_sms_statut.setText("SMS sent");
                        send_sms_statut.setTextColor(Color.parseColor("#00FF00"));
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        send_sms_statut.setText("Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        send_sms_statut.setText("No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        send_sms_statut.setText("Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        send_sms_statut.setText("Radio off");
                        break;
                    default:
                        send_sms_statut.setText("SMS not sent, please try again later");
                }
            }
        }, new IntentFilter(SENT));

        return  sentPI;
    }

    private void findViewByIds() {
        text_to_send = findViewById(R.id.text_to_send);
        receiver_number = findViewById(R.id.receiver_number);
        btn_send = findViewById(R.id.btn_send);
        sender = findViewById(R.id.sender);
        receive_sms = findViewById(R.id.receive_sms);
        send_sms_statut = findViewById(R.id.send_sms_statut);
    }

    private void requestSmsPermission() {
        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void onMessageReceive(String getsSender, String getMessage) {
        Toast.makeText(MainActivity.this, "New message from: "+getsSender, Toast.LENGTH_SHORT).show();
        sender.setText(getsSender);
        receive_sms.setText(getMessage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewMessageEvent(NewMessageEvent event){
        onMessageReceive(event.getSender(), event.getMessage());
    }

}