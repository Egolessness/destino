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

package com.egolessness.destino.common.remote.http;

import com.egolessness.destino.common.support.ResultSupport;
import com.egolessness.destino.common.utils.ByteUtils;
import com.egolessness.destino.common.utils.IoUtils;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.enumeration.ErrorCode;
import com.egolessness.destino.common.remote.HttpHeader;
import com.egolessness.destino.common.constant.HttpHeaderConstants;
import com.egolessness.destino.common.properties.HttpProperties;
import com.egolessness.destino.common.properties.RequestProperties;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.remote.HttpRequestConverter;
import com.egolessness.destino.common.model.HttpRequest;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.*;

/**
 * default http request client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultBaseClient {

    /**
     * default connect timeout
     */
    private int DEFAULT_CONNECT_TIMEOUT_MILLS = 5000;

    /**
     * default read timeout
     */
    private int DEFAULT_READ_TIMEOUT_MILLS = 2000;

    private static final String GZIP_ENCODING = "gzip";

    private static final HttpRequestConverter requestConverter = new HttpRequestConverter();

    public DefaultBaseClient(RequestProperties requestProperties) {
        HttpProperties httpProperties = requestProperties.getHttpProperties();
        this.DEFAULT_CONNECT_TIMEOUT_MILLS = Optional.ofNullable(httpProperties.getConnectTimeout()).
                map(Duration::toMillis).map(Long::intValue).filter(millis -> millis > 0)
                .orElse(this.DEFAULT_CONNECT_TIMEOUT_MILLS);
        this.DEFAULT_READ_TIMEOUT_MILLS = Optional.ofNullable(httpProperties.getReadTimeout()).
                map(Duration::toMillis).map(Long::intValue).filter(millis -> millis > 0)
                .orElse(this.DEFAULT_READ_TIMEOUT_MILLS);
    }
    
    public void setSSLContext(SSLContext context) {
        Optional.ofNullable(context).map(SSLContext::getSocketFactory)
                .ifPresent(HttpsURLConnection::setDefaultSSLSocketFactory);
    }
    
    public void replaceSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
        if (Objects.nonNull(hostnameVerifier)) {
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        }
    }

    public Response execute(URI uri, Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException {
        HttpRequest httpRequest = requestConverter.convert(request, headers);
        try {
            return execute(uri, httpRequest, timeout);
        } catch (Exception e) {
            throw new DestinoException(ErrorCode.REQUEST_FAILED, e);
        }
    }

    public HttpURLConnection connect(URI uri, HttpRequest httpRequest, Duration timout) throws Exception {

        Objects.requireNonNull(uri);
        Objects.requireNonNull(httpRequest);

        HttpHeader header = httpRequest.getHeader();
        byte[] body = httpRequest.getBody();
        String path = RequestSupport.joinParams(httpRequest.getPath(), httpRequest.getParams(), header.getCharset());
        if (PredicateUtils.isNotEmpty(uri.getPath())) {
            path = uri.getPath() + path;
        }

        HttpURLConnection connection = (HttpURLConnection) uri.resolve(path).toURL().openConnection();
        if (PredicateUtils.isNotEmpty(header.getHeaders())) {
            header.getHeaders().forEach(connection::setRequestProperty);
        }

        int timoutMillis = (int) timout.toMillis();
        int connectTimeout = timoutMillis > 0 ? timoutMillis : DEFAULT_CONNECT_TIMEOUT_MILLS;
        int readTimeout = timoutMillis > 0 ? timoutMillis >> 2 : DEFAULT_READ_TIMEOUT_MILLS;

        connection.setRequestMethod(httpRequest.getHttpMethod().name());
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        if (ByteUtils.isNotEmpty(body)) {
            connection.setDoOutput(true);
            connection.setRequestProperty(HttpHeaderConstants.CONTENT_LENGTH, String.valueOf(body.length));
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body, 0, body.length);
            outputStream.flush();
            try {
                outputStream.close();
            } catch (IOException ignored) {
            }
        }

        connection.connect();
        return connection;
    }
    
    public Response execute(URI uri, HttpRequest httpRequest, Duration timout) throws Exception {
        HttpURLConnection connection = connect(uri, httpRequest, timout);
        return convertResponse(connection);
    }

    private Response convertResponse(final HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>(connection.getHeaderFields().size());
        connection.getHeaderFields().forEach((key, values) -> {
            if (PredicateUtils.isNotEmpty(key) && PredicateUtils.isNotEmpty(values)) {
                headers.put(key, values.get(0));
            }
        });

        InputStream responseStream = null;
        try {
            InputStream errorStream = connection.getErrorStream();
            responseStream = errorStream != null ? errorStream : connection.getInputStream();
            String encoding = headers.get(HttpHeaderConstants.CONTENT_ENCODING);

            byte[] content;
            if (Objects.equals(encoding, GZIP_ENCODING)) {
                content = IoUtils.decompress(responseStream);
                if (ByteUtils.isEmpty(content)) {
                    return ResponseSupport.failed("Empty content.");
                }
            } else {
                content = IoUtils.read(responseStream);
            }

            return ResultSupport.toResponse(content, headers);
        } catch (Throwable throwable) {
            return ResponseSupport.failed(throwable.getMessage());
        } finally {
            if (Objects.nonNull(responseStream)) {
                try {
                    responseStream.close();
                } catch (Exception ignore) {
                }
            }
        }

    }

}
