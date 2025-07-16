package com.tiddev.jpa_util.annotations.aspect;

import com.tiddev.jpa_util.annotations.BatchInClause;
import com.tiddev.jpa_util.util.Lists;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Aspect that intercepts any method annotated with {@link BatchInClause} and,
 * when the specified collection parameter exceeds the configured batch size,
 * splits it into sub-lists (batches), invokes the target method repeatedly
 * for each batch, and aggregates the results.
 *
 * <p>This is useful for breaking up large "IN" clause collections (or
 * other bulk operations) into smaller chunks to avoid exceeding database
 * or service limits.</p>
 */
@Aspect
@Component
public class BatchInClauseAspect {

    /**
     * Wraps a method invocation annotated with {@link BatchInClause}, checks
     * if the target parameter is a {@link Collection} larger than the
     * configured batch size, and if so:
     * <ol>
     *     <li>Partitions the collection into batches.</li>
     *     <li>Revokes the method with each batch.</li>
     *     <li>Aggregates and returns all results as a single list.</li>
     * </ol>
     *
     * @param pjp           the join point representing the intercepted method
     * @param batchInClause annotation that provides {@code batchSize} and
     *                      {@code parameterName} to target
     * @return the combined result of invoking the method on every batch, or
     * the original invocation result if no batching was needed
     * @throws Throwable if the target method throws any exception
     */
    @Around("@annotation(batchInClause)")
    public Object interceptBatchInClause(
            ProceedingJoinPoint pjp,
            BatchInClause batchInClause
    ) throws Throwable {
        Object[] originalArgs = pjp.getArgs();
        int batchSize = batchInClause.batchSize();
        String paramName = batchInClause.parameterName();

        int paramIndex = findParameterIndex(pjp, paramName);
        Object argValue = originalArgs[paramIndex];

        if (argValue instanceof Collection<?> collection && collection.size() > batchSize) {

            List<List<?>> partitions =
                    Collections.singletonList(Lists.partition(new ArrayList<>(collection), batchSize));

            List<Object> aggregated = new ArrayList<>();

            for (List<?> batch : partitions) {

                Object[] modifiedArgs = Arrays.copyOf(originalArgs, originalArgs.length);
                modifiedArgs[paramIndex] = batch;
                Object result = pjp.proceed(modifiedArgs);

                if (result instanceof Collection<?> subCollection) {

                    aggregated.addAll(subCollection);
                } else {

                    aggregated.add(result);
                }
            }

            return List.copyOf(aggregated);
        }

        return pjp.proceed(originalArgs);
    }

    /**
     * Locates the index of a method parameter by:
     * <ul>
     *     <li>Matching {@link Param @Param(value=...)} annotation value</li>
     *     <li>Falling back to the parameter's actual name</li>
     * </ul>
     *
     * @param pjp       the join point whose signature contains the target method
     * @param paramName the name (or @Param value) of the parameter to find
     * @return zero-based index of the matching method parameter
     * @throws IllegalArgumentException if no matching parameter is found
     */
    protected int findParameterIndex(ProceedingJoinPoint pjp, String paramName) {

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {

            Param annotation = parameters[i].getAnnotation(Param.class);

            if ((annotation != null && annotation.value().equals(paramName)) ||
                parameters[i].getName().equals(paramName)) {

                return i;
            }
        }

        throw new IllegalArgumentException(
                String.format("Parameter '%s' not found in method %s()", paramName, method.getName())
        );
    }
}
