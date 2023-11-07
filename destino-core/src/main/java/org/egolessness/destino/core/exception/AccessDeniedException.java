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

import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.fixedness.BaseCode;

/**
 * exception for access denied
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccessDeniedException extends DestinoRuntimeException {

    private static final long serialVersionUID = 2793407224340109119L;

    public AccessDeniedException(final BaseCode errCode, String message) {
        super(errCode, message);
    }

}
