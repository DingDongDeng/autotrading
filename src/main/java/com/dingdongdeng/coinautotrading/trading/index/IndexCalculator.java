package com.dingdongdeng.coinautotrading.trading.index;

import com.dingdongdeng.coinautotrading.trading.exchange.common.model.ExchangeCandles;
import com.dingdongdeng.coinautotrading.trading.exchange.common.model.ExchangeCandles.Candle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class IndexCalculator {

    private final int RSI_STANDARD_PERIOD = 14;

    public List<Double> getResistancePrice(ExchangeCandles candles) {
        Map<Double, Integer> priceMap = new HashMap<>();
        for (Candle candle : candles.getCandleList()) {
            Double price = candle.getTradePrice();
            if (Objects.isNull(priceMap.get(price))) {
                priceMap.put(price, 1);
            } else {
                priceMap.put(candle.getTradePrice(), priceMap.get(price) + 1);
            }
        }
        return priceMap.entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .map(Entry::getKey)
            .limit(10)
            .collect(Collectors.toList());
    }

    // RSI(지수 가중 이동 평균)
    // https://www.investopedia.com/terms/r/rsi.asp
    // https://rebro.kr/139
    public double getRsi(ExchangeCandles candles) {

        List<Candle> candleList = candles.getCandleList();

        double U = 0d;
        double D = 0d;
        for (int i = 0; i < candleList.size(); i++) {
            if (!hasNext(i, candleList.size())) {
                break;
            }
            double yesterdayPrice = candleList.get(i).getTradePrice();
            double todayPrice = candleList.get(i + 1).getTradePrice();

            if (todayPrice > yesterdayPrice) {
                U += todayPrice - yesterdayPrice;
            } else {
                D += yesterdayPrice - todayPrice;
            }
        }

        double AU = U / RSI_STANDARD_PERIOD;
        double AD = D / RSI_STANDARD_PERIOD;
        for (int i = 0; i < candleList.size(); i++) {
            if (!hasNext(i, candleList.size())) {
                break;
            }
            double yesterdayPrice = candleList.get(i).getTradePrice();
            double todayPrice = candleList.get(i + 1).getTradePrice();

            if (yesterdayPrice < todayPrice) {
                AU = ((RSI_STANDARD_PERIOD - 1) * AU + todayPrice - yesterdayPrice) / RSI_STANDARD_PERIOD;
                AD = ((RSI_STANDARD_PERIOD - 1) * AD + 0) / RSI_STANDARD_PERIOD;
            } else {
                AU = ((RSI_STANDARD_PERIOD - 1) * AU + 0) / RSI_STANDARD_PERIOD;
                AD = ((RSI_STANDARD_PERIOD - 1) * AD + yesterdayPrice - todayPrice) / RSI_STANDARD_PERIOD;
            }
        }

        double RS = AU / AD;
        return RS / (1 + RS);
    }

    private boolean hasNext(int index, int size) {
        return index + 1 < size;
    }
}
