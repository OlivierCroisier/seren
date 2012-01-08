package net.thecodersbreakfast.seren;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class SerenConfig {

    private static final String CONFIG_FILE = "seren.properties";
    private static final String FILTERID_PROPERTY = "seren.filter";
    private static final String FILTER_PREFIX = "filter.";

    private String filterClassName;
    private Map<String, String> filterConfig = new HashMap<String, String>();

    public SerenConfig load() throws ConfigurationException {
        Properties config = loadConfigFile();
        parseConfigFile(config);
        return this;
    }

    private Properties loadConfigFile() throws ConfigurationException {
        try {
            InputStream configResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
            if (configResource == null) {
                throw new FileNotFoundException("Could not locate " + CONFIG_FILE + " in the classpath.");
            }
            Properties config = new Properties();
            config.load(configResource);
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Could not load the configuration file (" + CONFIG_FILE + "). " +
                    "Please make sure it exists at the root of the classpath.", e);
        }
    }

    private void parseConfigFile(Properties config) throws ConfigurationException {
        String filterId = config.getProperty(FILTERID_PROPERTY);
        if (filterId == null || filterId.length() == 0) {
            throw new ConfigurationException("Required property '" + FILTERID_PROPERTY + "' is missing or blank.");
        }

        this.filterClassName = config.getProperty(FILTER_PREFIX + filterId);
        if (filterClassName == null || filterClassName.length() == 0) {
            throw new ConfigurationException("Missing class name for filter '" + filterId + "'. " +
                    "Please specify a full class name under the '" + FILTER_PREFIX + filterId + "' property key."
            );
        }

        String filterPrefix = FILTER_PREFIX + filterId;
        int filterConfigPrefixLength = filterPrefix.length() + 1; //+1 for the dot separator
        for (Object key : config.keySet()) {
            String keyName = (String) key;
            if (!keyName.equals(filterPrefix) && keyName.startsWith(filterPrefix)) {
                filterConfig.put(keyName.substring(filterConfigPrefixLength), (String) config.get(keyName));
            }
        }
    }

    public String getFilterClassName() {
        return filterClassName;
    }

    public Map<String, String> getFilterConfig() {
        return filterConfig;
    }
}
