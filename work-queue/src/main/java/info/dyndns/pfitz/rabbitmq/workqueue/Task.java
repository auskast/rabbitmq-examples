package info.dyndns.pfitz.rabbitmq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Task implements InitializingBean, DisposableBean {
    private static int messageCount = 1;

    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.queueDeclare(queueName, true, false, false, null);
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    private void run() throws IOException {
        for (int i = 0; i < 1000; ++i) {
            final String message = getMessage();
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("work-queue.xml");
        context.registerShutdownHook();
        final Task task = (Task) context.getBean("task");
        task.run();
        System.exit(0);
    }

    private static String getMessage() {
        return StringUtils.rightPad(messageCount++ + ". Hello", (int) (Math.random() * 20) + 5, ".");
    }
}
