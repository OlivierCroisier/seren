package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Map;

/**
 * A ClassFilter decides which classes should be enhanced.
 * The critieria used to choose are up to each particular implementation of this interface.
 *
 * @author Olivier Croisier
 */
public interface ClassFilter {

    /**
     * Configures the filter with the parameters that were defined in the configuration file.<br/>
     * This method is called only once, when the agent starts.
     *
     * @param config The filter's configuration. Not null.
     * @throws Exception If the configuration is incorrect.
     */
    public void configure(Map<String, String> config) throws Exception;

    /**
     * Decides whether the given class should be instrumented or not.
     * The algorithm is entierely up to the developer.<br/>
     * This method is called once per candidate class.
     *
     * @param classLoader     The class' classloader.
     * @param classDefinition The class that may be enhanced.
     * @return true if the class should be instrumented, false otherwise.
     * @throws Exception If any problem occurs.
     */
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception;

}
