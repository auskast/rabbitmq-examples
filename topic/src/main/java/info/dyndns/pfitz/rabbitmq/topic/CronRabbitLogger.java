package info.dyndns.pfitz.rabbitmq.topic;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class CronRabbitLogger {
    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("topic.xml");
        context.registerShutdownHook();
        final ConsoleLogger consoleLogger = (ConsoleLogger) context.getBean("cronRabbitLogger");

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
