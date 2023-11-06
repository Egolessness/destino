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

package com.egolessness.destino.common.utils;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * utils of function
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NetUtils {

    private static final String LEGAL_LOCAL_IP_PROPERTY = "java.net.preferIPv6Addresses";
    
    private static String localIp;
    
    public static String localIP() {
        if (!PredicateUtils.isBlank(localIp)) {
            return localIp;
        }
        return localIp = getAddress();
        
    }

    private static String getAddress() {
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return PredicateUtils.emptyString();
        }
        return inetAddress.getHostAddress();
    }

    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion =
                                Boolean.parseBoolean(System.getProperty(LEGAL_LOCAL_IP_PROPERTY))
                                        ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }

                }
            }
        } catch (IOException ignored) {
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) {
        }

        return null;

    }
}