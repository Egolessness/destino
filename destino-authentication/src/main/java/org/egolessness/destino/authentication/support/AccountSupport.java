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

package org.egolessness.destino.authentication.support;

import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.authentication.model.Permission;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.List;
import java.util.Objects;

/**
 * support of account
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountSupport {

    public static boolean isChangedForPassword(Account account, String password) {
        return !Objects.equals(account.getPassword(), password);
    }

    public static boolean hasOtherRoles(Account account, List<String> roles) {
        if (PredicateUtils.isEmpty(account.getRoles())) {
            return PredicateUtils.isNotEmpty(roles);
        }
        if (PredicateUtils.isEmpty(roles)) {
            return true;
        }
        return !account.getRoles().containsAll(roles);
    }

    public static boolean hasOtherPermissions(Account account, List<Permission> permissions) {
        if (PredicateUtils.isEmpty(account.getPermissions())) {
            return PredicateUtils.isNotEmpty(permissions);
        }
        return true;
    }

    public static void incrementMelody(Account account) {
        account.setMelody(account.getMelody() + 1);
    }

}
