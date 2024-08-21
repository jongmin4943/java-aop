package study.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CglibTest {

    @DisplayName("CglibProxy 로 say 로 시작하는 메서드의 반환 값을 대문자로 변환할 수 있다")
    @Test
    public void cglibProxyTest() {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(HelloTarget.class);
        enhancer.setCallback(new UpperCaseMethodInterceptor());
        final HelloTarget proxy = (HelloTarget) enhancer.create();

        final String name = "jongmin";

        assertSoftly(softly -> {
            softly.assertThat(proxy.sayHello(name)).isEqualTo("HELLO JONGMIN");
            softly.assertThat(proxy.sayHi(name)).isEqualTo("HI JONGMIN");
            softly.assertThat(proxy.sayThankYou(name)).isEqualTo("THANK YOU JONGMIN");
        });
    }

    static class UpperCaseMethodInterceptor implements MethodInterceptor {


        @Override
        public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy) throws Throwable {
            final Object result = methodProxy.invokeSuper(o, objects);
            if(result instanceof final String s && method.getName().startsWith("say")) {
                return s.toUpperCase();
            }
            return result;
        }
    }

}