package com.dingdongdeng.autotrading.application.backtest

import com.dingdongdeng.autotrading.application.backtest.model.CoinBackTestResultDto
import com.dingdongdeng.autotrading.application.backtest.model.CoinBackTestTradeHistory
import com.dingdongdeng.autotrading.application.backtest.model.CoinBackTestTradeStatistics
import com.dingdongdeng.autotrading.domain.autotrade.factory.AutoTradeProcessorFactory
import com.dingdongdeng.autotrading.domain.backtest.factory.BackTestProcessorFactory
import com.dingdongdeng.autotrading.domain.backtest.model.CoinBackTestProcessor
import com.dingdongdeng.autotrading.domain.process.repository.ProcessRepository
import com.dingdongdeng.autotrading.domain.strategy.type.CoinStrategyType
import com.dingdongdeng.autotrading.domain.trade.service.CoinTradeService
import com.dingdongdeng.autotrading.infra.common.annotation.UseCase
import com.dingdongdeng.autotrading.infra.common.type.CandleUnit
import com.dingdongdeng.autotrading.infra.common.type.CoinType
import com.dingdongdeng.autotrading.infra.common.type.ExchangeType
import com.dingdongdeng.autotrading.infra.common.utils.round
import java.time.LocalDateTime

@UseCase
class CoinBackTestUseCase(
    private val processRepository: ProcessRepository,
    private val autoTradeProcessorFactory: AutoTradeProcessorFactory,
    private val backTestProcessorFactory: BackTestProcessorFactory,
    private val coinTradeService: CoinTradeService,
) {
    fun backTest(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        durationUnit: CandleUnit, // 백테스트 시간 간격
        userId: Long,
        coinStrategyType: CoinStrategyType,
        exchangeType: ExchangeType,
        coinTypes: List<CoinType>,
        candleUnits: List<CandleUnit>,
        config: Map<String, Any>
    ): String {
        val autoTradeProcessor = autoTradeProcessorFactory.of(
            userId = userId,
            exchangeType = exchangeType,
            coinTypes = coinTypes,
            candleUnits = candleUnits,
            config = config,
            coinStrategyType = coinStrategyType,
        )
        val backTestProcessor = backTestProcessorFactory.of(
            exchangeType = exchangeType,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            durationUnit = durationUnit,
            autoTradeProcessor = autoTradeProcessor,
        )
        processRepository.save(backTestProcessor)
        backTestProcessor.start()
        return backTestProcessor.id
    }

    fun getResult(backTestProcessorId: String): CoinBackTestResultDto {
        val processor = processRepository.findById(backTestProcessorId) as CoinBackTestProcessor
        val tradeResult = coinTradeService.getTradeResult(
            exchangeType = processor.exchangeType,
            processorId = processor.id,
            coinTypes = processor.coinTypes,
            now = processor.now(),
        )

        return CoinBackTestResultDto(
            backTestProcessorId = processor.id,
            progressRate = processor.progressRate().round(2.0),
            startDateTime = processor.startDateTime,
            endDateTime = processor.endDateTime,
            totalProfitRate = tradeResult.totalProfitRate.round(2.0),
            totalProfitPrice = tradeResult.totalProfitPrice.round(),
            totalAccProfitValuePrice = tradeResult.totalAccProfitPrice.round(),
            totalFee = tradeResult.totalFee.round(),
            tradeHistories = tradeResult.details
                .map { it.summary }
                .filter { it.tradeHistories.isNotEmpty() }
                .associate {
                    val coinType = it.tradeHistories.first().coinType
                    val histories = it.tradeHistories.map { history ->
                        CoinBackTestTradeHistory(
                            coinType = history.coinType,
                            orderType = history.orderType,
                            volume = history.volume.round(8.0),
                            price = history.price.round(),
                            profit = history.profit.round(),
                            tradeAt = history.tradedAt,
                        )
                    }
                    coinType to histories
                },
            tradeStatistics = tradeResult.details
                .map { it.statistics }
                .associate {
                    val coinType = it.first().coinType
                    val statistics = it.map { stat ->
                        CoinBackTestTradeStatistics(
                            coinType = stat.coinType,
                            from = stat.from,
                            to = stat.to,
                            totalAccProfitPrice = stat.totalAccProfitPrice.round(),
                        )
                    }
                    coinType to statistics
                },
        )
    }
}