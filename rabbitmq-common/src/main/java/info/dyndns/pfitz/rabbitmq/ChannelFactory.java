package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface ChannelFactory {
    Channel getChannel(String hostname) throws IOException;
}
