package com.dingdongdeng.coinautotrading.trading.strategy;

import com.dingdongdeng.coinautotrading.trading.strategy.model.SpotTradingResult;
import com.dingdongdeng.coinautotrading.trading.strategy.model.type.TradingTag;
import lombok.Getter;

@Getter
public class StrategySpotRecorder implements StrategyRecorder<SpotTradingResult> {

    private double totalBuyPrice;
    private double totalProfitPrice;
    private double totalLossPrice;
    private double totalFee;
    private double marginPrice; // 이익금
    private double marginRate; // 이익율
    private String eventMessage = ""; //fixme 메모리 이슈 가능성이 있음

    @Override
    public void apply(SpotTradingResult tradingResult) {
        addEventMessage("주문", tradingResult);

        totalFee += tradingResult.getFee();
        TradingTag tag = tradingResult.getTradingTag();
        if (tag == TradingTag.BUY) {
            totalBuyPrice += tradingResult.getPrice() * tradingResult.getVolume();
            return;
        }
        if (tag == TradingTag.PROFIT) {
            totalProfitPrice += tradingResult.getPrice() * tradingResult.getVolume();
        }
        if (tag == TradingTag.LOSS) {
            totalLossPrice += tradingResult.getPrice() * tradingResult.getVolume();
        }
        calcMargin();
    }

    @Override
    public void revert(SpotTradingResult tradingResult) {
        addEventMessage("취소", tradingResult);

        totalFee -= tradingResult.getFee();
        TradingTag tag = tradingResult.getTradingTag();
        if (tag == TradingTag.BUY) {
            totalBuyPrice -= tradingResult.getPrice() * tradingResult.getVolume();
            return;
        }
        if (tag == TradingTag.PROFIT) {
            totalProfitPrice -= tradingResult.getPrice() * tradingResult.getVolume();
        }
        if (tag == TradingTag.LOSS) {
            totalLossPrice -= tradingResult.getPrice() * tradingResult.getVolume();
        }
        calcMargin();
    }

    private void calcMargin() {
        this.marginRate = ((totalProfitPrice + totalLossPrice - totalFee) / totalBuyPrice) * 100d - 100d;
        this.marginPrice = (totalProfitPrice + totalLossPrice - totalFee) - totalBuyPrice;
    }

    private void addEventMessage(String event, SpotTradingResult tradingResult) {
        String tagName = tradingResult.getTradingTag().getDesc();
        double price = tradingResult.getPrice();
        double orderPrice = tradingResult.getPrice() * tradingResult.getVolume();
        this.eventMessage += tradingResult.getCreatedAt() + "/" + event + " / " + tagName + " / " + price + " / " + orderPrice + "</br>\n";
    }
}