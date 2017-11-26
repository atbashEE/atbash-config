/*
 * Copyright 2017 Rudy De Busscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 *
 */

public final class ClassUtils {

    /**
     * Private internal log instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClassUtils.class);

    /**
     */
    private static final ClassLoaderAccessor THREAD_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() throws Throwable {
            return Thread.currentThread().getContextClassLoader();
        }
    };

    /**
     */
    private static final ClassLoaderAccessor CLASS_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() throws Throwable {
            return ClassUtils.class.getClassLoader();
        }
    };

    /**
     */
    private static final ClassLoaderAccessor SYSTEM_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() throws Throwable {
            return ClassLoader.getSystemClassLoader();
        }
    };

    private ClassUtils() {
    }

    /**
     * Returns the specified resource by checking the current thread's
     * {@link Thread#getContextClassLoader() context class loader}, then the
     * current ClassLoader (<code>ClassUtils.class.getClassLoader()</code>), then the system/application
     * ClassLoader (<code>ClassLoader.getSystemClassLoader()</code>, in that order, using
     * {@link ClassLoader#getResourceAsStream(String) getResourceAsStream(name)}.
     *
     * @param name the name of the resource to acquire from the classloader(s).
     * @return the InputStream of the resource found, or <code>null</code> if the resource cannot be found from any
     * of the three mentioned ClassLoaders.
     */
    public static InputStream getResourceAsStream(String name) {

        InputStream is = THREAD_CL_ACCESSOR.getResourceStream(name);

        if (is == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Resource [" + name + "] was not found via the thread context ClassLoader.  Trying the " +
                        "current ClassLoader...");
            }
            is = CLASS_CL_ACCESSOR.getResourceStream(name);
        }

        if (is == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Resource [" + name + "] was not found via the current class loader.  Trying the " +
                        "system/application ClassLoader...");
            }
            is = SYSTEM_CL_ACCESSOR.getResourceStream(name);
        }

        if (is == null && LOG.isTraceEnabled()) {

            LOG.trace("Resource [" + name + "] was not found via the thread context, current, or " +
                    "system/application ClassLoaders.  All heuristics have been exhausted.  Returning null.");

        }

        return is;
    }

    private interface ClassLoaderAccessor {
        Class loadClass(String fqcn);

        InputStream getResourceStream(String name);
    }

    private static abstract class ExceptionIgnoringAccessor implements ClassLoaderAccessor {

        public Class loadClass(String fqcn) {
            Class clazz = null;
            ClassLoader cl = getClassLoader();
            if (cl != null) {
                try {
                    clazz = cl.loadClass(fqcn);
                } catch (ClassNotFoundException e) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Unable to load clazz named [" + fqcn + "] from class loader [" + cl + "]");
                    }
                }
            }
            return clazz;
        }

        public InputStream getResourceStream(String name) {
            InputStream is = null;
            ClassLoader cl = getClassLoader();
            if (cl != null) {
                is = cl.getResourceAsStream(name);
            }
            return is;
        }

        final ClassLoader getClassLoader() {
            try {
                return doGetClassLoader();
            } catch (Throwable t) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unable to acquire ClassLoader.", t);
                }
            }
            return null;
        }

        protected abstract ClassLoader doGetClassLoader() throws Throwable;
    }

}
