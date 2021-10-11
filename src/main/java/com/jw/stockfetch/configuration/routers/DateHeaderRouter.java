package com.jw.stockfetch.configuration.routers;

import com.jw.stockfetch.exceptions.InvalidDateParamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

import static com.jw.stockfetch.constants.HeaderConstants.*;

@Component
public class DateHeaderRouter extends AbstractMessageRouter {

    private final MessageChannel singleFetchChannel;

    private final MessageChannel multiFetchChannel;

    @Autowired
    public DateHeaderRouter(MessageChannel singleFetchChannel, MessageChannel multiFetchChannel) {
        this.singleFetchChannel = singleFetchChannel;
        this.multiFetchChannel = multiFetchChannel;
    }

    @Override
    protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();
        if (messageHeaders.containsKey(DATE_TARGET_HEADER)) {
            return Collections.singletonList(singleFetchChannel);
        } else if (messageHeaders.containsKey(DATE_FROM_HEADER) && messageHeaders.containsKey(DATE_TO_HEADER)) {
            return Collections.singletonList(multiFetchChannel);
        } else {
            throw new InvalidDateParamsException("Expecting date or dateFrom and dateTo params");
        }
    }
}
