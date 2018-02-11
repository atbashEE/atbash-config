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
package org.apache.geronimo.config.cdi;

import org.apache.geronimo.config.DefaultConfigProvider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ConfigExtension implements Extension {
    private Config config;

    private static final Map<Type, Type> REPLACED_TYPES = new HashMap<>();

    static {
        REPLACED_TYPES.put(double.class, Double.class);
        REPLACED_TYPES.put(int.class, Integer.class);
        REPLACED_TYPES.put(float.class, Float.class);
        REPLACED_TYPES.put(long.class, Long.class);
        REPLACED_TYPES.put(boolean.class, Boolean.class);
    }

    private Set<InjectionPoint> injectionPoints = new HashSet<>();

    public void collectConfigProducer(@Observes ProcessInjectionPoint<?, ?> pip) {
        ConfigProperty configProperty = pip.getInjectionPoint().getAnnotated().getAnnotation(ConfigProperty.class);
        if (configProperty != null) {
            injectionPoints.add(pip.getInjectionPoint());
        }
    }

    public void registerConfigProducer(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        Set<Type> types = filterInjectionPoints(injectionPoints, true);

        Set<Type> providerTypes = filterInjectionPoints(injectionPoints, false);

        types.addAll(providerTypes);

        for (Type type : types) {
            abd.addBean(new ConfigInjectionBean(bm, type));
        }

    }

    private Set<Type> filterInjectionPoints(Set<InjectionPoint> injectionPoints, boolean notProviders) {
        Set<Type> result = new HashSet<>();

        for (InjectionPoint injectionPoint : injectionPoints) {
            boolean keep;
            if (notProviders) {
                keep = isNotprovider(injectionPoint);
            } else {
                keep = !isNotprovider(injectionPoint);
            }

            if (keep) {
                if (REPLACED_TYPES.containsKey(injectionPoint.getType())) {
                    result.add(REPLACED_TYPES.get(injectionPoint.getType()));
                } else {
                    result.add(injectionPoint.getType());
                }
            }
        }
        return result;
    }

    private boolean isNotprovider(InjectionPoint ip) {
        return (ip.getType() instanceof Class) || (ip.getType() instanceof ParameterizedType && ((ParameterizedType) ip.getType()).getRawType() != Provider.class);

    }

    public void validate(@Observes AfterDeploymentValidation add) {
        List<String> deploymentProblems = new ArrayList<>();

        config = ConfigProvider.getConfig();

        for (InjectionPoint injectionPoint : injectionPoints) {
            Type type = injectionPoint.getType();

            // replace native types with their Wrapper types
            type = useReplacedTypes(type);

            ConfigProperty configProperty = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
            if (type instanceof Class) {
                // a direct injection of a ConfigProperty
                // that means a Converter must exist.
                String key = ConfigInjectionBean.getConfigKey(injectionPoint, configProperty);
                if ((isDefaultUnset(configProperty.defaultValue()))
                        && ConfigOptionalValue.getValue(key, (Class) type) == null) {
                    deploymentProblems.add("No Config Value exists for " + key);
                }
            }
        }

        if (!deploymentProblems.isEmpty()) {
            add.addDeploymentProblem(new DeploymentException("Error while validating Configuration\n"
                    + stringJoining("\n", deploymentProblems)));
        }

    }

    private String stringJoining(String joiningToken, List<String> deploymentProblems) {
        StringBuilder result = new StringBuilder();
        for (String deploymentProblem : deploymentProblems) {
            if (result.length() > 0) {
                result.append(joiningToken);
            }
            result.append(deploymentProblem);
        }
        return result.toString();
    }

    private Type useReplacedTypes(Type type) {
        Type result = REPLACED_TYPES.get(type);
        if (result == null) {
            result = type;
        }
        return result;
    }

    public void shutdown(@Observes BeforeShutdown bsd) {
        DefaultConfigProvider.instance().releaseConfig(config);
    }

    static boolean isDefaultUnset(String defaultValue) {
        return defaultValue.equals(ConfigProperty.UNCONFIGURED_VALUE);
    }
}
