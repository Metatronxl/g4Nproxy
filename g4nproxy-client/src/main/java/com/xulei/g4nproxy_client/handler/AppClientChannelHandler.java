package com.xulei.g4nproxy_client.handler;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 10:12 AM
 */

@Slf4j
public class AppClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final String tag = "appClient_tag";



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info(tag,"读取到数据");
        System.out.println(msg.toString());
//        ctx.writeAndFlush("接收到数据");
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(tag,"app端的通道被激活");
        super.channelActive(ctx);
    }
}
