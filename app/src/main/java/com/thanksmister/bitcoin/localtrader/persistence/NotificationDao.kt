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
 */

package com.thanksmister.bitcoin.localtrader.persistence

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Data Access Object for the messages table.
 */
@Dao
interface NotificationDao {
    /**
     * Get a item by id.
     * @return the item from the table with a specific id.
     */
    @Query("SELECT * FROM Notification WHERE id = :id")
    fun getItemById(id: String): Flowable<Notification>

    /**
     * Get all messages
     * @return list of all items
     */
    @Query("SELECT * FROM Notification ORDER BY created_at DESC")
    fun getItems(): Flowable<List<Notification>>

    /**
     * Get all messages
     * @return list of all items
     */
    @Query("SELECT * FROM Notification ORDER BY created_at DESC")
    fun getItemsList(): List<Notification>

    /**
     * Get all unread items
     * @return list of all unread items
     */
    @Query("SELECT * FROM Notification WHERE read = 0")
    fun getUnreadItems(): Flowable<List<Notification>>

    /**
     * Insert a items in the database. If the item already exists, replace it.
     * @param user the item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: Notification):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<Notification>)

    @Transaction
    fun replaceItem(items: List<Notification>) {
        deleteAllItems()
        insertAll(items)
    }

    /**
     * Delete all messages.
     */
    @Query("DELETE FROM Notification")
    fun deleteAllItems()
}