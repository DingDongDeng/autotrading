package com.dingdongdeng.coinautotrading.trading.strategy.core;

import com.dingdongdeng.coinautotrading.trading.exchange.common.model.ExchangeCandles;
import com.dingdongdeng.coinautotrading.trading.exchange.common.model.ExchangeCandles.Candle;
import com.dingdongdeng.coinautotrading.trading.index.Index;
import com.dingdongdeng.coinautotrading.trading.strategy.StrategyCore;
import com.dingdongdeng.coinautotrading.trading.strategy.model.SpotTradingInfo;
import com.dingdongdeng.coinautotrading.trading.strategy.model.SpotTradingResult;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyCoreParam;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingResultPack;
import com.dingdongdeng.coinautotrading.trading.strategy.model.TradingTask;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TrendSwitchTradingStrategyCore implements StrategyCore<SpotTradingInfo, SpotTradingResult> {

    private final TrendSwitchTradingStrategyCoreParam param;

    private final BBandsTradingStrategyCore bBandsTradingStrategyCore;
    private final MacdTradingStrategyCore macdTradingStrategyCore;

    public TrendSwitchTradingStrategyCore(TrendSwitchTradingStrategyCoreParam param) {
        this.param = param;
        this.bBandsTradingStrategyCore = new BBandsTradingStrategyCore(
            BBandsTradingStrategyCoreParam.builder()
                .initOrderPrice(param.getInitOrderPrice())
                .conditionTimeBuffer(param.getConditionTimeBuffer())
                .accountBalanceLimit(param.getAccountBalanceLimit())
                .tooOldOrderTimeSeconds(param.getTooOldOrderTimeSeconds())
                .processDuration(param.getProcessDuration())
                .build()
        );
        this.macdTradingStrategyCore = new MacdTradingStrategyCore(
            MacdTradingStrategyCoreParam.builder()
                .initOrderPrice(param.getInitOrderPrice())
                .conditionTimeBuffer(param.getConditionTimeBuffer())
                .accountBalanceLimit(param.getAccountBalanceLimit())
                .tooOldOrderTimeSeconds(param.getTooOldOrderTimeSeconds())
                .processDuration(param.getProcessDuration())
                .build()
        );
    }

    @Override
    public List<TradingTask> makeTradingTask(SpotTradingInfo tradingInfo, TradingResultPack<SpotTradingResult> tradingResultPack) {
        Index index = tradingInfo.getIndex();
        ExchangeCandles candles = tradingInfo.getCandles();

        /**
         * FIXME
         *  볼린저전략 손절 미끄럼틀 해결 필요
         *      - macd를 볼까??
         *
         *  TODO
         *      하락장은 lower에 사서 middel 판매
         *      횡보장은 lower에 사서 higer에 판매
         */

        // 상승장이라면
        if (isUptrend(index, candles)) {
            log.info("[분기 조건] 상승 추세");
            return macdTradingStrategyCore.makeTradingTask(tradingInfo, tradingResultPack);
        }
        // 하락장이라면
        else if (isDowntrend(index, candles)) {
            log.info("[분기 조건] 하락 추세");
            return bBandsTradingStrategyCore.makeTradingTask(tradingInfo, tradingResultPack);
        }
        // 횡보장이라면
        else {
            log.info("[분기 조건] 횡보 추세");
            return bBandsTradingStrategyCore.makeTradingTask(tradingInfo, tradingResultPack);
        }
    }

    @Override
    public void handleOrderResult(SpotTradingResult tradingResult) {
        macdTradingStrategyCore.handleOrderResult(tradingResult);
        bBandsTradingStrategyCore.handleOrderResult(tradingResult);
    }

    @Override
    public void handleOrderCancelResult(SpotTradingResult tradingResult) {
        macdTradingStrategyCore.handleOrderCancelResult(tradingResult);
        bBandsTradingStrategyCore.handleOrderCancelResult(tradingResult);
    }

    @Override
    public StrategyCoreParam getParam() {
        return this.param;
    }

    private boolean isUptrend(Index index, ExchangeCandles candles) {
        int TARGET_CANDLE_COUNT = 10;
        double ema60 = index.getMa().getEma60();
        List<Candle> candleList = candles.getCandleList();
        int offset = candleList.size() - TARGET_CANDLE_COUNT;

        for (int i = offset; i < candleList.size(); i++) {
            if (candleList.get(i).getTradePrice() < ema60) {
                return false;
            }

        }
        return true;
    }

    private boolean isDowntrend(Index index, ExchangeCandles candles) {
        int TARGET_CANDLE_COUNT = 10;
        double ema60 = index.getMa().getEma60();
        List<Candle> candleList = candles.getCandleList();
        int offset = candleList.size() - TARGET_CANDLE_COUNT;

        for (int i = offset; i < candleList.size(); i++) {
            if (candleList.get(i).getTradePrice() > ema60) {
                return false;
            }

        }
        return true;
    }
}
