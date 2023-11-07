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

import org.egolessness.destino.common.fixedness.EFunction;
import org.egolessness.destino.common.exception.NotRequiredServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * copy from {@link ServiceLoader}， and add class instantiate function
 */
public class CustomServiceLoader<S> implements Iterable<S> {

    private static final String PREFIX = "META-INF/services/";

    private final Class<S> service;

    private final ClassLoader loader;

    private final AccessControlContext acc;

    private final LinkedHashMap<String,S> providers = new LinkedHashMap<>();

    private CustomServiceLoader<S>.LazyIterator lookupIterator;

    private final EFunction<Class<S>, S> instantiationFunction;

    public void reload() {
        providers.clear();
        lookupIterator = new CustomServiceLoader<S>.LazyIterator(service, loader);
    }

    private CustomServiceLoader(Class<S> svc, ClassLoader cl, EFunction<Class<S>, S> ief) {
        service = Objects.requireNonNull(svc, "Service interface cannot be null");
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        instantiationFunction = (ief == null) ? Class::newInstance : ief;
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        reload();
    }

    private static void fail(Class<?> service, String msg, Throwable cause)
            throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                cause);
    }

    private static void fail(Class<?> service, String msg)
            throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg)
            throws ServiceConfigurationError
    {
        fail(service, u + ":" + line + ": " + msg);
    }

    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
                          List<String> names)
            throws IOException, ServiceConfigurationError
    {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!providers.containsKey(ln) && !names.contains(ln))
                names.add(ln);
        }
        return lc + 1;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private Iterator<String> parse(Class<?> service, URL u)
            throws ServiceConfigurationError
    {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names)) >= 0);
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names.iterator();
    }

    private class LazyIterator
            implements Iterator<S>
    {

        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        @SuppressWarnings("unchecked")
        private S nextService() {
            if (!hasNextService())
                throw new NoSuchElementException();
            String cn = nextName;
            nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader);
            } catch (ClassNotFoundException x) {
                fail(service,
                        "Provider " + cn + " not found");
            }
            if (!service.isAssignableFrom(c)) {
                fail(service,
                        "Provider " + cn  + " not a subtype");
            }
            try {
                S p = instantiationFunction.apply((Class<S>) c);
                providers.put(cn, p);
                return p;
            } catch (NotRequiredServiceException e) {
                throw e;
            } catch (Throwable x) {
                fail(service,
                        "Provider " + cn + " could not be instantiated",
                        x);
            }
            throw new Error();
        }

        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = this::hasNextService;
                return AccessController.doPrivileged(action, acc);
            }
        }

        public S next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<S> action = this::nextService;
                return AccessController.doPrivileged(action, acc);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {

            final Iterator<Map.Entry<String,S>> knownProviders
                    = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext())
                    return true;
                return lookupIterator.hasNext();
            }

            public S next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue();
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    public static <S> CustomServiceLoader<S> load(Class<S> service, ClassLoader loader, EFunction<Class<S>, S> function)
    {
        return new CustomServiceLoader<>(service, loader, function);
    }

    public static <S> CustomServiceLoader<S> load(Class<S> service,
                                            ClassLoader loader)
    {
        return CustomServiceLoader.load(service, loader, null);
    }

    public static <S> CustomServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return CustomServiceLoader.load(service, cl);
    }

    public static <S> CustomServiceLoader<S> load(Class<S> service, EFunction<Class<S>, S> function)
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new CustomServiceLoader<>(service, cl, function);
    }

    public static <S> CustomServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return CustomServiceLoader.load(service, prev);
    }

    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}
