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

package com.egolessness.destino.authentication.container;

import com.egolessness.destino.authentication.Authentication;
import com.egolessness.destino.authentication.AuthenticationMessages;
import com.egolessness.destino.authentication.model.Permission;
import com.egolessness.destino.authentication.support.AuthenticationSupport;
import com.egolessness.destino.common.utils.FunctionUtils;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.authentication.model.Account;
import com.egolessness.destino.core.exception.DuplicateIdException;
import com.egolessness.destino.core.exception.DuplicateNameException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.egolessness.destino.authentication.support.AccountSupport.*;

/**
 * container of account
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountContainer implements Container {

    private final ConcurrentSkipListMap<Long, Account> ID_INDEXER = new ConcurrentSkipListMap<>();

    private final ConcurrentHashMap<String, Account> NAME_INDEXER = new ConcurrentHashMap<>();

    private final Account ADMIN;

    public AccountContainer() throws Exception {
        this.ADMIN = AuthenticationSupport.buildDefaultAdminAccount();
        add(this.ADMIN);
    }

    public synchronized void add(final Account account) throws DuplicateIdException, DuplicateNameException {
        if (ID_INDEXER.containsKey(account.getId())) {
            throw new DuplicateIdException(AuthenticationMessages.USER_ADD_DUPLICATE_ID.getValue());
        }

        if (NAME_INDEXER.containsKey(account.getUsername())) {
            throw new DuplicateNameException(AuthenticationMessages.USER_ADD_DUPLICATE_NAME.getValue());
        }

        NAME_INDEXER.computeIfAbsent(account.getUsername(), name -> {
            Account saved = ID_INDEXER.putIfAbsent(account.getId(), account);
            if (saved == null) {
                return account;
            }
            return null;
        });
    }

    public synchronized Account update(final long id, final Account account) {
        if (isAdmin(id)) {
            if (PredicateUtils.isEmpty(account.getPassword())) {
                throw new IllegalArgumentException("The administrator cannot be updated.");
            }
            changePassword(ADMIN, account.getPassword(), account.getModifiedTime());
            return ADMIN;
        }
        return ID_INDEXER.computeIfPresent(id, (key, value) -> {
            if (PredicateUtils.isNotEmpty(account.getPassword())) {
                changePassword(value, account.getPassword(), account.getModifiedTime());
                return value;
            }
            if (value.isFixed()) {
                return value;
            }
            changeRolesAndPermissions(value, account.getRoles(), account.getPermissions());
            value.setModifiedTime(account.getModifiedTime());
            value.setActivated(account.isActivated());
            FunctionUtils.setIfNotNull(value::setEmail, account.getEmail());
            return value;
        });
    }

    private void changePassword(Account account, String password, long modifiedTime) {
        account.setModifiedTime(modifiedTime);
        if (isChangedForPassword(account, password)) {
            account.setPassword(account.getPassword());
            incrementMelody(account);
        }
    }

    private void changeRolesAndPermissions(Account account, List<String> roles, List<Permission> permissions) {
        boolean needUpdateMelody = false;
        if (roles != null) {
            if (hasOtherRoles(account, roles)) {
                needUpdateMelody = true;
            }
            account.setRoles(roles);
        }
        if (permissions != null) {
            if (hasOtherPermissions(account, permissions)) {
                needUpdateMelody = true;
            }
            account.setPermissions(permissions);
        }
        if (needUpdateMelody) {
            incrementMelody(account);
        }
    }

    public Optional<Account> findByUsername(final String username) {
        return Optional.ofNullable(NAME_INDEXER.get(username));
    }

    public synchronized Account remove(final long id) {
        if (isAdmin(id)) {
            throw new IllegalArgumentException("The administrator cannot be deleted.");
        }
        Account removed = ID_INDEXER.remove(id);
        if (removed != null) {
            NAME_INDEXER.remove(removed.getUsername());
        }
        return removed;
    }

    public boolean isAdmin(long id) {
        return ADMIN.getId() == id;
    }

    public boolean isAdmin(Authentication authentication) {
        return Objects.equals(authentication.getUsername(), ADMIN.getUsername());
    }

    public List<Account> all() {
        return new ArrayList<>(ID_INDEXER.values());
    }

    @Override
    public void clear() {
        ID_INDEXER.clear();
        NAME_INDEXER.clear();
        ADMIN.setPassword(AuthenticationSupport.buildAdminDefaultPassword());
        try {
            add(this.ADMIN);
        } catch (Exception ignored) {
        }
    }

}
