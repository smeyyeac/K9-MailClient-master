package com.fsck.k9.NewActivityler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.fsck.k9.NewClasslar.KeyServer;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;

import java.util.ArrayList;

public class KeySearch extends K9Activity implements View.OnClickListener {

    private EditText editSearch;
    private Button buttonSearch;
    private ListView listViewSearch;

    final KeyServer keyServer = new KeyServer();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_search);

        editSearch = (EditText) findViewById(R.id.editSearch);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        listViewSearch = (ListView) findViewById(R.id.listViewSearch);


        findViewById(R.id.buttonSearch).setOnClickListener(this);
    }
        public void onClick(View v) { //anahtar oluşturma
            final ArrayList<String> result = new ArrayList<>();

            String name =  editSearch.getText().toString();
            result.addAll(keyServer.keyServerLookup(name));


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, result);
            listViewSearch.setAdapter(adapter);

            listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int size = result.get(position).replaceAll("\\s+","").length();
                    String addres =  result.get(position).replaceAll("\\s+","").substring(size-16 , size);
                    String publicKeyName = publicKeyMailName(result.get(position));
                    //Toast.makeText(KeyServerSearchActivity.this, result.get(position).replaceAll("\\s+","").substring(size-16 , size)+ "sectin", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(KeySearch.this, KeyResultActivity.class);
                    i.putExtra("sendAddress",addres);
                    i.putExtra("fileName", publicKeyName);
                    startActivity(i);
                }

            });
        }


    private static String publicKeyMailName(String search) {
        String startOfBlock = "<";
        String endOfBlock = ">";
        int startIndex = search.indexOf(startOfBlock) + startOfBlock.length();
        int endIndex = search.indexOf(endOfBlock);
        String result = search.substring(startIndex,endIndex);
        Log.e("Mail", result);
        return result;
    }

}
