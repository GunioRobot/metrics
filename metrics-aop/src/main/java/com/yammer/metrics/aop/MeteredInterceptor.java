package com.yammer.metrics.aop;

import com.yammer.metrics.aop.annotation.Metered;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * is invoked.
 */
public class MeteredInterceptor implements MethodInterceptor {
    public static MethodInterceptor forMethod(MetricsRegistry metricsRegistry, Class<?> klass, Method method) {
        final Metered annotation = method.getAnnotation(Metered.class);
        if (annotation != null) {
            final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
            final MeterMetric meter = metricsRegistry.newMeter(klass,
                                                               name,
                                                               annotation.eventType(),
                                                               annotation.rateUnit());
            return new MeteredInterceptor(meter);
        }
        return null;
    }

    private final MeterMetric meter;

    private MeteredInterceptor(MeterMetric meter) {
        this.meter = meter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        meter.mark();
        return invocation.proceed();
    }
}
