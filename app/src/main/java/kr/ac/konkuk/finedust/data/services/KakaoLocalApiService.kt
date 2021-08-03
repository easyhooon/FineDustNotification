package kr.ac.konkuk.finedust.data.services

import kr.ac.konkuk.finedust.BuildConfig
import kr.ac.konkuk.finedust.data.models.tmcoordinates.TmCoordinatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoLocalApiService {

    //헤더를 통해 인증정보를 전달 (앱키 내부에 restAPI 키를 전달
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    @GET("v2/local/geo/transcoord.json?output_coord=TM")
    suspend fun getTmCoordinates(
        //경도 latitude
        @Query("x") longitude: Double,
        //위도 latitude
        @Query("y") latitude: Double,
    ): Response<TmCoordinatesResponse>
}