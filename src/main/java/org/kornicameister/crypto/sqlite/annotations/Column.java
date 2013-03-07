package org.kornicameister.crypto.sqlite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kornicameister
 * @since 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
    enum Types {
        BLOB,
        INTEGER,
        STRING
    }

    String name();

    Types type() default Types.STRING;
}
