package com.xulei.g4nproxy_client.kidHttpProxy;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_client.kidHttpProxy.handler.ProxyServerHandler;
import com.xulei.g4nproxy_client.util.LogUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import static com.xulei.g4nproxy_client.kidHttpProxy.KidHttpProxyConstants.NAME_HTTPSERVER_CODEC;
import static com.xulei.g4nproxy_client.kidHttpProxy.KidHttpProxyConstants.NAME_HTTP_AGGREGATOR_HANDLER;
import static com.xulei.g4nproxy_client.kidHttpProxy.KidHttpProxyConstants.NAME_PROXY_SERVER_HANDLER;

/**
 * @author lei.X
 * @date 2019/4/14 4:17 PM
 */
public class ProxySerever {


    public static final String tag = "kidHttpProxy_proxyServer";


    public static void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(NAME_HTTPSERVER_CODEC,new HttpServerCodec())
                                /**
                                 * /**usually we receive http message infragment,if we want full http message,
                                 * we should bundle HttpObjectAggregator and we can get FullHttpRequest。
                                 * 我们通常接收到的是一个http片段，如果要想完整接受一次请求的所有数据，我们需要绑定HttpObjectAggregator，然后我们
                                 * 就可以收到一个FullHttpRequest-是一个完整的请求信息。
                                 **/
                                .addLast(NAME_HTTP_AGGREGATOR_HANDLER,new HttpObjectAggregator(1024*1024)) //定义缓冲区数据量大小
                                .addLast(NAME_PROXY_SERVER_HANDLER, new ProxyServerHandler());
                    }
                })
                //服务器端接受的队列长度
                .option(ChannelOption.SO_BACKLOG,2048)
                //保持连接,类似心跳检测,超过2小时空闲才激活
//                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                .option(ChannelOption.SO_RCVBUF,128*1024);

        LogUtil.i(tag,"代理服务器启动，端口: "+ String.valueOf(Constants.littleProxyPort));
        //绑定端口，进行监听，这里可以开启多个端口监听
        ChannelFuture future = serverBootstrap.bind(Constants.littleProxyPort).sync();
        //关闭前阻塞
        future.channel().closeFuture().sync();
        //关闭线程组
        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();

    }
}
