package info.dyndns.pfitz.rabbitmq.pubsub;

import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogEmitter implements InitializingBean, DisposableBean {
    private static final String END_STRING = ".";

    @Resource
    private Channel channel;
    @Value("${exchange.name}")
    private String exchangeName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.exchangeDeclare(exchangeName, "fanout");
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    public void emitLog(String message) throws IOException {
        channel.basicPublish(exchangeName, "", null, buildLogMessage(message).getBytes());
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("pub-sub.xml");
        context.registerShutdownHook();
        final LogEmitter logEmitter = (LogEmitter) context.getBean("logEmitter");

        System.out.println("Enter log messages. A single '.' quits.");

        while (true) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            final String message = StringUtils.trim(reader.readLine());
            if (END_STRING.equals(message)) {
                System.exit(0);
            }
            logEmitter.emitLog(message);
        }
    }

    private static String buildLogMessage(String message) {
        return DateTime.now().toString() + " - " + message;
    }
}
