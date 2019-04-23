package com.xulei.g4nproxy_server.handler;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;
import com.xulei.g4nproxy_server.server.ProxyServer;
import com.xulei.g4nproxy_server.util.LogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

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

   //
//    ProxyChannelManager.setNatServerChannel(Constants.NATSERVER_CHANNEL,ctx.channel());

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
            case ProxyMessage.TYPE_DISCONNECT:
                handleDisconnectMessage(ctx,msg);
                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx,msg);
                break;
            case ProxyMessage.TYPE_CONNECT_READY:
                handleTypeConnectReady(ctx,msg);
                break;


        }
    }

    private void handleTypeConnectReady(ChannelHandlerContext ctx, ProxyMessage proxyMessage){
        Channel natChannel = ctx.channel();

        long seq = proxyMessage.getSerialNumber();

        Channel userMappingChannel = ProxyChannelManager.getUsermappingChannel(seq);
        if (userMappingChannel == null) {
            log.warn("can not find userMapping channel for request :{} client:{} when connection ready", seq);
            ProxyMessage natMessage = new ProxyMessage();
            natMessage.setType(ProxyMessage.TYPE_DISCONNECT);
            natMessage.setSerialNumber(seq);
            natChannel.writeAndFlush(natMessage);
            return;
        }
        //开始接收代理业务的数据请求
        userMappingChannel.config().setOption(ChannelOption.AUTO_READ, true);
    }

    /**
     * 处理4g代理服务器返回的数据
     * @param ctx
     * @param proxyMessage
     */
    private void handleMessageRtn(ChannelHandlerContext ctx,ProxyMessage proxyMessage){
        //获取到对应的userChannel
        long seq = proxyMessage.getSerialNumber();
        Channel userMappingChannel = ProxyChannelManager.getUsermappingChannel(seq);
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
        heartbeatMessage.setSerialNumber(3333);
        log.info("response heartbeat message {}", ctx.channel());
    }


    /**
     * 认证消息，检测clientKey是否正确
     * && 连接建立处理
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


    }

    /**
     * 代理后端服务器断开连接消息
     * @param ctx
     * @param proxyMessage
     */
    private void handleDisconnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage){

        String clientKey = ctx.channel().attr(Constants.CLIENT_KEY).get();

        // 代理连接没有连上服务器由控制连接发送用户端断开连接消息
        if (clientKey == null) {
            String userId = proxyMessage.getUri();
            Channel userChannel = ProxyChannelManager.removeUserChannelFromCmdChannel(ctx.channel(), userId);
            if (userChannel != null) {
                // 数据发送完成后再关闭连接，解决http1.0数据传输问题
                userChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }

        Channel cmdChannel = ProxyChannelManager.getCmdChannel(clientKey);
        if (cmdChannel == null) {
            log.warn("ConnectMessage:error cmd channel key {}", ctx.channel().attr(Constants.CLIENT_KEY).get());
            return;
        }

        Channel userChannel = ProxyChannelManager.removeUserChannelFromCmdChannel(cmdChannel, ctx.channel().attr(Constants.USER_ID).get());
        if (userChannel != null) {
            // 数据发送完成后再关闭连接，解决http1.0数据传输问题
            userChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            ctx.channel().attr(Constants.SERVER_NEXT_CHANNEL).set(null);
            ctx.channel().attr(Constants.CLIENT_KEY).set(null);
            ctx.channel().attr(Constants.USER_ID).set(null);

        }

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
        Channel userMappingChannel  = ctx.channel().attr(Constants.SERVER_NEXT_CHANNEL).get();
        if (userMappingChannel != null){
            ByteBuf buf = ctx.alloc().buffer(message.getData().length);
            buf.writeBytes(message.getData());
            userMappingChannel.writeAndFlush(buf);

        }


    }

//    @Override
//
//    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//
//        // 获取到inactive对应的port
//        int port = ctx.channel().attr(Constants.SERVER_USER_PORT).get();
//        ProxyServer.getInstance().closeMappingPort(port);
//        LogUtil.i(tag,"端口："+ String.valueOf(port)+"对应的channel已经关闭");
//
//    }

    /**
     * 事件触发
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {

            LogUtil.i(tag,"超时检测");

            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                LogUtil.w(tag, "已经 10 秒没有收到信息！");
                //向服务端发送消息
            }else if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                LogUtil.w(tag, "已经 10 秒没有发出信息！");
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * channel inactice时关闭对应的usermapping 端口
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{

            // 获取到inactive对应的port
            int port = ctx.channel().attr(Constants.SERVER_USER_PORT).get();
            ProxyServer.getInstance().closeMappingPort(port);
            LogUtil.i(tag,"端口："+ String.valueOf(port)+"对应的channel已经关闭");
        super.channelInactive(ctx);
    }



}
