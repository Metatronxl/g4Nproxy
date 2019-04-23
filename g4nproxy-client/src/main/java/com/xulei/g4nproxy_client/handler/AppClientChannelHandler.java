package com.xulei.g4nproxy_client.handler;


import com.xulei.g4nproxy_client.ChannelStatusListener;
import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_client.ProxyClient;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 10:12 AM
 */

@Slf4j
public class AppClientChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static final String tag = "appClient_tag";

    private ProxyClient proxyClient;

    private ChannelStatusListener channelStatusListener;

    private static long sleepTimeMill = 1000;

    public AppClientChannelHandler(ChannelStatusListener channelStatusListener, ProxyClient proxyClient){
        this.proxyClient = proxyClient;
        this.channelStatusListener = channelStatusListener;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) throws Exception {

        LogUtil.i(tag,"receive message: "+msg.toString());
        switch (msg.getType()){
            case ProxyMessage.C_TYPE_AUTH:
                handleAuthMessage(ctx,msg);
                break;
            case ProxyMessage.TYPE_CONNECT:
                handleConnectMessages(ctx,msg);
                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx, msg);
                break;
            case ProxyMessage.TYPE_DISCONNECT:
                handleDisconnectMessage(ctx,msg);
                break;
        }




    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(tag,"app端的通道被激活");
        super.channelActive(ctx);
    }


    private void handleConnectMessages(ChannelHandlerContext ctx, ProxyMessage message){


        proxyClient.join2LittleProxyBootStrap.connect("127.0.0.1",Constants.littleProxyPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){
                    LogUtil.i(tag,"连接littleProxy服务器成功,端口："+Constants.littleProxyPort);
                    //关联cmdChannel 与 littleProxyChannel
                    future.channel().attr(Constants.NEXT_CHANNEL).set(ctx.channel());
                    //注册littleProxyChannel
                    proxyClient.getHttpProxyConnectionManager().register(message.getSerialNumber(), future.channel());
                    // 发送连接littleProxy成功的通知
                    ProxyMessage proxyMessage = new ProxyMessage();
                    proxyMessage.setType(ProxyMessage.TYPE_CONNECT_READY);
                    proxyMessage.setSerialNumber(message.getSerialNumber());
                    proxyMessage.setUri(message.getUri());
                    ctx.channel().writeAndFlush(proxyMessage);

                }else{
                    LogUtil.e(tag,"连接littleProxy服务器失败,端口："+Constants.littleProxyPort);
                    log.warn("connect to LITTEL proxy failed", future.cause());
                    ProxyMessage natMessage = new ProxyMessage();
                    natMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                    natMessage.setSerialNumber(message.getSerialNumber());
                    ctx.channel().writeAndFlush(natMessage);
                }

            }
        });
    }

    /**
     * 接受服务器的确定连接响应
     * @param ctx
     * @param message
     */
    private void handleAuthMessage(ChannelHandlerContext ctx,ProxyMessage message){


        LogUtil.i(tag,"请求服务器的认证请求： "+new String(message.getData()));
    }


    /**
     * 处理传输数据的请求
     * @param ctx
     * @param msg
     */
    private void handleTransferMessage(ChannelHandlerContext ctx,ProxyMessage msg){

        long serialNum = msg.getSerialNumber();
        Channel littleProxyServerChannel = proxyClient.getHttpProxyConnectionManager().query(serialNum);
        if (littleProxyServerChannel == null){
            LogUtil.w(tag,"littleProxy Connection lost");
            return;
        }
        ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
        buf.writeBytes(msg.getData());
        LogUtil.i(tag,"将请求数据写入littleProxy server,"+littleProxyServerChannel);
        littleProxyServerChannel.writeAndFlush(buf);

    }

    /**
     * 断开连接处理逻辑
     * @param ctx
     * @param msg
     */
    private void handleDisconnectMessage(ChannelHandlerContext ctx,ProxyMessage msg){

        long serialNum = msg.getSerialNumber();
        Channel littleProxyServerChannel = proxyClient.getHttpProxyConnectionManager().query(serialNum);
        LogUtil.i(tag, "handleDisconnectMessage, :" + littleProxyServerChannel);
        if (littleProxyServerChannel != null) {
            // 清空channel中的剩余消息
            littleProxyServerChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
            proxyClient.getHttpProxyConnectionManager().releaseConnection(serialNum);
        }
    }

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
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                LogUtil.w(tag, "已经 10 秒没有收到信息！");
                //向服务端发送消息

            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

//        // 控制连接
//        if (proxyClient.getClientChannelManager().getCmdChannel() == ctx.channel()) {
//            proxyClient.getClientChannelManager().setCmdChannel(null);
//            proxyClient.getClientChannelManager().clearRealServerChannels();
//            channelStatusListener.channelInactive(ctx);
//        } else {
//            // 数据传输连接
//            Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
//            if (realServerChannel != null && realServerChannel.isActive()) {
//                realServerChannel.close();
//            }
//        }
//
//        proxyClient.getClientChannelManager().removeProxyChannel(ctx.channel());

        LogUtil.i(tag,"channel 断开连接");
        super.channelInactive(ctx);

    }




    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        LogUtil.e(tag,"appClientHandler error",cause);
        // 当出现异常就关闭连接
        ctx.close();
    }





}
