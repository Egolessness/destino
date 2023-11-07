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

import java.io.Serializable;

/**
 * path
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Path implements Serializable {

    private static final long serialVersionUID = -7689950639005049739L;

    private String path;

    private boolean leaf;

    public Path() {
    }

    public Path(String path) {
        this.path = path;
    }

    public Path(String path, boolean leaf) {
        this.path = path;
        this.leaf = leaf;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

}
