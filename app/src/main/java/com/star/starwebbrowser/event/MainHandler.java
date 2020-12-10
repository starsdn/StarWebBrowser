package com.star.starwebbrowser.event;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class MainHandler {

    private static Handler mainHandler;
    public String Info;
    public MESSTYPE msgType;

//    private static final int CMD = 0;
//    private static final int CMDEND = 1;
//    private static final int SEND_FAIL = 2;
//    private static final int SEND_SUCCESS = 3;
//    private static final int REV_FAIL = 4;
//    private static final int REV_SUCCESS = 5;

    public MainHandler(MESSTYPE msgType, String info) {
        Info = info;
        this.msgType = msgType;
    }

    public static void Init(Handler paramHandle) {
        mainHandler = paramHandle;
    }

    /**
     * 发送信息给handle 并由handler执行
     *
     * @param messtype
     * @param strParam
     */
    public static void SendMessage(MESSTYPE messtype, String strParam) {
        MainHandler localMainMessage = new MainHandler(messtype, strParam);
        Message localMessage = mainHandler.obtainMessage(1, 1, 1, localMainMessage);
        mainHandler.sendMessage(localMessage);
    }

    /**
     * Handle 消息类型
     * CMD 接收图片命令
     * CMDEND 结束检验
     * SEND_FAIL 发送失败
     * SEND_SUCCESS 发送成功
     * REV_FAIL 接收失败
     * REV_SUCCESS 接收成功
     * ERROR 错误
     */
    public static enum MESSTYPE{
        CMD,CMDEND,SEND_FAIL,SEND_SUCCESS,REV_FAIL,REV_SUCCESS,ERROE
    }
//    @IntDef({CMD, CMDEND, SEND_FAIL, SEND_SUCCESS, REV_FAIL, REV_SUCCESS})
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface MESSTYPE {
//        //通过注解方式使用枚举
//    }

}
