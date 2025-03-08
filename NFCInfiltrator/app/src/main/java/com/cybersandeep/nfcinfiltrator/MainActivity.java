package com.cybersandeep.nfcinfiltrator;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private TextView nfcStatusTextView;
    private MaterialCardView challenge1Card, challenge2Card;
    private MaterialButton challenge1Button, challenge2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop() + insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                    v.getPaddingRight(),
                    v.getPaddingBottom() + insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                );
                return insets;
            });
        }

        nfcStatusTextView = findViewById(R.id.nfcStatusTextView);
        challenge1Card = findViewById(R.id.challenge1Card);
        challenge2Card = findViewById(R.id.challenge2Card);
        challenge1Button = findViewById(R.id.challenge1Button);
        challenge2Button = findViewById(R.id.challenge2Button);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNfcStatus();

        challenge1Card.setOnClickListener(v -> startChallenge1());
        challenge2Card.setOnClickListener(v -> startChallenge2());
        challenge1Button.setOnClickListener(v -> startChallenge1());
        challenge2Button.setOnClickListener(v -> startChallenge2());

        SecurityUtils.getEncryptedChallenge1Flag();
        SecurityUtils.getEncryptedChallenge2Flag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNfcStatus();
    }

    private void checkNfcStatus() {
        if (nfcAdapter == null) {
            nfcStatusTextView.setText("NFC Status: Not supported on this device");
            nfcStatusTextView.setTextColor(getCompatColor(android.R.color.holo_red_light));
            showNfcNotSupportedDialog();
        } else if (!nfcAdapter.isEnabled()) {
            nfcStatusTextView.setText("NFC Status: Disabled");
            nfcStatusTextView.setTextColor(getCompatColor(android.R.color.holo_red_light));
            showNfcDisabledDialog();
        } else {
            nfcStatusTextView.setText("NFC Status: Ready");
            nfcStatusTextView.setTextColor(getCompatColor(android.R.color.holo_green_light));
        }
    }

    private int getCompatColor(int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorResId, null);
        } else {
            return getResources().getColor(colorResId);
        }
    }

    private void showNfcNotSupportedDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_nfc_status);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView titleTextView = dialog.findViewById(R.id.dialogTitleTextView);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessageTextView);
        Button positiveButton = dialog.findViewById(R.id.dialogPositiveButton);
        Button negativeButton = dialog.findViewById(R.id.dialogNegativeButton);

        titleTextView.setText("NFC Not Supported");
        messageTextView.setText("This device does not support NFC, which is required for this application.");
        positiveButton.setText("Continue Anyway");

        negativeButton.setVisibility(View.GONE);

        positiveButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showNfcDisabledDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_nfc_status);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView titleTextView = dialog.findViewById(R.id.dialogTitleTextView);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessageTextView);
        Button positiveButton = dialog.findViewById(R.id.dialogPositiveButton);
        Button negativeButton = dialog.findViewById(R.id.dialogNegativeButton);

        titleTextView.setText("NFC Disabled");
        messageTextView.setText("NFC is disabled. Please enable NFC to use all features of this application.");
        positiveButton.setText("Enable NFC");
        negativeButton.setText("Continue Anyway");

        positiveButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startChallenge1() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is required for this challenge", Toast.LENGTH_SHORT).show();
            checkNfcStatus();
            return;
        }

        Intent intent = new Intent(this, Challenge1Activity.class);
        startActivity(intent);
    }

    private void startChallenge2() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is required for this challenge", Toast.LENGTH_SHORT).show();
            checkNfcStatus();
            return;
        }

        Intent intent = new Intent(this, Challenge2Activity.class);
        startActivity(intent);
    }
}