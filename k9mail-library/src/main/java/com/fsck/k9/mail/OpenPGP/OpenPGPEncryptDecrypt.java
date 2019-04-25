package com.fsck.k9.mail.OpenPGP;

import android.os.Environment;
import android.util.Log;

import com.fsck.k9.mail.Key.KeyOperation;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Signature.OpenPGPSignature;

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
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;


public class OpenPGPEncryptDecrypt {
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

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
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PGPPublicKey publicKey = null;
        String encryptMessage = null;

        try {
            publicKey = (PGPPublicKey) KeyOperation.getPublicKey(KeyOperation.readKeyFile(messageTo + "_publicKey"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        try {
            encryptMessage = encrypt(publicKey, message);
            return encryptMessage;
        }  catch (PGPException e) {
            e.printStackTrace();
        }
        Log.e("Getir encrypt", encryptMessage );
        return encryptMessage;

    }
    private static String decrypt(PGPPrivateKey privateKey, String encryptedText) throws Exception {
        byte[] encrypted = encryptedText.getBytes();
        InputStream in = new ByteArrayInputStream(encrypted);
        in = PGPUtil.getDecoderStream(in);
        BcPGPObjectFactory pgpF = new BcPGPObjectFactory(in);
        in.close();
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();
        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        }else{
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }
        PGPPublicKeyEncryptedData pbe = null;
        if (privateKey != null && enc.getEncryptedDataObjects().hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next();
        }
        if (pbe != null) {
            InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));
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

            clear.close();
            unc.close();
            out.close();
            return new String(returnBytes);
        }

        return null;
    }

    public static String decrypted(String email){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PGPSecretKey keys = null;
        PGPPrivateKey privateKey = null;
        String decryptMessage = null;

        String encryptedMessage = null;

        encryptedMessage = OpenPGPEncryptDecrypt.readDownloadFile("encrypted");

        Log.e("Getir encrypt", encryptedMessage);
        try {
            keys = KeyOperation.getPrivateSecretKey(KeyOperation.readKeyFile(email + "_privateKey"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }


        String parola = "as";
        char[] charParola = parola.toCharArray();
        try {
            PBESecretKeyDecryptor b = new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC").build(charParola);
            privateKey = keys.extractPrivateKey(b);

        } catch (PGPException e) {
            e.printStackTrace();
        }

        try {
            if (encryptedMessage != "") {
                decryptMessage = decrypt(privateKey, encryptedMessage);
                //decryptMessage = decrypt(encryptedMessage, keys, "as");
                return decryptMessage;
            }else {
                Log.w("Getir encrypt","dosya okunamadı");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptMessage;
    }

    public static String readDownloadFile(String Name)  {

        String mainFile="Download";
        String fileName = Name + ".asc";
        java.io.File keyfile = new java.io.File(Environment.getExternalStorageDirectory().getAbsolutePath(), mainFile);
        keyfile.mkdir();
        File file = new File(keyfile, fileName);
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e("hata",e.toString());
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

}
