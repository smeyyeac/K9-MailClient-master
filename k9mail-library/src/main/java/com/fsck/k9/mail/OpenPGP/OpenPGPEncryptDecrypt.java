package com.fsck.k9.mail.OpenPGP;

import android.os.Environment;
import android.util.Log;

import com.fsck.k9.mail.Key.KeyOperation;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;


import java.util.Iterator;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;


public class OpenPGPEncryptDecrypt {
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();
    private static final String PROVIDER = "BC";
    static {
        Security.addProvider(provider);
    }

    private static String encrypt(PGPPublicKey publicKey, String msgText) throws PGPException {
        try {
            byte[] clearData = msgText.getBytes();
            ByteArrayOutputStream encOut = new ByteArrayOutputStream();
            OutputStream out = new ArmoredOutputStream(encOut);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
            OutputStream cos = comData.open(bOut);
            PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
            OutputStream pOut = lData.open(cos, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, clearData.length, new Date());
            pOut.write(clearData);
            lData.close();
            comData.close();
            PGPEncryptedDataGenerator encGen =
                    new PGPEncryptedDataGenerator(
                            new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(true).setSecureRandom(
                                    new SecureRandom()).setProvider(provider));
            if (publicKey != null) {
                encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider(provider));
                byte[] bytes = bOut.toByteArray();
                OutputStream cOut = encGen.open(out, bytes.length);
                cOut.write(bytes);
                cOut.close();
            }
            out.close();

            return new String(encOut.toByteArray());
        } catch (Exception e) {
            Log.e("Hata", e.toString());
            throw new PGPException("Error in encrypt", e);
        }
    }


  public  static String encrypted(String messageTo, String message){
        Security.addProvider(new BouncyCastleProvider());
        PGPPublicKey publicKey = null;
        String encryptMessage = null;
        Log.e("GetirYol" ,Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/"+messageTo+ "_publicKey.asc");

        PGPPublicKey publicKeySon;
        try {
            ArrayList keyList = publicKeyList(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/"+messageTo+ "_publicKey.asc");
            Log.e("GetirList 1" ,String.valueOf(keyList.get(1)));
            publicKeySon = getPublicKey(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/"+messageTo+ "_publicKey.asc", String.valueOf(keyList.get(1)));
            encryptMessage = encrypt(publicKeySon, message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }
        return encryptMessage;

    }

    public static String decrypt(String encryptedText,String privateKey, String password) throws Exception {
        byte[] encrypted = encryptedText.getBytes();
        InputStream in = new ByteArrayInputStream(encrypted);
        in = PGPUtil.getDecoderStream(in);
        BcPGPObjectFactory pgpF = new BcPGPObjectFactory(in);
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();

        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;
        while (sKey == null && enc.getEncryptedDataObjects().hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next();
            Log.e("GetirPbeId",Long.toHexString(pbe.getKeyID()).toUpperCase());
            sKey = getPrivateKey(getPGPSecretKeyRing(privateKey.getBytes()), pbe.getKeyID(), password.toCharArray());
            Log.e( "GetirSkeyId", Long.toHexString(sKey.getKeyID()).toUpperCase());
        }
        if (pbe != null) {
            InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));
            BcPGPObjectFactory pgpFact = new BcPGPObjectFactory(clear);
            PGPCompressedData cData = (PGPCompressedData) pgpFact.nextObject();
            pgpFact = new BcPGPObjectFactory(cData.getDataStream());

            PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
            InputStream unc = ld.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int ch;
            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
            byte[] returnBytes = out.toByteArray();
            out.close();
            return new String(returnBytes);
        }
        return null;
    }

    public static String decrypted(String email, String password, String encryptedMessage){
        Security.addProvider(new BouncyCastleProvider());
        String decryptMessage = null;
        Log.e("Getir encrypt", encryptedMessage);
        try {
            if (encryptedMessage != "") {
                Log.e("GetirKeyRead", KeyOperation.readKeyFile( email + "_privateKey"));
                String  readKey = KeyOperation.readKeyFile(email + "_privateKey");
                decryptMessage = decrypt(encryptedMessage , readKey, password);

                return decryptMessage;
            }else {
                Log.w("Getir encrypt","dosya okunamadı");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptMessage;
    }

    private static PGPPrivateKey getPrivateKey(PGPSecretKeyRing keyRing, long keyID, char[] pass) throws PGPException {
        PGPSecretKey secretKey = keyRing.getSecretKey(keyID);
        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
        return secretKey.extractPrivateKey(decryptor);
    }

    private static PGPSecretKeyRing getPGPSecretKeyRing(byte[] privateKey) throws IOException {
        ArmoredInputStream ais = new ArmoredInputStream(new ByteArrayInputStream(privateKey));
        return (PGPSecretKeyRing) new BcPGPObjectFactory(ais).nextObject();
    }

    public static ArrayList<String> publicKeyList(String path) throws IOException, PGPException {
        ArrayList<String> keys = new ArrayList<>();
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(is, calculator);
        Iterator<PGPPublicKeyRing> it = keyRings.getKeyRings();
        while (it.hasNext()) {
            PGPPublicKeyRing keyRing = it.next();
            Iterator<PGPPublicKey> kit = keyRing.getPublicKeys();
            while (kit.hasNext()) {
                PGPPublicKey k = kit.next();
                if (k.isEncryptionKey()) {
                    keys.add(Long.toHexString(k.getKeyID()).toUpperCase());
                    Log.e("GetirList",Long.toHexString(k.getKeyID()).toUpperCase());

                }
            }
        }
        return keys;
    }
    public static PGPPublicKey getPublicKey(String path, String id) throws IOException, PGPException {
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(is, calculator);
        Iterator<PGPPublicKeyRing> it = keyRings.getKeyRings();
        PGPPublicKey publicKey = null;
        while (publicKey == null && it.hasNext()) {
            PGPPublicKeyRing keyRing = it.next();
            Iterator<PGPPublicKey> kit = keyRing.getPublicKeys();
            while (publicKey == null && kit.hasNext()) {
                PGPPublicKey k = kit.next();
                if (k.isEncryptionKey() && Long.toHexString(k.getKeyID()).toUpperCase().contains(id.toUpperCase())) {
                    publicKey = k;
                }
            }
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("No encryption key with given ID");
        }
        return publicKey;
    }

}
