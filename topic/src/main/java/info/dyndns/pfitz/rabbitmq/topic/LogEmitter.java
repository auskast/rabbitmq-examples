package info.dyndns.pfitz.rabbitmq.topic;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEmitter implements InitializingBean, DisposableBean {
    private static final String END_STRING = ".";

    @Resource
    private Channel channel;
    @Value("${exchange.name}")
    private String exchangeName;

    @Override
    public void afterPropertiesSet() throws Exception {
        channel.exchangeDeclare(exchangeName, Constants.EXCHANGE_TOPIC);
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }

    public void emitLog(LogInput input) throws IOException {
        if (input.isValid()) {
            channel.basicPublish(exchangeName, input.getRouting(), null, input.buildLogMessage().getBytes());
        }
    }

    private static class LogInput {
        private static final Pattern pattern = Pattern.compile("(\\S*)\\s*(\\S*)\\s*(.*)");

        Facility facility;
        Severity severity;
        String message;

        private LogInput(String input) {
            final Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                this.facility = Facility.getFacility(matcher.group(1));
                this.severity = Severity.getSeverity(matcher.group(2));
                this.message = matcher.group(3);
            }
        }

        boolean isValid() {
            return facility != null && severity != null;
        }

        String getRouting() {
            return new Binding(facility, severity).getBinding();
        }

        String buildLogMessage() {
            return DateTime.now().toString() + " - " + facility.getPrintValue() + " - " + severity.getPrintValue() +
                    " - " + message;
        }
    }

    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("topic.xml");
        context.registerShutdownHook();
        final LogEmitter logEmitter = (LogEmitter) context.getBean("logEmitter");

        System.out.println("Enter log messages, starting with the severity (i, w, e). A single '.' quits.");

        while (true) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            final String message = StringUtils.trim(reader.readLine());
            if (END_STRING.equals(message)) {
                System.exit(0);
            }
            logEmitter.emitLog(new LogInput(message));
        }
    }
}
