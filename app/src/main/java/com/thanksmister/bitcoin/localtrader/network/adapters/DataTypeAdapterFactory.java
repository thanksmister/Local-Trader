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

package com.thanksmister.bitcoin.localtrader.network.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import timber.log.Timber;

public class DataTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }
            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = elementAdapter.read(in);
                JsonObject dataObject = new JsonObject();
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    Timber.d("Data Return: " + jsonObject.toString());
                    if (jsonObject.has("data") && jsonObject.get("data").isJsonObject()) {
                        dataObject = jsonObject.getAsJsonObject("data");

                        Timber.d("dataObject: " + dataObject.toString());
                        if (jsonObject.has("actions") && jsonObject.get("actions").isJsonObject()) {
                            dataObject.add("actions", jsonObject.getAsJsonObject("actions"));
                            jsonElement = dataObject;
                        } else if (dataObject.has("currencies") && dataObject.get("currencies").isJsonObject()) {
                            jsonElement = dataObject.getAsJsonObject("currencies");
                        } else if (dataObject.has("methods") && dataObject.get("methods").isJsonObject()) {
                            jsonElement = dataObject.getAsJsonObject("methods");
                        } else {
                            jsonElement = dataObject;
                        }
                    } else if (jsonObject.has("data") && jsonObject.get("data").isJsonArray()) {
                        jsonElement = jsonObject.getAsJsonArray("data");
                    }
                }

                Timber.d("jsonElement: " + jsonElement.toString());

                return delegate.fromJsonTree(jsonElement);
            }
        }.nullSafe();
    }
}