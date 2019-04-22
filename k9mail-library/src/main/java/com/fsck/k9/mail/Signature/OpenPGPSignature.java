package com.fsck.k9.mail.Signature;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.fsck.k9.mail.Key.KeyOperation;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;

interface StreamHandler {
    void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException;
}

public class OpenPGPSignature {
    private static  PGPPrivateKey pKey = null;
    private static  String imza = null;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String signArmoredAscii(PGPPrivateKey privateKey, String data, int signatureAlgo) throws IOException, PGPException {
        String signature = null;
        final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(privateKey.getPublicKeyPacket().getAlgorithm(), signatureAlgo));
        signatureGenerator.init(org.bouncycastle.openpgp.PGPSignature.BINARY_DOCUMENT, privateKey);
        ByteArrayOutputStream signatureOutput = new ByteArrayOutputStream();
        try( BCPGOutputStream outputStream = new BCPGOutputStream( new ArmoredOutputStream(signatureOutput)) ) {
            processStringAsStream(data, new StreamHandler() {
                @Override
                public void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException {
                    signatureGenerator.update(buffer, offset, length);
                }
            });
            signatureGenerator.generate().encode(outputStream);
        }

        signature = new String(signatureOutput.toByteArray(), "UTF-8");

        return signature;
    }
    public static boolean verify(InputStream signedData, InputStream signature, PGPPublicKey keys) {
        try {
            signature = PGPUtil.getDecoderStream(signature);
            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(signature);
            org.bouncycastle.openpgp.PGPSignature sig = ((PGPSignatureList) pgpFact.nextObject()).get(0);
            sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), keys);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = signedData.read(buff)) != -1) {
                sig.update(buff, 0, read);
            }
            signedData.close();
            return sig.verify();
        }
        catch (Exception ex) {
            // can we put a logger here please?
            return false;
        }
    }

    private static final int BUFFER_SIZE = 4096;

    static void processStream(InputStream is, StreamHandler handler) throws IOException {
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while( (read = is.read(buffer)) != -1 ) {
            handler.handleStreamBuffer(buffer, 0, read);
        }
    }

    static void processStringAsStream(String data, StreamHandler handler) throws IOException {
        InputStream is = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name()));
        }
        processStream(is, handler);
    }

    public  static String imzalama(String imzalanacak){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PGPSecretKey keys = null;

        try {
            keys = KeyOperation.getPrivateSecretKey(readKeyFile("as" + "_privateKey"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        String parola = "as";
        char[] charParola = parola.toCharArray();
        try {
            PBESecretKeyDecryptor b = new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC").build(charParola);
            pKey = keys.extractPrivateKey(b);
            Log.e("suradaa","defol lan");
        } catch (PGPException e) {
            e.printStackTrace();
        }
        try {
            Log.e("hatadaaaa","defol lan");
            imza = OpenPGPSignature.signArmoredAscii(pKey,imzalanacak , 2);
            Log.e("imza", imza );
            return imza;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ilkcat",e.toString());
        } catch (PGPException e) {
            e.printStackTrace();
            Log.e("ikinci",e.toString());
        }

        return parola;
    }
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
    public static String readSignatureFile(String Name) {

        String mainFile="Download";
        String fileName = Name + ".asc";
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
    public static void dogrula(String mej){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String imzalanmis=readSignatureFile("signature");
        InputStream mesajj = new ByteArrayInputStream(mej.getBytes());
        InputStream sign = new ByteArrayInputStream(imzalanmis.getBytes());

        PGPPublicKey keys = null;
        try {
            keys = (PGPPublicKey) KeyOperation.getPublicKey(readKeyFile("as" + "_publicKey"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }
        Log.e("dogrula", String.valueOf(verify(mesajj, sign, keys))) ;
    }
}
