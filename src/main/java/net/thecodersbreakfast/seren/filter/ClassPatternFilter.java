package net.thecodersbreakfast.seren.filter;

import javassist.CtClass;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author olivier
 */
public class ClassPatternFilter extends BaseClassFilter {

    private Pattern pattern;

    @Override
    public void configure(Map<String, String> config) {
        String packagePattern = config.get("pattern");
        pattern = Pattern.compile(packagePattern);
    }

    @Override
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
        return super.acceptClass(classLoader, classDefinition) && pattern.matcher(classDefinition.getName()).matches();
    }
}
