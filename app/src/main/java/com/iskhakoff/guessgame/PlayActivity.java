package com.iskhakoff.guessgame;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import androidx.biometric.BiometricPrompt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.iskhakoff.guessgame.model.RepositoryPreferences;
import com.iskhakoff.guessgame.presenter.PlayPresenter;
import com.iskhakoff.guessgame.view.PlayView;

import java.util.ArrayList;

public class PlayActivity extends MvpAppCompatActivity implements PlayView, TextWatcher {

    @InjectPresenter
    PlayPresenter presenter;
    private RepositoryPreferences repositoryPreferences;

    @ProvidePresenter
    PlayPresenter providePlayPresenter(){
        repositoryPreferences = new RepositoryPreferences(getApplicationContext());
        return new PlayPresenter(repositoryPreferences);
    }

    private static final int NUM_OF_DIGITS = 4;
    private ArrayList<TextInputLayout> editTextArray = new ArrayList<>(NUM_OF_DIGITS);
    private String numTemp;

    private Button btnCheck, btnStartNewGame;
    private Dialog dialog;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, PlayActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        biometricPrompt = createBiometricPrompt();
        promptInfo      = createPromptInfo();
        setupUI();
        setupEntryFields();
    }

//    setup and controls focus entries after del key
    public void setupEntryFields() {
        ViewGroup layout = findViewById(R.id.parent_numbers);
        for (int i = 0; i < layout.getChildCount(); i++) {
            TextInputLayout view = (TextInputLayout) layout.getChildAt(i);
            editTextArray.add(view);
            editTextArray.get(i).getEditText().addTextChangedListener(this);

            int final1 = i;
            editTextArray.get(i).getEditText().setOnKeyListener((view1, keyCode, keyEvent)->{
                if(keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    if(final1 != 0){
                        editTextArray.get(final1).getEditText().setText("");
                        editTextArray.get(final1 - 1).requestFocus();
                        editTextArray.get(final1 - 1).getEditText().setSelection(editTextArray.get(final1 - 1).getEditText().length());
                    }
                }
                return false;
            });
        }
        editTextArray.get(0).requestFocus();
    }


    private void cleanFields(){
        for (int i = 0; i < editTextArray.size(); i++) {
            editTextArray.get(i).getEditText().setText("");
            if(i == 0){
                editTextArray.get(i).requestFocus();
            }
        }
    }

//    get data each fields to string
    private String pickDataForEntries(ArrayList<TextInputLayout> widgets){
        StringBuilder commonResultString = new StringBuilder();
        for (TextInputLayout widget : widgets){
            commonResultString.append(widget.getEditText().getText());
        }
        return commonResultString.toString();
    }

    private void initDialog(int title, int imgDrawable, boolean isSuccess){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.getAttributes().windowAnimations = R.style.DialogAnimation;

        Button btnNewGame     = dialog.findViewById(R.id.dialog_btn_start_new_game);
        Button btnTryAgain    = dialog.findViewById(R.id.dialog_btn_try_again);
        TextView statusText   = dialog.findViewById(R.id.dialog_text_status);
        ImageView statusImage = dialog.findViewById(R.id.dialog_image_status);

        if(isSuccess){
            btnNewGame.setVisibility(View.VISIBLE);
        }else{
            btnTryAgain.setVisibility(View.VISIBLE);
            statusText.setTextColor(getResources().getColor(R.color.red, null));
        }
        statusText.setText(title);
        statusImage.setImageResource(imgDrawable);

        btnNewGame.setOnClickListener(view -> {
            dialog.cancel();
            presenter.startNewGame();
        });

        btnTryAgain.setOnClickListener(view -> {
            dialog.cancel();
            cleanFields();
        });

        dialog.setCancelable(true);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void showDialogStatusSucceed(int message) {
        initDialog(message, R.drawable.ic_success, true);
    }

    @Override
    public void showDialogStatusFailure(int message) {
        initDialog(message, R.drawable.ic_fail, false);
    }

    @Override
    public void startNewGame() {
        presenter.authenticateCancel(biometricPrompt);
        Intent intent = newIntent(this);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void closeGame() {
        this.finish();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        numTemp = charSequence.toString();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        for (int i = 0; i < editTextArray.size(); i++) {
            if(editable == editTextArray.get(i).getEditText().getEditableText()){
                if(editable.toString().equals("")){
                    return;
                }
                if(editable.toString().length() >= 2){
                    String newTemp = editable.toString().substring(editable.length() - 1, editable.length());
                    if(!newTemp.equals(numTemp)){
                        editTextArray.get(i).getEditText().setText(newTemp);
                    }else{
                        editTextArray.get(i).getEditText().setText(editable.toString().substring(0, editable.toString().length() - 1));
                    }
                }else if(i != editTextArray.size() - 1){
                    editTextArray.get(i + 1).requestFocus();
                    editTextArray.get(i + 1).getEditText().setSelection(editTextArray.get(i + 1).getEditText().length());
                    return;
                }
            }
        }
    }

    @Override
    public void showAuthenticateEncrypt() {
        presenter.authenticateToEncrypt(biometricPrompt, promptInfo);
    }

    @Override
    public void showAuthenticateDecrypt(){
        presenter.authenticateToDecrypt(biometricPrompt, promptInfo);
    }

    private BiometricPrompt.PromptInfo createPromptInfo(){
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Check fingerprint!")
                .setSubtitle("")
                .setDescription("")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .build();
    }

    private BiometricPrompt createBiometricPrompt(){
        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    presenter.closeGame();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                presenter.processData(result.getCryptoObject(), pickDataForEntries(editTextArray));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        };
        return new BiometricPrompt(this, getMainExecutor(), callback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    private void setupUI() {
        btnCheck        = findViewById(R.id.btn_check);
        btnStartNewGame = findViewById(R.id.btn_start_new_game);

        btnStartNewGame.setOnClickListener(view -> {
            presenter.startNewGame();
        });

        btnCheck.setOnClickListener(view -> {
            if(pickDataForEntries(editTextArray).length() == NUM_OF_DIGITS){
                showAuthenticateDecrypt();
            }else{
                Snackbar.make(findViewById(R.id.parent_play_view), R.string.entry_value_must_snackbar, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}