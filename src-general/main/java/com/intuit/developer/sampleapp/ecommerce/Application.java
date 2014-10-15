package com.intuit.developer.sampleapp.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intuit.developer.sampleapp.ecommerce.oauth.controllers.OAuthInfoProviderImpl;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.developer.sampleapp.ecommerce.domain.SystemProperty;
import com.intuit.developer.sampleapp.ecommerce.oauth.OAuthInfoProvider;
import com.intuit.developer.sampleapp.ecommerce.qbo.PaymentGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOServiceFactory;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.serializers.MoneyDeserializer;
import com.intuit.developer.sampleapp.ecommerce.serializers.MoneySerializer;
import org.joda.money.Money;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import javax.sql.DataSource;


/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/18/14
 * Time: 9:33 AM
 */


@Configuration
@EnableJpaRepositories
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
@ComponentScan
public class Application extends RepositoryRestMvcConfiguration {

    public static void main(String[] args) {

        final ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
	    final DataSource dataSource = (DataSource)context.getBean("dataSource");
        DataLoader.initializeData(context);

        System.out.println(
            "______ _____  ___ ________   __\n" +
            "| ___ \\  ___|/ _ \\|  _  \\ \\ / /\n" +
            "| |_/ / |__ / /_\\ \\ | | |\\ V /\n" +
            "|    /|  __||  _  | | | | \\ /\n" +
            "| |\\ \\| |___| | | | |/ /  | |\n" +
            "\\_| \\_\\____/\\_| |_/___/   \\_/\n");
    }

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setReturnBodyOnCreate(true);
        config.exposeIdsFor(Company.class, Customer.class, ShoppingCart.class, SystemProperty.class);
    }

    @Override
    protected void configureJacksonObjectMapper(ObjectMapper objectMapper) {
        final SimpleModule myCustomModule = new SimpleModule("MyCustomModule");

        myCustomModule.addSerializer(Money.class, new MoneySerializer());
        myCustomModule.addDeserializer(Money.class, new MoneyDeserializer());

        objectMapper.registerModule(myCustomModule);
    }

    @Bean
    OAuthInfoProvider oAuthInfoProvider() {
        return new OAuthInfoProviderImpl();
    }

    @Bean
    QBOGateway qboDataManager() {
        return new QBOGateway();
    }

    @Bean
    QBOServiceFactory qboServiceFactory() {
        return new QBOServiceFactory();
    }

    @Bean
    PaymentGateway paymentGateway() {return new PaymentGateway();}
}
