package com.xulei.g4nproxy_client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * Created by virjar on 2019/2/23.
 */

public interface Constants {
    public static final String httpProxyServiceAction = "om.virjar.g4proxy.service";


    String DATA_CHANNEL = "dataTransformChannel";


    String PROXY_MESSAGE_ENCODE = "ProxyMessageEncode";
    String PROXY_MESSAGE_DECODE  = "proxyMessageDecoed";

    String APP_CLIENT_HANDLER = "appClientHandler";

    // 本地与littleProxy代理的channel
    String LOCAL_SERVER_CHANNEL = "localServerCtx";

    // clientId 和 ChannelHandlerContext 的绑定
    Map<String, ChannelHandlerContext> manageCtxMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
    // clientId 和 Channel 的绑定
    Map<String, Channel> manageChannelMap = new ConcurrentHashMap<String, Channel>();


    // littleProxy开启的端口为3128
    int littleProxyPort = 3128;

    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");

}
