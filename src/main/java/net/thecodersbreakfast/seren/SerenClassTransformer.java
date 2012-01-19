package net.thecodersbreakfast.seren;

import javassist.*;

import net.thecodersbreakfast.seren.filter.ClassFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.*;

/**
 * A {@link ClassFileTransformer} that enhances the serialization speed by injecting optimized writeObject/readObject
 * methods.
 *
 * @author Olivier Croisier
 */
public class SerenClassTransformer implements ClassFileTransformer {

    private ClassFilter filter;
    private boolean verbose;
    private ClassPool pool = ClassPool.getDefault();

    public SerenClassTransformer(ClassFilter filter, boolean verbose) {
        this.filter = filter;
        this.verbose = verbose;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        CtClass cl = null;
        try {
            cl = pool.makeClass(new ByteArrayInputStream(classBytes));

            if (filter.acceptClass(loader, cl)) {
                if (verbose) {
                    System.out.println("[SEREN] Enhancing class : " + cl.getName());
                }

                List<FieldInfo> serializableFields = findSerializableFields(cl);
                createCustomSerializationMethods(cl, serializableFields);
                classBytes = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("[SEREN] Warning: could not enhance class " + className + " : " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return classBytes;
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

    private void appendCodeForPrimitiveField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
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

    private void appendCodeForWrapperField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
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

    private void appendCodeForStringField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
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

    private void appendCodeForGenericField(FieldInfo field, Appendable serializationCode, Appendable deserializationCode) throws IOException {
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
        if (fields != null) {
            for (CtField field : fields) {
                if (isSerializableField(field)) {
                    serializableFields.add(new FieldInfo(field.getName(), field.getType().getName()));
                }
            }
        }
        return serializableFields;
    }

    private boolean isSerializableField(CtField field) {
    	return !isStatic(field.getModifiers()) && !isTransient(field.getModifiers()) && !isFinal(field.getModifiers());
    }

    private static String capitalize(String s) {
        if (s == null) return null;
        if (s.length() == 1) return s.toUpperCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }


}
