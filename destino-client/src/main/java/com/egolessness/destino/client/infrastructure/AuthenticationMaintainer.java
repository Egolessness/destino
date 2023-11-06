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

package com.egolessness.destino.client.infrastructure;

import com.egolessness.destino.client.logging.Loggers;
import com.egolessness.destino.client.properties.DestinoProperties;
import com.egolessness.destino.common.constant.CommonConstants;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.model.request.LoginRequest;
import com.egolessness.destino.common.model.response.IdentityResponse;
import com.egolessness.destino.common.remote.RequestHighLevelClient;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.utils.SecurityUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static com.egolessness.destino.common.enumeration.ErrorCode.*;

/**
 * authentication maintainer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AuthenticationMaintainer {

    private final static Duration DEFAULT_LOGIN_FAILED_DELAY = Duration.ofSeconds(3);

    private final RequestHighLevelClient requestClient;

    private final ScheduledExecutorService executorService;

    private final DestinoProperties properties;

    private volatile String accessToken;

    private long tokenActivatedTime;

    private long lastRefreshTime;

    private final Set<Integer> againLoginErrorCodes = getAgainLoginErrorCodes();

    private final Duration requestTimeout = Duration.ofSeconds(3);

    public AuthenticationMaintainer(DestinoProperties properties, RequestHighLevelClient highLevelClient) {
        this.properties = properties;
        this.accessToken = properties.getAccessToken();
        this.requestClient = highLevelClient;
        this.executorService = ExecutorCreator.createLoginExecutor();
    }

    private boolean isLoginEnable() {
        return PredicateUtils.isNotBlank(properties.getUsername()) &&
                (PredicateUtils.isNotBlank(properties.getPassword()) || PredicateUtils.isNotBlank(properties.getEncryptedPassword())) ;
    }

    public AuthenticationMaintainer start() {
        if (isLoginEnable()) {
            this.executorService.execute(this::loginTask);
        }
        return this;
    }

    public boolean login() {

        if (!isLoginEnable()) {
            return false;
        }

        try {
            if (System.currentTimeMillis() - lastRefreshTime < tokenActivatedTime) {
                return true;
            }

            int retryTimes = requestClient.getAddresses().size();
            for (int i = 0; i < retryTimes; i++) {
                if (tryLogin()) {
                    return true;
                }
                requestClient.connectNext();
            }
        } catch (Throwable throwable) {
            Loggers.AUTHENTICATION.error("Login failed.", throwable);
        }

        return false;
    }

    public boolean tryLogin() {

        try {
            LoginRequest request = new LoginRequest(properties.getUsername(), getEncryptedPassword(), true);
            Response response = requestClient.request(request, RequestSupport.commonHeaders(), requestTimeout);
            if (ResponseSupport.isSuccess(response)) {
                IdentityResponse identity = ResponseSupport.dataDeserialize(response, IdentityResponse.class);
                if (Objects.isNull(identity)) {
                    return false;
                }
                accessToken = identity.getToken();
                long tokenTtl = identity.getTtl();
                lastRefreshTime = System.currentTimeMillis();
                tokenActivatedTime = tokenTtl * 4 / 5;
                return true;
            }
        } catch (Exception e) {
            Loggers.AUTHENTICATION.warn("Login failed with username {}", properties.getUsername(), e);
        }

        return false;
    }

    private String getEncryptedPassword() {
        if (PredicateUtils.isNotBlank(properties.getEncryptedPassword())) {
            return properties.getEncryptedPassword();
        }
        return SecurityUtils.sha256(properties.getUsername() + properties.getPassword());
    }

    private void loginTask() {
        long delayMillis;
        if (login()) {
            delayMillis = Long.max(100, tokenActivatedTime - System.currentTimeMillis());
        } else {
            delayMillis = DEFAULT_LOGIN_FAILED_DELAY.toMillis();
        }
        this.executorService.schedule(this::loginTask, delayMillis, TimeUnit.MILLISECONDS);
    }

    private Set<Integer> getAgainLoginErrorCodes() {
        Set<Integer> errorCodes = new HashSet<>();
        errorCodes.add(LOGIN_FAILED.getCode());
        errorCodes.add(NOT_LOGGED_IN.getCode());
        errorCodes.add(TOKEN_EXPIRED.getCode());
        errorCodes.add(TOKEN_INVALID.getCode());
        errorCodes.add(ACCOUNT_UPDATED.getCode());
        errorCodes.add(PERMISSION_DENIED.getCode());
        return errorCodes;
    }

    public void tryLoginWhenResponseError(int errorCode) {
        if (againLoginErrorCodes.contains(errorCode)) {
            tryLogin();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void consumerAuthentication(BiConsumer<String, String> consumer) {
        String accessToken = getAccessToken();
        if (PredicateUtils.isNotBlank(accessToken)) {
            consumer.accept(RequestSupport.HEADER_TOKEN, CommonConstants.TOKEN_PREFIX + accessToken);
        } else if (isLoginEnable()) {
            consumer.accept(RequestSupport.HEADER_USERNAME, properties.getUsername());
            consumer.accept(RequestSupport.HEADER_PASSWORD, getEncryptedPassword());
        }
    }

}
