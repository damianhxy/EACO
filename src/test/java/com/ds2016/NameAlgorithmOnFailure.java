package com.ds2016;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

/**
 * Prefix assertion failures in per-algorithm tests with the algorithm's
 * name, so the Maven and CI failure summary says e.g. "[EACO] ..." rather
 * than only the parameter index.
 */
class NameAlgorithmOnFailure implements InvocationInterceptor {

    @Override
    public void interceptTestTemplateMethod(final Invocation<Void> invocation,
                                            final ReflectiveInvocationContext<Method> invocationContext,
                                            final ExtensionContext extensionContext) throws Throwable {
        try {
            invocation.proceed();
        } catch (AssertionError e) {
            final AssertionError named = new AssertionError(
                    "[" + extensionContext.getDisplayName() + "] " + e.getMessage(), e);
            named.setStackTrace(e.getStackTrace());
            throw named;
        }
    }
}
