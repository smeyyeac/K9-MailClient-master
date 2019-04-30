package com.fsck.k9.NewActivityler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.activity.compose.AttachmentPresenter;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;

public class SelectedKeyActivity extends K9Activity implements View.OnClickListener{

        private Button gozat;
        private AttachmentPresenter attachmentPresenter;
        private AttachmentPresenter.AttachmentMvpView attachmentMvpView;
        private EditText keyAd;
        private ListView keyLists;
        private TextView textView;
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.selected_key);

            gozat = (Button) findViewById(R.id.keyselected);
            keyAd = (EditText) findViewById(R.id.editText);
            keyLists = (ListView) findViewById(R.id.anahtarList);
            textView = (TextView) findViewById(R.id.keyShow);

            findViewById(R.id.keyselected).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/keyFile/";
            Log.d("Files", "Path: " + path);
            File f = new File(path);
            File file[] = f.listFiles();
            Editable ara = keyAd.getText();
            ArrayList<String> list = new ArrayList();
            for (int i = 0; i < file.length; i++) {

                if (file[i].getName().contains(ara))
                    list.add(file[i].getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
            keyLists.setAdapter(adapter );

            keyLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String s = keyLists.getItemAtPosition(i).toString();
                    textView.setText(s);
                }
            });
        }

    }
