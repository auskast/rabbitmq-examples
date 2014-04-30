package info.dyndns.pfitz.rabbitmq.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class RPCClient implements InitializingBean, DisposableBean {
    private static final String END_STRING = ".";

    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    private String replyQueueName;
    private QueueingConsumer consumer;

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    public Long calculate(int num) {
        final String corrId = UUID.randomUUID().toString();

        final AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        try {
            channel.basicPublish("", queueName, props, String.valueOf(num).getBytes());

            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                if (corrId.equals(delivery.getProperties().getCorrelationId())) {
                    return Long.parseLong(new String(delivery.getBody()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }

        return null;
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("rpc.xml");
        context.registerShutdownHook();
        final RPCClient client = (RPCClient) context.getBean("client");

        System.out.println("Enter numbers to calculate fibonacci. A single '.' quits.");

        while (true) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            final String input = StringUtils.trim(reader.readLine());
            if (END_STRING.equals(input)) {
                System.exit(0);
            }
            try {
                final int num = Integer.parseInt(input);
                System.out.println(" [x] Requesting fib(" + num + ")");
                final long response = client.calculate(num);
                System.out.println(" [.] Got '" + response + "'");
            } catch (NumberFormatException e) {
                System.out.println("Please enter numbers only");
            }
        }
    }
}
