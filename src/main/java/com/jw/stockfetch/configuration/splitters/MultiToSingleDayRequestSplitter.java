package com.jw.stockfetch.configuration.splitters;

import com.jw.stockfetch.exceptions.InvalidDateParamsException;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static com.jw.stockfetch.constants.HeaderConstants.DATE_FROM_HEADER;
import static com.jw.stockfetch.constants.HeaderConstants.DATE_TO_HEADER;

@Component
public class MultiToSingleDayRequestSplitter extends AbstractMessageSplitter {

    @Override
    protected Object splitMessage(Message<?> message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFrom;
        Date dateTo;
        try {
            dateFrom = simpleDateFormat.parse(message.getHeaders().get(DATE_FROM_HEADER).toString());
            dateTo = simpleDateFormat.parse(message.getHeaders().get(DATE_TO_HEADER).toString());
        } catch (ParseException e) {
            throw new InvalidDateParamsException("Could not parse dateFrom or dateTo");
        }

        return new Iterator<MessageBuilder<?>>() {

            private Date iteratorDateTo = dateTo;
            private Date iteratorNextDate = dateFrom;
            private Message<?> originalMessage = message;

            @Override
            public boolean hasNext() {
                return !iteratorNextDate.after(iteratorDateTo);
            }

            @Override
            public MessageBuilder<?> next() {
                MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(originalMessage)
                        .setHeader("dateTarget", simpleDateFormat.format(iteratorNextDate));
                iteratorNextDate = DateUtils.addDays(iteratorNextDate, 1);
                return messageBuilder;
            }
        };
    }

}
