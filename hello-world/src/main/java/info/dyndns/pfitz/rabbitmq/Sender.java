package info.dyndns.pfitz.rabbitmq;

import com.rabbitmq.client.Channel;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Sender {
    @Resource
    private Channel channel;
    @Value("${queue.name}")
    private String queueName;

    public void run() throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
        final String message = "Hello World! " + DateTime.now().toString();
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
    }

    public static void main(String[] args) throws IOException {
        final ApplicationContext context = new ClassPathXmlApplicationContext("hello-world.xml");
        final Sender sender = (Sender) context.getBean("sender");
        sender.run();
        System.exit(0);
    }
}
