package com.dingdongdeng.coinautotrading.trading.exchange.client;

import com.dingdongdeng.coinautotrading.common.type.CoinExchangeType;
import com.dingdongdeng.coinautotrading.domain.entity.ExchangeKey;
import com.dingdongdeng.coinautotrading.domain.repository.ExchangeKeyRepository;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.BinanceFutureClient;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureEnum.Interval;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureEnum.Side;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureEnum.Symbol;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureEnum.TimeInForce;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureEnum.Type;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureAccountBalanceRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureCandleRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureChangeLeverageRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureChangePositionModeRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureMarkPriceRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureNewOrderRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureOrderCancelRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FutureOrderInfoRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureRequest.FuturePositionRiskRequest;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.BinanceServerTimeResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureAccountBalanceResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureCandleResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureChangeLeverageResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureChangePositionModeResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureMarkPriceResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureNewOrderResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureOrderCancelResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FutureOrderInfoResponse;
import com.dingdongdeng.coinautotrading.trading.exchange.future.client.model.BinanceFutureResponse.FuturePositionRiskResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class BinanceFutureClientTest {

    @Autowired
    private ExchangeKeyRepository exchangeKeyRepository;
    @Autowired
    private BinanceFutureClient binanceFutureClient;

    @Value("${binance.future.client.accessKey}")
    private String accessKey;
    @Value("${binance.future.client.secretKey}")
    private String secretKey;

    private String userId = "123456";
    private String keyPairId;

    @BeforeEach
    public void setUp() {
        String keyPairId = UUID.randomUUID().toString();

        exchangeKeyRepository.save(
            ExchangeKey.builder()
                .pairId(keyPairId)
                .coinExchangeType(CoinExchangeType.BINANCE_FUTURE)
                .name("ACCESS_KEY")
                .value(accessKey)
                .userId(userId)
                .build()
        );

        exchangeKeyRepository.save(
            ExchangeKey.builder()
                .pairId(keyPairId)
                .coinExchangeType(CoinExchangeType.BINANCE_FUTURE)
                .name("SECRET_KEY")
                .value(secretKey)
                .userId(userId)
                .build()
        );

        this.keyPairId = keyPairId;

    }


    @Test
    public void 서버_시간_조회_테스트() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        log.info("result : {}", timeResponse);
    }

    @Test
    public void 전체_계좌_조회_테스트() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        Long time = timeResponse.getServerTime();
        FutureAccountBalanceRequest request = FutureAccountBalanceRequest.builder()
            .timestamp(time)
            .build();
        List<FutureAccountBalanceResponse> responseList = binanceFutureClient.getFuturesAccountBalance(
            request, keyPairId);
        for (FutureAccountBalanceResponse balanceResponse : responseList) {
            if (balanceResponse.getBalance().equals("0.00000000")) {
                continue;
            } else {
                log.info("내 계좌: {}", balanceResponse);
            }
        }
        log.info("result : {}", responseList);
    }

    @Test
    public void 레버리지_바꾸기() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        Long time = timeResponse.getServerTime();
        FutureChangeLeverageRequest request = FutureChangeLeverageRequest.builder()
            .symbol("BTCUSDT")
            .leverage(40)
            .timestamp(time)
            .build();

        FutureChangeLeverageResponse leverageResponse = binanceFutureClient.changeLeverage(request,
            keyPairId);
        log.info("result : {}", leverageResponse);
    }

    @Test
    public void 포지션모드_바꾸기() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        Long time = timeResponse.getServerTime();
        FutureChangePositionModeRequest request = FutureChangePositionModeRequest.builder()
            .dualSidePosition("true")
            .timestamp(time)
            .build();

        FutureChangePositionModeResponse positionModeResponse = binanceFutureClient.changePositionMode(
            request, keyPairId);
        log.info("result : {}", positionModeResponse);
    }

    @Test
    public void 주문하기() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        Long time = timeResponse.getServerTime();
        FutureNewOrderRequest request = FutureNewOrderRequest.builder()
                .symbol("BTCUSDT")
                .type(Type.MARKET)
                .side(Side.BUY)
                .quantity(0.001)
                .timestamp(time)
                .build();

        FutureNewOrderResponse orderResponse = binanceFutureClient.order(
                request, keyPairId);
        log.info("result : {}", orderResponse);
    }

    @Test
    public void 주문정보조회(){

        FutureOrderInfoRequest request1 = FutureOrderInfoRequest.builder()
            .symbol(Symbol.USDT_BTC.getCode())
            .orderId("58652985361")
            .timestamp(System.currentTimeMillis())
            .build();

        FutureOrderInfoResponse futureOrderInfoResponse = binanceFutureClient.getFutureOrderInfo(request1, keyPairId);
        log.info("코인 정보 : {}", futureOrderInfoResponse);

    }


    @Test
    public void 주문_및_주문_취소() {
        BinanceServerTimeResponse timeResponse = binanceFutureClient.getServerTime();
        Long time = timeResponse.getServerTime();
        FutureNewOrderRequest request = FutureNewOrderRequest.builder()
            .symbol(Symbol.USDT_BTC.getCode())
            .type(Type.LIMIT)
            .side(Side.SELL)
            .price(19308.6)
            .quantity(0.01)
            .timeInForce(TimeInForce.GTC)
            .timestamp(time)
            .build();

        FutureNewOrderResponse orderResponse = binanceFutureClient.order(
            request, keyPairId);
        log.info("주문 : {}", orderResponse.getOrderId());

        FutureOrderInfoRequest request1 = FutureOrderInfoRequest.builder()
            .symbol(Symbol.USDT_BTC.getCode())
            .orderId(orderResponse.getOrderId().toString())
            .timestamp(time)
            .build();

        FutureOrderInfoResponse futureOrderInfoResponse = binanceFutureClient.getFutureOrderInfo(request1, keyPairId);
        log.info("코인 정보 : {}", futureOrderInfoResponse);

        FutureOrderCancelRequest futureOrderCancelRequest = FutureOrderCancelRequest.builder()
            .orderId(futureOrderInfoResponse.getOrderId().toString())
            .symbol(Symbol.USDT_BTC.getCode())
            .timestamp(time)
            .build();

        FutureOrderCancelResponse futureOrderCancelResponse = binanceFutureClient.orderCancel(futureOrderCancelRequest, keyPairId);
        log.info("취소 : {}", futureOrderCancelResponse);
    }

    @Test
    public void 캔들_조회(){
        FutureCandleRequest futureCandleRequest = FutureCandleRequest.builder()
            .symbol("BTCUSDT")
            .interval(Interval.MINUTE_1.getCode())
            .build();

        List<FutureCandleResponse> response = binanceFutureClient.getMinuteCandle(futureCandleRequest);
        log.info("캔들 조회 : {}", response);
    }

    @Test
    public void markPrice(){
        FutureMarkPriceRequest request = FutureMarkPriceRequest.builder()
                .symbol("BTCUSDT")
                .build();

        FutureMarkPriceResponse response = binanceFutureClient.getMarkPrice(request);
        log.info("시장 현재가 : {}", response);
    }

    @Test
    public void 리스크조회(){
        FuturePositionRiskRequest request = FuturePositionRiskRequest.builder()
            .symbol("BTCUSDT")
            .timestamp(System.currentTimeMillis())
            .build();

        List<FuturePositionRiskResponse> response = binanceFutureClient.getPositionRisk(request, keyPairId);
        log.info("risk info : {}", response);
    }
}