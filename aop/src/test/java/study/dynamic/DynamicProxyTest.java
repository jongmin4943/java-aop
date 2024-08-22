package study.dynamic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.MethodMatcher;
import study.SayUppercaseMethodMatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class DynamicProxyTest {

    @DisplayName("JdkDynamicProxy 로 say 로 시작하는 메서드의 반환 값을 대문자로 변환할 수 있다")
    @Test
    public void dynamicProxyTest() {
        final Hello hello = (Hello) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{Hello.class},
                new UpperCaseInvocationHandler(new HelloTarget())
        );

        final String name = "jongmin";

        assertSoftly(softly -> {
            softly.assertThat(hello.sayHello(name)).isEqualTo("HELLO JONGMIN");
            softly.assertThat(hello.sayHi(name)).isEqualTo("HI JONGMIN");
            softly.assertThat(hello.sayThankYou(name)).isEqualTo("THANK YOU JONGMIN");
            softly.assertThat(hello.pingpong(name)).isEqualTo("Pong jongmin");
        });
    }

    static class UpperCaseInvocationHandler implements InvocationHandler {

        private final Object instance;
        private final MethodMatcher methodMatcher;

        public UpperCaseInvocationHandler(final Object instance) {
            this.instance = instance;
            this.methodMatcher = new SayUppercaseMethodMatcher();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final Object result = method.invoke(instance, args);
            if (methodMatcher.matches(method, method.getDeclaringClass(), args)) {
                return ((String) result).toUpperCase();
            }
            return result;
        }
    }

}
