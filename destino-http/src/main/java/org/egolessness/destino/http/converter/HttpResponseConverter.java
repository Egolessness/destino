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

package org.egolessness.destino.http.converter;

import org.egolessness.destino.http.support.HttpResponseSupport;
import org.egolessness.destino.common.model.message.Response;
import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;

import java.io.IOException;

/**
 * converter of response
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpResponseConverter extends AbstractAsyncResponseConsumer<Response> {

    private static final int MAX_INITIAL_BUFFER_SIZE = 256 * 1024;

    private volatile HttpResponse response;
    private volatile SimpleInputBuffer buf;

    public HttpResponseConverter() {
        super();
    }

    @Override
    protected void onResponseReceived(final HttpResponse response) {
        this.response = response;
    }

    @Override
    protected void onEntityEnclosed(final HttpEntity entity, final ContentType contentType) throws IOException {
        long len = entity.getContentLength();
        if (len > Integer.MAX_VALUE) {
            throw new ContentTooLongException("Entity content is too long: %,d", len);
        }
        if (len < 0) {
            len = 4096;
        }
        final int initialBufferSize = Math.min((int) len, MAX_INITIAL_BUFFER_SIZE);
        this.buf = new SimpleInputBuffer(initialBufferSize, new HeapByteBufferAllocator());
        this.response.setEntity(new ContentBufferEntity(entity, this.buf));
    }

    @Override
    protected void onContentReceived(
            final ContentDecoder decoder, final IOControl ioControl) throws IOException {
        Asserts.notNull(this.buf, "Content buffer");
        this.buf.consumeContent(decoder);
    }

    @Override
    protected void releaseResources() {
        this.response = null;
        this.buf = null;
    }

    @Override
    protected Response buildResult(final HttpContext context) {
        try {
            return HttpResponseSupport.of(this.response);
        } catch (Exception e) {
            return null;
        }
    }

}
