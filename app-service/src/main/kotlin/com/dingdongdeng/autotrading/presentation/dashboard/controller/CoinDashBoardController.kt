package com.dingdongdeng.autotrading.presentation.dashboard.controller

import com.dingdongdeng.autotrading.infra.common.web.CommonResponse
import com.dingdongdeng.autotrading.presentation.dashboard.model.CoinAutotradeRegisterRequest
import com.dingdongdeng.autotrading.presentation.dashboard.model.CoinExchangeKeyRegisterRequest
import com.dingdongdeng.autotrading.usecase.autotrade.CoinAutoTradeUsecase
import com.dingdongdeng.autotrading.usecase.key.CoinKeyUsecase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/coin")
@RestController
class CoinDashBoardController(
    private val coinAutoTradeUsecase: CoinAutoTradeUsecase,
    private val coinKeyUsecase: CoinKeyUsecase,
) {

    @PostMapping("/exchange-key/register")
    fun exchangeKeyRegister(
        @Valid @RequestBody request: CoinExchangeKeyRegisterRequest,
        //@SessionAttribute userId: Long,
    ): CommonResponse<String> {
        return CommonResponse(
            body = coinKeyUsecase.registerKey(
                exchangeType = request.exchangeType,
                accessKey = request.accessKey,
                secretKey = request.secretKey,
                userId = 12345,
            )
        )
    }

    @PostMapping("/autotrade/register")
    fun autotradeRegister(
        @Valid @RequestBody request: CoinAutotradeRegisterRequest,
        //@SessionAttribute userId: Long,
    ): CommonResponse<String> {
        return CommonResponse(
            body = coinAutoTradeUsecase.register(
                userId = 12345,
                coinStrategyType = request.coinStrategyType,
                exchangeType = request.exchangeType,
                coinTypes = request.coinTypes,
                candleUnits = request.candleUnits,
                keyPairId = request.keyPairId,
                config = request.config,
            )
        )
    }

    @PostMapping("/autotrade/{autoTradeProcessorId}/start")
    fun autotradeStart(
        @PathVariable autoTradeProcessorId: String,
    ): CommonResponse<String> {
        return CommonResponse(
            body = coinAutoTradeUsecase.start(autoTradeProcessorId)
        )
    }

    @PostMapping("/autotrade/{autoTradeProcessorId}/stop")
    fun autotradeStop(
        @PathVariable autoTradeProcessorId: String,
    ): CommonResponse<String> {
        return CommonResponse(
            body = coinAutoTradeUsecase.stop(autoTradeProcessorId)
        )
    }

    @PostMapping("/autotrade/{autoTradeProcessorId}/terminate")
    fun autotradeTerminate(
        @PathVariable autoTradeProcessorId: String,
    ): CommonResponse<String> {
        return CommonResponse(
            body = coinAutoTradeUsecase.terminate(autoTradeProcessorId)
        )
    }
}