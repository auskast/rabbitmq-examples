package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import info.dyndns.pfitz.rabbitmq.factory.ChannelFactory;
import info.dyndns.pfitz.rabbitmq.factory.ChannelFactoryImpl;
import org.joda.time.DateTime;

import java.io.IOException;

public class Sender {
    private final ChannelFactory channelFactory = new ChannelFactoryImpl();

    public void runSender() throws IOException {
        final Channel channel = channelFactory.getChannel(Configuration.HOSTNAME);

        channel.queueDeclare(Configuration.QUEUE_NAME, false, false, false, null);
        final String message = "Hello World! " + DateTime.now().toString();
        channel.basicPublish("", Configuration.QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        ((ChannelFactoryImpl) channelFactory).destroy();
    }

    public static void main(String[] args) throws IOException {
        final Sender sender = new Sender();
        sender.runSender();
    }
}
