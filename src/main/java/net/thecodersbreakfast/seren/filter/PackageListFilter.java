package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author olivier
 */
public class PackageListFilter implements ClassFilter {

    private Set<String> packages = new HashSet<String>();

    @Override
    public void configure(Map<String, String> config) {
        String packageNames = config.get("packages");
        packages.addAll(Arrays.asList(packageNames.split(",\\s+")));
        System.out.println(getClass().getName() + ".packages = " + packages);
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) {
        String packageName = classDefinition.getPackageName();
        return packages.contains(packageName);
    }
}
