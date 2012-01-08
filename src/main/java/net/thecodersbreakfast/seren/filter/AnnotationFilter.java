package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Map;

public class AnnotationFilter extends BaseClassFilter {

    @Override
    public void configure(Map<String, String> config) {
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
        return super.acceptClass(classLoader, classDefinition) && hasSerenAnnotation(classDefinition);
    }

    private boolean hasSerenAnnotation(CtClass classDefinition) throws ClassNotFoundException {
        return classDefinition.getAnnotation(SerenEnhanced.class) != null;
    }
}
