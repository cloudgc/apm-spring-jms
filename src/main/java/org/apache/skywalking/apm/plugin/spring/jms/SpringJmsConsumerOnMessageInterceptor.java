package org.apache.skywalking.apm.plugin.spring.jms;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author szw
 * @date 2024/7/31 13:58
 * @desc
 */
public class SpringJmsConsumerOnMessageInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
                             MethodInterceptResult methodInterceptResult) throws Throwable {
        if (Objects.isNull(objects) || objects.length != 2) {

            return;
        }
        Message message = (Message) objects[0];
        if (message == null) {
            return;
        }
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();

        while (next.hasNext()) {
            next = next.next();
            Object propertyValue = message.getStringProperty(next.getHeadKey());
            if (propertyValue != null) {
                next.setHeadValue(propertyValue.toString());
            }
        }
        AbstractSpan jms =  // ContextManager.createLocalSpan("JMS/MessageListener");
                ContextManager.createEntrySpan("JMS/MessageListener", contextCarrier);
        if (message.getJMSDestination() instanceof Queue || message.getJMSDestination() instanceof TemporaryQueue) {
            String queueName = ((Queue) message.getJMSDestination()).getQueueName();
            Tags.MQ_QUEUE.set(jms, queueName);

        } else if (message.getJMSDestination() instanceof Topic ||
                   message.getJMSDestination() instanceof TemporaryTopic) {
            String topicName = ((Topic) message.getJMSDestination()).getTopicName();

            Tags.MQ_TOPIC.set(jms, topicName);

        }
        jms.setComponent(ComponentsDefine.SPRING_ANNOTATION);
        SpanLayer.asRPCFramework(jms);

    }

    @Override
    public Object afterMethod(EnhancedInstance enhancedInstance, Method method, Object[] objects, Class<?>[] classes,
                              Object ret) throws Throwable {
        if (ContextManager.activeSpan() != null) {
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance enhancedInstance, Method method, Object[] objects,
                                      Class<?>[] classes, Throwable throwable) {
        if (ContextManager.activeSpan() != null) {
            ContextManager.stopSpan();
        }
    }
}
