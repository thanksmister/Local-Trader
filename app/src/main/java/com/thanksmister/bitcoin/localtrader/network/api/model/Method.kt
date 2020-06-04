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

package com.thanksmister.bitcoin.localtrader.network.api.model

import androidx.room.*

@Entity(tableName = "Methods", indices = [(Index(value = arrayOf("key"), unique = true))])
class Method {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "key")
    var key: String? = null

    @ColumnInfo(name = "code")
    var code: String? = null

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "currencies")
    @TypeConverters(StringConverter::class)
    var currencies: ArrayList<String> = ArrayList<String>()
}



