package com.esandinfo.utils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

public class EtasExcecuteObservable {

    private String url  = "";

    public EtasExcecuteObservable(String url) {

        this.url = url;
    }

    public Observable excecute(final String msg) {


        Observable etasExcecuteObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), msg);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try {

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {

                        subscriber.onNext(response.body().string());
                    }
                } catch (IOException e) {

                    //告诉订阅者错误信息
                    subscriber.onError(e);
                } finally {

                    //告诉订阅者请求数据结束
                    subscriber.onCompleted();
                }
            }
        });

        return etasExcecuteObservable;
    }



}
