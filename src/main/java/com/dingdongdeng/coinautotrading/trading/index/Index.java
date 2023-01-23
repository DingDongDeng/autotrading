package com.dingdongdeng.coinautotrading.trading.index;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class Index {

    private double rsi;
    private List<Double> resistancePriceList;
    private Macd macd;

    @ToString
    @Getter
    @Builder
    public static class Macd {

        private double current;
        private double currentUptrendHighest; // 현재 상승 추세에서 가장 높은 macd (상승 추세가 아니라면 0)
        private double currentDowntrendLowest; // 현재 하락 추세에서 가장 낮은 macd (하락 추세가 아니라면 0)
        private double[] macds;

        public double getLatestMacd(int index) {
            return macds[macds.length - 1 - index];
        }
    }
}
