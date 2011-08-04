package org.openspaces.ece.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

public class ConsoleClient {
    @Parameter(names = {"-type"}, description = "The type of the client to run (masterworker/executor)")
    public String type = "masterworker";
    @Parameter(names = "-iterations", description = "the number of iterations to run")
    public Integer maxIterations = null;
    @Parameter(names = "-trades", description = "the number of trades to run")
    public Integer maxTrades = null;
    @Parameter(names = {"-l", "--locator"})
    String locator = "127.0.0.1";
    @Parameter(names = {"-g", "--group"})
    String group = "Gigaspaces-XAPPremium-8.0.3-rc";

    ApplicationContext ctx;

    public static void main(String... args) {
        ConsoleClient client = new ConsoleClient();
        new JCommander(client, args);
        client.setContext("/applicationContext.xml");
        client.run();
    }

    private void run() {
        System.out.println("Running " + type);
        ECEClient eceClient = ctx.getBean(type, ECEClient.class);
        if (eceClient.isValid()) {
            if (maxIterations != null) {
                eceClient.setMaxIterations(maxIterations);
            }
            if (maxTrades != null) {
                eceClient.setMaxTrades(maxTrades);
            }
            eceClient.issueTrades();
        }
    }

    ConsoleClient() {
    }

    void setContext(String contextFileName) {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("group", group);
        properties.setProperty("locator", locator);
        configurer.setProperties(properties);
        ClassPathXmlApplicationContext classPathContext = new ClassPathXmlApplicationContext();
        classPathContext.addBeanFactoryPostProcessor(configurer);
        classPathContext.setConfigLocation(contextFileName);
        classPathContext.refresh();
        ctx=classPathContext;
    }
}
