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

package com.egolessness.destino.core.exception;

import com.egolessness.destino.core.message.ConsistencyDomain;

/**
 * exception for no such domain
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NoSuchDomainException extends RuntimeException {

    private static final long serialVersionUID = -3660357291079849278L;

    public NoSuchDomainException() {
    }

    public NoSuchDomainException(ConsistencyDomain domain) {
        super(domain+ " cannot find");
    }

    public NoSuchDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchDomainException(Throwable cause) {
        super(cause);
    }

    public NoSuchDomainException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
