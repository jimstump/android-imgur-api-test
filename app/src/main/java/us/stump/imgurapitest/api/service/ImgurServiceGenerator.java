package us.stump.imgurapitest.api.service;

import android.text.TextUtils;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handles the dirty work to create a reusable Retrofit client
 * See: https://futurestud.io/tutorials/retrofit-2-creating-a-sustainable-android-client
 */

public class ImgurServiceGenerator {
    /**
     * The base URL for the version of the Imgur API that we are using.
     */
    private static final String BASE_URL = "https://api.imgur.com/3/";

    /**
     * The HTTP client to use to make requests.
     */
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    /**
     * Retrofit.Builder instance to use to build Retrofit requests.
     */
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    /**
     * Retrofit instance to use to make requests.
     */
    private static Retrofit retrofit;

    /**
     * Make our Retrofit service.
     *
     * This service won't be automatically setting an Authorization header.
     *
     * @param serviceClass
     * @param <S>
     * @return
     */
    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    /**
     * Make our Retrofit service.
     *
     * This service will automatically set an Authorization header using the given auth token.
     *
     * @param serviceClass
     * @param authToken The value of the Authorization header to set for every request.
     * @param <S>
     * @return
     */
    public static <S> S createService(Class<S> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        if (retrofit == null) {
            builder.client(httpClient.build());
            retrofit = builder.build();
        }

        return retrofit.create(serviceClass);
    }
}
