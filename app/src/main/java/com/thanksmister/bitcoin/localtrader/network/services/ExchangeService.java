/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.thanksmister.bitcoin.localtrader.network.services;

import android.content.SharedPreferences;

import com.thanksmister.bitcoin.localtrader.network.api.BitcoinAverage;
import com.thanksmister.bitcoin.localtrader.network.api.BitfinexExchange;
import com.thanksmister.bitcoin.localtrader.network.api.BitstampExchange;
import com.thanksmister.bitcoin.localtrader.network.api.Coinbase;
import com.thanksmister.bitcoin.localtrader.network.api.model.ExchangeRate;
import com.thanksmister.bitcoin.localtrader.persistence.Preferences;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import retrofit2.Response;

import static com.thanksmister.bitcoin.localtrader.persistence.Preferences.COINBASE_EXCHANGE;

@Singleton
public class ExchangeService {

    public static final String PREFS_EXCHANGE_EXPIRE_TIME = "pref_exchange_expire";
    public static final String PREFS_SELECTED_EXCHANGE = "selected_exchange";
    public static final String PREFS_EXCHANGE_CURRENCY = "exchange_currency";
    public static final String PREFS_EXCHANGE = "pref_exchange";

    public static final int CHECK_EXCHANGE_DATA = 2 * 60 * 1000;// 5 minutes


    public static final String BITSTAMP_EXCHANGE = "Bitstamp";
    public static final String BITFINEX_EXCHANGE = "Bitfinex";
    public static final String BITCOINAVERAGE_EXCHANGE = "BitcoinAverage";


    @Inject
    public ExchangeService(Preferences preferences) {

    }

   /* @Deprecated
    public Observable<List<ExchangeCurrency>> getCurrencies() {
        return coinbase.currencies()
                .map(new ResponseToExchangeCurrencies());
    }
*/
    /*public Observable<ExchangeRate> getSpotPrice() {
        //if(needToRefreshExchanges()) {
        final String currency = preferences.getExchangeCurrency();
        if (preferences.getSelectedExchange().equals(COINBASE_EXCHANGE)) {
            return coinbase.spotPrice(currency)
                    .doOnNext(new Function<Response>() {
                        @Override
                        public void call(Response response) {
                            setExchangeExpireTime();
                        }
                    });
        } else if (preferences.getSelectedExchange().equals(BITSTAMP_EXCHANGE)) {
            return bitstamp.ticker("btc" + currency.toLowerCase())
                    .doOnNext(new Action1<Bitstamp>() {
                        @Override
                        public void call(Bitstamp bitstamp) {
                            setExchangeExpireTime();
                        }
                    })
                    .flatMap(new Func1<Bitstamp, Observable<ExchangeRate>>() {
                        @Override
                        public Observable<ExchangeRate> call(Bitstamp bitstamp) {
                            String currency = getExchangeCurrency();
                            ExchangeRate exchangeRate = new ExchangeRate(BITSTAMP_EXCHANGE, bitstamp.last, currency);
                            return Observable.just(exchangeRate);
                        }
                    });
        } else if (getSelectedExchange().equals(BITFINEX_EXCHANGE)) {
            return bitfinex.ticker("tBTC" + currency.toUpperCase())
                    .doOnNext(new Action1<Response>() {
                        @Override
                        public void call(Response response) {
                            setExchangeExpireTime();
                        }
                    })
                    .map(new ResponseToBitfinex())
                    .flatMap(new Func1<ExchangeRate, Observable<ExchangeRate>>() {
                        @Override
                        public Observable<ExchangeRate> call(ExchangeRate exchangeRate) {
                            exchangeRate.setDisplayName(BITFINEX_EXCHANGE);
                            exchangeRate.setCurrency(currency);
                            return Observable.just(exchangeRate);
                        }
                    });
        } else if (getSelectedExchange().equals(BITCOINAVERAGE_EXCHANGE)) {
            return bitcoinAverage.ticker()
                    .doOnNext(new Action1<Response>() {
                        @Override
                        public void call(Response response) {
                            setExchangeExpireTime();
                        }
                    })
                    .flatMap(new Func1<Response, Observable<ExchangeRate>>() {
                        @Override
                        public Observable<ExchangeRate> call(Response response) {
                            ExchangeRate exchangeRate = Parser.parseBitcoinAverageExchangeRate(BITCOINAVERAGE_EXCHANGE, currency, response);
                            return Observable.just(exchangeRate);
                        }
                    });
        } else {
            return Observable.just(null);
        }
        //}
    }

    public void clearExchangeExpireTime() {
        synchronized (this) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFS_EXCHANGE_EXPIRE_TIME).apply();
        }
    }

    private void setExchangeExpireTime() {
        synchronized (this) {
            long expire = System.currentTimeMillis() + CHECK_EXCHANGE_DATA; // 1 hours
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(PREFS_EXCHANGE_EXPIRE_TIME, expire);
            editor.apply();
        }
    }

    private boolean needToRefreshExchanges() {
        synchronized (this) {
            long expiresAt = preferences.getLong(PREFS_EXCHANGE_EXPIRE_TIME, -1);
            return System.currentTimeMillis() >= expiresAt;
        }
    }*/
}