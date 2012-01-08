package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author olivier
 */
public class PackageListFilter extends BaseClassFilter {

    private Set<String> packages = new HashSet<String>();

    @Override
    public void configure(Map<String, String> config) {
        String packageNames = config.get("packages");
        packages.addAll(Arrays.asList(packageNames.split(",\\s+")));
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
        return super.acceptClass(classLoader, classDefinition) && packages.contains(classDefinition.getPackageName());
    }
}
