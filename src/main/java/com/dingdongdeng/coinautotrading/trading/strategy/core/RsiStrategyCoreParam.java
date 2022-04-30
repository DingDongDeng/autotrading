package com.dingdongdeng.coinautotrading.trading.strategy.core;

import com.dingdongdeng.coinautotrading.trading.strategy.annotation.GuideMessage;
import com.dingdongdeng.coinautotrading.trading.strategy.model.StrategyCoreParam;
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
public class RsiStrategyCoreParam implements StrategyCoreParam {

    @GuideMessage("매수 주문을 할 rsi 기준을 입력해주세요.")
    private double buyRsi; // 매수 주문을 할 rsi 기준

    @GuideMessage("이익중일때 익절할 rsi 기준을 입력해주세요.")
    private double profitRsi;  // 이익중일때 익절할 rsi 기준

    @GuideMessage("손실중일때 손절할 rsi 기준을 입력해주세요.")
    private double lossRsi;  // 손실중일때 손절할 rsi 기준

    @GuideMessage("익절 이익율 상한을 입력해주세요.")
    private double profitLimitPriceRate; // 익절 이익율 상한

    @GuideMessage("손절 손실율 상한을 입력해주세요.")
    private double lossLimitPriceRate;  // 손절 손실율 상한

    @GuideMessage("미체결 주문 취소를 위한 대기 시간(second)을 입력해주세요. ex) 30")
    private int tooOldOrderTimeSeconds;  // 초(second)

    @GuideMessage("한번에 주문할 금액을 입력해주세요.")
    private double orderPrice; // 한번에 주문할 금액

    @GuideMessage("계좌 안전 금액을 입력해주세요.")
    private double accountBalanceLimit;  //계좌 금액 안전 장치

}