package net.thecodersbreakfast.seren.filter;

import javassist.*;

import java.util.Map;

/**
 * A {@link ClassFilter} performing the most basic checks. It should be used as a superclass by all
 * {@link ClassFilter} implementations.
 * <p/>
 * The candidate class :
 * <ul>
 * <li>must be a class (not an enum, interface, annotation, array, or primitive)</li>
 * <li>must not belong to the core java classes</li>
 * <li>must implement {@link java.io.Serializable}</li>
 * <li>must not already have custom serialization methods (readObject/writeObject)</li>
 * </ul>
 * <p/>
 * This filter does not have any configuration parameters.
 *
 * @author Olivier Croisier
 */
public class BaseClassFilter implements ClassFilter {

    private static final String[] IGNORED_PACKAGES = new String[]{"sun/", "com/sun/", "java/", "javax/"};

    @Override
    public void configure(Map<String, String> config) {
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
        return classDefinition != null &&
                !isCoreJavaClass(classDefinition.getName()) &&
                isAClass(classDefinition) &&
                implementsSerializable(classDefinition) &&
                !alreadyHasMagicSerializationMethods(classDefinition);
    }

    private boolean isCoreJavaClass(String className) {
        for (String IGNORED_PACKAGE : IGNORED_PACKAGES) {
            if (className.startsWith(IGNORED_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAClass(CtClass cl) {
        return (!cl.isInterface() && !cl.isEnum() && !cl.isAnnotation() && !cl.isArray() && !cl.isPrimitive());
    }

    private boolean implementsSerializable(CtClass cl) throws NotFoundException {
        CtClass[] interfaces = cl.getInterfaces();
        if (interfaces != null) {
            for (CtClass itf : interfaces) {
                if ("java.io.Serializable".equals(itf.getName())) {
                    return true;
                }
            }
        }
        CtClass superclass = cl.getSuperclass();
        return superclass != null && implementsSerializable(superclass);
    }

    private boolean alreadyHasMagicSerializationMethods(CtClass cl) throws NotFoundException {
        CtMethod[] methods = cl.getDeclaredMethods();
        if (methods != null) {
            for (CtMethod method : methods) {
                if (isWriteObjectMethod(method) || isReadObjectMethod(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWriteObjectMethod(CtMethod method) throws NotFoundException {
        return "writeObject".equals(method.getName()) &&
                "(Ljava/io/ObjectOutputStream;)V".equals(method.getSignature()) &&
                memberHasModifiers(method, Modifier.PRIVATE & ~Modifier.STATIC);
    }

    private boolean isReadObjectMethod(CtMethod method) throws NotFoundException {
        return "readObject".equals(method.getName()) &&
                "(Ljava/io/ObjectInputStream;)V".equals(method.getSignature()) &&
                memberHasModifiers(method, Modifier.PRIVATE & ~Modifier.STATIC);
    }

    protected boolean memberHasModifiers(CtMember member, int modifiers) {
        return (member.getModifiers() & modifiers) != 0;
    }
}
