package io.mockk.proxy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

@SuppressWarnings({"unused", "UnusedAssignment"})
public class MockKProxyAdvice extends MockKProxyDispatcher {
    public MockKProxyAdvice(Map<Object, MockKInvocationHandler> handlers) {
        super(handlers);
    }

    @OnMethodEnter(skipOn = OnNonDefaultValue.class)
    private static Callable<?> enter(@MockKProxyAdviceId long id,
                                     @This Object self,
                                     @Origin final Method method,
                                     @AllArguments final Object[] arguments) throws Throwable {
        MockKDispatcher dispatcher = MockKDispatcher.get(id, self);
        if (dispatcher == null) {
            return null;
        }

        return dispatcher.handle(self, method, arguments);
    }

    @OnMethodExit
    private static void exit(@Advice.Return(readOnly = false, typing = DYNAMIC) Object returned,
                             @This Object self,
                             @Origin final Method method,
                             @Advice.Enter Callable<?> mocked) throws Throwable {
        if (mocked != null) {
            returned = mocked.call();
        }
    }
}
