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

package org.egolessness.destino.core.support;

import org.egolessness.destino.core.properties.Properties;
import org.egolessness.destino.core.properties.constants.DefaultConstants;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.SecurityUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * support for security.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SecuritySupport {

    private static final String securityAuthServerSecret = Properties.get("destino.security.auth.server-secret",
            DefaultConstants.DEFAULT_SECURITY_AUTH_SERVER_SECRET);

    private static final Mark JOINER = Mark.BLANK;

    public static String createServerToken(final long timestamp, final String... encodeStr) {
        return SecurityUtils.sha256Hex(securityAuthServerSecret, JOINER.join(timestamp, encodeStr));
    }

    public static String createServerToken(final long timestamp, final List<?> encodeList) {
        return createServerToken(timestamp, JOINER.join(encodeList));
    }

    public static String createServerToken(final long timestamp, final Set<? extends Comparable<?>> encodeSet) {
        String encodeStr = encodeSet.stream().sorted().map(Objects::toString).collect(Collectors.joining(JOINER.getValue()));
        return createServerToken(timestamp, encodeStr);
    }

    public static boolean validateServerToken(final String token, final long timestamp, final String... encodeStr) {
        return Objects.equals(token, createServerToken(timestamp, encodeStr));
    }

    public static boolean validateServerToken(final String token, final long timestamp, final List<?> encodeList) {
        return validateServerToken(token, timestamp, JOINER.join(encodeList));
    }

    public static boolean validateServerToken(final String token, final long timestamp, final Set<? extends Comparable<?>> encodeSet) {
        String encodeStr = encodeSet.stream().sorted().map(Objects::toString).collect(Collectors.joining(JOINER.getValue()));
        return validateServerToken(token, timestamp, encodeStr);
    }

}
