package marekbodziony.eyewit.api;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Api {

    private static final String SERVER_URL = "http://192.168.43.76:8081/home/AddEvent";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String post (String json) throws IOException{
       // RequestBody requestBody = RequestBody.create(JSON, json);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", json)
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()){
            return response.body().string();
        }
    }

    public static class AsyncTask extends android.os.AsyncTask<String,Void,String>{

        Api api;

        public AsyncTask(Api api) {
            this.api = api;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return api.post(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }
    }
}
