package com.fsck.k9.mail.Key;


import android.os.Environment;
import android.util.Log;

import com.google.common.collect.Iterators;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KeyOperation {

    public static String readKeyFile(String keyName) {

        String mainFile="keyFile";
        String fileName = keyName + ".asc";
        java.io.File keyfile = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), mainFile);
        keyfile.mkdir();
        File file = new File(keyfile, fileName);
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
            Log.e("hata",e.toString());
            //You'll need to add proper error handling here
        }
        Log.e("dfgddb", text.toString());
        return text.toString();
    }

    public static PGPSecretKey getPrivateSecretKey(String privateKeyData) throws IOException, PGPException {
        PGPPrivateKey privKey = null;
        Log.e("dortttttttt","No keys in keyring");
        PGPSecretKeyRing pubKeyRing = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            pubKeyRing = new PGPSecretKeyRing(
                    new ArmoredInputStream(new ByteArrayInputStream(privateKeyData.getBytes(StandardCharsets.UTF_8))),
                    new BcKeyFingerprintCalculator()
            );
        }
        Log.e("bessssssssssss","No keys in keyring");
        if (Iterators.size(pubKeyRing.getSecretKeys()) < 1) {
            Log.e("hataaaaaaaa","No keys in keyring");
        }

        PGPSecretKey signingKey = pubKeyRing.getSecretKey();
        Log.e("secretttttttttttttt","No keys in keyring");
        return signingKey;
    }
    public static PGPPublicKey getPublicKey(String publicKeyData) throws IOException, PGPException {
        PGPPrivateKey privKey = null;
        Log.e("birrrrrrrrrrrrrr","No keys in keyring");
        PGPPublicKeyRing pubKeyRing = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            pubKeyRing = new PGPPublicKeyRing(
                    new ArmoredInputStream(new ByteArrayInputStream(publicKeyData.getBytes(StandardCharsets.UTF_8))),
                    new BcKeyFingerprintCalculator()
            );
        }
        Log.e("ikiiiiiiiiiiiiiii","No keys in keyring");
        if (Iterators.size(pubKeyRing.getPublicKeys()) < 1) {
            Log.e("hataaaaaaaa","No keys in keyring");
        }

        PGPPublicKey signingKey = pubKeyRing.getPublicKey();
        Log.e("publiccccccc","No keys in keyring");
        return signingKey;
    }
}
