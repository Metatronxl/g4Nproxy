package com.xulei.g4nproxy_client.handler;

import com.xulei.g4nproxy_client.Constants;
import com.xulei.g4nproxy_client.ProxyClient;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author lei.X
 * @date 2019/3/31 3:50 PM
 */
public class LittleProxyServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    private static final String tag = "LittleProxyServerChannelHandler";


    private ProxyClient proxyClient;

    public LittleProxyServerChannelHandler(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        Channel littleProxyChannel = ctx.channel();
        Channel natDataChannel = littleProxyChannel.attr(Constants.NEXT_CHANNEL).get();  //查看是否已经将2个channel绑定在一起了
        if (natDataChannel == null) {   //如果还没有绑定，就先退出
            // 代理客户端连接断开
            ctx.channel().close();
        } else {  // 已经绑定完成，进入处理数据逻辑
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
//            String userId = proxyClient.getClientChannelMannager().getRealServerChannelUserId(littleProxyChannel);  // 找到这个手机所分配的userID
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.P_TYPE_TANSFER_RTN);  //定义数据类型
            proxyMessage.setUri("TEST_USER_ID");  // userId
            proxyMessage.setData(bytes); // 具体的内容
            natDataChannel.writeAndFlush(proxyMessage);
            LogUtil.i(tag, "http响应数据写回, littleProxyChannel:" + littleProxyChannel + "  natDataChannel " + natDataChannel);
        }
    }

    /**
     * channel断开连接时发送TYPE_DISCONNECT包
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel littleProxyChannel = ctx.channel();
        String userId = proxyClient.getClientChannelManager().getRealServerChannelUserId(littleProxyChannel);
        proxyClient.getClientChannelManager().removeRealServerChannel(userId);  // 通道关闭后就将这个userId移除
        Channel channel = littleProxyChannel.attr(Constants.NEXT_CHANNEL).get();
        if (channel != null) {
            LogUtil.i(tag, "channelInactive :" + littleProxyChannel);
            ProxyMessage proxyMessage = new ProxyMessage();
            proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
            proxyMessage.setUri(userId);
            channel.writeAndFlush(proxyMessage);  // 发送通知过去，告之这个代理服务器已经失去连接

            //TODO 这里的逻辑处理有些粗糙，可能存在的情况是4g代理与littleProxy之间的连接断开，但是请求服务器与4g代理之间的链接并没有断开，这个时候需要做的是重新连接4g代理与littleProxy

        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.e(tag, "exception caught", cause);
        super.exceptionCaught(ctx, cause);
    }



}
