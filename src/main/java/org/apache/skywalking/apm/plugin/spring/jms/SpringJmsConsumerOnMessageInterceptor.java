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

        String url = UrlUtil.getUrl();
        AbstractSpan activeSpan = null;

        if (message.getJMSDestination() instanceof Queue || message.getJMSDestination() instanceof TemporaryQueue) {
            String queueName = ((Queue) message.getJMSDestination()).getQueueName();

            activeSpan =
                    ContextManager.createEntrySpan("ActiveMQ/Queue/" + queueName + "/Consumer", null).start(
                            System.currentTimeMillis());
            Tags.MQ_BROKER.set(activeSpan, url);
            Tags.MQ_QUEUE.set(activeSpan, queueName);

        } else if (message.getJMSDestination() instanceof Topic ||
                   message.getJMSDestination() instanceof TemporaryTopic) {
            String topicName = ((Topic) message.getJMSDestination()).getTopicName();
            activeSpan =
                    ContextManager.createEntrySpan("ActiveMQ/Topic/" + topicName + "/Consumer", null).start(
                            System.currentTimeMillis());

            Tags.MQ_BROKER.set(activeSpan, url);
            Tags.MQ_TOPIC.set(activeSpan, topicName);

        }
        if (activeSpan == null) {
            return;
        }

        ContextCarrier contextCarrier = new ContextCarrier();

        activeSpan.setPeer(url);
        activeSpan.setComponent(ComponentsDefine.ACTIVEMQ_CONSUMER);
        SpanLayer.asMQ(activeSpan);
        CarrierItem next = contextCarrier.items();

        while (next.hasNext()) {
            next = next.next();
            Object propertyValue = message.getStringProperty(next.getHeadKey());
            if (propertyValue != null) {
                next.setHeadValue(propertyValue.toString());
            }
        }

        ContextManager.extract(contextCarrier);
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
