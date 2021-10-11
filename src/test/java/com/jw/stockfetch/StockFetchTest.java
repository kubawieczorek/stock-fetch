package com.jw.stockfetch;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StockFetchTest {

    @Autowired
    private MockMvc mockMvc;

    @Rule
    public EmbeddedActiveMQBroker embeddedActiveMQBroker = new EmbeddedActiveMQBroker();

    @Test
    public void fetchSingleStockDataAndPutOnQueue() throws Exception {
        //when
        mockMvc.perform(post("/stock/fetchAndSave").param("date", "2020-08-20"));

        //then
        assertEquals(1, embeddedActiveMQBroker.getMessageCount("amq.outbound"));
    }

    @Test
    public void fetchMultipleStockDataAndPutOnQueue() throws Exception {
        //when
        mockMvc.perform(post("/stock/fetchAndSave")
                .param("dateFrom", "2020-08-18")
                .param("dateTo", "2020-08-20"));

        //then
        assertEquals(3, embeddedActiveMQBroker.getMessageCount("amq.outbound"));
    }
}
