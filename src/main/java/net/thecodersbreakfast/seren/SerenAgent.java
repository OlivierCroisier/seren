package net.thecodersbreakfast.seren;

import net.thecodersbreakfast.seren.filter.ClassFilter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author olivier
 */
public class SerenAgent implements ClassFileTransformer {

    public static final String CONFIG_FILE = "seren.properties";
    private ClassFilter filter;
    private SerenClassTransformer transformer;

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new SerenAgent(agentArgument));
    }

    private static final String[] IGNORED_PACKAGES = new String[]{"sun/", "java/", "javax/"};


    public SerenAgent(String filterName) {
        if (filterName == null || filterName.length() == 0) {
            stopAgent("Usage: -javaagent:<path/to/seren.jar>=<classFilterName>");
        }

        Properties config = null;
        try {
            config = loadConfiguration(CONFIG_FILE);
        } catch (IOException e) {
            stopAgent("Unable to load the configuration.");
        }
        String filterClassName = config.getProperty(filterName);

        if (filterClassName == null || filterClassName.length() == 0) {
            System.err.println("Missing filter class name in " + CONFIG_FILE + ", under the key " + filterName);
            System.exit(0);
        }
        instanciateFilter(filterClassName);

        transformer = new SerenClassTransformer();

        Map<String, String> filterConfig = getFilterConfiguration(filterName, config);
        filter.configure(filterConfig);

    }

    private void stopAgent(String message) {
        stopAgent(message, null);
    }

    private void stopAgent(String message, Throwable t) {
        System.err.println(message);
        if (t != null) {
            t.printStackTrace();
        }
        System.exit(0);
    }

    private void instanciateFilter(String filterClassName) {

        try {
            Class<?> filterClass = Class.forName(filterClassName);
            Constructor<?> filterClassConstructor = filterClass.getConstructor(null);
            filter = (ClassFilter) filterClassConstructor.newInstance(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getFilterConfiguration(String filterName, Properties config) {
        Map<String, String> filterConfig = new HashMap<String, String>();
        int filterConfigPrefixLength = filterName.length() + 1;
        for (Object key : config.keySet()) {
            String keyName = (String) key;
            if (!keyName.equals(filterName) && keyName.startsWith(filterName)) {
                filterConfig.put(keyName.substring(filterConfigPrefixLength), (String) config.get(keyName));
            }
        }
        return filterConfig;
    }

    private Properties loadConfiguration(String configFile) throws IOException {
        InputStream configResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
        if (configResource == null) {
            throw new FileNotFoundException("Could not locate " + configFile + " in the classpath.");
        }
        Properties config = new Properties();
        config.load(configResource);
        return config;
    }


    public byte[] transform(ClassLoader loader, String className, Class<?> classDefinition, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        if (isCoreJavaClass(className)) return classBytes;
        return transformer.transformClass(loader, className, classDefinition, classBytes, filter);
    }

    private boolean isCoreJavaClass(String className) {
        for (String IGNORED_PACKAGE : IGNORED_PACKAGES) {
            if (className.startsWith(IGNORED_PACKAGE)) {
                return true;
            }
        }
        return false;
    }


}
