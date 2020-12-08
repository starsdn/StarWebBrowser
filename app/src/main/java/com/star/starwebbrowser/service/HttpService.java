package com.star.starwebbrowser.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.star.starwebbrowser.event.MainHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

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
        Map<String,String> body = new HashMap<>();
        try {
            session.parseBody(body);
            //从body里面解析传递过来的值
            String str_params = body.get("postData");//从post方法中获取对应的post参数
            JsonObject json_params = new JsonParser().parse(str_params).getAsJsonObject();//得到jsonObject
            String strtype = json_params.get("type").getAsString();//得到type指令
            switch (strtype){
                case "cmd"://得到拍照命令
                    JsonObject json_Data = json_params.getAsJsonObject("data"); //得到json数据
                    //String hphm = json_Data.get("hphm").toString();//得到号牌号码
                    MainHandler.SendMessage(MainHandler.MESSTYPE.CMD,json_Data.toString());
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        finally {
            return newFixedLengthResponse("ok");
        }
    }

    /**
     * 自定义Response返回内容
     *
     * @param status
     * @return
     */
    private Response addHeaderResponse(Response.IStatus status) {
        Response response = null;
        response = newFixedLengthResponse(status, "application/json;charset=utf-8", "msg");
        response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
        return response;
    }
}
