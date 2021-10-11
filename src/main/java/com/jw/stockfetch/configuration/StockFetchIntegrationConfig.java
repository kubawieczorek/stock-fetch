package com.jw.stockfetch.configuration;

import com.jw.stockfetch.configuration.routers.DateHeaderRouter;
import com.jw.stockfetch.configuration.splitters.MultiToSingleDayRequestSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jms.JmsOutboundGateway;
import org.springframework.messaging.MessageChannel;

import javax.jms.ConnectionFactory;

import static com.jw.stockfetch.constants.HeaderConstants.*;

@Configuration
public class StockFetchIntegrationConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DateHeaderRouter dateHeaderRouter;

    @Autowired
    private MultiToSingleDayRequestSplitter multiToSingleDayRequestSplitter;

    @Bean
    public MessageChannel singleFetchChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel multiFetchChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow httpInboundFlow() {
        return IntegrationFlows
                .from(Http.inboundGateway("/stock/fetchAndSave")
                        .replyTimeout(10000L)
                        .requestTimeout(10000L)
                        .requestMapping(r -> r.methods(HttpMethod.POST))
                        .headerExpression(DATE_TARGET_HEADER, "#requestParams['date'] != null ? #requestParams['date'][0] : null")
                        .headerExpression(DATE_FROM_HEADER, "#requestParams['dateFrom'] != null ? #requestParams['dateFrom'][0] : null")
                        .headerExpression(DATE_TO_HEADER, "#requestParams['dateTo'] != null ? #requestParams['dateTo'][0] : null"))
                .log(LoggingHandler.Level.INFO, "test.info", m -> "Request received")
                .route(dateHeaderRouter)
                .get();
    }

    @Bean
    public IntegrationFlow multiFetchFlow() {
        return IntegrationFlows
                .from(multiFetchChannel())
                .log(LoggingHandler.Level.INFO, "test.info", m -> "Message received on multi fetch Channel")
                .split(multiToSingleDayRequestSplitter)
                .channel(singleFetchChannel())
                .get();
    }

    @Bean
    public IntegrationFlow singleFetchFlow() {
        return IntegrationFlows
                .from(singleFetchChannel())
                .log(LoggingHandler.Level.INFO, "test.info", m -> "Message received on single fetch Channel")
                .handle(Http.outboundGateway(m -> "https://www.gpw.pl/archiwum-notowan?fetch={fetch}&type={type}&date={date}")
                        .uriVariable("date", "headers." + DATE_TARGET_HEADER)
                        .uriVariable("fetch", "'1'")
                        .uriVariable("type", "'10'")
                        .expectedResponseType(byte[].class)
                        .httpMethod(HttpMethod.GET))
                .log(LoggingHandler.Level.INFO, "test.info", m -> "Stock file fetched")
                .handle(jmsOutboundGateway())
                .get();
    }

    @Bean
    public JmsOutboundGateway jmsOutboundGateway() {
        JmsOutboundGateway jmsOutboundGateway = new JmsOutboundGateway();
        jmsOutboundGateway.setConnectionFactory(this.connectionFactory);
        jmsOutboundGateway.setRequestDestinationName("amq.outbound");
        return jmsOutboundGateway;
    }
}
