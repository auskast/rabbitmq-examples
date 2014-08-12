package info.dyndns.pfitz.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import info.dyndns.pfitz.rabbitmq.Constants;
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
        channel.exchangeDeclare(exchangeName, Constants.EXCHANGE_DIRECT);
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    public void emitLog(String message, Severity severity) throws IOException {
        if (severity != null) {
            channel.basicPublish(exchangeName, severity.name(), null, buildLogMessage(message, severity).getBytes());
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("routing.xml");
        context.registerShutdownHook();
        final LogEmitter logEmitter = (LogEmitter) context.getBean("logEmitter");

        System.out.println("Enter log messages, starting with the severity (i, w, e). A single '.' quits.");

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                final String message = StringUtils.trim(reader.readLine());
                if (END_STRING.equals(message)) {
                    System.exit(0);
                }
                logEmitter.emitLog(StringUtils.substring(message, 2), Severity.getSeverity(StringUtils.left(message, 1)));
            }
        }
    }

    private static String buildLogMessage(String message, Severity severity) {
        return DateTime.now().toString() + " - " + severity.getPrintValue() + " - " + message;
    }
}
