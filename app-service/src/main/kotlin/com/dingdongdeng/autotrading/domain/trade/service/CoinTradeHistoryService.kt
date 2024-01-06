package com.dingdongdeng.autotrading.domain.trade.service

import com.dingdongdeng.autotrading.domain.trade.entity.CoinTradeHistory
import com.dingdongdeng.autotrading.domain.trade.repository.CoinTradeHistoryRepository
import com.dingdongdeng.autotrading.infra.common.type.CoinType
import com.dingdongdeng.autotrading.infra.common.type.TradeState
import org.springframework.stereotype.Service

@Service
class CoinTradeHistoryService(
    private val coinTradeHistoryRepository: CoinTradeHistoryRepository,
) {

    fun findAllTradeHistory(
        autoTradeProcessorId: String,
        coinType: CoinType,
        states: List<TradeState> = listOf(
            TradeState.WAIT,
            TradeState.DONE,
            TradeState.CANCEL
        )
    ): List<CoinTradeHistory> {
        return coinTradeHistoryRepository.findByAutoTradeProcessorIdAndCoinTypeAndStateInOrderByTradedAtAsc(
            autoTradeProcessorId,
            coinType,
            states,
        )
    }

    fun save(history: CoinTradeHistory): CoinTradeHistory {
        return coinTradeHistoryRepository.save(history)
    }
}