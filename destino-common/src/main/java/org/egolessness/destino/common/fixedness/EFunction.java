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

package org.egolessness.destino.common.fixedness;

import java.util.Objects;

/**
 * function with exception
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@FunctionalInterface
public interface EFunction<T, R> {

    R apply(T t) throws Exception;

    default <V> EFunction<V, R> compose(EFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }


    default <V> EFunction<T, V> andThen(EFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> EFunction<T, T> identity() {
        return t -> t;
    }

}
