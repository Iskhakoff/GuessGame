package com.iskhakoff.guessgame.view;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;


public interface PlayView extends MvpView {
    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAuthenticateEncrypt();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAuthenticateDecrypt();
    @StateStrategyType(SkipStrategy.class)
    void showDialogStatusSucceed(int message);
    @StateStrategyType(SkipStrategy.class)
    void showDialogStatusFailure(int message);
    @StateStrategyType(SkipStrategy.class)
    void startNewGame();
    @StateStrategyType(SkipStrategy.class)
    void closeGame();
}
