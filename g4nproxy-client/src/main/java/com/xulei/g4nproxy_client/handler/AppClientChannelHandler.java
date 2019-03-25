package com.xulei.g4nproxy_client.handler;


import com.alibaba.fastjson.JSON;
import com.xulei.g4nproxy_client.Constants;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 10:12 AM
 */

@Slf4j
public class AppClientChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static final String tag = "appClient_tag";



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) throws Exception {

        LogUtil.i(tag,"receive message: "+msg.toString());
        switch (msg.getType()){
            case ProxyMessage.C_TYPE_AUTH:
                handleAuthMessage(ctx,msg);
                break;
//            case ProxyMessage.TYPE_CONNECT:
//                handleConnectMessages(ctx,msg);
//                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx, msg);
                break;
        }



        //将这个管道放到Map中，方便服务器返回数据时调用
        Constants.manageCtxMap.put(Constants.DATA_CHANNEL,ctx);
        LogUtil.i(tag,"与请求服务器建立连接成功，添加HttpMsgHandler，去除AppClientChannelHandler");

//        ctx.fireChannelRead(msg);


    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(tag,"app端的通道被激活");
        super.channelActive(ctx);
    }

    private void handleAuthMessage(ChannelHandlerContext ctx,ProxyMessage message){

        LogUtil.i(tag,"请求服务器的认证请求： "+new String(message.getData()));
    }


    private static String genPayLoad(){
        StringBuilder stringBuilder = new StringBuilder();
        for( int i=0;i<10;i++){
            StringBuilder stringBuilder1 = new StringBuilder();
            for(int j=0;j<100;j++){
                stringBuilder1.append(i);
            }
            stringBuilder.append(stringBuilder1);
        }
        return stringBuilder.toString();
    }

    private static String  payload = genPayLoad()+ genPayLoad() + genPayLoad();
    private static byte[] bytePayload = payload.getBytes();


    /**
     * TODO 真正的代理实现
     * 处理传输数据的请求
     * @param ctx
     * @param msg
     */
    private void handleTransferMessage(ChannelHandlerContext ctx,ProxyMessage msg){
        // 测试返回是否成功
        LogUtil.w(tag,"代理服务器处理传输数据的请求");


        //HttpServerCodec只接受PooledUnsafeDIrectByteBuf编码的消息
//        ByteBuf byteBuf = Unpooled.copiedBuffer(msg.getData());

        //不能解析数据
        FullHttpRequest  requestData = JSON.parseObject(msg.getData(),FullHttpRequest.class);


        //添加HTTP 消息的处理逻辑

        ctx.pipeline()
//                .addLast(NAME_HTTPSERVER_CODEC,new HttpServerCodec())
//                /**
//                 * /**usually we receive http message infragment,if we want full http message,
//                 * we should bundle HttpObjectAggregator and we can get FullHttpRequest。
//                 * 我们通常接收到的是一个http片段，如果要想完整接受一次请求的所有数据，我们需要绑定HttpObjectAggregator，然后我们
//                 * 就可以收到一个FullHttpRequest-是一个完整的请求信息。
//                 **/
//                .addLast(NAME_HTTP_AGGREGATOR_HANDLER,new HttpObjectAggregator(1024*1024)) //定义缓冲区数据量大小
                .addLast(new HttpMsgHandler());
//        将消息发往下一个channel

//        收到的消息没有指定的结束标记。 比如指定了lineBasedFrameDecoder，没有换行标志，是不会调用channelRead方法的，其他的类似
//        ctx.writeAndFlush(data);

        //将这个管道放到Map中，方便服务器返回数据时调用
        Constants.manageCtxMap.put(Constants.DATA_CHANNEL,ctx);

        ctx.fireChannelRead(requestData);



    }
    /**
     * 处理连接请求
     * TODO 废弃的请求
     * @param ctx
     * @param msg
     */
    private void handleConnectMessages(ChannelHandlerContext ctx, ProxyMessage msg){

        LogUtil.i(tag,"进入连接处理模块");

        //获取数据传输的channel
        Channel dataChannel = ctx.channel().attr(com.xulei.g4nproxy_protocol.protocol.Constants.NEXT_CHANNEL).get();
        ProxyMessage testMesage = new ProxyMessage();
        testMesage.setData(testData());
        testMesage.setUri("test");
        dataChannel.writeAndFlush(testMesage);

        //测试数据发回的位置
        testMesage.setUri("testCTX");
        ctx.channel().writeAndFlush(testMesage);

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        LogUtil.e(tag,"appClientHandler error",cause);
        // 当出现异常就关闭连接
        ctx.close();
    }


    private byte[] testData(){
        ByteBuf buf = Unpooled.buffer(10);
        byte[] bytes = {1,2,3,4,5};
        buf.writeBytes(bytes);

//        return buf;
        return bytes;
    }

}
