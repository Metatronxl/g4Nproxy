package com.xulei.g4nproxy_server.handler;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;
import com.xulei.g4nproxy_server.util.LogUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * 手机与服务器之间的channelHandler
 * @author lei.X
 * @date 2019/3/20 10:12 AM
 */

@Slf4j
@ChannelHandler.Sharable
public class UserMappingChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    private static AtomicLong userIdProducer = new AtomicLong(0);

    private static final String tag = "userMappingCtx";

    /**
     * 在userMapping port检测到连接后，建立dataChannel和userChannel之间的绑定
     *
     *
     * 只有在读取到数据后（也就是在channelRead0中）才能互相绑定channel，不然绑定的可能是错误的channel
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        LogUtil.i(tag,"receive data from user end point");

//        LogUtil.i("TEST",ByteArrayUtil.msgToString(msg));

        // 获取代理服务器（4g）所在的channel
        Channel userMappingChannel =  ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userMappingChannel.localAddress();
//        Channel natDataChannel = ProxyChannelManager.getCmdChannel(sa.getPort());

        Channel natDataChannel = ProxyChannelManager.getNatServerChannel(Constants.NATSERVER_CHANNEL);

        //通道未建立时拒绝连接
        if(natDataChannel == null){
            LogUtil.w(tag,"data mapping channel is empty, reject connection");
            ctx.channel().close();
        }else{
            // channel互相绑定
            natDataChannel.attr(Constants.SERVER_NEXT_CHANNEL).set(userMappingChannel);
            userMappingChannel.attr(Constants.SERVER_NEXT_CHANNEL).set(natDataChannel);


            byte[] bytes = new byte[msg.readableBytes()];
            msg.readBytes(bytes);
            String userId = ProxyChannelManager.getUserChannelUserId(userMappingChannel);
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.P_TYPE_TRANSFER);
            proxyMessage.setUri(userId);
            proxyMessage.setData(bytes);
            natDataChannel.writeAndFlush(proxyMessage);
            LogUtil.i(tag,"将http请求以proxyMessage的类型发送到4g代理服务器");





        }

    }


    /**
     * 处理发送数据的逻辑
     * @param ctx
     * @param msg
     */
    private  void handleMessgeTransfer(ChannelHandlerContext ctx, ProxyMessage msg){
        LogUtil.i(tag,"forward data to nat channel");
        LogUtil.w(tag,"处理发送数据的逻辑"+msg.toString());

        //获取数据channel

        Channel natDataChannel = ctx.channel().attr(Constants.SERVER_NEXT_CHANNEL).get();
        natDataChannel.writeAndFlush(msg);
//        ctx.channel().writeAndFlush(msg);

    }

    /***
     * 处理接受数据的逻辑
     * @param ctx
     * @param msg
     */
    private void handleMessageRtn(ChannelHandlerContext ctx, ProxyMessage msg){
        LogUtil.w(tag,"处理接受数据的逻辑"+msg.toString());
        Channel userMappingChannel = ctx.channel().attr(Constants.SERVER_NEXT_CHANNEL).get();
        userMappingChannel.writeAndFlush(msg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{

        Channel userChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress)userChannel.localAddress();
        //根据端口可以找到对应的channel
        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
        LogUtil.i(tag,"a new connect from user endpoint,local port:{}"+sa.getPort());
        if (cmdChannel == null){
            LogUtil.w(tag,"nat channel not ready! reject connection");
            ctx.channel().close();
        }else {
            String userId = newUserId();
            log.info("alloc new user id for conenction:{} with uid:{} with local port:{}", userChannel, userId, sa.getPort());
            // 用户连接到代理服务器时，设置用户连接不可读，等待代理后端服务器连接成功后再改变为可读状态
//            userChannel.config().setOption(ChannelOption.AUTO_READ, false);
            ProxyChannelManager.addUserChannelToCmdChannel(cmdChannel, userId, userChannel);
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.TYPE_CONNECT);
            proxyMessage.setUri(userId);

            LogUtil.i(tag,"connect to nat client ,request a data channel");
            cmdChannel.writeAndFlush(proxyMessage);
        }

        super.channelActive(ctx);
    }


    /**
     * 用户与代理服务器的请求断开
     * @param ctx
     * @throws Exception
     */
//   TODO 连接断开的处理
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        // 通知代理客户端
//        Channel userMappingChannel = ctx.channel();
//        InetSocketAddress sa = (InetSocketAddress) userMappingChannel.localAddress();
//        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
//        if (cmdChannel == null) {
//            ctx.channel().close();
//        }else {
//            String userId = ProxyChannelManager.getUserChannelUserId(userMappingChannel);
//            ProxyChannelManager.removeUserChannelFromCmdChannel(userMappingChannel,userId);
//            Channel natDataChannel = userMappingChannel.attr(Constants.SERVER_NEXT_CHANNEL).get();
//            // 清除DataChannel
//            if (natDataChannel != null && natDataChannel.isActive()) {
//                natDataChannel.attr(Constants.SERVER_NEXT_CHANNEL).set(null);
//                natDataChannel.attr(Constants.CLIENT_KEY).set(null);
//                natDataChannel.attr(Constants.USER_ID).set(null);
//
//                natDataChannel.config().setOption(ChannelOption.AUTO_READ, true);
//                // 通知客户端，用户连接已经断开

//                ProxyMessage proxyMessage = new ProxyMessage();
//                proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
//                proxyMessage.setUri(userId);
//                natDataChannel.writeAndFlush(proxyMessage);
//            }
//
//        }
//
        Channel userMappingChannel = ctx.channel();
        InetSocketAddress sa  = (InetSocketAddress) userMappingChannel.localAddress();
        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
        LogUtil.w(tag,"userMappingChannel lose connection, port :"+String.valueOf(sa.getPort()));
        // 4g代理服务器与server之间的管道，一般不会断开
        if (cmdChannel == null){
            ctx.channel().close();
        }else{
            String userId = ProxyChannelManager.getUserChannelUserId(userMappingChannel);
            ProxyChannelManager.removeUserChannelFromCmdChannel(userMappingChannel,userId);
            // 检测通道是否仍然存活
            if (cmdChannel.isActive()){
                // 发送请求断开数据包
                ProxyMessage proxyMessage = new ProxyMessage();
                proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                proxyMessage.setUri(userId);
                cmdChannel.writeAndFlush(proxyMessage);
            }

        }

        super.channelInactive(ctx);

    }

//    @Override
//    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//
//        // 通知代理客户端
//        Channel userChannel = ctx.channel();
//        InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();
//        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
//        if (cmdChannel == null) {
//
//            // 该端口还没有代理客户端
//            ctx.channel().close();
//        } else {
//            Channel proxyChannel = userChannel.attr(Constants.SERVER_NEXT_CHANNEL).get();
//            if (proxyChannel != null) {
//                proxyChannel.config().setOption(ChannelOption.AUTO_READ, userChannel.isWritable());
//            }
//        }
//
//        super.channelWritabilityChanged(ctx);
//    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        LogUtil.e(tag,"userMapingChannelHandler error",cause);
        // 当出现异常就关闭连接
        ctx.close();
    }

    /**
     * 为用户连接产生ID
     *
     * @return
     */
    private static String newUserId() {
        return String.valueOf(userIdProducer.incrementAndGet());
    }

}

