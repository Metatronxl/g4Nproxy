package com.xulei.g4nproxy_client;


import com.xulei.g4nproxy_client.handler.AppClientChannelHandler;
import com.xulei.g4nproxy_client.handler.LittleProxyServerChannelHandler;
import com.xulei.g4nproxy_client.kidHttpProxy.ProxySerever;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.ClientChannelManager;
import com.xulei.g4nproxy_protocol.ClientIdleCheckHandler;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageDecoder;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.xulei.g4nproxy_protocol.protocol.Constants.APP_CLIENT_HANDLER;
import static com.xulei.g4nproxy_protocol.protocol.Constants.PROXY_MESSAGE_DECODE;
import static com.xulei.g4nproxy_protocol.protocol.Constants.PROXY_MESSAGE_ENCODE;

/**
 * @author lei.X
 * @date 2019/3/18 9:25 AM
 */


@Slf4j
public class ProxyClient implements ChannelStatusListener {



    private static final String tag = "proxyCient_tag";

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private static final int LENGTH_FIELD_OFFSET = 0;

    private static final int LENGTH_FIELD_LENGTH = 4;

    private static final int INITIAL_BYTES_TO_STRIP = 0;

    private static final int LENGTH_ADJUSTMENT = 0;


    private static long sleepTimeMill = 1000;

    private String serverHost;
    private int serverPort;
    private String clientID;


    private ClientChannelManager clientChannelManager;

    @Getter
    private HttpProxyConnectionManager httpProxyConnectionManager = new HttpProxyConnectionManager();

    //手机的代理客户端
    public Bootstrap appBootstrap;

    //连接手机代理服务器
    public Bootstrap join2LittleProxyBootStrap;

    private NioEventLoopGroup workerGroup;


    public ProxyClient(String serverHost,int serverPort,String clientId){
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientID = clientId;

        clientChannelManager = new ClientChannelManager(serverHost);


    }


    public ClientChannelManager getClientChannelManager(){return clientChannelManager;}


    /**
     * 启动内网穿透客户端
     *
     * @param serverHost 公网服务主机
     * @param serverPort 公网服务端口
     * @param clientID   客户端标记，同一个客户端标记在服务器端会映射为同一个端口
     */
    public static ProxyClient start(String serverHost, int serverPort, final String clientID) throws InterruptedException {
        ProxyClient proxyClient = new ProxyClient(serverHost, serverPort, clientID);
        //启动littleProxy服务器
//        Launcher.startHttpProxyService(Constants.littleProxyPort);

        new Thread(() -> {
            try {
                ProxySerever.start();
            } catch (InterruptedException e) {
                LogUtil.e(tag,e.getMessage());
            }

        }).start();

        proxyClient.startInernal();
        return proxyClient;
    }


    private void startInernal(){
        workerGroup = new NioEventLoopGroup();

        join2LittleProxyBootStrap = new Bootstrap(); // 连接手机开启的代理服务器
        join2LittleProxyBootStrap.group(workerGroup);
        join2LittleProxyBootStrap.channel(NioSocketChannel.class);
        join2LittleProxyBootStrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LittleProxyServerChannelHandler(ProxyClient.this));
            }
        });


        appBootstrap = new Bootstrap();   // 内网穿透
        appBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(PROXY_MESSAGE_ENCODE,new ProxyMessageEncoder());
                        ch.pipeline().addLast(PROXY_MESSAGE_DECODE,new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));

                        ch.pipeline().addLast(new ClientIdleCheckHandler());
                        ch.pipeline().addLast(APP_CLIENT_HANDLER,new AppClientChannelHandler(ProxyClient.this,ProxyClient.this));
                    }
                });
        connectServer();
    }



    /**
     * 连接请求服务器
     * param channel
     */
    private void connectServer(){
        appBootstrap.connect(serverHost,serverPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){

                    //连接成功后，手机代理服务器向server发送认证消息
                    clientChannelManager.setCmdChannel(future.channel());
                    ProxyMessage proxyMessage = new ProxyMessage();
                    proxyMessage.setType(ProxyMessage.C_TYPE_AUTH);
                    proxyMessage.setUri(clientID);
                    future.channel().writeAndFlush(proxyMessage);

                    //控制重连接时长
                    sleepTimeMill = 1000;
                    LogUtil.i(tag,"： connect to server successs");

                }else {
                    LogUtil.e(tag,"： connect to server failed");
                    //等待后重新尝试连接
                    reconnectWait();
                    connectServer();
                }
            }
        });
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }



    private static void reconnectWait() {
        try {
            if (sleepTimeMill > 60000) {
                sleepTimeMill = 1000;
            }

            synchronized (ProxyClient.class) {
                sleepTimeMill = sleepTimeMill * 2;
                Thread.sleep(sleepTimeMill);
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        reconnectWait();
        connectServer();
    }
}
