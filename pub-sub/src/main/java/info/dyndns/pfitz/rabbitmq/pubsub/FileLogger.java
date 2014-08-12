package info.dyndns.pfitz.rabbitmq.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import info.dyndns.pfitz.rabbitmq.Constants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileLogger implements InitializingBean, DisposableBean, Runnable {
    @Resource
    private Channel channel;
    @Value("${exchange.name}")
    private String exchangeName;
    @Value("${logfile.path}")
    private String filePath;

    private String queueName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.exchangeDeclare(exchangeName, Constants.EXCHANGE_FANOUT);
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "");
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
                writeMessage(message);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMessage(String message) throws IOException {
        final File file = new File(filePath);

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Error creating file " + file.getAbsolutePath());
            }
        }

        try (final FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(message.getBytes());
            fos.write("\n".getBytes());
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("pub-sub.xml");
        context.registerShutdownHook();
        final FileLogger consoleLogger = (FileLogger) context.getBean("fileLogger");

        final Thread thread = new Thread(consoleLogger);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted...");
        }
        System.exit(0);
    }
}
