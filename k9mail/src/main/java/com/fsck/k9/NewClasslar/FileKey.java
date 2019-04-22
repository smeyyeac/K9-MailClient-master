package com.fsck.k9.NewClasslar;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileKey {
    public void createKeyFile(String keyName, String key, String mail) {
        String mainFile="keyFile";
        String fileName = mail + "_" + keyName + ".asc";
        java.io.File keyfile = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), mainFile);
        keyfile.mkdir();
        Log.e("Dosya Yeri", String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()));
        File file = new File(keyfile, fileName);
        Log.e("Dosya Yeri", String.valueOf(file));
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(key.getBytes());
            fos.close();
            Log.e("Saved", "Create");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Fail", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Use Storage.", "Fail");
        }
    }
    public static  void createSignatureFile(String signature) {
        String mainFile="signatureFile";
        String fileName =  "signature" + ".asc";
        java.io.File keyfile = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), mainFile);
        keyfile.mkdir();
        Log.e("Dosya Yeri", String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()));
        File file = new File(keyfile, fileName);
        Log.e("Dosya Yeri", String.valueOf(file));
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(signature.getBytes());
            fos.close();
            Log.e("Saved", "Create");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Fail", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Use Storage.", "Fail");
        }
    }

    public void readKeyFile(String keyName) {

        String fileName = keyName + ".asc";

        java.io.File file = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            Log.e("saved", "okundu");
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
    }
}