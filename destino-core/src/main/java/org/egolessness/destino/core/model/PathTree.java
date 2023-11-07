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

package org.egolessness.destino.core.model;

import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.List;

/**
 * path tree
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PathTree extends Path {

    private static final long serialVersionUID = 2319519955686600226L;

    private List<PathTree> children;

    public PathTree() {
    }

    public PathTree(String path) {
        super(path);
        this.setLeaf(true);
    }

    public PathTree(String path, List<PathTree> children) {
        super(path);
        this.children = children;
        this.setLeaf(PredicateUtils.isEmpty(children));
    }

    public List<PathTree> getChildren() {
        return children;
    }

    public void setChildren(List<PathTree> children) {
        this.children = children;
    }
}
