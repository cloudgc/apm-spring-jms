package org.apache.skywalking.apm.plugin.spring.jms.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

import static org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;

/**
 * @author szw
 * @date 2024/8/1 15:00
 * @desc
 */
public class ActiveMQConnectionFactoryInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    public static final String ENHANCE_CLASS =
            "org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryFactory";

    public static final String CONSTRUCTOR_INTERCEPTOR_CLASS =
            "org.apache.skywalking.apm.plugin.spring.jms.ActiveMQConnectionFactoryInterceptor";

    public static final String CONSTRUCTOR_INTERCEPT_TYPE =
            "org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties";

    @Override
    protected ClassMatch enhanceClass() {
        return NameMatch.byName(ENHANCE_CLASS);
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[]{
                new ConstructorInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgumentWithType(0, CONSTRUCTOR_INTERCEPT_TYPE);
                    }

                    @Override
                    public String getConstructorInterceptor() {
                        return CONSTRUCTOR_INTERCEPTOR_CLASS;
                    }
                }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[0];
    }
}
