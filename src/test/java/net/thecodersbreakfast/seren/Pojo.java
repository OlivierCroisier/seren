package net.thecodersbreakfast.seren;

import net.thecodersbreakfast.seren.filter.SerenEnhanced;

import java.io.Serializable;

@SerenEnhanced
public class Pojo implements Serializable {

    boolean primitiveBoolean = true;
    byte primitiveByte = 1;
    char primitiveChar = 2;
    short primitiveShort = 3;
    int primitiveInt = 4;
    long primitiveLong = 5;
    float primitiveFloat = 6;
    double primitiveDouble = 7;

    Boolean wrapperBoolean = Boolean.TRUE;
    Byte wrapperByte = 1;
    Character wrapperChar = 2;
    Short wrapperShort = 3;
    Integer wrapperInt = 4;
    Long wrapperLong = 5L;
    Float wrapperFloat = 6.0F;
    Double wrapperDouble = 7.0;

    String string = "Hello World";

    transient Object object;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pojo pojo = (Pojo) o;

        if (primitiveBoolean != pojo.primitiveBoolean) return false;
        if (primitiveByte != pojo.primitiveByte) return false;
        if (primitiveChar != pojo.primitiveChar) return false;
        if (Double.compare(pojo.primitiveDouble, primitiveDouble) != 0) return false;
        if (Float.compare(pojo.primitiveFloat, primitiveFloat) != 0) return false;
        if (primitiveInt != pojo.primitiveInt) return false;
        if (primitiveLong != pojo.primitiveLong) return false;
        if (primitiveShort != pojo.primitiveShort) return false;
        if (object != null ? !object.equals(pojo.object) : pojo.object != null) return false;
        if (string != null ? !string.equals(pojo.string) : pojo.string != null) return false;
        if (wrapperBoolean != null ? !wrapperBoolean.equals(pojo.wrapperBoolean) : pojo.wrapperBoolean != null)
            return false;
        if (wrapperByte != null ? !wrapperByte.equals(pojo.wrapperByte) : pojo.wrapperByte != null) return false;
        if (wrapperChar != null ? !wrapperChar.equals(pojo.wrapperChar) : pojo.wrapperChar != null) return false;
        if (wrapperDouble != null ? !wrapperDouble.equals(pojo.wrapperDouble) : pojo.wrapperDouble != null)
            return false;
        if (wrapperFloat != null ? !wrapperFloat.equals(pojo.wrapperFloat) : pojo.wrapperFloat != null) return false;
        if (wrapperInt != null ? !wrapperInt.equals(pojo.wrapperInt) : pojo.wrapperInt != null) return false;
        if (wrapperLong != null ? !wrapperLong.equals(pojo.wrapperLong) : pojo.wrapperLong != null) return false;
        if (wrapperShort != null ? !wrapperShort.equals(pojo.wrapperShort) : pojo.wrapperShort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (primitiveBoolean ? 1 : 0);
        result = 31 * result + (int) primitiveByte;
        result = 31 * result + (int) primitiveChar;
        result = 31 * result + (int) primitiveShort;
        result = 31 * result + primitiveInt;
        result = 31 * result + (int) (primitiveLong ^ (primitiveLong >>> 32));
        result = 31 * result + (primitiveFloat != +0.0f ? Float.floatToIntBits(primitiveFloat) : 0);
        temp = primitiveDouble != +0.0d ? Double.doubleToLongBits(primitiveDouble) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (wrapperBoolean != null ? wrapperBoolean.hashCode() : 0);
        result = 31 * result + (wrapperByte != null ? wrapperByte.hashCode() : 0);
        result = 31 * result + (wrapperChar != null ? wrapperChar.hashCode() : 0);
        result = 31 * result + (wrapperShort != null ? wrapperShort.hashCode() : 0);
        result = 31 * result + (wrapperInt != null ? wrapperInt.hashCode() : 0);
        result = 31 * result + (wrapperLong != null ? wrapperLong.hashCode() : 0);
        result = 31 * result + (wrapperFloat != null ? wrapperFloat.hashCode() : 0);
        result = 31 * result + (wrapperDouble != null ? wrapperDouble.hashCode() : 0);
        result = 31 * result + (string != null ? string.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
