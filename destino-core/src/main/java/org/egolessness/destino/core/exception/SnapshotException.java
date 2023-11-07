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

import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.common.exception.DestinoException;

/**
 * exception for snapshot
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SnapshotException extends DestinoException {

    private static final long serialVersionUID = -5872444254266438022L;

    public SnapshotException() {
        super();
    }

    public SnapshotException(Errors code, String errMsg) {
        super(code.getCode(), errMsg);
    }

    public SnapshotException(Errors errCode, Throwable throwable) {
        super(errCode.getCode(), throwable);
    }

    public SnapshotException(Errors errCode, String errMsg, Throwable throwable) {
        super(errCode.getCode(), errMsg, throwable);
    }

    public SnapshotException(int errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public SnapshotException(int errCode, Throwable throwable) {
        super(errCode, throwable);
    }

    public SnapshotException(int errCode, String errMsg, Throwable throwable) {
        super(errCode, errMsg, throwable);
    }

}