package com.dingdongdeng.autotrading.domain.chart.model

import com.dingdongdeng.autotrading.infra.common.type.CandleUnit
import java.time.LocalDateTime

class Chart(
    val from: LocalDateTime,
    val to: LocalDateTime,
    val currentPrice: Double,
    val candleUnit: CandleUnit,
    val candles: List<Candle>,
)