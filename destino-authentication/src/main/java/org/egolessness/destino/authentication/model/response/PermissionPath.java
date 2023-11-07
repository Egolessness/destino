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

package org.egolessness.destino.authentication.model.response;

import org.egolessness.destino.authentication.model.Permission;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * response of permission.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PermissionPath implements Serializable {

    private static final long serialVersionUID = -8372553637264514220L;

    private String path;

    private Set<String> actions;

    private List<PermissionPath> sub;

    public PermissionPath() {
    }

    public PermissionPath(String path, Set<String> actions) {
        this.path = path;
        this.actions = actions;
    }

    public static PermissionPath of(Permission permission) {
        PermissionPath permissionPath = new PermissionPath(permission.getResource(), permission.getActions());
        if (PredicateUtils.isNotEmpty(permission.getChildren())) {
            List<PermissionPath> paths = permission.getChildren().values().stream().map(PermissionPath::of).collect(Collectors.toList());
            permissionPath.setSub(paths);
        }
        return permissionPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public List<PermissionPath> getSub() {
        return sub;
    }

    public void setSub(List<PermissionPath> sub) {
        this.sub = sub;
    }
}
