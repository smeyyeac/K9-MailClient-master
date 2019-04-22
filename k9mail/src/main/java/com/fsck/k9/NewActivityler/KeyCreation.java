package com.fsck.k9.NewActivityler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fsck.k9.NewClasslar.OpenPGP;
import com.fsck.k9.NewClasslar.FileKey;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

//import android.support.annotation.RequiresApi;

import org.bouncycastle.openpgp.PGPException;

//import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.*;

import java.security.SecureRandom;

//@RequiresApi(api = Build.VERSION_CODES.O)
public class KeyCreation extends K9Activity implements View.OnClickListener{

    private OpenPGP openPgp;
    private  FileKey filekey;

   // private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OpenPGP.class);
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
   private EditText editKeySize,editName,editEmail,editParola;
   private TextView textPublic,textPrivate;
   private  Button buttonAnahtar,gozat;

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
        gozat=(Button) findViewById(R.id.button2);

        findViewById(R.id.buttonAnahtar).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

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
       // showPickAttachmentDialog(1);
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
    public void showPickAttachmentDialog(int requestCode) {
        requestCode |= 1;

        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        Boolean isInSubActivity = true;

        startActivityForResult(Intent.createChooser(i, null), requestCode);
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
        filekey.createKeyFile("privateKey", armoredKeyPair.privateKey(),email);
    }

}
