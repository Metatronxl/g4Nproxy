package com.xulei.g4nproxy_server.handler;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;
import com.xulei.g4nproxy_server.server.ProxyServer;
import com.xulei.g4nproxy_server.util.LogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import sun.rmi.runtime.Log;

/**
 *
 * 数据传输channel
 * @author lei.X
 * @date 2019/3/18 11:15 AM
 */

@Slf4j
@ChannelHandler.Sharable
public class NatServerChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static final String tag = "NatServerHandler";



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{

//        //将系统端口与channel进行连接
//        LogUtil.i(tag,"将内网穿透端口添加进cmdChannels");
//        ProxyChannelManager.setCmdChannels(Constants.g4nproxyServerPort,ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) throws Exception {
        log.info("recieved proxy message, type is:{}"+ msg.toString());
        switch (msg.getType()){
            case ProxyMessage.C_TYPE_AUTH:
                handleAuthMessage(ctx,msg);
                break;
            case ProxyMessage.TYPE_CONNECT:
                handleConnectMessage(ctx,msg);


        }
    }

    /**
     * 认证消息，检测clientKey是否正确
     *
     * @param ctx
     * @param proxyMessage
     */
    private void handleAuthMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        String clientKey = proxyMessage.getUri();
        log.info("client connect :{}", clientKey);
        // 将对应的手机和管道 组合起来
        ProxyChannelManager.addCmdChannel(clientKey, ctx.channel());


        byte[] bytes = "建立连接成功".getBytes();
        //构建数据包
        ProxyMessage responseMsg = new ProxyMessage();
        responseMsg.setType(ProxyMessage.P_TYPE_TRANSFER);
        responseMsg.setData(bytes);
        ctx.writeAndFlush(responseMsg);
    }

    /**
     * 处理连接请求消息
     * TODO 废弃方法
     * @param ctx
     * @param proxyMessage
     */
    private void handleConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage){

        LogUtil.i(tag,"handle connectMessage");
        LogUtil.i(tag,"MSG: "+ proxyMessage.toString());
    }

}
