/*
 * Copyright 2017-2018 Rudy De Busscher
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

import be.atbash.util.StringUtils;
import be.atbash.util.reflection.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
// TODO Maybe a candidate for Utils-se
public final class ResourceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceUtils.class);

    /**
     * Resource path prefix that specifies to load from a classpath location, value is <b>{@code classpath:}</b>
     */
    public static final String CLASSPATH_PREFIX = "classpath:";
    /**
     * Resource path prefix that specifies to load from a url location, value is <b>{@code url:}</b>
     */
    public static final String URL_PREFIX = "url:";
    /**
     * Resource path prefix that specifies to load from a file location, value is <b>{@code file:}</b>
     */
    public static final String FILE_PREFIX = "file:";

    /**
     * Singleton pattern
     */
    private ResourceUtils() {
    }

    /**
     * Returns {@code true} if the resource at the specified path exists, {@code false} otherwise.  This
     * method supports scheme prefixes on the path as defined in {@link #getInputStreamForPath(String)}.
     *
     * @param resourcePath the path of the resource to check.
     * @return {@code true} if the resource at the specified path exists, {@code false} otherwise.
     */
    public static boolean resourceExists(String resourcePath) {
        InputStream stream = null;
        boolean exists = false;

        try {
            stream = getInputStream(resourcePath);
            exists = stream != null;
        } catch (IOException e) {
            stream = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return exists;
    }

    public static InputStream getInputStream(String path) throws IOException {
        // FIXME Refactor in resource API
        InputStream result = null;
        if (StringUtils.hasText(path)) {
            try {
                result = ResourceUtils.getInputStreamForPath(path);
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unable to load optional path '" + path + "'.", e);
                }
            }

        }
        return result;

    }

    /**
     * Returns the InputStream for the resource represented by the specified path, supporting scheme
     * prefixes that direct how to acquire the input stream
     * ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
     * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}).  If the path is not prefixed by one
     * of these schemes, the path is assumed to be a file-based path that can be loaded with a
     * {@link FileInputStream FileInputStream}.
     *
     * @param resourcePath the String path representing the resource to obtain.
     * @return the InputStraem for the specified resource.
     * @throws IOException if there is a problem acquiring the resource at the specified path.
     */
    private static InputStream getInputStreamForPath(String resourcePath) throws IOException {

        InputStream is;
        if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
            is = loadFromClassPath(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(URL_PREFIX)) {
            is = loadFromUrl(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(FILE_PREFIX)) {
            is = loadFromFile(stripPrefix(resourcePath));

        } else {
            is = loadFromFile(resourcePath);
        }

        if (is == null) {
            throw new IOException("Resource [" + resourcePath + "] could not be found.");
        }

        return is;
    }

    private static InputStream loadFromFile(String path) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening file [" + path + "]...");
        }
        return new FileInputStream(path);
    }

    private static InputStream loadFromUrl(String urlPath) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening url {}", urlPath);
        }
        URL url = new URL(urlPath);
        return url.openStream();
    }

    private static InputStream loadFromClassPath(String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening resource from class path [{}]", path);
        }
        return ClassUtils.getResourceAsStream(path);
    }

    private static String stripPrefix(String resourcePath) {
        return resourcePath.substring(resourcePath.indexOf(":") + 1);
    }

    /**
     * Returns {@code true} if the resource path is not null and starts with one of the recognized
     * resource prefixes ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
     * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}), {@code false} otherwise.
     *
     * @param resourcePath the resource path to check
     * @return {@code true} if the resource path is not null and starts with one of the recognized
     * resource prefixes, {@code false} otherwise.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean hasResourcePrefix(String resourcePath) {
        return resourcePath != null &&
                (resourcePath.startsWith(CLASSPATH_PREFIX) ||
                        resourcePath.startsWith(URL_PREFIX) ||
                        resourcePath.startsWith(FILE_PREFIX));
    }

}
