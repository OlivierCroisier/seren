package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author olivier
 */
public class PackagePatternFilter implements ClassFilter {

    private Pattern pattern;

    @Override
    public void configure(Map<String, String> config) {
        String packagePattern = config.get("pattern");
        pattern = Pattern.compile(packagePattern);
        System.out.println(getClass().getName() + ".pattern = " + pattern);
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) {
        String packageName = classDefinition.getPackageName();
        return pattern.matcher(packageName).matches();
    }
}
