package info.dyndns.pfitz.rabbitmq.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class RPCServer implements InitializingBean, DisposableBean, Runnable {
    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    private Integer prefetchCount = 1;

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicQos(prefetchCount);
    }

    @Override
    public void run() {
        System.out.println(" [x] Awaiting RPC requests");

        final QueueingConsumer consumer = new QueueingConsumer(channel);

        try {
            channel.basicConsume(queueName, false, consumer);

            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final AMQP.BasicProperties requestProps = delivery.getProperties();
                final AMQP.BasicProperties responseProps = new AMQP.BasicProperties.Builder()
                        .correlationId(requestProps.getCorrelationId())
                        .build();

                final String message = new String(delivery.getBody());
                final int n = Integer.parseInt(message);

                final String response = "" + fib(n);
                System.out.println(" [.] fib(" + message + ") = " + response);

                channel.basicPublish("", requestProps.getReplyTo(), responseProps, response.getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
    }

    private static long fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        long x1 = 0;
        long x2 = 1;
        for (int i = 1; i < n; ++i) {
            x2 = x2 + x1;
            x1 = x2 - x1;
        }
        return x2;
    }

    public static void main(String[] args) {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("rpc.xml");
        context.registerShutdownHook();
        final RPCServer server = (RPCServer) context.getBean("server");

        final Thread thread = new Thread(server);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
        System.exit(0);
    }
}
