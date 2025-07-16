package com.tiddev.jpa_util.annotations;

import java.lang.annotation.*;

/**
 * Annotation indicating that a method parameter should be processed in batches
 * when used in an IN clause of a SQL query. This helps avoid hitting database
 * limitations on the number of parameters in an IN clause.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchInClause {

    /**
     * The size of each batch when processing the IN clause parameters.
     * Defaults to 1000 if not specified.
     *
     * @return the batch size for IN clause parameter processing
     */
    int batchSize() default 1000;

    /**
     * The name of the method parameter that contains the values for the IN clause.
     * This should match the parameter name in the method signature.
     *
     * @return the name of the parameter to be processed in batches
     */
    String parameterName();
}
