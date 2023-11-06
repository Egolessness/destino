/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.egolessness.destino.core.storage;

import com.egolessness.destino.core.properties.StorageProperties;

/**
 * options of create storage.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class StorageOptions {

    private int prefixLength;

    private boolean writeAsync = true;

    private boolean flushAsync = true;

    public StorageOptions() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static StorageOptions of(StorageProperties properties) {
        return StorageOptions.newBuilder()
                .writeAsync(properties.isWriteAsync())
                .flushAsync(properties.isFlushAsync())
                .build();
    }

    private StorageOptions(Builder builder) {
        setWriteAsync(builder.writeAsync);
        setFlushAsync(builder.flushAsync);
        setPrefixLength(builder.prefixLength);
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public void setPrefixLength(int prefixLength) {
        this.prefixLength = prefixLength;
    }

    public boolean isWriteAsync() {
        return writeAsync;
    }

    public void setWriteAsync(boolean writeAsync) {
        this.writeAsync = writeAsync;
    }

    public boolean isFlushAsync() {
        return flushAsync;
    }

    public void setFlushAsync(boolean flushAsync) {
        this.flushAsync = flushAsync;
    }


    public static final class Builder {
        private boolean writeAsync;
        private boolean flushAsync;
        private int prefixLength;

        public Builder() {
        }

        public Builder prefixLength(int val) {
            prefixLength = val;
            return this;
        }

        public Builder writeAsync(boolean val) {
            writeAsync = val;
            return this;
        }

        public Builder flushAsync(boolean val) {
            flushAsync = val;
            return this;
        }

        public StorageOptions build() {
            return new StorageOptions(this);
        }
    }
}
