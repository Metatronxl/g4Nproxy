package com.xulei.g4nproxy.handler;

import android.util.Log;

import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author lei.X
 * @date 2019/3/18 10:12 AM
 */
public class AppClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final String tag = "appClient_tag";



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Log.i(tag,"读取到数据");
        System.out.println(msg.toString());
//        ctx.writeAndFlush("接收到数据");
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(tag,"app端的通道被激活");
        super.channelActive(ctx);
    }
}
