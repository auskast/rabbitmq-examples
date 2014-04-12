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

public class Task implements InitializingBean, DisposableBean, Runnable {
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

    @Override
    public void run() {
        for (int i = 0; i < 1000; ++i) {
            final String message = getMessage();
            try {
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getMessage() {
        return StringUtils.rightPad(messageCount++ + ". Hello", (int) (Math.random() * 20) + 5, ".");
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("work-queue.xml");
        context.registerShutdownHook();
        final Task task = (Task) context.getBean("task");

        final Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
        System.exit(0);
    }
}
