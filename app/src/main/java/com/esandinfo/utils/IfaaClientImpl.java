package com.esandinfo.utils;

import com.esandinfo.etas.IfaaRequestBaseInfo;
import com.esandinfo.etas.callback.IfaaHttpCallback;
import com.esandinfo.etas.utils.IfaaClient;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 此类负责与 ifaa 服务器进行数据通讯，必须实现 IfaaClient 接口
 */
public class IfaaClientImpl implements IfaaClient, okhttp3.Callback {

    public IfaaHttpCallback ifaaHttpCallback;
    public IfaaRequestBaseInfo ifaaRequestBaseInfo;
    private String url = "http://bizserver.dev.esandinfo.com:80/gateway";

    @Override
    public void execute(IfaaRequestBaseInfo ifaaRequestBaseInfo, IfaaHttpCallback ifaaHttpCallback) {

        this.ifaaRequestBaseInfo = ifaaRequestBaseInfo;
        this.ifaaHttpCallback = ifaaHttpCallback;
        this.url = ifaaRequestBaseInfo.getUrl();
        MyLog.info("----- url = " + this.url);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        // 此字段为客户端将要同步给 ifaa 服务器的报文数据
        String jsonStr =ifaaRequestBaseInfo.getSentData();
        RequestBody requestBody = RequestBody.create(mediaType, jsonStr);
        Request request = new Request.Builder()
                .url(this.url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(this);

    }

    @Override
    public void onFailure(Call call, IOException e) {

        ifaaHttpCallback.onCompeleted(-1, e.getMessage(), ifaaRequestBaseInfo.getIfaaProcess());

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        if (response.code() == 200) {

            ifaaHttpCallback.onCompeleted(response.code(), response.body().string(), ifaaRequestBaseInfo.getIfaaProcess());

        } else {

            ifaaHttpCallback.onCompeleted(response.code(), response.message(), ifaaRequestBaseInfo.getIfaaProcess());
        }

    }
}

