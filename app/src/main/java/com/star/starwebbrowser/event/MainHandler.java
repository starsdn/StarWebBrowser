package com.star.starwebbrowser.event;

import android.os.Handler;
import android.os.Message;


public class MainHandler {

    private static Handler mainHandler;
    public String Info;
    public MESSTYPE msgType;


    public MainHandler(MESSTYPE msgType,String info) {
        Info = info;
        this.msgType = msgType;
    }

    public static void  Init(Handler paramHandle){
        mainHandler = paramHandle;
    }

    /**
     * 发送信息给handle 并由handler执行
     * @param messtype
     * @param strParam
     */
    public static void SendMessage(MESSTYPE messtype,String strParam){
        MainHandler localMainMessage = new MainHandler(messtype, strParam);
        Message localMessage = mainHandler.obtainMessage(1, 1, 1, localMainMessage);
        mainHandler.sendMessage(localMessage);
    }

    /**
     * Handle 消息类型
     */
    public static enum MESSTYPE{
        CMD,CMDEND,FAILE,SUCCESS
    }

}
