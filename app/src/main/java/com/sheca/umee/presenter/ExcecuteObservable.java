package com.sheca.umee.presenter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

public class ExcecuteObservable {

    private String url;

    public ExcecuteObservable(String url) {

        this.url = url;
    }

    public Observable excecute(final String message) {

        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), message);
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
                } catch (Exception e) {
                    //告诉订阅者错误信息
                    subscriber.onError(e);
                } finally {
                    //告诉订阅者请求数据结束
                    subscriber.onCompleted();
                }
            }
        });

        return observable;

    }

}
