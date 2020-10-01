package com.iskhakoff.guessgame.presenter;

import com.iskhakoff.guessgame.view.GreetingView;

import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class GreetingPresenter extends MvpPresenter<GreetingView> {

    public void startAnimation(){
        getViewState().fadeIn();
    }

    public void startGame(){
        getViewState().startGame();
    }
}
