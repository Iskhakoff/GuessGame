package com.iskhakoff.guessgame.view;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface GreetingView extends MvpView {
    void fadeIn();
    void startGame();
}
