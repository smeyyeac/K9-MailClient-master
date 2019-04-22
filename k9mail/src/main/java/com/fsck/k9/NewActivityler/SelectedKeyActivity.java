package com.fsck.k9.NewActivityler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.selected_key);

            gozat = (Button) findViewById(R.id.keyselected);

            findViewById(R.id.keyselected).setOnClickListener(this);
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

    }
