package com.recordlab.dailyscoop.network

import com.recordlab.dailyscoop.network.request.RequestSignIn
import com.recordlab.dailyscoop.network.request.RequestSignup
import com.recordlab.dailyscoop.network.request.RequestWriteDiary
import com.recordlab.dailyscoop.network.response.*
import retrofit2.Call
import retrofit2.http.*

public interface RequestService {
    // 회원가입
    @POST("/api/signup")
    fun requestSignup(@Body body: RequestSignup): Call<TokenData> // Call<ResponseSignup>

    @POST("/api/login")
    fun requestSingIn(@Body body: RequestSignIn): Call<UserInfoData> //Call<ResponseSignin>

    @POST("/diaries")
    fun requestWriteDiary(
        @HeaderMap header: Map<String, String?>,
        @Body diary: RequestWriteDiary
    ): Call<ResponseWriteDiary>

    @GET("/api/diaries")
    fun requestGetDiaries(
        @HeaderMap header: Map<String, String?>
    ): Call<ResponseDiaryList>

    @GET("/api/diaries/{diaryDate}")
    fun requestGetDiaryDetail(
        @HeaderMap header: Map<String, String?>,
        @Path("diaryDate") date: String
    ): Call<ResponseDiaryDetail>

    @GET("/api/diaries/calendar")
    fun requestGetCalendar(
        @HeaderMap header: Map<String, String?>,
        @Query("date") date: String,
        @Query("type") type: String
    ) : Call<ResponseDiaryList>
}