package info.dyndns.pfitz.rabbitmq.hello;

import com.rabbitmq.client.Channel;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;

public class Sender implements InitializingBean, DisposableBean {
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

    public void run() throws IOException {
        final String message = "Hello World! " + DateTime.now().toString();
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("hello-world.xml");
        context.registerShutdownHook();
        final Sender sender = (Sender) context.getBean("sender");
        sender.run();
        System.exit(0);
    }
}
