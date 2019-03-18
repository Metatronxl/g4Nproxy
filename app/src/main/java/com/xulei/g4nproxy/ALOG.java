package com.xulei.g4nproxy;

/**
 * Created by virjar on 2019/2/23.
 */

public class ALOG {
    public interface LogImpl {
        void i(String tag, String msg);

        void w(String tag, String msg);

        void w(String tag, String msg, Throwable throwable);

        void e(String tag, String msg);

        void e(String tag, String msg, Throwable throwable);
    }

    public static void setUpLogComponent(LogImpl logImpl) {
        ALOG.logImpl = logImpl;
    }

    private static LogImpl logImpl = new LogImpl() {
        @Override
        public void i(String tag, String msg) {

        }

        @Override
        public void w(String tag, String msg) {

        }

        @Override
        public void w(String tag, String msg, Throwable throwable) {

        }

        @Override
        public void e(String tag, String msg) {

        }

        @Override
        public void e(String tag, String msg, Throwable throwable) {

        }
    };

    public static void i(String tag, String msg) {
        logImpl.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        logImpl.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        logImpl.w(tag, msg, throwable);
    }

    public static void e(String tag, String msg) {
        logImpl.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        logImpl.e(tag, msg, throwable);
    }

}
