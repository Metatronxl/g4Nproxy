package com.xulei.g4nproxy_protocol.protocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public interface Constants {

    AttributeKey<Channel> SERVER_NEXT_CHANNEL = AttributeKey.newInstance("server_nxt_channel");


    AttributeKey<Integer> SERVER_USER_PORT = AttributeKey.newInstance("server_user_port");


    AttributeKey<String> USER_ID = AttributeKey.newInstance("user_id_protocol");

    AttributeKey<String> CLIENT_KEY = AttributeKey.newInstance("client_key");

    // 消息的序列号
    AttributeKey<Long> SERIAL_NUM = AttributeKey.newInstance("user_mapping_seq");

//    String g4ProxyServerHost_1 = "114.116.98.169";
//    String g4ProxyServerHost_2 = "39.106.55.139";

    // 请求服务器IP地址
//    98.142.143.217
    String g4nproxyServerHost = "127.0.0.1";
    // 请求服务器port端口
    int g4nproxyServerPort = 30000;


    int READ_IDLE_TIME = 60;

    int WRITE_IDLE_TIME = 40;

    String tag = "weijia";

    String HOST_SEPARATOR = ":";

    String NAME_HTTP_AGGREGATOR_HANDLER = "httpAggregator";
    String NAME_PROXY_SERVER_HANDLER = "proxyServerHandler";
    String NAME_HTTPSERVER_CODEC = "httpserver_codec";


    String PROXY_MESSAGE_ENCODE = "ProxyMessageEncode";
    String PROXY_MESSAGE_DECODE = "proxyMessageDecoed";


// ProxyMessage 参数相关

    int MAX_FRAME_LENGTH = 2 * 1024 * 1024;

    int LENGTH_FIELD_OFFSET = 0;

    int LENGTH_FIELD_LENGTH = 4;

    int INITIAL_BYTES_TO_STRIP = 0;

    int LENGTH_ADJUSTMENT = 0;

    //natserverChannel
    String NATSERVER_CHANNEL = "natserver_Channel";

    // littleProxy开启的端口为3128
    int littleProxyPort = 3128;

    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");

    String APP_CLIENT_HANDLER = "appClientHandler";

}
