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

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.enumration.Errors;

/**
 * exception for generate failed
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GenerateFailedException extends DestinoException {

    private static final long serialVersionUID = -4666221480465858197L;

    public GenerateFailedException() {
    }

    public GenerateFailedException(String message, Throwable cause) {
        super(Errors.DATA_ID_INVALID, message, cause);
    }

    public GenerateFailedException(final String msgFormat, Object... args) {
        super(Errors.DATA_ID_INVALID, String.format(msgFormat, args));
    }

}
