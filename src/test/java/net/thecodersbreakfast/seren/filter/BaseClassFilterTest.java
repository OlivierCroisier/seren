package net.thecodersbreakfast.seren.filter;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author olivier
 */
public class BaseClassFilterTest {

    @Test
    public void detectCoreJavaClass() {
        BaseClassFilter filter = new BaseClassFilter();

        assertTrue(filter.isCoreJavaClass("java/lang/String"));
        assertTrue(filter.isCoreJavaClass("javax/swing/JFrame"));
        assertTrue(filter.isCoreJavaClass("sun/beans/infos/ComponentBeanInfo"));
        assertTrue(filter.isCoreJavaClass("com/sun/beans/ObjectHandler"));
        assertFalse(filter.isCoreJavaClass("net/thecodersbreakfast/seren/SerenAgent.java"));
    }

    @Test
    public void detectIsAClass() throws IOException {
        ClassPool pool = ClassPool.getDefault();
        CtClass pojoClass = pool.makeClass(getClass().getResourceAsStream("PojoClass.class"));
        CtClass pojoInterface = pool.makeClass(getClass().getResourceAsStream("PojoInterface.class"));
        CtClass pojoEnum = pool.makeClass(getClass().getResourceAsStream("PojoEnum.class"));

        BaseClassFilter filter = new BaseClassFilter();
        assertFalse(filter.isAClass(pojoInterface));
        assertFalse(filter.isAClass(pojoEnum));
        assertTrue(filter.isAClass(pojoClass));
    }

    @Test
    public void detectSerializableInterface() throws IOException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass pojoClass = pool.makeClass(getClass().getResourceAsStream("PojoClass.class"));
        CtClass pojoSerializable = pool.makeClass(getClass().getResourceAsStream("PojoSerializable.class"));
        CtClass pojoSerializableSuperclass = pool.makeClass(getClass().getResourceAsStream("PojoSerializableSuperclass.class"));

        BaseClassFilter filter = new BaseClassFilter();
        assertFalse(filter.implementsSerializable(pojoClass));
        assertTrue(filter.implementsSerializable(pojoSerializable));
        assertTrue(filter.implementsSerializable(pojoSerializableSuperclass));
    }

    @Test
    public void detectMagicSerializationMethods() throws IOException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass pojoWithReadObjectMethod = pool.makeClass(getClass().getResourceAsStream("PojoWithReadObjectMethod.class"));
        CtClass pojoWithWriteObjectMethod = pool.makeClass(getClass().getResourceAsStream("PojoWithWriteObjectMethod.class"));
        CtClass pojo = pool.makeClass(getClass().getResourceAsStream("PojoClass.class"));

        BaseClassFilter filter = new BaseClassFilter();
        assertTrue(filter.alreadyHasMagicSerializationMethods(pojoWithReadObjectMethod));
        assertTrue(filter.alreadyHasMagicSerializationMethods(pojoWithWriteObjectMethod));
        assertFalse(filter.alreadyHasMagicSerializationMethods(pojo));
    }


}
