package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;

public class Worker {
    private final ChannelFactory channelFactory = new ChannelFactoryImpl();

    public void run() throws IOException {
        final Channel channel = channelFactory.getChannel(Configuration.HOSTNAME);
        channel.queueDeclare(Configuration.QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(Configuration.PREFETCH_COUNT);

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(Configuration.QUEUE_NAME, false, consumer);

        try {
            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final String message = new String(delivery.getBody());
                System.out.println(" [x] " + DateTime.now().toString() + " Received '" + message + "'");
                doWork(message);
                System.out.println(" [x] " + DateTime.now().toString() + " Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (InterruptedException e) {
            System.out.println("Exiting...");
        } finally {
            ((ChannelFactoryImpl) channelFactory).destroy();
        }
    }

    private void doWork(String message) throws InterruptedException {
        Thread.sleep(50 * StringUtils.countMatches(message, "."));
    }

    public static void main(String[] args) throws IOException {
        final Worker worker = new Worker();
        worker.run();
    }
}
