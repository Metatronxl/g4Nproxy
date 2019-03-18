package com.xulei.g4nproxy_server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 11:15 AM
 */

@Slf4j
@ChannelHandler.Sharable
public class NatServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("recieved proxy message, type is:{}", msg.toString());
    }

}
