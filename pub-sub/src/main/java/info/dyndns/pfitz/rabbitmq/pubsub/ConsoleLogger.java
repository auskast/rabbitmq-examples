package info.dyndns.pfitz.rabbitmq.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class ConsoleLogger implements InitializingBean, DisposableBean {
    @Resource
    private Channel channel;
    @Value("${exchange.name}")
    private String exchangeName;

    private String queueName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.exchangeDeclare(exchangeName, "fanout");
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "");
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    public void run() throws IOException {
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        try {
            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("pub-sub.xml");
        context.registerShutdownHook();
        final ConsoleLogger consoleLogger = (ConsoleLogger) context.getBean("consoleLogger");
        consoleLogger.run();
        System.exit(0);
    }
}
