package com.iskhakoff.guessgame.utils.cryptography;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;

public interface CryptographyManager {

    Cipher getInitializedCipherForEncryption(String keyName) throws InvalidKeyException, CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException;

    Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector) throws CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException;

    EncryptedData encryptData(String value, Cipher cipher);

    String decryptData(byte[] cipherText, Cipher cipher);

}
