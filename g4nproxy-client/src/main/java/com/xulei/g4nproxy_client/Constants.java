package com.xulei.g4nproxy_client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by virjar on 2019/2/23.
 */

public interface Constants {
    public static final String httpProxyServiceAction = "om.virjar.g4proxy.service";


    String DATA_CHANNEL = "dataTransformChannel";


    String PROXY_MESSAGE_ENCODE = "ProxyMessageEncode";
    String PROXY_MESSAGE_DECODE  = "proxyMessageDecoed";

    String APP_CLIENT_HANDLER = "appClientHandler";

    // clientId 和 channel 的绑定
    Map<String, ChannelHandlerContext> manageCtxMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
}
