package com.xulei.g4nproxy_server.handler;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;
import com.xulei.g4nproxy_server.server.ProxyServer;
import com.xulei.g4nproxy_server.util.ByteArrayUtil;
import com.xulei.g4nproxy_server.util.CtxUtil;
import com.xulei.g4nproxy_server.util.LogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
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
            case ProxyMessage.TYPE_HEARTBEAT:
                handleHeartbeatMessage(ctx, msg);
                break;
            case ProxyMessage.C_TYPE_AUTH:
                handleAuthMessage(ctx,msg);
                break;
            case ProxyMessage.P_TYPE_TANSFER_RTN:
                handleMessageRtn(ctx,msg);
                break;
//            case ProxyMessage.TYPE_CONNECT:
//                handleConnectMessage(ctx,msg);
//                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx,msg);
                break;


        }
    }

    /**
     * 处理4g代理服务器返回的数据
     * @param ctx
     * @param proxyMessage
     */
    private void handleMessageRtn(ChannelHandlerContext ctx,ProxyMessage proxyMessage){
        //获取到对应的userChannel
        Channel userMappingChannel  = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        String userId =  userMappingChannel.id().asShortText();

//        LogUtil.i("TTTTTTTTT", new String(proxyMessage.getData()));

        if (userMappingChannel!=null){
            LogUtil.i(tag,"处理4g代理服务器返回的数据");
            ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
            buf.writeBytes(proxyMessage.getData());
            userMappingChannel.writeAndFlush(buf);
        }else{
            LogUtil.e(tag,"userMappingChannel 已经关闭！");
        }


    }

    /**
     * 心跳处理
     *
     * @param ctx
     * @param proxyMessage
     */
    private void handleHeartbeatMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        ProxyMessage heartbeatMessage = new ProxyMessage();
        heartbeatMessage.setSerialNumber(heartbeatMessage.getSerialNumber());
        heartbeatMessage.setType(ProxyMessage.TYPE_HEARTBEAT);
        log.info("response heartbeat message {}", ctx.channel());
        ctx.channel().writeAndFlush(heartbeatMessage);
    }


    /**
     * 认证消息，检测clientKey是否正确
     *
     * @param ctx
     * @param proxyMessage
     */
    private void handleAuthMessage(final ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        String clientKey = proxyMessage.getUri();
        log.info("client connect :{}", clientKey);
        // 将对应的手机和管道 组合起来
        ProxyChannelManager.addCmdChannel(clientKey, ctx.channel());


        byte[] bytes = "建立连接成功".getBytes();
        //构建数据包
        ProxyMessage responseMsg = new ProxyMessage();
        responseMsg.setType(ProxyMessage.C_TYPE_AUTH);
        responseMsg.setData(bytes);

        //去掉ProxyMessage编解码器
//        ctx.pipeline().remove(Constants.PROXY_MESSAGE_DECODE);
//        ctx.pipeline().remove(Constants.PROXY_MESSAGE_ENCODE);

        //发送认证消息给4g代理服务器
        ctx.writeAndFlush(responseMsg);
//                .addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                //发送完数据后再添加上
//                CtxUtil.AddProxyMessageHandler(future.channel());
//            }
//        });

    }

    /**
     * 代理数据传输
     *
     * @param ctx
     * @param message
     */
    private void handleTransferMessage(ChannelHandlerContext ctx,ProxyMessage message){

        LogUtil.i(tag,"发送请求数据前往4g服务器");
        //获取到对应的userChannel
        Channel userMappingChannel  = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if (userMappingChannel != null){
            ByteBuf buf = ctx.alloc().buffer(message.getData().length);
            buf.writeBytes(message.getData());
            userMappingChannel.writeAndFlush(buf);

        }


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
