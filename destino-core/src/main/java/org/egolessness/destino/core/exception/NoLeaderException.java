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

package org.egolessness.destino.core.exception;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.message.ConsistencyDomain;

/**
 * exception for no leader
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NoLeaderException extends DestinoException {

    private static final long serialVersionUID = -3343269473901702065L;

    public NoLeaderException() {
    }
    
    public NoLeaderException(ConsistencyDomain domain) {
        super(Errors.PROTOCOL_UNAVAILABLE, domain+ " cannot find Leader");
    }
    
    public NoLeaderException(String message, Throwable cause) {
        super(Errors.PROTOCOL_UNAVAILABLE, message, cause);
    }
    
    public NoLeaderException(Throwable cause) {
        super(Errors.PROTOCOL_UNAVAILABLE, cause);
    }

}
