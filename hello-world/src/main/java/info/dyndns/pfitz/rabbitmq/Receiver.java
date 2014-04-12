package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

public class Receiver {
    private final ChannelFactory channelFactory = new ChannelFactoryImpl();

    public void run() throws IOException {
        final Channel channel = channelFactory.getChannel(Configuration.HOSTNAME);
        channel.queueDeclare(Configuration.QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(Configuration.QUEUE_NAME, true, consumer);

        try {
            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
            }
        } catch (InterruptedException e){
            System.out.println("Exiting...");
        } finally {
            ((ChannelFactoryImpl) channelFactory).destroy();
        }
    }

    public static void main(String[] args) throws IOException {
        final Receiver receiver = new Receiver();
        receiver.run();
    }
}
