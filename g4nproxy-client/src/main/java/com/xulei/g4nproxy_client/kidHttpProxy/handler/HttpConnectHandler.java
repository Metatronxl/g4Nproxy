package com.xulei.g4nproxy_client.kidHttpProxy.handler;

import com.xulei.g4nproxy_client.kidHttpProxy.util.ProxyUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2018/11/7
 * <p>
 * 用于将客户端发送的http请求和目标主机建立连接后,
 * 处理目标主机的输入事件的处理器
 * <p>
 * 每建立一个连接,都需要创建一个该对象
 */
@Slf4j
public class HttpConnectHandler extends ChannelInboundHandlerAdapter {

    private static final String LOG_PRE = "[Http连接处理类]通道id:{}";

    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     */
    private final ChannelHandlerContext ctx;

    public HttpConnectHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 当目标服务器取消注册
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx0) throws Exception {
        log.info(LOG_PRE + ",在目标服务器取消注册", ProxyUtil.getChannelId(ctx));

        //关闭与客户端的通道
//		ctx.close();
    }


    /**
     * 读取到消息
     * <p>
     * 注意,从逻辑上来说,进行到这一步,客户端已经发送了它的请求报文,并且我们也收到目标服务器的响应.
     * 那么似乎可以直接使用如下语句,在将消息发回给客户端后,关闭与客户端的连接通道.
     * ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
     * 但据我理解,浏览器会复用一些通道,所以最好不要关闭.
     * (ps: 我关闭后,看直播时,无法加载出视频.... 不将它关闭,就一切正常.  并且,我之前测试过,客户端多次连接会使用相同id的channel.
     * 也就是同一个TCP连接.)
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
        //目标主机的响应数据
//			FullHttpResponse response = (FullHttpResponse) msg;
        log.info(LOG_PRE + ",读取到响应.", ProxyUtil.getChannelId(ctx));
        //发回给客户端
        ProxyUtil.writeAndFlush(ctx, msg, true);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx0, Throwable cause) throws Exception {
        log.error(LOG_PRE + ",发生异常:{}", ProxyUtil.getChannelId(ctx), cause.getMessage(), cause);
        //关闭 与目标服务器的连接
        ctx0.close();
        //关闭 与客户端的连接
        ctx.close();
    }


}
