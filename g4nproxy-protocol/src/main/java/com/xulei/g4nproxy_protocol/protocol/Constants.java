package com.xulei.g4nproxy_protocol.protocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public interface Constants {

    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");

    AttributeKey<String> USER_ID = AttributeKey.newInstance("user_id");

    AttributeKey<String> CLIENT_KEY = AttributeKey.newInstance("client_key");

    String g4ProxyServerHost_1 = "114.116.98.169";
    String g4ProxyServerHost_2 = "39.106.55.139";
    int g4ProxyServerPort = 30000;


    int READ_IDLE_TIME = 60;

    int WRITE_IDLE_TIME = 40;

    String tag = "weijia";

}
