package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ClassFilter} that accepts classes based on their package.
 * <p/>
 * This filter accepts the following configuration parameters :
 * <ul>
 * <li>packages : a comma-separated list of fully qualified package names.</li>
 * </ul>
 * <p/>
 * Example :
 * <pre>
 *     filter.&lt;filterId&gt;.packages=com.company.project.model
 * </pre>
 *
 * @author Olivier Croisier
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
