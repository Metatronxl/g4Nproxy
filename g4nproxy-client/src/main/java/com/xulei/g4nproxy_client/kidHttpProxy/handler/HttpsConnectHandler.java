package com.xulei.g4nproxy_client.kidHttpProxy.handler;

import com.xulei.g4nproxy_client.kidHttpProxy.util.ChannelCacheUtil;
import com.xulei.g4nproxy_client.kidHttpProxy.util.ProxyUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2018/11/7
 * 用于将客户端发送的https请求和目标主机建立连接后,
 * 处理目标主机的输入事件的处理器
 * <p>
 * 每建立一个连接,都需要创建一个该对象
 */
@Slf4j
public class HttpsConnectHandler  extends ChannelInboundHandlerAdapter{

    private static final String LOG_PRE = "[Https连接处理类]通道id:{}";
    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     */
    private final ChannelHandlerContext ctx;

    public HttpsConnectHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 当目标服务器取消注册
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx0) throws Exception {
        String channelId = ProxyUtil.getChannelId(ctx);
        log.info(LOG_PRE + ",在目标服务器取消注册",channelId);

        //关闭与客户端的通道
        ctx.close();
        //清除缓存
        ChannelCacheUtil.remove(channelId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx1, Object msg) throws Exception {
        log.info(LOG_PRE + ",读取到响应.",ProxyUtil.getChannelId(ctx));
        //使用客户端通道的ctx,将消息发回给客户端
        ProxyUtil.writeAndFlush(ctx, msg,true);

    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx0, Throwable cause) throws Exception {
        String channelId = ProxyUtil.getChannelId(ctx);
        log.error(LOG_PRE + ",发生异常:{}",channelId, cause.getMessage(), cause);
        //关闭 与目标服务器的连接
        ctx0.close();
        //关闭 与客户端的连接
        ctx.close();
        //清除缓存
        ChannelCacheUtil.remove(channelId);
    }


}
