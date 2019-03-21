package com.xulei.g4nproxy_client.listener;

import com.xulei.g4nproxy_client.util.ProxyUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;

/**
 * @author lei.X
 * @date 2018/11/7
 * 用于http请求的 与目标主机建立连接后的监听器类
 */
@Slf4j
public class HttpChannelFutureListener implements ChannelFutureListener {
    private static final String LOG_PRE = "[http连接建立监听器]通道id:{}";

    /**
     * 客户端要发送给目标主机的消息
     */
    private Object msg;
    /**
     * 通道上下文,如果与目标主机建立连接失败,返回失败响应给客户端,并关闭连接
     */
    private ChannelHandlerContext ctx;

    public HttpChannelFutureListener(Object msg, ChannelHandlerContext ctx) {
        this.msg = msg;
        this.ctx = ctx;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        String channelId = ProxyUtil.getChannelId(ctx);

        if (future.isSuccess()){
            log.info(LOG_PRE + ",与目标主机建立连接成功.",channelId);
            //将客户端请求报文发送给服务端
            future.channel().writeAndFlush(msg);
            return;
        }
        log.info(LOG_PRE + ",与目标主机建立连接失败.",channelId);
        //连接失败操作,暂且返回408,请求超时
        ProxyUtil.responseFailedToClient(ctx);

        Throwable cause = future.cause();
        if(cause instanceof ConnectTimeoutException)
            log.error(LOG_PRE + ",连接超时:{}", ProxyUtil.getChannelId(ctx), cause.getMessage());
        else if (cause instanceof UnknownHostException)
            log.error(LOG_PRE + ",未知主机:{}", ProxyUtil.getChannelId(ctx), cause.getMessage());
        else
            log.error(LOG_PRE + ",异常:{}", ProxyUtil.getChannelId(ctx),cause.getMessage(),cause);
        log.info(LOG_PRE + ",给客户端响应失败信息成功.",channelId);
        //并关闭 与客户端的连接
        ctx.close();

    }
}
