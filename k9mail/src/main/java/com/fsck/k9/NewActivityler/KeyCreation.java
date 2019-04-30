package com.fsck.k9.NewActivityler;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.NewClasslar.OpenPGP;
import com.fsck.k9.NewClasslar.FileKey;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

//import android.support.annotation.RequiresApi;

import org.bouncycastle.openpgp.PGPException;

//import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

//@RequiresApi(api = Build.VERSION_CODES.O)
public class KeyCreation extends K9Activity implements View.OnClickListener {

    private OpenPGP openPgp;
    private FileKey filekey;
    String keySize;

    // private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(OpenPGP.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private EditText editName, editEmail, editParola;
    private Button buttonAnahtar;
    private Spinner spinner;
    List<String> list;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_creation);


        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editParola = (EditText) findViewById(R.id.editParola);
        //textPublic = (TextView) findViewById(R.id.textViewPublic);
        //textPrivate = (TextView) findViewById(R.id.textViewPrivate);
        buttonAnahtar = (Button) findViewById(R.id.buttonAnahtar);

        findViewById(R.id.buttonAnahtar).setOnClickListener(this);

        //dosya icin eklendi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }


            list = new ArrayList<>();
            list.add("1024");
            list.add("2048");
            list.add("4096");

            spinner = (Spinner) findViewById(R.id.spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
            spinner.setAdapter(adapter);


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("keyyyyy", spinner.getSelectedItem().toString());
                keySize = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        final Context context = this;
        Toast.makeText(context,  "Anahtar çiftiniz oluşturulmuştur.", Toast.LENGTH_LONG).show();
    }

    public void anahtar_olustur(){
     int keySizes = Integer.parseInt(keySize);
        String name =  editName.getText().toString();
        String email = editEmail.getText().toString();
        String parola = editParola.getText().toString();

        OpenPGP.ArmoredKeyPair armoredKeyPair = null;

        openPgp = new OpenPGP(SECURE_RANDOM);
        try {
            armoredKeyPair = openPgp.generateKeys(keySizes, name, email, parola);
        } catch (PGPException e) {
            e.printStackTrace();
            Log.e("Hata", "Buttona bastık try");
        }

        assertThat(armoredKeyPair).hasNoNullFieldsOrProperties();

        filekey=new FileKey();
        filekey.createKeyFile(email+"_publicKey", armoredKeyPair.publicKey());
        filekey.createKeyFile(email+"_privateKey", armoredKeyPair.privateKey());


    }

}
