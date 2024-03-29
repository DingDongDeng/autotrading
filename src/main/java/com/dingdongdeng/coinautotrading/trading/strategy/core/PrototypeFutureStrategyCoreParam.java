package com.dingdongdeng.coinautotrading.trading.strategy.core;

import com.dingdongdeng.coinautotrading.trading.strategy.annotation.GuideMessage;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyCoreFutureParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrototypeFutureStrategyCoreParam implements StrategyCoreFutureParam {

    @GuideMessage("레버리지")
    private int leverage;

    @GuideMessage("매수 주문을 할 rsi 기준을 입력해주세요. ex) 0.30")
    private double buyRsi; // 매수 주문을 할 rsi 기준

    @GuideMessage("이익중일때 익절할 rsi 기준을 입력해주세요. ex) 0.7")
    private double profitRsi;  // 이익중일때 익절할 rsi 기준

    @GuideMessage("익절 이익율 상한을 입력해주세요. ex) 0.1 <== 10% 제한 의미")
    private double profitLimitPriceRate; // 익절 이익율 상한

    @GuideMessage("미체결 주문 취소를 위한 대기 시간(second)을 입력해주세요. ex) 30")
    private int tooOldOrderTimeSeconds;  // 초(second)

    @GuideMessage("최초 주문할 금액을 입력해주세요. ex) 40000")
    private double orderPrice; // 한번에 주문할 금액

    @GuideMessage("계좌 안전 금액을 입력해주세요.")
    private double accountBalanceLimit;  //계좌 금액 안전 장치

    @GuideMessage("분할 매수를 할 손실율(0.10을 설정하면 손실율이 10%가 될때마다 매수)")
    private double buyLossRate;

    @GuideMessage("분할 매수를 할때 수량 비율(1를 설정하면 분할 매수할때마다 보유 물량의 1배를 매수)")
    private double buyVolumeRate;

    @GuideMessage("프로세스 동작 주기(milliseconds)")
    private int processDuration;  // milliseconds
}
