package net.thecodersbreakfast.seren.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a candidate for enhancement.
 * <p/>
 * This annotation is meant to be used with the {@link AnnotationFilter} filter.
 *
 * @author Olivier Croisier
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SerenEnhanced {
}
