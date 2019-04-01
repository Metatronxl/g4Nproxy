package com.xulei.g4nproxy_client.handler;

import com.xulei.g4nproxy_client.Constants;
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
}
