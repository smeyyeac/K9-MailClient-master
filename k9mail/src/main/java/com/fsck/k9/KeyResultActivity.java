package com.fsck.k9;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fsck.k9.activity.K9Activity;

import java.security.SecureRandom;

public class KeyResultActivity extends K9Activity implements View.OnClickListener {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    FileKey file=new FileKey();
    private  String publicKeyName;
    private String address;
    final KeyServer keyServer = new KeyServer();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_result_activiy);

        final EditText editPublicKey = (EditText) findViewById(R.id.editPublicKey);
        final Button btnPublicKeyDownland = (Button) findViewById(R.id.btnDownload);

        findViewById(R.id.btnDownload).setOnClickListener(this);

        final OpenPGP pgp = new OpenPGP(SECURE_RANDOM);

        Bundle extras = getIntent().getExtras();
        publicKeyName = extras.getString("fileName");
        address = extras.getString("sendAddress");

        Log.e("KeyServerResult", String.valueOf(keyServer.getKeyServerPublicKey(address)));

        Log.e("RESULT", address);

        editPublicKey.setText(String.valueOf(keyServer.getKeyServerPublicKey(address)));
        file.createKeyFile(publicKeyName, String.valueOf(keyServer.getKeyServerPublicKey(address)),"mail");

    }

    public void onClick(View v) { //anahtar oluşturma

        file.createKeyFile(publicKeyName, String.valueOf(keyServer.getKeyServerPublicKey(address)),"mail");
        Toast.makeText(KeyResultActivity.this,  "İndirildi", Toast.LENGTH_LONG).show();
    }
}