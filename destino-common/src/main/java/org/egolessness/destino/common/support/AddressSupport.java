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

package org.egolessness.destino.common.support;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.utils.NumberUtils;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.Objects;

/**
 * support for address
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AddressSupport {

    private final static Mark ADDRESS_LINKER_MARK = Mark.COLON;

    public static Address parserAddress(final String addressStr) {
        Address address = new Address();
        if (PredicateUtils.isNotBlank(addressStr)) {
            String[] hostPort = ADDRESS_LINKER_MARK.split(addressStr);
            if (hostPort.length >= 2) {
                address.setHost(hostPort[0]);
                address.setPort(NumberUtils.parseInt(hostPort[1], 0));
            }
        }
        return address;
    }

    public static boolean isAvailable(final Address address) {
        return Objects.nonNull(address) && address.validate();
    }

}
