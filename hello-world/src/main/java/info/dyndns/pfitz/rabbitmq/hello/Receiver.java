package info.dyndns.pfitz.rabbitmq.hello;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Receiver implements InitializingBean, DisposableBean, Runnable {
    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    @Override
    public void run() {
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);

        try {
            channel.basicConsume(queueName, true, consumer);

            while (true) {
                final QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                final String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
            }
        } catch (InterruptedException e){
            System.err.println("Interrupted...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("hello-world.xml");
        context.registerShutdownHook();
        final Receiver receiver = (Receiver) context.getBean("receiver");

        final Thread thread = new Thread(receiver);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
        System.exit(0);
    }
}
