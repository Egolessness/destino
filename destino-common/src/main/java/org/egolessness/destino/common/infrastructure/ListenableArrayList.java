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

package org.egolessness.destino.common.infrastructure;

import org.egolessness.destino.common.infrastructure.monitor.Monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * listenable array list
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ListenableArrayList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 389832237982372691L;

    private Monitor<ListenableArrayList<E>> monitor;

    public Monitor<ListenableArrayList<E>> getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor<ListenableArrayList<E>> monitor) {
        this.monitor = monitor;
        monitor.notifyUpdate(this);
    }

    public ListenableArrayList(int initialCapacity) {
        super(initialCapacity);
        this.monitor = new Monitor<>();
    }

    public ListenableArrayList() {
        this.monitor = new Monitor<>();
    }

    public ListenableArrayList(Collection<? extends E> c) {
        super(null != c ? c : Collections.emptyList());
        this.monitor = new Monitor<>();
    }

    public ListenableArrayList(Collection<? extends E> c, Monitor<ListenableArrayList<E>> monitor) {
        super(null != c ? c : Collections.emptyList());
        this.monitor = monitor;
        monitor.notifyUpdate(this);
    }

    @Override
    public E set(int index, E element) {
        E r = super.set(index, element);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public boolean add(E e) {
        boolean r = super.add(e);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        monitor.notifyUpdate(this);
    }

    @Override
    public E remove(int index) {
        E r = super.remove(index);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public boolean remove(Object o) {
        boolean r = super.remove(o);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public void clear() {
        super.clear();
        monitor.notifyUpdate(this);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean r = super.addAll(c);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean r = super.addAll(index, c);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        monitor.notifyUpdate(this);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean r = super.removeAll(c);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean r = super.retainAll(c);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean r = super.removeIf(filter);
        monitor.notifyUpdate(this);
        return r;
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        super.replaceAll(operator);
        monitor.notifyUpdate(this);
    }
}
