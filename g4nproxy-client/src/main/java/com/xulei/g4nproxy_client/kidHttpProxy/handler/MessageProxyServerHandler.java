package com.xulei.g4nproxy_client.kidHttpProxy.handler;

import com.xulei.g4nproxy_client.kidHttpProxy.factory.BootstrapFactory;
import com.xulei.g4nproxy_client.kidHttpProxy.initializer.HttpConnectChannelInitializer;
import com.xulei.g4nproxy_client.kidHttpProxy.initializer.HttpsConnectChannelInitializer;
import com.xulei.g4nproxy_client.kidHttpProxy.listener.HttpChannelFutureListener;
import com.xulei.g4nproxy_client.kidHttpProxy.listener.HttpsChannelFutureListener;
import com.xulei.g4nproxy_client.kidHttpProxy.util.ChannelCache;
import com.xulei.g4nproxy_client.kidHttpProxy.util.ChannelCacheUtil;
import com.xulei.g4nproxy_client.kidHttpProxy.util.ProxyUtil;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import static com.xulei.g4nproxy_client.kidHttpProxy.KidHttpProxyConstants.NAME_HTTPSERVER_CODEC;

/**
 * @author lei.X
 * @date 2018/11/7
 */
@ChannelHandler.Sharable
public class MessageProxyServerHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static final String LOG_PRE = "【代理服务器handler】通道id:{}";



    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(LOG_PRE ,",客户端关闭连接."+ ProxyUtil.getChannelId(ctx));

        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(LOG_PRE ,",通道未激活."+ ProxyUtil.getChannelId(ctx));
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(LOG_PRE ,",通道激活."+ ProxyUtil.getChannelId(ctx));    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(LOG_PRE ,",读取完成."+ ProxyUtil.getChannelId(ctx));    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.i(LOG_PRE ,",发生异常."+ ProxyUtil.getChannelId(ctx));        //关闭
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) throws Exception  {
        String channelId = ProxyUtil.getChannelId(ctx);

        LogUtil.i(LOG_PRE,"测试数据："+msg.toString());

        try {

            //HTTP/HTTPS : 如果是 http报文格式的,此时已经被编码解码器转为了该类,如果不是,则表示是https协议建立第一次连接后后续的请求等.
            if (msg instanceof FullHttpRequest){
                final FullHttpRequest request = (FullHttpRequest)msg;

                InetSocketAddress address = ProxyUtil.getAddressByRequest(request);

                //method 为CONNECT则说明为https
                if (HttpMethod.CONNECT.equals(request.method())){
                    LogUtil.i(LOG_PRE,",https requests，target "+channelId+" url: "+request.uri());

                    //存入缓存
                    ChannelCacheUtil.put(channelId,new ChannelCache(address,connect(false,address,ctx,msg)));
                    //给客户端响应成功信息 HTTP/1.1 200 Connection Established  .失败时直接退出
                    //此处没有添加Connection Established,似乎也没问题
                    if (!ProxyUtil.writeAndFlush(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), true))
                        return;

                    //此处将用于报文编码解码的处理器去除,因为后面上方的信息都是加密过的,不符合一般报文格式,我们直接转发即可
//                   ctx.pipeline().remove(ProxyServer.NAME_HTTP_ENCODE_HANDLER1);
//                   ctx.pipeline().remove(ProxyServer.NAME_HTTP_DECODE_HANDLER);
                    ctx.pipeline().remove(NAME_HTTPSERVER_CODEC);
                    //TODO 不确定聚合是否应该去掉
//                   ctx.pipeline().remove(ProxyServer.NAME_HTTP_AGGREGATOR_HANDLER);

                    //此时 客户端已经和目标服务器 建立连接(其实是 客户端 -> 代理服务器 -> 目标服务器),
                    //直接退出等待下一次双方连接即可.
                    return;
                }

                //Http
                LogUtil.i(LOG_PRE,",http request, target url:{}"+channelId+request.uri());
                HttpHeaders headers = request.headers();
                headers.add("Connection",headers.get("Proxy-Connection"));
                headers.remove("Proxy-Connection");

                connect(true,address,ctx,msg);
                return;

            }

            //其他格式数据(建立https connect后的客户端再次发送的加密数据):
            //从缓存获取到数据
            ChannelCache cache = ChannelCacheUtil.get(ProxyUtil.getChannelId(ctx));
            //如果缓存为空,应该是缓存已经过期,直接返回客户端请求超时,并关闭连接

            if (cache == null) {
                LogUtil.i(LOG_PRE , ",缓存过期"+channelId);
                ProxyUtil.writeAndFlush(ctx, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT), false);
                ctx.close();
                return;
            }

            //此处,表示https协议的请求第x次访问(x > 2; 第一次我们响应200,第二次同目标主机建立连接, 此处直接发送消息即可)
            //如果此时通道是可写的,写入消息
            boolean flag = false;
            LogUtil.i(LOG_PRE,",https,正在向目标发送后续消息，"+channelId);
            for (int i = 0; i < 100; i++) {
                if ((flag = cache.getChannelFuture().channel().isActive()))
                    break;
                Thread.sleep(10);
            }

            if (flag){
                cache.getChannelFuture().channel().writeAndFlush(msg).addListener((ChannelFutureListener)future ->{
                    if (future.isSuccess())
                        LogUtil.i(LOG_PRE,"通道id:{},https,向目标发送后续消息成功."+channelId);
                    else
                        LogUtil.i(LOG_PRE,"通道id:{},https,向目标发送后续消息失败.e:{}"+channelId+future.cause());

                });

                return;

            }

            LogUtil.i(LOG_PRE , ",https,与目标通道不可写,关闭与客户端连接"+channelId);
            ProxyUtil.responseFailedToClient(ctx);

        }catch (Exception e){
            LogUtil.i(LOG_PRE , "error:{}"+channelId+e.getMessage()+ e);
        }
    }


    /**
     * 和 目标主机 建立连接
     */
    private ChannelFuture connect(boolean isHttp,InetSocketAddress address,ChannelHandlerContext ctx,Object msg){
        Bootstrap bootstrap = BootstrapFactory.getInstance().build();
        if (isHttp){
            return bootstrap.handler(new HttpConnectChannelInitializer(ctx))
                    .connect(address)
                    .addListener(new HttpChannelFutureListener(msg,ctx));
        }
        //如果为https请求
        return bootstrap.handler(new HttpsConnectChannelInitializer(ctx))
                .connect(address)
                .addListener(new HttpsChannelFutureListener(msg,ctx));
    }



}
