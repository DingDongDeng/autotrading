package com.dingdongdeng.autotrading.usecase.autotrade.service

import com.dingdongdeng.autotrading.domain.exchange.model.SpotCoinExchangeOrder
import com.dingdongdeng.autotrading.domain.exchange.service.SpotCoinExchangeService
import com.dingdongdeng.autotrading.domain.strategy.model.SpotCoinStrategyTradeInfoParam
import com.dingdongdeng.autotrading.domain.trade.entity.CoinTradeHistory
import com.dingdongdeng.autotrading.domain.trade.service.CoinTradeHistoryService
import com.dingdongdeng.autotrading.infra.common.type.CoinType
import com.dingdongdeng.autotrading.infra.common.type.ExchangeType
import com.dingdongdeng.autotrading.infra.common.type.OrderType
import com.dingdongdeng.autotrading.infra.common.type.TradeState
import org.springframework.stereotype.Service

@Service
class CoinAutoTradeInfoService(
    private val exchangeServices: List<SpotCoinExchangeService>,
    private val coinTradeHistoryService: CoinTradeHistoryService,
) {

    fun makeTradeInfo(
        exchangeType: ExchangeType,
        keyPairId: String,
        autoTradeProcessorId: String,
        coinType: CoinType,
        currentPrice: Double,
    ): SpotCoinStrategyTradeInfoParam {

        // WAIT 상태의 거래건들 업데이트
        val notSyncedTradeHistories = coinTradeHistoryService.findAllTradeHistory(autoTradeProcessorId, coinType)
        val waitTradeHistories = notSyncedTradeHistories.filter { it.state == TradeState.WAIT }
        waitTradeHistories.forEach { waitTradeHistory ->
            val exchangeService = exchangeServices.first { it.support(exchangeType) }
            val exchangeKeyPair = exchangeService.getKeyPair(keyPairId)
            val order = exchangeService.getOrder(waitTradeHistory.orderId, exchangeKeyPair)
            coinTradeHistoryService.record(makeTradeHistory(waitTradeHistory.id, order, autoTradeProcessorId))
        }

        val syncedTradeHistories = coinTradeHistoryService.findAllTradeHistory(autoTradeProcessorId, coinType)
        val buyTradeHistories = syncedTradeHistories.filter { it.isBuyOrder() }
        val sellTradeHistories = syncedTradeHistories.filter { it.isSellOrder() }

        val volume = buyTradeHistories.sumOf { it.volume } - sellTradeHistories.sumOf { it.volume }
        val value = buyTradeHistories.sumOf { it.price * it.volume } - sellTradeHistories.sumOf { it.price * it.volume }
        val averagePrice = if (volume == 0.0) 0.0 else (value / volume)
        val valuePrice = (volume * currentPrice)
        val originPrice = (volume * averagePrice)

        return SpotCoinStrategyTradeInfoParam(
            volume = buyTradeHistories.sumOf { it.volume } - sellTradeHistories.sumOf { it.volume },
            averagePrice = averagePrice,
            valuePrice = valuePrice,
            originPrice = originPrice,
            profitPrice = (valuePrice - originPrice),
            coinTradeHistory = coinTradeHistoryService.findAllTradeHistory(autoTradeProcessorId, coinType)
        )
    }

    private fun makeTradeHistory(
        coinTradehistoryId: Long? = null,
        order: SpotCoinExchangeOrder,
        autoTradeProcessorId: String
    ): CoinTradeHistory {
        return CoinTradeHistory(
            id = coinTradehistoryId,
            orderId = order.orderId,
            state = order.tradeState,
            processorId = autoTradeProcessorId,
            exchangeType = order.exchangeType,
            coinType = order.coinType,
            orderType = order.orderType,
            priceType = order.priceType,
            volume = order.volume,
            price = order.price,
            fee = order.fee,
            tradedAt = if (order.orderType == OrderType.CANCEL) order.cancelDateTime!! else order.orderDateTime!!,
        )
    }
}