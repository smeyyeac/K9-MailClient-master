package com.fsck.k9.NewActivityler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fsck.k9.NewClasslar.FileKey;
import com.fsck.k9.NewClasslar.KeyServer;
import com.fsck.k9.NewClasslar.OpenPGP;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

import java.security.SecureRandom;

public class KeyResultActivity extends K9Activity implements View.OnClickListener {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    FileKey file=new FileKey();
    private  String publicKeyName;
    private String address;
    private Button btnPublicKeyDownland;
    final KeyServer keyServer = new KeyServer();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_result_activiy);

        final EditText editPublicKey = (EditText) findViewById(R.id.editPublicKey);
        btnPublicKeyDownland = (Button) findViewById(R.id.btnDownload);

        findViewById(R.id.btnDownload).setOnClickListener(this);

        final OpenPGP pgp = new OpenPGP(SECURE_RANDOM);

        Bundle extras = getIntent().getExtras();
        publicKeyName = extras.getString("fileName");
        address = extras.getString("sendAddress");

        Log.e("KeyServerResult", String.valueOf(keyServer.getKeyServerPublicKey(address)));

        Log.e("RESULT", address);

        editPublicKey.setText(String.valueOf(keyServer.getKeyServerPublicKey(address)));

    }

    public void onClick(View v) { //anahtar oluşturma
Log.d("resultda","resultda");
        file.createKeyFile(publicKeyName, String.valueOf(keyServer.getKeyServerPublicKey(address)),"mail");
        Toast.makeText(KeyResultActivity.this,  "İndirildi", Toast.LENGTH_LONG).show();
    }
}
