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
        java.io.File  signaturefile = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), mainFile);
        signaturefile.mkdir();
        File file = new File( signaturefile, fileName);
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

    public static void deleteStorageFile(String depoyeri,String fileName){

        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ depoyeri ); // yol belirtmeyip sadece dosya ismi belirttiğimiz zaman otomatik olarak bulunduğu klasöre göre işlem yapar.
        f.mkdir();
        File file = new File( f, fileName);

        if(!file.exists()){ // eğer dosya yoksa
            System.out.println("Dosya bulunamadığından silinemedi");
        }else{
            file.delete(); // eğer dosyamız varsa.. // silme işlemi gerçekleştirir.
            System.out.println(f.getName() +" adlı dosya başarılı bir şekilde silinmiştir.");
        }
    }
}