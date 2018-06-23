/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.config;

import org.eclipse.microprofile.config.spi.Converter;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import java.util.*;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Typed
@Vetoed
public class ConfigValueImpl<T> {

    private final ConfigImpl config;

    private String keyOriginal;

    private String keyResolved;

    private Class<?> configEntryType = String.class;

    private boolean evaluateVariables = false;

    private long cacheTimeNs = -1;
    private volatile long reloadAfter = -1;
    private long lastReloadedAt = -1;
    private T lastValue = null;
    // private ConfigChanged valueChangeListener;

    private boolean isList;
    private boolean isSet;

    private T defaultValue;
    private boolean withDefault;

    /**
     * Alternative Converter to be used instead of the default converter
     */
    private Converter<T> converter;

    public ConfigValueImpl(ConfigImpl config, String key) {
        this.config = config;
        this.keyOriginal = key;
    }

    //X @Override
    public <N> ConfigValueImpl<N> as(Class<N> clazz) {
        configEntryType = clazz;
        return (ConfigValueImpl<N>) this;
    }

    //X @Override
    public ConfigValueImpl<List<T>> asList() {
        isList = true;
        ConfigValueImpl<List<T>> listTypedResolver = (ConfigValueImpl<List<T>>) this;

        if (defaultValue == null) {
            // the default for lists is an empty list instead of null
            return listTypedResolver.withDefault(Collections.<T>emptyList());
        }

        return listTypedResolver;
    }

    //X @Override
    public ConfigValueImpl<T> withDefault(T value) {
        defaultValue = value;
        withDefault = true;
        return this;
    }

    //X @Override
    public ConfigValueImpl<T> withStringDefault(String value) {
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Empty String or null supplied as string-default value for property "
                    + keyOriginal);
        }
        value = replaceVariables(value);

        if (isList) {
            defaultValue = splitAndConvertListValue(value);
        } else {
            defaultValue = convert(value);
        }
        withDefault = true;
        return this;
    }

    //X @Override
    public ConfigValueImpl<T> evaluateVariables(boolean evaluateVariables) {
        this.evaluateVariables = evaluateVariables;
        return this;
    }

    private T get(boolean convert) {
        long now = -1;
        if (cacheTimeNs > 0) {
            now = System.nanoTime();
            if (now <= reloadAfter) {
                // now check if anything in the underlying Config got changed
                long lastCfgChange = config.getLastChanged();
                if (lastCfgChange < lastReloadedAt) {
                    return lastValue;
                }
            }
        }

        String valueStr = resolveStringValue();

        if ((valueStr == null || valueStr.isEmpty()) && withDefault) {
            return defaultValue;
        }

        T value;
        if (isList || isSet) {
            value = splitAndConvertListValue(valueStr);
            if (isSet) {
                value = (T) new HashSet((List) value);
            }
        } else {
            value = convert ? convert(valueStr) : (T) valueStr;
        }

        lastValue = value;

        if (cacheTimeNs > 0) {
            reloadAfter = now + cacheTimeNs;
            lastReloadedAt = now;
        }

        return value;
    }

    private void addListValue(List<T> values, StringBuilder sb) {
        String val = sb.toString().trim();
        if (!val.isEmpty()) {
            values.add(convert(val));
        }
        sb.setLength(0);
    }

    public T get() {
        return get(true);
    }

    //X @Override
    public T getValue() {
        T val = get();
        if (val == null) {
            throw new NoSuchElementException("No config value present for key " + keyOriginal);
        }
        return val;
    }

    private String resolveStringValue() {
        String value = null;

        if (value == null) {
            value = config.getValue(keyOriginal);
            this.keyResolved = keyOriginal;
        }

        if (evaluateVariables && value != null) {
            value = replaceVariables(value);

        }
        return value;
    }

    private String replaceVariables(String value) {
        // recursively resolve any ${varName} in the value
        int startVar = 0;
        while ((startVar = value.indexOf("${", startVar)) >= 0) {
            int endVar = value.indexOf("}", startVar);
            if (endVar <= 0) {
                break;
            }
            String varName = value.substring(startVar + 2, endVar);
            if (varName.isEmpty()) {
                break;
            }
            String variableValue = config.access(varName).evaluateVariables(true).get();
            if (variableValue != null) {
                value = value.replace("${" + varName + "}", variableValue);
            }
            startVar++;
        }
        return value;
    }

    private T convert(String value) {
        if (converter != null) {
            return converter.convert(value);
        }

        if (String.class == configEntryType) {
            return (T) value;
        }

        return (T) config.convert(value, configEntryType);
    }

    private T splitAndConvertListValue(String valueStr) {
        if (valueStr == null) {
            return null;
        }

        List list = new ArrayList();
        StringBuilder currentValue = new StringBuilder();
        int length = valueStr.length();
        for (int i = 0; i < length; i++) {
            char c = valueStr.charAt(i);
            if (c == '\\') {
                if (i < length - 1) {
                    char nextC = valueStr.charAt(i + 1);
                    currentValue.append(nextC);
                    i++;
                }
            } else if (c == ',') {
                String trimedVal = currentValue.toString().trim();
                if (trimedVal.length() > 0) {
                    list.add(convert(trimedVal));
                }

                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }

        String trimmedVal = currentValue.toString().trim();
        if (trimmedVal.length() > 0) {
            list.add(convert(trimmedVal));
        }

        return (T) list;
    }

}
