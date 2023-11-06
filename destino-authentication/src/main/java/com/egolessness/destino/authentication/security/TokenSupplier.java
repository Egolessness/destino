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

package com.egolessness.destino.authentication.security;

import com.egolessness.destino.authentication.Authentication;
import com.egolessness.destino.authentication.AuthenticationMessages;
import com.egolessness.destino.authentication.AuthenticationSetting;
import com.egolessness.destino.authentication.properties.JwtProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.AccessDeniedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

/**
 * supplier of jwt token
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class TokenSupplier {

    private final static String CLAIM_KEY_MELODY = "melody";

    private final Key key;

    private final JwtParser jwtParser;

    private final AuthenticationSetting authenticationSetting;

    @Inject
    public TokenSupplier(JwtProperties jwtProperties, AuthenticationSetting authenticationSetting) {
        byte[] keyBytes;
        String secret = jwtProperties.getBase64Secret();
        if (PredicateUtils.isNotBlank(secret)) {
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            secret = jwtProperties.getSecret();;
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        this.authenticationSetting = authenticationSetting;
    }

    public String createToken(Authentication authentication, long validityMillis) {

        Date validity = new Date(System.currentTimeMillis() + validityMillis);
        
        return Jwts.builder()
                .setSubject(authentication.getUsername())
                .claim(CLAIM_KEY_MELODY, authentication.getMelody())
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token) throws AccessDeniedException {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            long melody = claims.get(CLAIM_KEY_MELODY, Long.class);
            return new Authentication(claims.getSubject(), melody, Collections.emptyList());
        } catch (ExpiredJwtException e) {
            throw new AccessDeniedException(Errors.TOKEN_EXPIRED, AuthenticationMessages.TOKEN_EXPIRED.getValue());
        } catch (Exception e) {
            Loggers.AUTH.trace("Invalid JWT token trace.", e);
            throw new AccessDeniedException(Errors.TOKEN_INVALID, AuthenticationMessages.TOKEN_INVALID.getValue());
        }
    }

    public long getTokenValidityMillis(boolean rememberMe) {
        return rememberMe ? authenticationSetting.getTokenExpireSecondsForRememberMe() * 1000 :
                authenticationSetting.getTokenExpireSeconds() * 1000;
    }

}
