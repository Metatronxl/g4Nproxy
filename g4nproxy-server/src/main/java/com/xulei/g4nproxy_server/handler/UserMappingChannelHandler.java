package com.xulei.g4nproxy_server.handler;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_server.server.ProxyChannelManager;
import com.xulei.g4nproxy_server.util.LogUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
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
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        LogUtil.i(tag,"receive data from user end point");

        // 获取代理服务器（4g）所在的channel
        Channel userMappingChannel =  ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userMappingChannel.localAddress();
        Channel natDataChannel = ProxyChannelManager.getCmdChannel(sa.getPort());

        //通道未建立时拒绝连接
        if(natDataChannel == null){
            LogUtil.w(tag,"data mapping channel is empty, reject connection");
            ctx.channel().close();
        }else{
            // channel互相绑定
            natDataChannel.attr(Constants.NEXT_CHANNEL).set(userMappingChannel);
            userMappingChannel.attr(Constants.NEXT_CHANNEL).set(natDataChannel);


            LogUtil.i(tag,"forward data to nat channel");
            LogUtil.w(tag,"处理发送数据的逻辑"+msg.toString());

            //发送数据
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.P_TYPE_TRANSFER);
            String userId = ProxyChannelManager.getUserChannelUserId(userMappingChannel);
            proxyMessage.setUri(userId);
            byte[] bytes = new byte[msg.readableBytes()];
            msg.readBytes(bytes);
            proxyMessage.setData(bytes);
            natDataChannel.writeAndFlush(proxyMessage);

//            switch (msg.getType()){
//                case ProxyMessage.P_TYPE_TRANSFER:
//                    handleMessgeTransfer(ctx,msg);
//                    break;
//                case  ProxyMessage.P_TYPE_TANSFER_RTN:
//                    handleMessageRtn(ctx,msg);
//                    break;
//
//                 default:
//                        handleDefault(ctx,msg);
//                        break;
//            }



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

        Channel natDataChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
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
        Channel userMappingChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        // 通知代理客户端
        Channel userMappingChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userMappingChannel.localAddress();
        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
        if (cmdChannel == null) {
            ctx.channel().close();
        }else {
            String userId = ProxyChannelManager.getUserChannelUserId(userMappingChannel);
            ProxyChannelManager.removeUserChannelFromCmdChannel(userMappingChannel,userId);
            Channel natDataChannel = userMappingChannel.attr(Constants.NEXT_CHANNEL).get();
            // 清除DataChannel
            if (natDataChannel != null && natDataChannel.isActive()) {
                natDataChannel.attr(Constants.NEXT_CHANNEL).set(null);
                natDataChannel.attr(Constants.CLIENT_KEY).set(null);
                natDataChannel.attr(Constants.USER_ID).set(null);

                natDataChannel.config().setOption(ChannelOption.AUTO_READ, true);
                // 通知客户端，用户连接已经断开
                ProxyMessage proxyMessage = new ProxyMessage();
                proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                proxyMessage.setUri(userId);
                natDataChannel.writeAndFlush(proxyMessage);
            }

        }
        super.channelInactive(ctx);

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {

        // 通知代理客户端
        Channel userChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) userChannel.localAddress();
        Channel cmdChannel = ProxyChannelManager.getCmdChannel(sa.getPort());
        if (cmdChannel == null) {

            // 该端口还没有代理客户端
            ctx.channel().close();
        } else {
            Channel proxyChannel = userChannel.attr(Constants.NEXT_CHANNEL).get();
            if (proxyChannel != null) {
                proxyChannel.config().setOption(ChannelOption.AUTO_READ, userChannel.isWritable());
            }
        }

        super.channelWritabilityChanged(ctx);
    }


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

