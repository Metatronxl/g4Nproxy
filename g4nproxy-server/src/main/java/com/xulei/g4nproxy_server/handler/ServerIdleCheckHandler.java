package com.xulei.g4nproxy_server.handler;


import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by virjar on 2019/2/23.
 */

public class ServerIdleCheckHandler extends IdleStateHandler {


    private static Logger logger = LoggerFactory.getLogger(ServerIdleCheckHandler.class);

    public ServerIdleCheckHandler() {
        super(Constants.READ_IDLE_TIME, Constants.WRITE_IDLE_TIME, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        String clientKey = ctx.channel().attr(ProxyChannelManager.CHANNEL_CLIENT_KEY).get();
        logger.info("server channel check:{}  idle event:{} isFirst:{}", clientKey, evt.state(), evt.isFirst());
        if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT == evt) {
            logger.info("channel write timeout {}", ctx.channel());
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.TYPE_HEARTBEAT);
            ctx.channel().writeAndFlush(proxyMessage);
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            logger.warn("channel read timeout {}  clone port mapping ", ctx.channel());
            ProxyChannelManager.removeCmdChannel(ctx.channel());
        }
        super.channelIdle(ctx, evt);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String clientKey = ctx.channel().attr(ProxyChannelManager.CHANNEL_CLIENT_KEY).get();
        if (cause instanceof IOException) {
            if (clientKey == null) {
                ctx.channel().close();
            } else {
                ProxyChannelManager.removeCmdChannel(ctx.channel());
            }
            return;
        }
        super.exceptionCaught(ctx, cause);
    }
}
