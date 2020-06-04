/*
 * Copyright (c) 2020 ThanksMister LLC
 *  http://www.thanksmister.com
 *   
 *  Mozilla Public License 2.0
 *  
 *  Permissions of this weak copyleft license are conditioned on making
 *  available source code of licensed files and modifications of those files
 *  under the same license (or in certain cases, one of the GNU licenses).
 *  Copyright and license notices must be preserved. Contributors provide
 *  an express grant of patent rights. However, a larger work using the
 *  licensed work may be distributed under different terms and without source
 *  code for files added in the larger work.
 */

package com.thanksmister.bitcoin.localtrader.network.services

import com.google.gson.JsonElement
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CoinbaseService {

    /*@GET("/v2/exchange-rates?currency=BTC")
    Observable<Response> exchangeRates();

    @GET("/v2/currencies")
    Observable<Response> currencies();
*/
    @GET("/v2/prices/BTC-{currency}/spot")
    fun spotPrice(@Path("currency") currency: String): Observable<JsonElement>
}