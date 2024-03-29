package com.dingdongdeng.coinautotrading.trading.record;

import com.dingdongdeng.coinautotrading.common.type.OrderType;
import com.dingdongdeng.coinautotrading.trading.strategy.model.type.TradingTag;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;

@Getter
public class Recorder {

    private final String id = UUID.randomUUID().toString();
    /**
     * fixme
     *  인메모리에서 들고있으면 안되고 다른 방법 필요
     *  저장 h2에다가 하고(파일 디비쓰면 메모리도 절약!)
     *  클라에서 차트 보여주느라 컨슈밍해야하는 얘들은 redis에서 읽고 다읽으면 h2에서 보충(contextLoader랑 같은 맥락이겠네)
     *  ++) 굳이 레디스 쓰기싫으면 인메모리에다가 저장하고.... 구현체만 나중에라도 바꿀수있게 래핑만해두자
     */
    private final List<RecordContext> recordContextList = new CopyOnWriteArrayList<>(); //fixme 어휴 이거 꼭써야해?
    private final int RECORD_CONTEXT_COUNT_LIMIT = 300000; // 1일 1440개 약 3~4일 치 //fixme autotradingProcessor도 쌓일꺼니까 개수 조절해야함

    private double totalBuyValue;
    private double totalProfitValue;
    private double totalLossValue;

    private double totalFee;
    private double marginPrice; // 이익금
    private double marginRate; // 이익율
    private String eventMessageFormat = "%20s / %5s / %10s <br>";
    private String eventMessage = String.format(eventMessageFormat, "시간", "주문", "현재가");

    public void record(RecordContext recordContext) {
        this.addRecordContextList(recordContext);

        recordContext.getTradingResultList().forEach(tradingResult -> {
            OrderType orderType = tradingResult.getOrderType();
            TradingTag tradingTag = tradingResult.getTradingTag();

            // 주문 취소 일때
            if (orderType == OrderType.CANCEL) {
                switch (tradingResult.getTradingTag()) {
                    case BUY -> totalBuyValue -= tradingResult.getPrice() * tradingResult.getVolume();
                    case PROFIT -> totalProfitValue -= tradingResult.getPrice() * tradingResult.getVolume();
                    case LOSS -> totalLossValue -= tradingResult.getPrice() * tradingResult.getVolume();
                }
            } else { // 주문(매수,매도) 일때
                switch (tradingResult.getTradingTag()) {
                    case BUY -> totalBuyValue += tradingResult.getPrice() * tradingResult.getVolume();
                    case PROFIT -> totalProfitValue += tradingResult.getPrice() * tradingResult.getVolume();
                    case LOSS -> totalLossValue += tradingResult.getPrice() * tradingResult.getVolume();
                }

                // 수수료 계산(반올림)
                this.totalFee += Math.round(tradingResult.getFee());
            }

            // 마진 계산(매수만 한 시점에서는 이익/손실 실현이 안됐기때문에 계산하지 않음)
            if (tradingTag != TradingTag.BUY) {
                // 이익율 소수점 둘째자리 아래 반올림 (xx.xx%)
                this.marginRate = Math.round((((totalProfitValue + totalLossValue - totalFee) / totalBuyValue) * 100d - 100d) * 100) / 100.0;
                // 이익금 소수점 반올림
                this.marginPrice = Math.round((totalProfitValue + totalLossValue - totalFee) - totalBuyValue);
            }

            // 메세지 기록
            double currentPrice = recordContext.getCurrentPrice();
            eventMessage += String.format(
                eventMessageFormat,
                recordContext.getCurrentDateTime(),
                tradingTag.getDesc(),
                currentPrice
            );
        });
    }

    private void addRecordContextList(RecordContext recordContext) {
        this.recordContextList.add(recordContext);
        if (this.recordContextList.size() > RECORD_CONTEXT_COUNT_LIMIT) {
            this.recordContextList.remove(0);
        }
    }
}
