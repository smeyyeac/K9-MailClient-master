package com.fsck.k9.mail.OpenPGP;

import android.os.Environment;
import android.util.Log;

import com.fsck.k9.mail.Key.KeyOperation;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;


import java.util.Iterator;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.util.io.Streams;


public class OpenPGPEncryptDecrypt {
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();
    private static final String PROVIDER = "BC";
    public  static String signAndEncrypt = "";

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
        //PGPPublicKey publicKey = null;
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

    public static String decrypted(String messageTo, String messageFrom, String password, String encryptedMessage){
        Security.addProvider(new BouncyCastleProvider());
        String decryptMessage = null;
        Log.e("Getir encrypt", encryptedMessage);
        if ((encryptedMessage != "") && (decryptMessage == null) ) {
            try {
                Log.e("GetirKeyRead", KeyOperation.readKeyFile( messageTo + "_privateKey"));
                String  readKey = KeyOperation.readKeyFile(messageTo + "_privateKey");
                decryptMessage = decrypt(encryptedMessage , readKey, password);
                signAndEncrypt = "";
                return decryptMessage;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Log.w("Getir encrypt","dosya okunamadı");
        }

        if ((encryptedMessage != "") && (decryptMessage == null)) {
            try {
                Log.e("GetirMFrom", messageFrom);
                String pathFrom = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/" + messageFrom + "_publicKey.asc";
                ArrayList publicKeyList = publicKeyList(pathFrom);
                PGPPublicKey publicKey = getPublicKey(pathFrom, String.valueOf(publicKeyList.get(0)));

                Log.e("GetirMTo", messageTo);

                String pathTo = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/" + messageTo + "_privateKey.asc";
                ArrayList privateKeyList = privateKeyList(pathTo);
                loadPrivateKey(pathTo, String.valueOf(privateKeyList.get(1)), password);

                ByteArrayInputStream encyptInputStream = new ByteArrayInputStream(encryptedMessage.getBytes());

                decryptMessage = decryptVerifyMessage(encyptInputStream, privateKey, publicKey);


                Log.e("GETİRRRSONUCC", signAndEncrypt);
                /*byte[] returnBytes = out.toByteArray();
                out.close();
                decryptMessage = "";
                decryptMessage = new String(returnBytes);
                out.flush();*/
            } catch (IOException e) {
                e.printStackTrace();
            } catch (PGPException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                    Log.e("GetirPublicList",Long.toHexString(k.getKeyID()).toUpperCase());

                }
            }
        }
        is.close();
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
        is.close();
        return publicKey;
    }


    private static PGPPrivateKey privateKey;

    public static ArrayList<String> privateKeyList(String path) throws IOException, PGPException {
        ArrayList<String> keys = new ArrayList<>();
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPSecretKeyRingCollection keyRings = new  PGPSecretKeyRingCollection(is , calculator);

        Iterator<PGPSecretKeyRing> it = keyRings.getKeyRings();
        while (it.hasNext()) {
            PGPSecretKeyRing keyRing = it.next();
            Iterator<PGPSecretKey> kit = keyRing.getSecretKeys();
            while (kit.hasNext()) {
                PGPSecretKey k = kit.next();
                if (k.isSigningKey()) {
                    keys.add(Long.toHexString(k.getKeyID()).toUpperCase());
                    Log.e("GetirPrivateList",Long.toHexString(k.getKeyID()).toUpperCase());

                }
            }
        }
        is.close();
        return keys;
    }
    public static void loadPrivateKey(String path, String id, String password) throws IOException, PGPException {
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(is, new BcKeyFingerprintCalculator());
        Iterator<PGPSecretKeyRing> it = keyRings.getKeyRings();
        privateKey = null;
        while(it.hasNext() && privateKey == null) {
            PGPSecretKeyRing keyRing = it.next();
            Iterator<PGPSecretKey> kit = keyRing.getSecretKeys();
            while(kit.hasNext() && privateKey == null) {
                PGPSecretKey k= kit.next();
                if(Long.toHexString(k.getKeyID()).toUpperCase().contains(id.toUpperCase())) {
                    PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder((new BcPGPDigestCalculatorProvider())).build(password.toCharArray());
                    privateKey = k.extractPrivateKey(decryptor);
                }
            }
        }

        if(privateKey == null) {
            throw new IllegalArgumentException("No private key found with given ID!");
        }
        is.close();
    }


    public static String decryptVerifyMessage(InputStream in, PGPPrivateKey privateKey, PGPPublicKey publicKey) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        in = new ArmoredInputStream(in);

        BcPGPObjectFactory pgpF = new BcPGPObjectFactory(in);
        PGPEncryptedDataList enc = (PGPEncryptedDataList) pgpF.nextObject();

