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

package com.egolessness.destino.common.support;

import com.egolessness.destino.common.fixedness.Callback;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * support for callback
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class CallbackSupport {

    public static <T> Callback<T> build(Duration timeout) {
        return new Callback<T>() {
            @Override
            public void onResponse(T response) {
            }
            @Override
            public void onThrowable(Throwable e) {
            }
            @Override
            public long getTimeoutMillis() {
                return timeout.toMillis();
            }
        };
    }

    public static <T> Callback<T> build(CompletableFuture<T> future, Duration timeout) {
        return new Callback<T>() {
            @Override
            public void onResponse(T response) {
                future.complete(response);
            }
            @Override
            public void onThrowable(Throwable e) {
                future.completeExceptionally(e);
            }
            @Override
            public long getTimeoutMillis() {
                return timeout.toMillis();
            }
        };
    }

    public static <T> Callback<T> buildErrorCallback(Runnable errorFunction) {
        return new Callback<T>() {
            @Override
            public void onResponse(T response) {
            }
            @Override
            public void onThrowable(Throwable e) {
                errorFunction.run();
            }
        };
    }

    public static <T> void trigger(final Callback<T> callback, final T data, final Throwable throwable) {
        if (Objects.nonNull(throwable)) {
            triggerThrowable(callback, throwable);
        } else {
            triggerResponse(callback, data);
        }
    }

    public static <T> void triggerResponse(final Callback<T> callback, final T data) {
        if (Objects.isNull(callback)) {
            return;
        }

        Runnable runnable = () -> callback.onResponse(data);

        if (Objects.nonNull(callback.getExecutor())) {
            callback.getExecutor().execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static void triggerThrowable(final Callback<?> callback, final Throwable throwable) {
        if (Objects.isNull(callback)) {
            return;
        }

        Runnable runnable = () -> callback.onThrowable(throwable);

        if (Objects.nonNull(callback.getExecutor())) {
            callback.getExecutor().execute(runnable);
        } else {
            runnable.run();
        }
    }

}
