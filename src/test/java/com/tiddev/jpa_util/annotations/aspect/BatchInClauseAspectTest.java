package com.tiddev.jpa_util.annotations.aspect;

import com.tiddev.jpa_util.annotations.BatchInClause;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchInClauseAspectTest {

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private BatchInClauseAspect aspect;

    private Method methodWithParam;
    private BatchInClause batchAnnotation;

    interface TestServiceWithParam {
        @BatchInClause(parameterName = "ids", batchSize = 100)
        List<String> findNamesByIds(@Param("ids") List<Long> ids);
    }

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        methodWithParam = TestServiceWithParam.class.getMethod("findNamesByIds", List.class);
        batchAnnotation = mock(BatchInClause.class);
        when(batchAnnotation.batchSize()).thenReturn(100);
        when(batchAnnotation.parameterName()).thenReturn("ids");
    }

    private void mockMethodSignature(Method method, Object[] args) {
        when(pjp.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(pjp.getArgs()).thenReturn(args);
    }

    @Test
    void interceptBatchInClause_NonCollectionParameter() throws Throwable {
        Object nonCollectionArg = "test";
        mockMethodSignature(methodWithParam, new Object[]{nonCollectionArg});
        when(pjp.proceed(any())).thenReturn("result");

        Object result = aspect.interceptBatchInClause(pjp, batchAnnotation);

        verify(pjp, times(1)).proceed(new Object[]{nonCollectionArg});
        assertEquals("result", result);
    }

    @Test
    void interceptBatchInClause_CollectionWithinBatchSize() throws Throwable {
        List<Long> smallList = LongStream.range(0, 50).boxed().toList();
        mockMethodSignature(methodWithParam, new Object[]{smallList});
        when(pjp.proceed(any())).thenReturn(Collections.singletonList("result"));

        Object result = aspect.interceptBatchInClause(pjp, batchAnnotation);

        verify(pjp, times(1)).proceed(new Object[]{smallList});
        assertEquals(Collections.singletonList("result"), result);
    }

    @Test
    void interceptBatchInClause_CollectionExceedsBatchSize() throws Throwable {
        List<Long> largeList = LongStream.range(0, 200).boxed().toList();
        mockMethodSignature(methodWithParam, new Object[]{largeList});
        when(pjp.proceed(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArgument(0);
            List<?> batchList = (List<?>) args[0];
            return batchList.stream()
                    .map(batch -> "Processed batch of " + ((List<?>) batch).size() + " items")
                    .toList();
        });

        Object result = aspect.interceptBatchInClause(pjp, batchAnnotation);

        verify(pjp, times(1)).proceed(any());
        assertInstanceOf(List.class, result);
        List<?> results = (List<?>) result;
        assertEquals(2, results.size());
        assertEquals("Processed batch of 100 items", results.get(0));
        assertEquals("Processed batch of 100 items", results.get(1));
    }

    @Test
    void interceptBatchInClause_NonCollectionResultDuringBatching() throws Throwable {
        List<Long> largeList = LongStream.range(0, 200).boxed().toList();
        mockMethodSignature(methodWithParam, new Object[]{largeList});
        when(pjp.proceed(any())).thenReturn("singleResult");

        Object result = aspect.interceptBatchInClause(pjp, batchAnnotation);

        verify(pjp, times(1)).proceed(any());
        assertInstanceOf(List.class, result);
        List<?> results = (List<?>) result;
        assertEquals(1, results.size());
        assertEquals("singleResult", results.get(0));
    }
}
