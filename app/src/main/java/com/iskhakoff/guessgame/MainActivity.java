package com.iskhakoff.guessgame;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iskhakoff.guessgame.presenter.GreetingPresenter;
import com.iskhakoff.guessgame.view.GreetingView;


public class MainActivity extends MvpAppCompatActivity implements GreetingView {

    @InjectPresenter
    GreetingPresenter presenter;

    private Button btnPlay;
    private TextView greetingText;
    private ImageView greetingPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        presenter.startAnimation();
    }

    @Override
    public void fadeIn() {
        greetingText.animate()
                .translationY(-(greetingText.getHeight() - 50))
                .alpha(1.0F)
                .setDuration(600);

        btnPlay.animate()
                .translationY(-(btnPlay.getHeight() - 50))
                .alpha(1.0f)
                .setDuration(600);

        greetingPerson.animate()
                .translationY(-(btnPlay.getHeight() - 50))
                .alpha(1.0f)
                .setDuration(600);
    }

    @Override
    public void startGame() {
        Intent intent = PlayActivity.newIntent(this);
        startActivity(intent);
    }

    private void setupUI() {
        btnPlay        = findViewById(R.id.btn_play);
        greetingText   = findViewById(R.id.greeting_text);
        greetingPerson = findViewById(R.id.greeting_person);

        btnPlay.setOnClickListener(view -> {
            presenter.startGame();
        });
    }
}