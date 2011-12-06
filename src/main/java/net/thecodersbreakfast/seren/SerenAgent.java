package net.thecodersbreakfast.seren;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * @author olivier
 */
public class SerenAgent implements ClassFileTransformer {

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new SerenAgent(agentArgument));
    }

    private static final String[] IGNORED_PACKAGES = new String[]{"sun/", "java/", "javax/"};


    public SerenAgent(String agentArgument) {
        //TODO
    }

    static String capitalize(String s) {
        if (s == null) return null;
        if (s.length() == 1) return s.toUpperCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classDefinition, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        if (isCoreJavaClass(className) || !isClassToBeTransformed(className)) return classBytes;
        return transformClass(className, classDefinition, classBytes);
    }

    private boolean isCoreJavaClass(String className) {
        for (String IGNORED_PACKAGE : IGNORED_PACKAGES) {
            if (className.startsWith(IGNORED_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClassToBeTransformed(String className) {
        return true; //TODO
    }

    private byte[] transformClass(String className, Class<?> classDefinition, byte[] classBytes) {
        ClassPool pool = ClassPool.getDefault();
        CtClass cl = null;
        try {
            cl = pool.makeClass(new java.io.ByteArrayInputStream(classBytes));

            if (isSerializableClass(cl) && !classAlreadyHasMagicSerializationMethods(cl)) {
                List<FieldInfo> serializableFields = findSerializableFields(cl);
                createCustomSerializationMethods(cl, serializableFields);
                classBytes = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("Could not instrument  " + className + ", exception : " + e.getMessage());
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return classBytes;
    }

    private boolean isSerializableClass(CtClass cl) throws NotFoundException {
        if (cl == null || cl.isInterface() || cl.isEnum()) {
            return false;
        }

        CtClass[] interfaces = cl.getInterfaces();
        for (CtClass itf : interfaces) {
            if ("java.io.Serializable".equals(itf.getName())) {
                return true;
            }
        }

        return isSerializableClass(cl.getSuperclass());
    }


    private boolean classAlreadyHasMagicSerializationMethods(CtClass cl) throws NotFoundException {
        CtMethod[] methods = cl.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (isWriteObjectMethod(method) || isReadObjectMethod(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWriteObjectMethod(CtMethod method) throws NotFoundException {
        return "(Ljava/io/ObjectOutputStream;)V".equals(method.getSignature()) &&
                memberHasModifiers(method, Modifier.PRIVATE & ~Modifier.STATIC);
    }

    private boolean isReadObjectMethod(CtMethod method) throws NotFoundException {
        return "(Ljava/io/ObjectInputStream;)V".equals(method.getSignature()) &&
                memberHasModifiers(method, Modifier.PRIVATE & ~Modifier.STATIC);
    }

    private void createCustomSerializationMethods(CtClass cl, List<FieldInfo> serializableFields) throws CannotCompileException, IOException {
        if (serializableFields == null || serializableFields.size() == 0) {
            return;
        }

        StringBuilder serializationCode = new StringBuilder();
        StringBuilder deserializationCode = new StringBuilder();

        generateCodeForFields(serializableFields, serializationCode, deserializationCode);

        String serCode = serializationCode.toString();
        String deserCode = deserializationCode.toString();

        System.out.println("[Seren] Enhancing " + cl.getName());
        //System.out.println("- Serialization code for " + cl.getName() + ": \n\n" + serCode + "\n\n" + deserCode);

        CtMethod writeObjectMethod = CtMethod.make(serCode, cl);
        cl.addMethod(writeObjectMethod);
        CtMethod readObjectMethod = CtMethod.make(deserCode, cl);
        cl.addMethod(readObjectMethod);
    }

    private void generateCodeForFields(List<FieldInfo> serializableFields, Appendable serializationCode, Appendable deserializationCode) throws IOException {
        serializationCode.append("private void writeObject (java.io.ObjectOutputStream out) throws java.io.IOException { \n");
        deserializationCode.append("private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException { \n");
        for (FieldInfo field : serializableFields) {
            if (field.isPrimitive()) {
                appendCodeForPrimitiveField(field, serializationCode, deserializationCode);
            } else if (field.isWrapper()) {
                appendCodeForWrapperField(field, serializationCode, deserializationCode);
            } else if (field.isString()) {
                appendCodeForStringField(field, serializationCode, deserializationCode);
            } else {
                appendCodeForGenericField(field, serializationCode, deserializationCode);
            }
        }
        serializationCode.append("} \n");
        deserializationCode.append("} \n");
    }

    void appendCodeForPrimitiveField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
        String capitalizedType = capitalize(field.getSimpleType());

        serializationCode.append("out.write");
        serializationCode.append(capitalizedType);
        serializationCode.append("(");
        serializationCode.append(field.getName());
        serializationCode.append("); \n");

        deserializationCode.append(field.getName());
        deserializationCode.append(" = in.read");
        deserializationCode.append(capitalizedType);
        deserializationCode.append("(); \n");
    }

    void appendCodeForWrapperField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
        String name = field.getName();
        String type = field.toPrimitiveType();

        serializationCode.append("out.writeBoolean(").append(name).append(" != null); \n");
        serializationCode.append("if (").append(name).append(" != null) { \n");
        serializationCode.append("    out.write").append(capitalize(type)).append("(").append(name).append(".").append(type).append("Value()); \n");
        serializationCode.append("} \n");

        deserializationCode.append("if (in.readBoolean()) { \n");
        deserializationCode.append(name + " = " + field.getType() + ".valueOf(in.read").append(capitalize(type)).append("()); \n");
        deserializationCode.append("} else { \n");
        deserializationCode.append(name + " = null; \n");
        deserializationCode.append("} \n");
    }

    void appendCodeForStringField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
        String name = field.getName();

        serializationCode.append("out.writeBoolean(").append(name).append(" != null); \n");
        serializationCode.append("if (").append(name).append(" != null) { \n");
        serializationCode.append("    out.writeBoolean(").append(name).append(".length() > 0xFFFF); \n");
        serializationCode.append("    if (").append(name).append(".length() > 0xFFFF) { \n");
        serializationCode.append("        out.writeObject(").append(name).append("); \n");
        serializationCode.append("    } else { \n");
        serializationCode.append("        out.writeUTF(").append(name).append("); \n");
        serializationCode.append("    } \n");
        serializationCode.append("} \n");

        deserializationCode.append("if (in.readBoolean()) { \n");
        deserializationCode.append("    if (in.readBoolean()) { \n");
        deserializationCode.append(name).append(" = (String) in.readObject(); \n");
        deserializationCode.append("    } else { \n");
        deserializationCode.append(name).append("= in.readUTF(); \n");
        deserializationCode.append("    } \n");
        deserializationCode.append("} else { \n");
        deserializationCode.append(name + " = null; \n");
        deserializationCode.append("} \n");
    }

    void appendCodeForGenericField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
        String name = field.getName();

        serializationCode.append("out.writeBoolean(").append(name).append(" != null); \n");
        serializationCode.append("if (").append(name).append(" != null) { \n");
        serializationCode.append("out.writeObject(").append(name).append("); \n");
        serializationCode.append("} \n");

        deserializationCode.append("if (in.readBoolean()) { \n");
        deserializationCode.append(name + " = (" + field.getType() + ") in.readObject(); \n");
        deserializationCode.append("} else { \n");
        deserializationCode.append(name + " = null; \n");
        deserializationCode.append("} \n");
    }


    private List<FieldInfo> findSerializableFields(CtClass cl) throws NotFoundException {
        List<FieldInfo> serializableFields = new ArrayList<FieldInfo>();
        CtField[] fields = cl.getDeclaredFields();
        for (CtField field : fields) {
            if (isSerializableField(field)) {
                serializableFields.add(new FieldInfo(field.getName(), field.getType().getName()));
            }
        }
        return serializableFields;
    }

    private boolean isSerializableField(CtField field) {
        return !memberHasModifiers(field, Modifier.STATIC) && !memberHasModifiers(field, Modifier.TRANSIENT);
    }

    private boolean memberHasModifiers(CtMember member, int modifiers) {
        return (member.getModifiers() & modifiers) == modifiers;
    }

    /*
    private void writeObject (ObjectOutputStream out) throws IOException {}
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {}
    */

}
