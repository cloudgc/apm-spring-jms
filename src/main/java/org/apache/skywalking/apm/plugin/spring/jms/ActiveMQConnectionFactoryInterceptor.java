package org.apache.skywalking.apm.plugin.spring.jms;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;

/**
 * @author szw
 * @date 2024/8/1 15:02
 * @desc
 */
public class ActiveMQConnectionFactoryInterceptor implements InstanceConstructorInterceptor {

    @Override
    public void onConstruct(EnhancedInstance enhancedInstance, Object[] objects) throws Throwable {
        ActiveMQProperties properties = (ActiveMQProperties) objects[0];
        if (properties == null) {
            return;
        }
        UrlUtil.setUrl(properties.getBrokerUrl());
    }
}
