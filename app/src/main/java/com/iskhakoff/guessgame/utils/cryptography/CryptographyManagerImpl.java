package com.iskhakoff.guessgame.utils.cryptography;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptographyManagerImpl implements CryptographyManager {
    private final int KEY_SIZE = 256;
    private final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private final String ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    private final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    private final String ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;

    @Override
    public Cipher getInitializedCipherForEncryption(String keyName) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException, IOException, CertificateException {
        Cipher cipher = getCipher();
        SecretKey secretKey = getOrCreateSecretKey(keyName);
        if (cipher != null) {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
        return cipher;
    }

    @Override
    public Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException, IOException, CertificateException {
        Cipher cipher = getCipher();
        SecretKey secretKey = getOrCreateSecretKey(keyName);
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, initializationVector));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    @Override
    public EncryptedData encryptData(String value, Cipher cipher) {
        byte[] cipherText = new byte[]{};
        EncryptedData model = new EncryptedData();
        try {
            cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        model.setCipherText(cipherText);
        model.setInitializationVector(cipher.getIV());
        return model;
    }

    @Override
    public String decryptData(byte[] cipherText, Cipher cipher){
        byte[] value = new byte[]{};
        try {
            value = cipher.doFinal(cipherText);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public Cipher getCipher(){
        String transformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/" + ENCRYPTION_PADDING;

        try {
            return Cipher.getInstance(transformation);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SecretKey getOrCreateSecretKey(String keyName) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        if(keyStore.getKey(keyName, null) != null){
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(keyName, null)).getSecretKey();
        }else{
            KeyGenParameterSpec.Builder paramsBuilder = new KeyGenParameterSpec.Builder(
                    keyName,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);

            paramsBuilder.setBlockModes(ENCRYPTION_BLOCK_MODE)
                    .setEncryptionPaddings(ENCRYPTION_PADDING)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(true);

            KeyGenParameterSpec keyGenParams = paramsBuilder.build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(keyGenParams);
            return keyGenerator.generateKey();
        }
    }
}
