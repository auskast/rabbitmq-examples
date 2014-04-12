package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class ChannelFactory extends AbstractFactoryBean<Channel> {
    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    private final Set<Connection> connections = newHashSet();

    private String hostname = "localhost";

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Class<?> getObjectType() {
        return Channel.class;
    }

    @Override
    protected Channel createInstance() throws Exception {
        connectionFactory.setHost(hostname);
        final Connection connection = connectionFactory.newConnection();
        connections.add(connection);
        return connection.createChannel();
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        for (final Connection connection : connections) {
            connection.close();
        }
    }
}
