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

package org.egolessness.destino.registration.provider;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * provider of service group
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface GroupProvider {

    Collection<String> list(@Nonnull String namespace);

    long count(@Nonnull String namespace, Predicate<String> predicate);

}
