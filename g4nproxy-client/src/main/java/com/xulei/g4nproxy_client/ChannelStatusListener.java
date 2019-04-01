package com.xulei.g4nproxy_client;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelStatusListener {

    void channelInactive(ChannelHandlerContext ctx);

}
