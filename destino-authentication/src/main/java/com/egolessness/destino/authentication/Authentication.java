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

package com.egolessness.destino.authentication;

import com.egolessness.destino.authentication.model.Permission;

import java.util.Collection;

/**
 * authentication with login info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Authentication {

    private final String username;

    private final long melody;

    private final Collection<Permission> authorities;

    public Authentication(String username, long melody, Collection<Permission> authorities) {
        this.username = username;
        this.melody = melody;
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public long getMelody() {
        return melody;
    }

    public Collection<Permission> getAuthorities() {
        return authorities;
    }
}
