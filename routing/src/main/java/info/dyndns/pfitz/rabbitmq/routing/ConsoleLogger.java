package info.dyndns.pfitz.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import info.dyndns.pfitz.rabbitmq.Constants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class ConsoleLogger implements InitializingBean, DisposableBean, Runnable {
    @Resource
    private Channel channel;
    @Value("${exchange.name}")
    private String exchangeName;

    private Set<Severity> levels = newHashSet(Severity.error);

    private String queueName;

    public void setLevels(Set<Severity> levels) {
        this.levels = levels;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.exchangeDeclare(exchangeName, Constants.EXCHANGE_DIRECT);
        queueName = channel.queueDeclare().getQueue();
        for (final Severity level : levels) {
            channel.queueBind(queueName, exchangeName, level.name());
        }
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    @Override
    public void run() {
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);

        try {
            channel.basicConsume(queueName, true, consumer);

            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
