package com.dingdongdeng.coinautotrading.trading.strategy;

import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyCoreParam;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingInfo;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingResult;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingResultPack;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingTask;
import java.util.List;

public interface StrategyCore<T extends TradingResult> {

    List<TradingTask> makeTradingTask(TradingInfo tradingInfo, TradingResultPack<T> tradingResultPack);

    void handleOrderResult(T tradingResult);

    void handleOrderCancelResult(T tradingResult);

    StrategyCoreParam getParam();

}
