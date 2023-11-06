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

package com.egolessness.destino.core.function;

import java.util.Objects;

/**
 * functional interface of predicate with exception
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@FunctionalInterface
public interface EPredicate<T> {

    boolean test(T t) throws Exception;

    default EPredicate<T> and(EPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default EPredicate<T> negate() {
        return (t) -> !test(t);
    }

    default EPredicate<T> or(EPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    static <T> EPredicate<T> isEqual(Object targetRef) {
        return (null == targetRef) ? Objects::isNull : targetRef::equals;
    }

}
