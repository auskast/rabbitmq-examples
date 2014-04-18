package info.dyndns.pfitz.rabbitmq.routing;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ErrorLogger {
    public static void main(String[] args) throws IOException {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("routing.xml");
        context.registerShutdownHook();
        final ConsoleLogger consoleLogger = (ConsoleLogger) context.getBean("errorLogger");

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
