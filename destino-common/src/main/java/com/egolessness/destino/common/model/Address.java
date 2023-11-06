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

package com.egolessness.destino.common.model;

import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.egolessness.destino.common.support.AddressSupport;

import java.io.Serializable;
import java.util.Objects;

/**
 * address: ip + port
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Address implements Serializable {

    private static final long serialVersionUID = -6482306502010512261L;

    private String host;

    private int port = -1;

    public Address() {
    }

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static Address of(final String address) {
        return AddressSupport.parserAddress(address);
    }

    public static Address of(final String host, final int port) {
        return new Address(host, port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @JsonIgnore
    public boolean validate() {
        return PredicateUtils.isNotBlank(host) && port > 0 && port < 0xFFFF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address that = (Address) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return Mark.COLON.join(host, port);
    }

}
