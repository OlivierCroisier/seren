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
        SerenConfig config = null;
        try {
            config = new SerenConfig().load();
        } catch (ConfigurationException e) {
            e.printStackTrace(); //FIXME
            System.exit(0);
        }

        System.out.println("[SEREN] Filter class  : " + config.getFilterClassName());
        System.out.println("[SEREN] Filter config : " + config.getFilterConfig());

        try {
            ClassFilter filter = instanciateFilter(config.getFilterClassName());
            filter.configure(config.getFilterConfig());
            transformer = instanciateTransformer(TRANSFORMER_CLASS, filter);
        } catch (Exception e) {
            e.printStackTrace(); //FIXME
            System.exit(0);
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classDefinition, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        return transformer.transform(loader, className, classDefinition, protectionDomain, classBytes);
    }

    private ClassFilter instanciateFilter(String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> klass = Class.forName(className);
        Constructor<?> klassConstructor = klass.getConstructor(null);
        return (ClassFilter) klassConstructor.newInstance(null);
    }

    private ClassFileTransformer instanciateTransformer(String className, ClassFilter filter) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> klass = Class.forName(className);
        Constructor<?> klassConstructor = klass.getConstructor(new Class[]{ClassFilter.class});
        return (ClassFileTransformer) klassConstructor.newInstance(filter);
    }

}
