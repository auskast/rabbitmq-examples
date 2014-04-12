package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Task {
    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    private void run() throws IOException {
        channel.queueDeclare(queueName, true, false, false, null);

        for (int i = 0; i < 1000; ++i) {
            final String message = getMessage();
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

        channel.close();
    }

    public static void main(String[] args) throws IOException {
        final ApplicationContext context = new ClassPathXmlApplicationContext("work-queue.xml");
        final Task task = (Task) context.getBean("task");
        task.run();
        System.exit(0);
    }

    private static String getMessage() {
        return StringUtils.rightPad("Hello", (int) (Math.random() * 20) + 5, ".");
    }
}
