package com.cybersandeep.nfcinfiltrator;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class Challenge2Activity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private TextView terminalOutputTextView;
    private TextView verificationStatusTextView;
    private TextView nfcStatusTextView;
    private TextView flagTextView;

    private boolean exploitSuccessful = false;
    private static final String REQUIRED_MIME_TYPE = "application/x-facility-clearance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge2);

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
        terminalOutputTextView = findViewById(R.id.terminalOutputTextView);
        verificationStatusTextView = findViewById(R.id.verificationStatusTextView);
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
        IntentFilter mimeFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            mimeFilter.addDataType(REQUIRED_MIME_TYPE);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        
        intentFiltersArray = new IntentFilter[] { mimeFilter };
        
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
        // Check if intent is from NFC
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // Get MIME type from intent
            String type = intent.getType();
            if (REQUIRED_MIME_TYPE.equals(type)) {
                // MIME type is correct, now check payload
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMsgs != null) {
                    NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                    for (int i = 0; i < rawMsgs.length; i++) {
                        msgs[i] = (NdefMessage) rawMsgs[i];
                    }
                    
                    // Process NDEF messages
                    processNdefMessages(msgs);
                }
            } else {
                // Wrong MIME type
                updateTerminalOutput("MIME TYPE VERIFICATION FAILED\n> EXPECTED: " + REQUIRED_MIME_TYPE + "\n> RECEIVED: " + type);
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
            // Check if record is MIME record with correct MIME type
            if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA && 
                new String(record.getType()).equals(REQUIRED_MIME_TYPE)) {
                
                // Get payload from record
                byte[] payload = record.getPayload();
                String payloadText;
                try {
                    payloadText = new String(payload, "UTF-8");
                    processMimePayload(payloadText);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void processMimePayload(String payload) {
        // Update terminal output
        updateTerminalOutput("MIME TYPE VERIFICATION SUCCESSFUL\n> PROCESSING PAYLOAD\n> " + payload);
        
        // Check if payload contains exploit condition
        if (SecurityUtils.verifyCh2(payload)) {
            // Exploit successful
            exploitSuccessful = true;
            
            // Update UI after a short delay to show terminal output
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Update terminal output
                updateTerminalOutput("AUTHORIZATION SUCCESSFUL\n> USER: CYBERSANDEEP\n> ACCESS LEVEL: ADMINISTRATOR\n> SECURITY OVERRIDE ACCEPTED");
                
                // Update verification status
                verificationStatusTextView.setText("ACCESS GRANTED");
                verificationStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
                verificationStatusTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                
                // Show flag - pass the payload to the decryption method
                String flag = SecurityUtils.decryptChallenge2Flag(payload);
                flagTextView.setText(flag);
                flagTextView.setVisibility(View.VISIBLE);

                // Set a bright background to make it more visible
                flagTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, null));
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
            }, 1500);
        } else {
            // Exploit failed
            updateTerminalOutput("AUTHORIZATION FAILED\n> INVALID CREDENTIALS\n> ACCESS DENIED");
            Toast.makeText(this, "Invalid authorization", Toast.LENGTH_SHORT).show();
            nfcStatusTextView.setText("Invalid authorization. Try again.");
        }
    }
    
    private void updateTerminalOutput(String text) {
        terminalOutputTextView.setText("SYSTEM READY\n> " + text);
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