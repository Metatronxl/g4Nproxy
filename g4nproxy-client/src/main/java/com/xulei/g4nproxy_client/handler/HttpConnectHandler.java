package com.xulei.g4nproxy_client.handler;

import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_client.util.ProxyUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2018/11/7
 *
 * 用于将客户端发送的http请求和目标主机建立连接后,
 * 处理目标主机的输入事件的处理器
 *
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
     *
     * 注意,从逻辑上来说,进行到这一步,客户端已经发送了它的请求报文,并且我们也收到目标服务器的响应.
     * 那么似乎可以直接使用如下语句,在将消息发回给客户端后,关闭与客户端的连接通道.
     * 	ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
     * 	但据我理解,浏览器会复用一些通道,所以最好不要关闭.
     * 	(ps: 我关闭后,看直播时,无法加载出视频.... 不将它关闭,就一切正常.  并且,我之前测试过,客户端多次连接会使用相同id的channel.
     * 	也就是同一个TCP连接.)
     *
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {

        //目标主机的响应数据
//			FullHttpResponse response = (FullHttpResponse) msg;
//        PooledUnsafeDirectByteBuf(ridx: 0, widx: 725, cap: 1024)
//			LogUtil.i("TTTT", msg.toString());

        //添加解码器
//        ctx.pipeline().addLast(new HttpServerCodec());
//        ctx.pipeline().addLast(new HttpResponseHandler(ctx0));
//        ctx.fireChannelRead(msg);

//        ctx0.pipeline().addLast(new HttpResponseDecoder());
//        ctx0.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
//        ctx0.pipeline().addLast(new HttpResponseHandler(ctx));
//
//        ctx0.fireChannelRead(msg);




        LogUtil.i("test msg kind",msg.toString());

        DefaultHttpResponse test = (DefaultHttpResponse)msg;
        test.getDecoderResult();



//        if (msg instanceof HttpResponse){
//
//        }
//
        // 发回给客户端

//        ChannelHandlerContext dataChannelCtx = Constants.manageCtxMap.get(Constants.DATA_CHANNEL);
//
//
//        ProxyMessage dataMsg = wrapperTransFormData(msg);
//
//        dataChannelCtx.pipeline().writeAndFlush(dataMsg).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()){
//                    LogUtil.i(LOG_PRE,"响应数据返回");
//                }else{
//                    LogUtil.e(LOG_PRE,"响应数据返回失败");
//                }
//            }
//        });


    }




    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx0, Throwable cause) throws Exception {
        log.error(LOG_PRE + ",发生异常:{}", ProxyUtil.getChannelId(ctx),cause.getMessage(),cause);
        //关闭 与目标服务器的连接
        ctx0.close();
        //关闭 与客户端的连接
        ctx.close();
    }





}
