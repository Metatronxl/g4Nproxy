package com.xulei.g4nproxy_protocol;

import com.xulei.g4nproxy_protocol.ALOG;
import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * check idle chanel.
 *
 * @author fengfei
 */
public class ClientIdleCheckHandler extends IdleStateHandler {

    public static final int USER_CHANNEL_READ_IDLE_TIME = 1200;


    public ClientIdleCheckHandler() {
        super(Constants.READ_IDLE_TIME, Constants.WRITE_IDLE_TIME - 10, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ALOG.i(Constants.tag, "client chanel check:" + ctx + " idle event" + evt);
        if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT == evt) {
            ALOG.i(Constants.tag, "channel write timeout :" + ctx.channel());
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setSerialNumber(1111);
            proxyMessage.setType(ProxyMessage.TYPE_HEARTBEAT);
            ctx.channel().writeAndFlush(proxyMessage);
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            ALOG.w(Constants.tag, "channel read timeout :" + ctx.channel());
            ctx.channel().close();
        }
        super.channelIdle(ctx, evt);
    }
}
