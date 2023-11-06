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

package com.egolessness.destino.common.enumeration;

import com.egolessness.destino.common.utils.PredicateUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * link mark and operations
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum Mark {

    EMPTY("", ""),
    COLON(":", ":"),
    SEMICOLON(";", ";"),
    COMMA(",", ","),
    COMMENT("#", "#"),
    BLANK(" ", " "),
    CROSSED("-", "-"),
    UNDERLINE("_", "_"),
    AT("@", "@"),
    SLASH("/", "/"),
    AND("&", "&"),
    DOT(".", "\\."),
    EQUALS_SIGN("=", "="),
    LINE_FEED("\n", "\n"),
    QUESTION("?", "\\?");

    private final String value;

    private final String splitSign;

    Mark(String value, String splitSign) {
        this.value = value;
        this.splitSign = splitSign;
    }

    public String getValue() {
        return value;
    }

    public String getSplitSign() {
        return splitSign;
    }

    public String[] split(String content) {
        if (PredicateUtils.isBlank(content)) {
            return new String[0];
        }
        return content.split(getSplitSign());
    }

    public String join(Collection<?> objs) {
        return objs.stream().map(Objects::toString).collect(Collectors.joining(getValue()));
    }

    public String join(Number... objs) {
        return Stream.of(objs).map(Objects::toString).collect(Collectors.joining(getValue()));
    }

    public String join(String prefix, Object... objs) {
        return Stream.concat(Stream.of(prefix), Stream.of(objs).map(Objects::toString)).collect(Collectors.joining(getValue()));
    }

    public String join(String... objs) {
        return Stream.of(objs).collect(Collectors.joining(getValue()));
    }

    public String join(Object prefix, String... objs) {
        return Stream.concat(Stream.of(prefix).map(Object::toString), Stream.of(objs)).collect(Collectors.joining(getValue()));
    }

    public static MarksLink link(Mark... marks) {
        return new MarksLink(marks);
    }

    public static class MarksLink {

        private final List<Mark> links;

        public MarksLink(Mark... marks) {
            this.links = Stream.of(marks).collect(Collectors.toList());
        }

        public MarksLink link(Mark marks) {
            this.links.add(marks);
            return this;
        }

        public String[] splitOne(String origin) {
            if (PredicateUtils.isBlank(origin)) {
                return new String[0];
            }
            for (Mark marks : links) {
                if (origin.contains(marks.getValue())) {
                    return origin.split(marks.splitSign);
                }
            }
            return new String[]{ origin };
        }

    }

    @Override
    public String toString() {
        return value;
    }
}
