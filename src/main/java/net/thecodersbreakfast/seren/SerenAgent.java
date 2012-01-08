package net.thecodersbreakfast.seren;

import net.thecodersbreakfast.seren.filter.ClassFilter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;

/**
 * Java Agent for load-time serialization enhancement.
 * <p/>
 * Delegates the class selection to a configurable {@link ClassFilter}.<br/>
 * Configured by the {@link SerenConfig#CONFIG_FILE} file.
 *
 * @author Olivier Croisier
 */
public class SerenAgent implements ClassFileTransformer {

    public static final String TRANSFORMER_CLASS = "net.thecodersbreakfast.seren.SerenClassTransformer";
    private ClassFileTransformer transformer;

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        instrumentation.addTransformer(new SerenAgent());
    }

    public SerenAgent() {
        System.out.println("[SEREN] Seren agent activated.");

        SerenConfig config = null;
        try {
            config = new SerenConfig().load();
        } catch (ConfigurationException e) {
            System.err.println("[SEREN] Configuration error : " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }

        boolean verbose = config.isVerbose();
        if (verbose) {
            System.out.println("[SEREN] Filter class  : " + config.getFilterClassName());
            System.out.println("[SEREN] Filter config : " + config.getFilterConfig());
        }

        try {
            ClassFilter filter = instanciateFilter(config.getFilterClassName());
            filter.setVerbose(verbose);
            filter.configure(config.getFilterConfig());
            transformer = instanciateTransformer(TRANSFORMER_CLASS, filter, verbose);
        } catch (Exception e) {
            System.err.println("[SEREN] Initialization error : " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classDefinition, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        return transformer.transform(loader, className, classDefinition, protectionDomain, classBytes);
    }

    private ClassFilter instanciateFilter(String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> klass = Class.forName(className);
        Constructor<?> klassConstructor = klass.getConstructor((Class[]) null);
        return (ClassFilter) klassConstructor.newInstance((Object[]) null);
    }

    private ClassFileTransformer instanciateTransformer(String className, ClassFilter filter, boolean verbose) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> klass = Class.forName(className);
        Constructor<?> klassConstructor = klass.getConstructor(new Class[]{ClassFilter.class, Boolean.TYPE});
        return (ClassFileTransformer) klassConstructor.newInstance(filter, verbose);
    }

}
