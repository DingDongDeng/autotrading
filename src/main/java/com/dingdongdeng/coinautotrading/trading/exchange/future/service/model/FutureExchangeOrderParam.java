package com.dingdongdeng.coinautotrading.trading.exchange.future.service.model;

import com.dingdongdeng.coinautotrading.common.type.CoinType;
import com.dingdongdeng.coinautotrading.common.type.OrderType;
import com.dingdongdeng.coinautotrading.common.type.PriceType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class FutureExchangeOrderParam {

    private CoinType coinType; // 마켓 ID (필수)
    private OrderType orderType; // 주문 종류 (필수) - bid : 매수 - ask : 매도
    private Double volume; // 주문량 (지정가, 시장가 매도 시 필수)
    private Double price; // 주문 가격. (지정가, 시장가 매수 시 필수)  ex) KRW-BTC 마켓에서 1BTC당 1,000 KRW로 거래할 경우, 값은 1000 이 된다. ex) KRW-BTC 마켓에서 1BTC당 매도 1호가가 500 KRW 인 경우, 시장가 매수 시 값을 1000으로 세팅하면 2BTC가 매수된다. (수수료가 존재하거나 매도 1호가의 수량에 따라 상이할 수 있음)
    private PriceType priceType; // 주문 타입 (필수) - limit : 지정가 주문 - price : 시장가 주문(매수) - market : 시장가 주문(매도)
}
