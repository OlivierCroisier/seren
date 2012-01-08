package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

/**
 * A {@link ClassFilter} which based on the detection of the {@link SerenEnhanced} annotation.
 * <p/>
 * This filter does not have any configuration parameters.
 *
 * @author Olivier Croisier
 */
public class AnnotationFilter extends BaseClassFilter {

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
        return super.acceptClass(classLoader, classDefinition) && hasSerenAnnotation(classDefinition);
    }

    private boolean hasSerenAnnotation(CtClass classDefinition) throws ClassNotFoundException {
        return classDefinition.getAnnotation(SerenEnhanced.class) != null;
    }
}
