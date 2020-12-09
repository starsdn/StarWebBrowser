package com.star.starwebbrowser.utils;

import com.star.starwebbrowser.event.MainHandler;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPost {

    /**
     * 上传post上传json数据
     * @param url
     * @param json_data
     */
    public void Post_json(String url,String json_data){
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(RequestBody.create(mediaType,json_data)).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
                MainHandler.SendMessage(MainHandler.MESSTYPE.SEND_FAIL,"fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功
                MainHandler.SendMessage(MainHandler.MESSTYPE.SEND_SUCCESS,"success");
            }
        });

    }

}
