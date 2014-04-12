package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class Task {
    private final ChannelFactory channelFactory = new ChannelFactoryImpl();

    private void run() throws IOException {
        final Channel channel = channelFactory.getChannel(Configuration.HOSTNAME);

        channel.queueDeclare(Configuration.QUEUE_NAME, true, false, false, null);

        for (int i = 0; i < 10000; ++i) {
            final String message = getMessage();
            channel.basicPublish("", Configuration.QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

        channel.close();
        ((ChannelFactoryImpl) channelFactory).destroy();
    }

    public static void main(String[] args) throws IOException {
        final Task task = new Task();
        task.run();
    }

    private static String getMessage() {
        return StringUtils.rightPad("Hello", (int) (Math.random() * 20) + 5, ".");
    }
}
