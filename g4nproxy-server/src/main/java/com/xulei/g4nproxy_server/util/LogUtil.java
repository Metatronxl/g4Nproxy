package com.xulei.g4nproxy_server.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录工具
 * @author lei.X
 * @date 2019/3/18 4:27 PM
 */

@Slf4j
public class LogUtil {

    public static void i(String tag, String msg) {
        log.info(tag+": "+msg);
    }

    public static void w(String tag, String msg) {
        log.warn(tag+": "+msg);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        log.warn(tag+": "+msg+": Throwable: "+throwable);
    }

    public static void e(String tag, String msg) {
        log.error(tag+": "+msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        log.error(tag+": "+msg+": Throwable: "+throwable);
    }
}
