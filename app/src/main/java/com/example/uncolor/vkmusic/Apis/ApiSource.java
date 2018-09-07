package com.example.uncolor.vkmusic.Apis;


import com.example.uncolor.vkmusic.Apis.response_models.AuthResponseModel;
import com.example.uncolor.vkmusic.Apis.response_models.MusicListResponseModel;
import com.example.uncolor.vkmusic.Apis.response_models.VKMusicResponseModel;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Uncolor on 27.02.2018.
 */

public interface ApiSource {

    @Headers({"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15",
            "Referer: https://api-2.datmusic.xyz"})
    @GET("https://api-2.datmusic.xyz/search")
    Call<MusicListResponseModel> getMusic(@Query("q") String query,
                                          @Query("page") int offset);

    @GET
    @Headers("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15")
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);

    @FormUrlEncoded
    @POST("https://audio.bigbadbird.ru/src/examples/example_microg.php")
    Call<AuthResponseModel> login(@Field("login")String login,
                                  @Field("pass") String pass);

    @Headers("User-Agent: KateMobileAndroid/48.2 lite-433 (Android 8.1.0; SDK 27; arm64-v8a; Google Pixel 2 XL; en)")
    @GET("https://api.vk.com/method/audio.get")
    Call<VKMusicResponseModel> getVkMusic(@Query("access_token") String access_token,
                                          @Query("v") String v,
                                          @Query("offset") int offset,
                                          @Query("count") int count);

}


