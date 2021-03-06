package com.star.starwebbrowser.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.star.starwebbrowser.event.MainHandler;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HttpService extends NanoHTTPD {

    private static final String TAG = "HttpServer";

    public static final String DEFAULT_SHOW_PAGE = "index.html";
    public static final int DEFAULT_PORT = 8888;//此参数随便定义，最好定义1024-65535；1-1024是系统常用端口,1024-65535是非系统端口

    public HttpService(int port) {
        super(port);
    }

    public HttpService(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        // return super.serve(session);
        String strUri = session.getUri(); //得到请求地址IP
        Method strMethod = session.getMethod();//得到请求方法类型  post put等
        Map<String, String> body = new HashMap<>();
        try {
            session.parseBody(body);
            //从body里面解析传递过来的值
            String str_params = body.get("postData");//从post方法中获取对应的post参数
            JsonObject json_params = new JsonParser().parse(str_params).getAsJsonObject();//得到jsonObject
            if (json_params.has("type")) {
                String strtype = json_params.get("type").getAsString();//得到type指令
                switch (strtype) {
                    case "cmd"://得到拍照命令
                        //JsonObject json_Data = json_params.getAsJsonObject("data"); //得到json数据
                        //String hphm = json_Data.get("hphm").toString();//得到号牌号码
                        //MainHandler.SendMessage(MainHandler.MESSTYPE.CMD, json_Data.toString());
                        MainHandler.SendMessage(MainHandler.MESSTYPE.CMD, str_params);
                        break;
                    case "cmdend"://拍照结束
                        MainHandler.SendMessage(MainHandler.MESSTYPE.CMDEND, "end");
                        break;
                    case "rspprocsuccess": //服务端接收照片成功
                        MainHandler.SendMessage(MainHandler.MESSTYPE.REV_SUCCESS, "success");
                        break;
                    case "rspprocfail": //服务端接收到照片失败
                        MainHandler.SendMessage(MainHandler.MESSTYPE.REV_FAIL, "fail");
                        break;
                }
            }else {
                //服务端发送指令不符合规范
                MainHandler.SendMessage(MainHandler.MESSTYPE.ERROE,"服务端发送指令不符合规范");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        } finally {
           // return newFixedLengthResponse("ok");
           return Response.newFixedLengthResponse("ok");
        }
    }

    /**
     * 自定义Response返回内容
     *
     * @param status
     * @return
     */
    private Response addHeaderResponse(IStatus status) {
        Response response = null;
        response = Response.newFixedLengthResponse(status, "application/json;charset=utf-8", "msg");
        response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
        return response;
    }
}
