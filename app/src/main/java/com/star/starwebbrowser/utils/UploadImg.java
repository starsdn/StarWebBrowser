package com.star.starwebbrowser.utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImg implements Runnable {

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private final OkHttpClient client = new OkHttpClient();
    String strImg;//base64位图片
    String strUrl;//上传图片地址

    public UploadImg(String _strImg,String _url) {
        strImg = _strImg;
        strUrl = _url;
    }

    @Override
    public void run() {
        //2.创建RequestBody
        RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, strImg);
        //3.构建MultipartBody
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test.png", fileBody)
                // .addFormDataPart("userName", userName)
                .build();
        //4.构建请求
        Request request = new Request.Builder()
                .url(strUrl)
                .post(requestBody)
                .build();
        //5.发送请求
        Call call = client.newCall(request);//.execute();
        call.enqueue(new Callback() { //异步请求
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求成功
            }
        });
    }
}
