package com.fsck.k9.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.icu.text.StringPrepParseException;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fsck.k9.OpenPGP;
import com.fsck.k9.FileKey;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

import android.view.LayoutInflater;
//import android.support.annotation.RequiresApi;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.LoggerFactory;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;

//import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.logging.Logger;

//@RequiresApi(api = Build.VERSION_CODES.O)
public class KeyCreation extends K9Activity implements View.OnClickListener{

    private OpenPGP openPgp;
    private  FileKey filekey;

   // private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OpenPGP.class);
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
   private EditText editKeySize,editName,editEmail,editParola;
   private TextView textPublic,textPrivate;
   private  Button buttonAnahtar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_creation);

        editKeySize = (EditText) findViewById(R.id.editKey);
        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editParola = (EditText) findViewById(R.id.editParola);
        textPublic = (TextView) findViewById(R.id.textViewPublic);
        textPrivate = (TextView) findViewById(R.id.textViewPrivate);
        buttonAnahtar = (Button) findViewById(R.id.buttonAnahtar);

        findViewById(R.id.buttonAnahtar).setOnClickListener(this);

        //dosya icin eklendi
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

    }

    //dosya icin eklendi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.e("garanti", "garanted");
                }
        }
    }

    @Override
    public void onClick(View v) { //anahtar oluşturma
        anahtar_olustur();
       /* try {
            generateKeysAndEncryptAndDecryptMessage();
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void anahtar_olustur(){
     int keySize = Integer.parseInt(editKeySize.getText().toString());
        String name =  editName.getText().toString();
        String email = editEmail.getText().toString();
        String parola = editParola.getText().toString();
        /*final int keySize = 2048;
        final String name =  "ben";
        final String email = "saruhanur@gmail.com";
        final String parola = "saruhan";*/

        OpenPGP.ArmoredKeyPair armoredKeyPair = null;

        openPgp = new OpenPGP(SECURE_RANDOM);
        try {
            armoredKeyPair = openPgp.generateKeys(keySize, name, email, parola);
        } catch (PGPException e) {
            e.printStackTrace();
            Log.e("Hata", "Buttona bastık try");
            textPublic.setText("Cachte");
        }

        assertThat(armoredKeyPair).hasNoNullFieldsOrProperties();

        //LOGGER.info("java's private key ring:\n" + armoredKeyPair.privateKey());
        //LOGGER.info("java's public key ring:\n" + armoredKeyPair.publicKey());

        textPublic.setText(armoredKeyPair.publicKey());
        textPrivate.setText(armoredKeyPair.privateKey());

        filekey=new FileKey();
        filekey.createKeyFile("publicKey", armoredKeyPair.publicKey(),email);
        filekey.createKeyFile("privateKey", armoredKeyPair.publicKey(),email);
    }

}