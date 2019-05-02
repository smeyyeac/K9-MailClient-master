package com.fsck.k9.NewActivityler;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

public class ServerShare extends K9Activity implements View.OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_share);

       // findViewById(R.id.buttonSearch).setOnClickListener(this); buton içic ornek onclick tanımlama
    }
    public void onClick(View v){

    }
}
