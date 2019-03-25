package com.xulei.g4nproxy_client.handler;

import com.xulei.g4nproxy_client.Constants;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_client.util.ProxyUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import sun.rmi.runtime.Log;

import static com.xulei.g4nproxy_client.util.ProxyUtil.wrapperTransFormData;

/**
 * @author lei.X
 * @date 2019/3/22 11:11 AM
 */

@Slf4j
public class HttpResponseHandler extends ChannelInboundHandlerAdapter {


    private static final String LOG_PRE = "[Http数据返回处理类]通道id:{}";

    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     */
    private final ChannelHandlerContext ctx;

    public HttpResponseHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


    /**
     * 将数据写回服务器端
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {




        ChannelHandlerContext dataChannelCtx = Constants.manageCtxMap.get(Constants.DATA_CHANNEL);


        ProxyMessage dataMsg = wrapperTransFormData(msg);

        dataChannelCtx.pipeline().writeAndFlush(dataMsg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){
                    LogUtil.i(LOG_PRE,"响应数据返回");
                }else{
                    LogUtil.e(LOG_PRE,"响应数据返回失败");
                }
            }
        });


    }


}