        BcPGPObjectFactory plainFact = new BcPGPObjectFactory(((PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next()).getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey)));
        Object message = null;

        PGPOnePassSignatureList onePassSignatureList = null;
        PGPSignatureList signatureList = null;
        PGPCompressedData compressedData = null;

        message = plainFact.nextObject();
        ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();

        while (message != null) {
            System.out.println(message.toString());
            if (message instanceof PGPCompressedData) {
                compressedData = (PGPCompressedData) message;
                plainFact = new BcPGPObjectFactory(compressedData.getDataStream());
                message = plainFact.nextObject();
                System.out.println(message.toString());
            }

            if (message instanceof PGPLiteralData) {
                Streams.pipeAll(((PGPLiteralData) message).getInputStream(), actualOutput);
            } else if (message instanceof PGPOnePassSignatureList) {
                onePassSignatureList = (PGPOnePassSignatureList) message;
            } else if (message instanceof PGPSignatureList) {
                signatureList = (PGPSignatureList) message;
            } else {
                throw new PGPException("message unknown message type.");
            }
            message = plainFact.nextObject();
        }
        actualOutput.close();
        byte[] output = actualOutput.toByteArray();
        if (onePassSignatureList == null || signatureList == null) {
            throw new PGPException("Poor PGP. Signatures not found.");
        } else {

            for (int i = 0; i < onePassSignatureList.size(); i++) {
                PGPOnePassSignature ops = onePassSignatureList.get(0);
                System.out.println("verifier : " + ops.getKeyID());
                if (publicKey != null) {
                    ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
                    ops.update(output);
                    PGPSignature signature = signatureList.get(i);
                    if (ops.verify(signature)) {
                        Iterator<?> userIds = publicKey.getUserIDs();
                        while (userIds.hasNext()) {
                            String userId = (String) userIds.next();
                            System.out.println("Signed by " + userId);
                        }
                        signAndEncrypt = "true" ;
                        System.out.println("Signature verified");
                    } else {
                        signAndEncrypt = "false" ;
                        throw new SignatureException("Signature verification failed");
                    }
                }
            }
        }

        in.close();
        out.write(output);
        byte[] returnBytes = out.toByteArray();
        out.close();
        out.flush();
        return new String(returnBytes);
    }

    public static PGPSecretKey readSecretKey(String path) throws IOException, PGPException {
        InputStream in = PGPUtil.getDecoderStream(new FileInputStream(path));
        PGPSecretKeyRingCollection keyRingCollection = new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());
        PGPSecretKey secretKey = null;

        Iterator<PGPSecretKeyRing> rIt = keyRingCollection.getKeyRings();
        while (secretKey == null && rIt.hasNext()) {
            PGPSecretKeyRing keyRing = rIt.next();
            Iterator<PGPSecretKey> kIt = keyRing.getSecretKeys();
            while (secretKey == null && kIt.hasNext()) {
                PGPSecretKey key = kIt.next();
                if (key.isSigningKey()) {
                    secretKey = key;
                }
            }
        }
        Log.e("GetirSecretKey", String.valueOf(secretKey));
        return secretKey;
    }
    public static String signAndEncrypted(String message, String emailSign, String password, String emailTo){
        String signEncryptMessage = "";
        String pathSign = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/" + emailSign + "_privateKey.asc";
        String pathEncrpt = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/keyFile/" + emailTo + "_publicKey.asc";

        PGPSecretKey secretKey = null;
        PGPPublicKey publicKey = null;

        try {
            secretKey = readSecretKey(pathSign);
            ArrayList keyList = publicKeyList(pathEncrpt);
            Log.e("GetirList EncrytId" ,String.valueOf(keyList.get(1)));
            publicKey = getPublicKey(pathEncrpt, String.valueOf(keyList.get(1)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        if(secretKey != null && publicKey != null){
            try {
                signEncryptMessage = signAndEncrypt(message.getBytes(), secretKey, password, publicKey);
            } catch (PGPException e) {
                e.printStackTrace();
            }
        }

        if (signEncryptMessage == null){
            throw new IllegalArgumentException("Şifreli imzalı mesaj oluşturulamadı!!!");
        }

        return signEncryptMessage;
    }

    private static String signAndEncrypt( byte[] message,  PGPSecretKey secretKey , String secretPwd,  PGPPublicKey publicKey) throws PGPException {
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder( SymmetricKeyAlgorithmTags.AES_256 ).setWithIntegrityPacket( true )
                            .setSecureRandom(new SecureRandom())
                            .setProvider( provider ) );

            encryptedDataGenerator.addMethod(
                    new JcePublicKeyKeyEncryptionMethodGenerator( publicKey )
                            .setSecureRandom( new SecureRandom() ).setProvider( provider ) );

            OutputStream theOut = true ? new ArmoredOutputStream( out ) : out;
            OutputStream encryptedOut = encryptedDataGenerator.open( theOut, new byte[4096] );

            PGPCompressedDataGenerator compressedDataGenerator =
                    new PGPCompressedDataGenerator( CompressionAlgorithmTags.ZIP );
            OutputStream compressedOut = compressedDataGenerator.open( encryptedOut, new byte[4096] );

            PGPPrivateKey privateKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder().setProvider( provider ).build( secretPwd.toCharArray() ) );

            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder( secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1 ).setProvider( provider ) );

            signatureGenerator.init( PGPSignature.BINARY_DOCUMENT, privateKey );
            Iterator<?> it = secretKey.getPublicKey().getUserIDs();
            if ( it.hasNext() )
            {
                PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
                spGen.setSignerUserID( false, ( String ) it.next() );
                signatureGenerator.setHashedSubpackets( spGen.generate() );
            }
            signatureGenerator.generateOnePassVersion( false ).encode( compressedOut );
            PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            OutputStream literalOut = literalDataGenerator.open( compressedOut, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(), new byte[4096] );
            InputStream in = new ByteArrayInputStream( message );
            byte[] buf = new byte[4096];
            for ( int len; ( len = in.read( buf ) ) > 0; )
            {
                literalOut.write( buf, 0, len );
                signatureGenerator.update( buf, 0, len );
            }
            in.close();
            literalDataGenerator.close();
            signatureGenerator.generate().encode( compressedOut );
            compressedDataGenerator.close();
            encryptedDataGenerator.close();
            theOut.close();
            return out.toString();
        }
        catch ( Exception e )
        {
            throw new PGPException( "Error in signAndEncrypt", e );
        }
    }
}
