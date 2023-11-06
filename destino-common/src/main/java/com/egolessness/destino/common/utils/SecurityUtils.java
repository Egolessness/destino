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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * utils of security
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SecurityUtils {

    public static String sha256Hex(String key, String encodeStr) {
        try {
            return generateHmac256(encodeStr, key);
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateHmac256(String message, String key) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] bytes = hmac("HmacSHA256", key.getBytes(StandardCharsets.UTF_8), message.getBytes());
        return bytesToHex(bytes);
    }

    public static byte[] hmac(String algorithm, byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(message);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte item : bytes) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString().toUpperCase();
    }

    public static String md5Hex(String value) {
        return md5Hex(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String md5Hex(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return bytesToHex(digest.digest(value));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha256(String value) {
        value = (value == null ? "" : value);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
        messageDigest.update(value.getBytes(StandardCharsets.UTF_8));

        byte[] bytes = messageDigest.digest();
        StringBuilder builder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                builder.append("0");
            }
            builder.append(temp);
        }

        return builder.toString();
    }

    public static String urlEncode(final String value) {
        if (PredicateUtils.isEmpty(value)) {
            return value;
        }

        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

}
