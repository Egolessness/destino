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

package org.egolessness.destino.common.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * page
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Page<T> implements Pageable {

    private static final long serialVersionUID = 6353854696480278042L;

    private List<T> records;

    private int page;

    private int size;
    
    private int total;

    public Page() {
    }

    public static <T> Page<T> empty() {
        Page<T> page = new Page<>();
        page.setRecords(Collections.emptyList());
        return page;
    }

    @SuppressWarnings("unchecked")
    public <R> Page<R> convert(Function<T, R> mappingFunction) {
        List<R> convertList = this.getRecords().stream().map(mappingFunction).collect(Collectors.toList());
        Page<R> result = ((Page<R>) this);
        result.setRecords(convertList);
        return result;
    }

    public Page<T> filter(Predicate<T> predicate) {
        List<T> convertList = this.getRecords().stream().filter(predicate).collect(Collectors.toList());
        setRecords(convertList);
        return this;
    }

    public Page<T> replace(Function<List<T>, List<T>> replaceFunction) {
        setRecords(replaceFunction.apply(this.getRecords()));
        return this;
    }

    public List<T> getRecords() {
        return records;
    }
    
    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getPages() {
        if (size == 0) {
            return 0L;
        }
        long pages = total / size;
        if (total % size != 0) {
            pages++;
        }
        return pages;
    }

    @Override
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}