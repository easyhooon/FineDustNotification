package kr.ac.konkuk.finedust.data.services

import kr.ac.konkuk.finedust.BuildConfig
import kr.ac.konkuk.finedust.data.models.monitoringstation.MonitoringStationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApiService {

    //Open API 활용가이드 문서(에어코리아_측정소정보 조회 서비스_기술문서_v1.0.docx) 의 CallBack URL의  뒷자리 ㅇㅇ 앞쪽은 BaseURL
    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
            "?serviceKey=${BuildConfig.AIRKOREA_SERVICE_KEY}" +
            "&returnType=json")
    suspend fun getNearbyMonitoringStation(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>
}