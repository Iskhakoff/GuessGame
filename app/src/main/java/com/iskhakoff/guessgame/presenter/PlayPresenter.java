package com.iskhakoff.guessgame.presenter;

import android.util.Base64;
import android.util.Log;

import com.iskhakoff.guessgame.R;
import com.iskhakoff.guessgame.model.RepositoryPreferences;
import com.iskhakoff.guessgame.utils.EqualityEnum;
import com.iskhakoff.guessgame.utils.cryptography.CryptographyManager;
import com.iskhakoff.guessgame.utils.cryptography.CryptographyManagerImpl;
import com.iskhakoff.guessgame.utils.cryptography.EncryptedData;
import com.iskhakoff.guessgame.view.PlayView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Random;
import javax.crypto.Cipher;

import androidx.biometric.BiometricPrompt;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class PlayPresenter extends MvpPresenter<PlayView>{

    private static final String TAG = "PlayPresenter";

    private RepositoryPreferences repositoryPreferences;
    private EqualityEnum stateEquality;
    private CryptographyManager cryptographyManager;
    private String secretKeyName = "encryption_key";
    private byte[] initializationVector;
    private boolean readyToEncrypt = false;



    public PlayPresenter(RepositoryPreferences repositoryPreferences){
        this.repositoryPreferences = repositoryPreferences;
        cryptographyManager = new CryptographyManagerImpl();
        getViewState().showAuthenticateEncrypt();
    }

    public void generateValue(BiometricPrompt.CryptoObject cryptoObject){
        Random random = new Random();
        int range = 9999;
        String value = String.valueOf(1000 + random.nextInt(range - 1000));
//        remove the comment to see the generated value in the stacktrace
//        System.out.println("GENERATE VALUE = " + value);
        String encryptedValue = encryptValue(value, cryptoObject.getCipher());
        repositoryPreferences.setValueToPrefs(encryptedValue);
    }

    public void checkEquals (BiometricPrompt.CryptoObject cryptoObject, String userValue) {
        int enteredValue = Integer.parseInt(userValue);
        int savedValue = Integer.parseInt(decryptValue(cryptoObject.getCipher()));

        if(savedValue == enteredValue){
            stateEquality = EqualityEnum.EQUAL;
        }else{
            compareValues(enteredValue, savedValue);
        }

        showResult();
    }

    private void compareValues(int enteredValue, int savedValue){
        if(enteredValue > savedValue){
            stateEquality = EqualityEnum.GREATER;
        }else{
            stateEquality = EqualityEnum.LESS;
        }
    }

    private void showResult(){
        switch (stateEquality){
            case EQUAL:
                getViewState().showDialogStatusSucceed(R.string.congratulation);
                break;
            case LESS:
                getViewState().showDialogStatusFailure(R.string.less_value);
                break;
            case GREATER:
                getViewState().showDialogStatusFailure(R.string.greater_value);
                break;
        }
    }

    public void authenticateCancel(BiometricPrompt biometricPrompt){
        biometricPrompt.cancelAuthentication();
    }

    public void startNewGame(){
        getViewState().startNewGame();
    }

    public void closeGame(){
        getViewState().closeGame();
    }

    public void deleteFromPrefs(){
        repositoryPreferences.deleteValueFromPrefs();
    }

    public void authenticateToEncrypt(BiometricPrompt biometricPrompt, BiometricPrompt.PromptInfo info){
        readyToEncrypt = true;
        Cipher cipher = null;
        try {
            cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName);
        } catch (InvalidKeyException | CertificateException | UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "authenticateToEncryptError: ", e);
        }
        biometricPrompt.authenticate(info, new BiometricPrompt.CryptoObject(Objects.requireNonNull(cipher)));
    }

    public void authenticateToDecrypt(BiometricPrompt biometricPrompt, BiometricPrompt.PromptInfo info) {
        readyToEncrypt = false;
        Cipher cipher = null;
        try {
            cipher = cryptographyManager.getInitializedCipherForDecryption(secretKeyName, initializationVector);
        } catch (CertificateException | UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "authenticateToDecryptError: ", e);
        }
        biometricPrompt.authenticate(info, new BiometricPrompt.CryptoObject(Objects.requireNonNull(cipher)));
    }

    public void processData(BiometricPrompt.CryptoObject cryptoObject, String value){
        if(readyToEncrypt){
            generateValue(cryptoObject);
        }else{
            checkEquals(cryptoObject, value);
        }
    }

    private String encryptValue(String value, Cipher cipher){
        EncryptedData encryptedData = cryptographyManager.encryptData(value, cipher);
        byte[] cipherText = encryptedData.getCipherText();
        initializationVector = encryptedData.getInitializationVector();
        return Base64.encodeToString(cipherText, Base64.DEFAULT);
    }

    private String decryptValue(Cipher cipher){
        String encryptedData = repositoryPreferences.getValueFromPrefs();
        return cryptographyManager.decryptData(Base64.decode(encryptedData.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT), cipher);
    }
}
