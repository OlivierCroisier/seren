package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Map;

/**
 * @author olivier
 */
public interface ClassFilter {

    public void configure(Map<String, String> config);

    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition);

}
