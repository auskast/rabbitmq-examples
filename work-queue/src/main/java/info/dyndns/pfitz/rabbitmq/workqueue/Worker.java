package info.dyndns.pfitz.rabbitmq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Worker implements InitializingBean, DisposableBean {
    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    private Integer prefetchCount = 1;

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.queueDeclare(queueName, true, false, false, null);
        channel.basicQos(prefetchCount);
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    public void run() throws IOException {
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);

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
            System.out.println("Interrupted...");
        }
    }

    private void doWork(String message) throws InterruptedException {
        Thread.sleep(50 * StringUtils.countMatches(message, "."));
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("work-queue.xml");
        context.registerShutdownHook();
        final Worker worker = (Worker) context.getBean("worker");
        worker.run();
        System.exit(0);
    }
}
