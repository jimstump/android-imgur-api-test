package us.stump.imgurapitest.api.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Intercepts an HTTP request made by OkHttp3 and adds an Authorization header to it
 */

public class AuthenticationInterceptor implements Interceptor {
    /**
     * The value of the Authorization header to use with requests.
     */
    private String authToken;

    /**
     * Construct the AuthenticationInterceptor
     * @param token The value of the Authorization header to use with requests.
     */
    public AuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder().header("Authorization", authToken);

        Request request = builder.build();
        return chain.proceed(request);
    }
}
