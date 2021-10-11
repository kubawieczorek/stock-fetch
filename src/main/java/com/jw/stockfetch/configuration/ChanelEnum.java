package com.jw.stockfetch.configuration;

public enum ChanelEnum {
    SINGLE_FETCH_CHANNEL("singleFetchChannel"),
    MULTI_FETCH_CHANNEL("multiFetchChannel");

    private String channelName;

    ChanelEnum(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
