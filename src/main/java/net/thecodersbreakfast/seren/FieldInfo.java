package net.thecodersbreakfast.seren;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a field found in an enhanced class.
 *
 * @author Olivier Croisier
 */
public final class FieldInfo {

    /**
     * Maps wrappers to their primitive counterparts.<br/>
     * Used to detect wrappers and primitives, and to deduce the names of the ObjectOutputStream's writeXXX methods.
     */
    private static final Map<String, String> WRAPPERS_TO_PRIMITIVES = new HashMap<String, String>();

    static {
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Byte", "byte");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Character", "char");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Short", "short");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Integer", "int");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Long", "long");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Float", "float");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Double", "double");
        WRAPPERS_TO_PRIMITIVES.put("java.lang.Boolean", "boolean");
    }

    private final String name;
    private final String type;
    private final String simpleType;

    public FieldInfo(String name, String type) {
        this.name = name;
        this.type = type;
        this.simpleType = toSimpleType(type);
    }

    private String toSimpleType(String type) {
        if (type == null) return null;
        int lastDotIndex = type.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == type.length() - 1) return type;
        return type.substring(lastDotIndex + 1);
    }

    public boolean isPrimitive() {
        return WRAPPERS_TO_PRIMITIVES.containsValue(type);
    }

    public boolean isWrapper() {
        return WRAPPERS_TO_PRIMITIVES.containsKey(type);
    }

    public boolean isString() {
        return "java.lang.String".equals(type);
    }

    public String toPrimitiveType() {
        return WRAPPERS_TO_PRIMITIVES.get(type);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSimpleType() {
        return simpleType;
    }
}
