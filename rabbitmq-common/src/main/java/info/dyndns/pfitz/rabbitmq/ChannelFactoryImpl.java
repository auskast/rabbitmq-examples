package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class ChannelFactoryImpl implements ChannelFactory {
    private ConnectionFactory connectionFactory = new ConnectionFactory();
    private Set<Connection> connections = newHashSet();

    @Override
    public Channel getChannel(String hostname) throws IOException {
        connectionFactory.setHost(hostname);
        final Connection connection = connectionFactory.newConnection();
        connections.add(connection);
        return connection.createChannel();
    }

    public void destroy() throws IOException {
        for (final Connection connection : connections) {
            connection.close();
        }
    }
}
