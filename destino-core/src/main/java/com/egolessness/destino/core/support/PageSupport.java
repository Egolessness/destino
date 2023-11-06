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

package com.egolessness.destino.core.support;

import com.linecorp.armeria.server.ServiceRequestContext;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.PageParam;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.utils.NumberUtils;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.util.Collections;
import java.util.List;

/**
 * support for page.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PageSupport {

    public static int getStart(Pageable pageable) {
        return getStart(pageable.getPage(), pageable.getSize());
    }

    public static int getStart(int page, int size) {
        if (page < 1) {
            page = 0;
        }

        if (size < 1) {
            size = 0;
        }

        return page * size;
    }

    public static int[] transToStartEnd(int page, int size) {
        int start = getStart(page, size);
        if (size < 1) {
            size = 0;
        }

        int end = start + size;
        return new int[]{start, end};
    }

    public static <T> Page<T> page(List<T> list, int page, int size) {
        if (PredicateUtils.isEmpty(list)) {
            return Page.empty();
        }

        int total = list.size();

        Page<T> pageResult = new Page<>();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(total);

        if (total <= size) {
            if (page <= 0) {
                pageResult.setRecords(list);
            } else {
                pageResult.setRecords(Collections.emptyList());
            }
            return pageResult;
        }

        int[] startEnd = transToStartEnd(page, size);
        if (startEnd[1] > total) {
            startEnd[1] = total;
        }

        List<T> records = list.subList(startEnd[0], startEnd[1]);
        pageResult.setRecords(records);
        return pageResult;
    }

    public static Pageable getPage(ServiceRequestContext ctx) {
        String page = ctx.queryParam("page");
        String size = ctx.queryParam("size");
        return new PageParam(NumberUtils.parseInt(page, 0), NumberUtils.parseInt(size, 10));
    }

}
