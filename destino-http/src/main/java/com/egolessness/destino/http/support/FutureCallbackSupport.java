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

package com.egolessness.destino.http.support;

import com.egolessness.destino.common.exception.RequestCancelledException;
import com.egolessness.destino.common.fixedness.Callback;
import org.apache.http.concurrent.FutureCallback;

import java.util.Objects;

/**
 * support for callback
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FutureCallbackSupport {

    public static <T> FutureCallback<T> convertFutureCallback(final Callback<T> callback) {
        if (Objects.isNull(callback)) {
            return null;
        }
        return new FutureCallback<T>() {
            @Override
            public void completed(T result) {
                callback.onResponse(result);
            }

            @Override
            public void failed(Exception ex) {
                callback.onThrowable(ex);
            }

            @Override
            public void cancelled() {
                callback.onThrowable(new RequestCancelledException("http request cancelled"));
            }
        };
    }

    public static <T> FutureCallback<T> emptyFutureCallback() {
        return new FutureCallback<T>() {
            @Override
            public void completed(T result) {
            }

            @Override
            public void failed(Exception ex) {
            }

            @Override
            public void cancelled() {
            }
        };
    }

}
