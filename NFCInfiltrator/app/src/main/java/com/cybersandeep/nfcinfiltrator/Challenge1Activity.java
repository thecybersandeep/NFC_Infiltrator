package com.cybersandeep.nfcinfiltrator;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Challenge1Activity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private TextView accessStatusTextView;
    private TextView nfcStatusTextView;
    private TextView flagTextView;

    private boolean exploitSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge1);

        // Set up edge-to-edge display (compatible version)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop() + insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                    v.getPaddingRight(),
                    v.getPaddingBottom() + insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                );
                return insets;
            });
        }
        
        // Initialize views
        accessStatusTextView = findViewById(R.id.accessStatusTextView);
        nfcStatusTextView = findViewById(R.id.nfcStatusTextView);
        flagTextView = findViewById(R.id.flagTextView);
        
        // Set up NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            showNfcNotSupportedDialog();
            return;
        }
        
        // Set up pending intent for NFC
        int flags = CompatUtils.getPendingIntentFlags(true);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags);
        
        // Set up intent filters for NFC
        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataScheme("nfcinfiltrator");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        intentFiltersArray = new IntentFilter[] { ndefFilter };
        
        // Set up tech lists for NFC
        techListsArray = new String[][] { new String[] { Ndef.class.getName() } };
        
        // Process intent if activity was started from NFC
        processIntent(getIntent());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Enable foreground dispatch for NFC
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Disable foreground dispatch for NFC
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        // Process new intent from NFC
        processIntent(intent);
    }
    
    private void processIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // Get NDEF messages from intent
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                
                // Process NDEF messages
                processNdefMessages(msgs);
            }
            
            // Check URI directly from intent data
            Uri uri = intent.getData();
            if (uri != null) {
                String uriString = uri.toString();
                processUri(uriString);
            }
        }
    }
    
    private void processNdefMessages(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }
        
        // Get records from first message
        NdefRecord[] records = msgs[0].getRecords();
        for (NdefRecord record : records) {
            // Check if record is URI record
            if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && 
                Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                
                // Get URI from record
                byte[] payload = record.getPayload();
                String uri = parseUriRecord(payload);
                if (uri != null) {
                    processUri(uri);
                }
            }
        }
    }
    
    private String parseUriRecord(byte[] payload) {
        if (payload.length < 2) {
            return null;
        }
        
        // Get URI prefix
        byte prefixByte = payload[0];
        String prefix = "";
        switch (prefixByte) {
            case 0x01: prefix = "http://www."; break;
            case 0x02: prefix = "https://www."; break;
            case 0x03: prefix = "http://"; break;
            case 0x04: prefix = "https://"; break;
            // Add more prefixes as needed
        }
        
        // Get URI text
        String text;
        try {
            text = new String(payload, 1, payload.length - 1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        
        return prefix + text;
    }
    
    private void processUri(String uri) {
        // Check if URI is valid for challenge
        if (SecurityUtils.verifyCh1(uri)) {
            // Exploit successful
            exploitSuccessful = true;

            // Update UI
            accessStatusTextView.setText("ACCESS GRANTED");
            accessStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
            accessStatusTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, null));

            // Show flag - pass the uri to the decryption method
            String flag = SecurityUtils.decryptChallenge1Flag(uri);
            flagTextView.setText(flag);

            // Ensure visibility is set to VISIBLE
            flagTextView.setVisibility(View.VISIBLE);

            // Set a bright background to make it more visible
            flagTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, null));
            flagTextView.setTextColor(Color.WHITE);
            flagTextView.setTextSize(20);

            // Animate flag with longer duration
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(2000);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(2);
            flagTextView.startAnimation(anim);

            // Update NFC status
            nfcStatusTextView.setText("Exploit successful! Flag revealed below.");
        } else {
            // Exploit failed
            Toast.makeText(this, "Invalid access card", Toast.LENGTH_SHORT).show();
            nfcStatusTextView.setText("Invalid access card. Try again.");
        }
    }

    private void showNfcNotSupportedDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_nfc_status);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        // Set dialog content
        TextView titleTextView = dialog.findViewById(R.id.dialogTitleTextView);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessageTextView);
        Button positiveButton = dialog.findViewById(R.id.dialogPositiveButton);
        Button negativeButton = dialog.findViewById(R.id.dialogNegativeButton);

        titleTextView.setText("NFC Not Supported");
        messageTextView.setText("This device does not support NFC, which is required for this challenge.");
        positiveButton.setText("Continue Anyway");

        // Hide negative button
        negativeButton.setVisibility(View.GONE);

        // Set button click listeners
        positiveButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}