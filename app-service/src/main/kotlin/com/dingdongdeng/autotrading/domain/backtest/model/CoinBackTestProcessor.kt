package com.dingdongdeng.autotrading.domain.backtest.model

import com.dingdongdeng.autotrading.domain.autotrade.model.CoinAutoTradeProcessor
import com.dingdongdeng.autotrading.domain.chart.service.CoinChartService
import com.dingdongdeng.autotrading.domain.process.model.Processor
import com.dingdongdeng.autotrading.domain.strategy.service.CoinStrategyService
import com.dingdongdeng.autotrading.domain.strategy.type.CoinStrategyType
import com.dingdongdeng.autotrading.domain.trade.service.CoinTradeService
import com.dingdongdeng.autotrading.infra.common.exception.WarnException
import com.dingdongdeng.autotrading.infra.common.type.CandleUnit
import com.dingdongdeng.autotrading.infra.common.type.CoinType
import com.dingdongdeng.autotrading.infra.common.type.ExchangeType
import com.dingdongdeng.autotrading.infra.common.utils.TimeContext
import java.time.LocalDateTime
import java.util.UUID

class CoinBackTestProcessor(
    override val id: String = "BACKTEST-${UUID.randomUUID()}",
    override val userId: Long,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val durationUnit: CandleUnit, // 백테스트 시간 간격

    val coinStrategyType: CoinStrategyType,
    val exchangeType: ExchangeType,
    val coinTypes: List<CoinType>,
    val candleUnits: List<CandleUnit>,
    val config: Map<String, Any>,
    private val coinChartService: CoinChartService,
    private val coinTradeService: CoinTradeService,
    private val coinStrategyService: CoinStrategyService,
) : Processor(
    id = id,
    userId = userId,
    duration = 0,
    slackSender = null,
) {
    private val autoTradeProcessor: Processor = CoinAutoTradeProcessor(
        id = id,
        userId = userId,
        coinStrategyType = coinStrategyType,
        exchangeType = ExchangeType.BACKTEST,
        coinTypes = coinTypes,
        candleUnits = candleUnits,
        keyPairId = "",
        config = config,
        duration = 0,
        slackSender = null,
        coinChartService = coinChartService,
        coinTradeService = coinTradeService,
        coinStrategyService = coinStrategyService,
    )
    private var initialize = false

    init {
        validateBackTestRange()
    }

    override fun process() {
        autoTradeProcessor.process()
    }

    override fun runnable(): Boolean {
        if (initialize.not()) {
            TimeContext.update { startDateTime }
            initialize = true
        }
        val now = TimeContext.now().plusSeconds(durationUnit.getSecondSize())
        TimeContext.update { now }
        return now < endDateTime
    }

    private fun validateBackTestRange() {
        coinTypes.forEach { coinType ->
            val availBackTestRanges = getAvailBackTestRanges(coinType)
            if (availBackTestRanges.none { it.isRanged(coinType, startDateTime, endDateTime) }) {
                throw WarnException.of("백테스트 불가능한 구간입니다. availBackTestRanges=$availBackTestRanges")
            }
        }
    }

    private fun getAvailBackTestRanges(coinType: CoinType): List<AvailBackTestRange> {
        val minUnit = CandleUnit.min()
        val missingCandles = coinChartService.getMissingChart(
            exchangeType = exchangeType,
            coinType = coinType,
            candleUnit = minUnit,
            from = startDateTime,
            to = endDateTime,
        ).candles

        val availBackTestRanges = mutableListOf<AvailBackTestRange>()
        missingCandles.windowed(2, 1) { subList ->
            val firstMissingDateTime = subList.first().candleDateTimeKst
            val lastMissingDateTime = subList.last().candleDateTimeKst

            // 누락된 캔들이 연속된 시간에 존재하면 생략
            if (firstMissingDateTime.plusSeconds(minUnit.getSecondSize()) >= lastMissingDateTime) {
                return@windowed
            }

            availBackTestRanges.add(
                AvailBackTestRange(
                    exchangeType = exchangeType,
                    coinType = coinType,
                    startDateTime = firstMissingDateTime,
                    endDateTime = lastMissingDateTime,
                )
            )
        }
        return availBackTestRanges
    }
}