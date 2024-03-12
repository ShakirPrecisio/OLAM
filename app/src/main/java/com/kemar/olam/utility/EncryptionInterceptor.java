package com.kemar.olam.utility;

import com.google.gson.Gson;
import com.kemar.olam.login.model.request.CommonEncyrptRequest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class EncryptionInterceptor implements Interceptor {
    private static final String TAG = EncryptionInterceptor.class.getSimpleName();
    private final EncrypterDecrypter mEncryptionStrategy;

    public EncryptionInterceptor(EncrypterDecrypter mEecryptionStrategy) {
        this.mEncryptionStrategy = mEecryptionStrategy;
    }
    public static String requestBodyToString(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return buffer.readUtf8();
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody rawBody = request.body();
        String encryptedBody = "";
        String finalRequest = "";


        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        if (mEncryptionStrategy != null) {
            try {
                String rawBodyString = null;
                if (rawBody != null) {
                    rawBodyString = requestBodyToString(rawBody);
                    encryptedBody = mEncryptionStrategy.encrypt(Constants.secreteKey,Constants.initialVector,rawBodyString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("No encryption strategy!");
        }
        CommonEncyrptRequest encryptRequest  = new CommonEncyrptRequest();
        encryptRequest.setRequest(encryptedBody);
        finalRequest = new Gson().toJson(encryptRequest);
        RequestBody body = RequestBody.create(mediaType, finalRequest);

        //build new request
        request = request.
                newBuilder()
                .header("Content-Type", body.contentType().toString())
                .header("Content-Length", String.valueOf(body.contentLength()))
                .method(request.method(), body).build();

        return chain.proceed(request);
    }
}
