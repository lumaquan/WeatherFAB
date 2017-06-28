package luismelendezcom.weatherfab;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Controller implements Callback {
     private Activity context;

    public  void execute(Activity context, URL url){

        this.context = context;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()

                .addNetworkInterceptor(new ResponseCacheInterceptor())
                .addInterceptor(new OfflineResponseCacheInterceptor(context))
                .cache(new Cache(new File(context.getCacheDir(),
                        "apiResponses"), 5 * 1024 * 1024))
                .build();


        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(this);


    }


    private static class ResponseCacheInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + 60)
                    .build();
        }
    }

    private static class OfflineResponseCacheInterceptor implements Interceptor {

        private Context context;

        public OfflineResponseCacheInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!isNetworkAvailable()) {
                request = request.newBuilder()
                        .header("Cache-Control",
                                "public, only-if-cached, max-stale=" + 2419200)
                        .build();
            }
            return chain.proceed(request);
        }


        public boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
        }

    }



    @Override
    public void onFailure(Call call, IOException e) {
        Snackbar.make(context.findViewById(R.id.coordinatorLayout),R.string.connect_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

      if(response.isSuccessful()){
          String jsonAnswer = response.body().string();
          try {
              EventBus.getDefault().post(new JsonEvent(new JSONObject(jsonAnswer)));
          } catch (JSONException e) {
              e.printStackTrace();
          }

      }

    }
}
