package com.dingdongdeng.coinautotrading.trading.record;

import com.dingdongdeng.coinautotrading.trading.exchange.common.model.ExchangeCandles.Candle;
import com.dingdongdeng.coinautotrading.trading.index.Index;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyExecuteResult;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingInfo;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordContext {

    private LocalDateTime currentDateTime;
    private double currentPrice; // 현재가
    private Candle currentCandle;
    private Index index;
    private List<TradingResult> tradingResultList;

    public static RecordContext ofStrategyExecuteResult(StrategyExecuteResult executeResult) {
        TradingInfo tradingInfo = executeResult.getTradingInfo();
        Candle candle = tradingInfo.getCandles().getLatest(0);
        return new RecordContext(
            candle.getCandleDateTimeKst(),
            candle.getTradePrice(),
            candle,
            tradingInfo.getIndex(),
            Objects.isNull(executeResult.getTradingResultList()) ? new ArrayList<>() : executeResult.getTradingResultList()
        );
    }

    public static RecordContext ofStrategyExecuteResult(StrategyExecuteResult executeResult, Candle candle) {
        TradingInfo tradingInfo = executeResult.getTradingInfo();
        return new RecordContext(
            candle.getCandleDateTimeKst(),
            candle.getTradePrice(),
            candle,
            tradingInfo.getIndex(),
            Objects.isNull(executeResult.getTradingResultList()) ? new ArrayList<>() : executeResult.getTradingResultList()
        );
    }
}
