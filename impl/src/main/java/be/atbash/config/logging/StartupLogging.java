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
package be.atbash.config.logging;

import be.atbash.config.util.ProxyUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@ApplicationScoped
public class StartupLogging {

    private static Logger LOGGER = LoggerFactory.getLogger(StartupLogging.class);

    private String separator = System.getProperty("line.separator");

    private boolean allLoggingActivated;

    private boolean loggingDisabled;

    @Inject
    private DynamicConfigValueHelper valueHelper;

    @Inject
    private Instance<ModuleConfig> moduleConfigs;

    public void logAtStartApplication(@Observes @Initialized(ApplicationScoped.class) Object event) {

        checkLoggingParameters();

        if (loggingDisabled) {
            return;
        }

        StringBuilder configInfo = new StringBuilder();
        configInfo.append('\n');

        for (ModuleConfig config : moduleConfigs) {
            configInfo.append(getConfigInfo(config));
        }
        LOGGER.info(configInfo.toString());
    }

    private void checkLoggingParameters() {
        String logAllProperty = System.getProperty("atbash.config.log.all");
        allLoggingActivated = "true".equalsIgnoreCase(logAllProperty);

        Config config = ConfigProvider.getConfig();
        Boolean disabledLogging = config.getOptionalValue("atbash.config.log.disabled", Boolean.class);
        loggingDisabled = disabledLogging == null ? false : disabledLogging;
    }

    //generic alternative to #toString to avoid an overriden #toString at custom implementations
    public String getConfigInfo(ModuleConfig config) {
        if (loggingDisabled) {
            return null;
        }
        StringBuilder info = new StringBuilder();

        List<String> methodNames = new ArrayList<>();

        Class currentClass = ProxyUtils.getUnproxiedClass(config.getClass());
        ModuleConfigName moduleConfigName = (ModuleConfigName) currentClass.getAnnotation(ModuleConfigName.class);

        while (currentClass != null &&
                !Object.class.getName().equals(currentClass.getName())) {

            StringBuilder configLogging = new StringBuilder();

            //inspect the other methods of the implementing class
            for (Method currentMethod : currentClass.getDeclaredMethods()) {
                if (!currentMethod.isAnnotationPresent(ConfigEntry.class) ||
                        methodNames.contains(currentMethod.getName())) {
                    continue;
                }

                methodNames.add(currentMethod.getName());

                configLogging.append("   method:\t").append(currentMethod.getName());
                configLogging.append(separator);

                addConfigEntryValue(config, configLogging, currentMethod);

                configLogging.append(separator);
                configLogging.append(separator);
            }

            // Was there anything annotated with @ConfigEntry
            if (configLogging.length() > 0) {

                outputConfigurationName(info, moduleConfigName, currentClass);

                info.append(configLogging);
            }

            // Look into the parent class
            currentClass = currentClass.getSuperclass();
        }

        return info.toString();
    }

    private void outputConfigurationName(StringBuilder info, ModuleConfigName moduleConfigName, Class currentClass) {

        if (moduleConfigName == null) {
            // No annotation -> output class name, azlso for the parent classes.
            info.append("Config implementation: ");
            info.append(currentClass.getName());
            info.append(separator);
        } else {
            if (info.length() > 0) {
                // There is already something to output, so we are in a parent class
                if (moduleConfigName.className()) {
                    // moduleConfig specifies that we need to output the class name.
                    outputNameAndClassName(info, moduleConfigName, currentClass);
                }
                // When className not needs to be outputted, do not repeat module config name
            } else {
                // The first class where we have something to log.
                if (moduleConfigName.className()) {
                    // moduleConfig specifies that we need to output the class name.
                    outputNameAndClassName(info, moduleConfigName, currentClass);
                } else {
                    info.append(moduleConfigName.value());
                    info.append(" :");
                    info.append(separator);
                }
            }
        }
    }

    private void outputNameAndClassName(StringBuilder info, ModuleConfigName moduleConfigName, Class currentClass) {
        info.append("Config implementation: ");
        info.append(moduleConfigName.value());
        info.append(" ( ");
        info.append(currentClass.getName());
        info.append(" )");
        info.append(separator);
    }

    private void addConfigEntryValue(ModuleConfig config, StringBuilder info, Method currentMethod) {
        ConfigEntry configEntry = currentMethod.getAnnotation(ConfigEntry.class);
        if (!Void.class.equals(configEntry.classResult())) {
            info.append("   value:\t").append(configEntry.classResult().getCanonicalName());
        } else {
            if (configEntry.value() != null && configEntry.value().length > 0) {

                info.append("   value:\t").append(Arrays.toString(configEntry.value()));
            } else {
                if (currentMethod.getParameterTypes().length == 0) {
                    if (void.class.equals(currentMethod.getReturnType())) {
                        info.append("   value:\tunknown - Method has no return value");
                    } else {

                        executeMethodForConfigRetrieval(config, info, currentMethod, configEntry.noLogging());
                    }
                } else {
                    info.append("   value:\tunknown - Method has a parameter");
                }
            }
        }

    }

    private void executeMethodForConfigRetrieval(ModuleConfig config, StringBuilder info, Method currentMethod, boolean noLogging) {
        Object value;
        try {
            value = currentMethod.invoke(config);
            if (noLogging && !allLoggingActivated) {
                info.append("   value:\t").append("No logging parameter active ").append(value == null ? "null" : "[non null value]");
            } else {
                info.append("   value:\t");
                if (value == null) {
                    info.append("null");
                } else {
                    info.append(valueHelper.getTruncatedConfigValue(value.toString()));
                }

            }
        } catch (IllegalAccessException e) {
            info.append("   value:\t[unknown]");
        } catch (InvocationTargetException e) {
            info.append("   value:\t [unknown]");
        }
    }

    public static void logConfiguration(ModuleConfig moduleConfig) {

        StartupLogging startupLogging = new StartupLogging();
        startupLogging.valueHelper = new DynamicConfigValueHelper();

        startupLogging.checkLoggingParameters();

        if (startupLogging.loggingDisabled) {
            return;
        }

        LOGGER.info(startupLogging.getConfigInfo(moduleConfig));
    }
}
