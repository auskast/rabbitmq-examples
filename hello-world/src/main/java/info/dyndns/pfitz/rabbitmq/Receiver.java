package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import info.dyndns.pfitz.rabbitmq.factory.ChannelFactory;
import info.dyndns.pfitz.rabbitmq.factory.ChannelFactoryImpl;

import java.io.IOException;

public class Receiver {
    private final ChannelFactory channelFactory = new ChannelFactoryImpl();

    public void runReceiver() throws IOException, InterruptedException {
        final Channel channel = channelFactory.getChannel(Configuration.HOSTNAME);
        channel.queueDeclare(Configuration.QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(Configuration.QUEUE_NAME, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            final String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final Receiver receiver = new Receiver();
        receiver.runReceiver();
    }
}
