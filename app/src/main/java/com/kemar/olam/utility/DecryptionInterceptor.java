package com.kemar.olam.utility;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DecryptionInterceptor implements Interceptor {

    private final EncrypterDecrypter mDecryptionStrategy;

    //injects the type of decryption to be used
    public DecryptionInterceptor(EncrypterDecrypter mDecryptionStrategy) {
        this.mDecryptionStrategy = mDecryptionStrategy;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());
        if (response.isSuccessful()) {
            Response.Builder newResponse = response.newBuilder();
            String contentType = response.header("Content-Type");
            if (TextUtils.isEmpty(contentType)) contentType = "application/json";
            assert response.body() != null;
            String responseString = response.body().string();
            JSONObject jobj = new JSONObject();
            try {
                jobj = new JSONObject(responseString);
                responseString = jobj.get("response").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            String decryptedString = null;
            if (mDecryptionStrategy != null) {
                try {
                    if(responseString!=null || !responseString.equals("null"))
                    decryptedString = mDecryptionStrategy.decrypt(Constants.secreteKey,Constants.initialVector,responseString);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                throw new IllegalArgumentException("No decryption strategy!");
            }
            if(decryptedString!=null) {
                newResponse.body(ResponseBody.create(MediaType.parse(contentType), decryptedString));
            }
            return newResponse.build();
        }
        return response;
    }

}
