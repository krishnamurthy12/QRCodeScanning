package com.vvt.digitalfifo.utils;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.widget.Adapter;

import com.vvt.digitalfifo.apirequests.KanbanScan;
import com.vvt.digitalfifo.apirequests.LogIn;
import com.vvt.digitalfifo.apirequests.LogOut;
import com.vvt.digitalfifo.apirequests.QuarantineOut;
import com.vvt.digitalfifo.apirequests.StationName;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Krish on 12-11-2018.
 */

public class WebServices<T> {
    T t;
    Call<T> call=null;
    public T getT() {
        return t;
    }

    public void setT(T t) {

        this.t = t;
    }
    ApiType apiTypeVariable;
    Context context;
    OnResponseListener<T> onResponseListner;
    private static OkHttpClient.Builder builder;

    public enum ApiType {
        lineNames,stationNames,logIn,kanbonScan,analysisAndLomsOut,quarantineOut,logOut
    }

    public WebServices(OnResponseListener<T> onResponseListner) {
        this.onResponseListner = onResponseListner;

        if (onResponseListner instanceof Activity) {
            this.context = (Context) onResponseListner;
        } else if (onResponseListner instanceof IntentService) {
            this.context = (Context) onResponseListner;
        } else if (onResponseListner instanceof android.app.DialogFragment) {
            android.app.DialogFragment dialogFragment = (android.app.DialogFragment) onResponseListner;
            this.context = dialogFragment.getActivity();
        }else if (onResponseListner instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) onResponseListner;
            this.context = fragment.getActivity();
        }
         else if (onResponseListner instanceof Adapter) {

            this.context = (Context) onResponseListner;
        }
        else if (onResponseListner instanceof Adapter) {
            this.context = (Context) onResponseListner;
        }
            else {
            android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) onResponseListner;
            this.context = fragment.getActivity();
        }

        builder = getHttpClient();
    }

    public WebServices(Context context, OnResponseListener<T> onResponseListner) {
        this.onResponseListner = onResponseListner;
        this.context = context;
        builder = getHttpClient();
    }


    public OkHttpClient.Builder getHttpClient() {

        if (builder == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            client.connectTimeout(10000, TimeUnit.SECONDS);
            client.readTimeout(10000, TimeUnit.SECONDS).build();
            client.addInterceptor(loggingInterceptor);
            /*to pass header information with request*/
           /* client.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Content-Type", "application/json").build();
                    return chain.proceed(request);
                }
            });*/

            return client;
        }
        return builder;
    }

    private Retrofit getRetrofitClient(String api)
    {
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(api)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public void getLineNames(String api, ApiType apiTypes)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.getLines();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }

    public void getStationNames(String api, ApiType apiTypes, StationName stationName)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.getstations(stationName);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }


    public void userLogIn(String api, ApiType apiTypes, LogIn logIn)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.logIn(logIn);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false, 0);

            }
        });
    }

    public void validateKanbon(String api, ApiType apiTypes, KanbanScan kanbonScan)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.validateKanbon(kanbonScan);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }

    public void analysisAndLomsOut(String api, ApiType apiTypes, KanbanScan kanbonScan)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.analySisAndLomsOut(kanbonScan);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }

    public void quarantineOut(String api, ApiType apiTypes, QuarantineOut quarantineOut)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.quarantineOut(quarantineOut);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }


    public void logOut(String api, ApiType apiTypes, LogOut logOut)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        DigitalFIFOAPI digitalFIFOAPI=retrofit.create(DigitalFIFOAPI.class);
        call=(Call<T>)digitalFIFOAPI.logOut(logOut);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }


}
