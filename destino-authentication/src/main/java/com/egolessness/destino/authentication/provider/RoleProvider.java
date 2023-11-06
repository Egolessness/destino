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

package com.egolessness.destino.authentication.provider;

import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * provider of role.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RoleProvider {

    Page<Role> page(Predicate<Role> predicate, Pageable pageable) throws DestinoException;

    Role create(Role role) throws DestinoException;

    Role update(Role role) throws DestinoException;

    Role delete(long id) throws DestinoException;

    List<Role> batchDelete(Collection<Long> ids) throws DestinoException;

}
