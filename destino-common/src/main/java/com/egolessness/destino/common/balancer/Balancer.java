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

package com.egolessness.destino.common.balancer;

import com.egolessness.destino.common.fixedness.Picker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * abstract for balancer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class Balancer<T> {

    protected final List<T> dataList;

    protected T current;

    public Balancer(Collection<T> metaCollection) {
        Objects.requireNonNull(metaCollection);
        this.dataList = new ArrayList<>(metaCollection);
    }

    public Picker<T> convertPicker() {
        return new Picker<T>() {
            @Override
            public List<T> list() {
                return dataList;
            }

            @Override
            public T next() {
                return Balancer.this.next();
            }

            @Override
            public T current() {
                return current;
            }
        };
    }

    public abstract T next();

}
