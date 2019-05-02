package com.fsck.k9.NewActivityler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.activity.compose.AttachmentPresenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ServerShare extends K9Activity implements View.OnClickListener {

    private Button gozat;
    private Button yayinla;
    private TextView pathTextView;
    private AttachmentPresenter attachmentPresenter;
    private AttachmentPresenter.AttachmentMvpView attachmentMvpView;
    String path = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_share);

        pathTextView = (TextView) findViewById(R.id.uzantiTextView);
        gozat = (Button) findViewById(R.id.buttonGozat);
        yayinla = (Button) findViewById(R.id.buttonYayinla);

        findViewById(R.id.buttonGozat).setOnClickListener(this);

        final Context context = this;
        findViewById(R.id.buttonYayinla).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!path.equals(""))
                    Toast.makeText(context, "Serverda yayınlanmıştır", Toast.LENGTH_LONG).show();
                }

        });

    }

    @Override
    public void onClick(View v) {
        showPickAttachmentDialog(1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    path = data.getData().getPath();
                    pathTextView.setText(path);
                }
            break;
        }

    }



}