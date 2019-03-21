package com.xulei.g4nproxy_server.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Optional;

import io.netty.buffer.ByteBuf;

/**
 * @author lei.X
 * @date 2019/3/21 3:52 PM
 */
public class ByteArrayUtil {

    public static<T> Optional<byte[]> objectToBytes(T obj){
        byte[] bytes = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream sOut;
        try {
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(obj);
            sOut.flush();
            bytes= out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(bytes);
    }


    public static String msgToString(Object msg){
        ByteBuf bf = (ByteBuf)msg;
        byte[] byteArray = new byte[bf.readableBytes()];
        bf.readBytes(byteArray);
        return new String(byteArray);
    }
}
