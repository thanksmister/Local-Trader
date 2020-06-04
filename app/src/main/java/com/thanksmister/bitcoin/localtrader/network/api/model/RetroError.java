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

package com.thanksmister.bitcoin.localtrader.network.api.model;

import java.lang.Error;

public class RetroError extends Error {
    private String message;
    private int code;

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public RetroError(String detailMessage) {
        super(detailMessage);
        this.message = detailMessage;
    }

    public RetroError(String detailMessage, int code) {
        super(detailMessage);
        this.message = detailMessage;
        this.code = code;
    }

    public boolean isAuthenticationError() {
        return (code == 403 || code == 4);
    }

    public boolean isNetworkError() {
        return (code == 404);
    }
}
