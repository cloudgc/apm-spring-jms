package org.apache.skywalking.apm.plugin.spring.jms.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;

/**
 * @author szw
 * @date 2024/7/31 13:51
 * @desc
 */
public class SpringJmsConsumerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    public static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.spring.jms.SpringJmsConsumerOnMessageInterceptor";
    public static final String ENHANCE_CLASS_CONSUMER = "org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener";
    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(ENHANCE_CLASS_CONSUMER);
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("onMessage")
                                .and(takesArgumentWithType(0, "javax.jms.Message"))
                                .and(takesArgumentWithType(1, "javax.jms.Session"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
