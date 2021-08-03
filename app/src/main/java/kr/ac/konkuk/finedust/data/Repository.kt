package kr.ac.konkuk.finedust.data

import android.util.Log
import kr.ac.konkuk.finedust.BuildConfig
import kr.ac.konkuk.finedust.data.models.monitoringstation.MonitoringStation
import kr.ac.konkuk.finedust.data.services.AirKoreaApiService
import kr.ac.konkuk.finedust.data.services.KakaoLocalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

//retrofit

//싱글톤으로 사용할 것이기 때문에 object 클래스

//GPS 정보로 경도, 위도를 가져와서
//TM 좌표계로 변환을 하고
//TM 좌표계를 전달해서 가장 가까운 측정소를 가져오는 작업
object Repository {

    //getTmCoordinates 가 suspend 함수이기 때문에 이것을 사용하는 함수도 suspend
    suspend fun getNearbyMonitoringStation(longitude: Double, latitude: Double):MonitoringStation? {
        val tmCoordinates = kakaoLocalApiService
            .getTmCoordinates(longitude, latitude)
            .body()
            ?.documents  //배열로 구성되어있음
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y

        return airKoreaApiService
            .getNearbyMonitoringStation(tmX!!, tmY!!)
                //성공시 body() 값이 정상적으로 들어옴
            .body()
            ?.response
            ?.body
            //items == monitoringStations (0..n)
            ?.monitoringStations
                //가장 가까운 값을 전달
                //null 값을 후순위로 밀리게 하기 위해서
            ?.minByOrNull { it.tm ?: Double.MAX_VALUE  }
    }

    private val kakaoLocalApiService: KakaoLocalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
                //gson 으로 변환
            .addConverterFactory(GsonConverterFactory.create())
                //loggind 을 client 내부에서 추가
            .client(buildHttpClient())
            .build() //여기까지 하면 retrofit만 생성
            .create() //service 까지 생성, kotlin extension 파일을 보면 알아서 제네릭 타입으로 넣어줌, 확장함수의 형태로
    }

    private val airKoreaApiService: AirKoreaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_BASE_URL)
            //gson 으로 변환
            .addConverterFactory(GsonConverterFactory.create())
            //loggind 을 client 내부에서 추가
            .client(buildHttpClient())
            .build() //여기까지 하면 retrofit만 생성
            .create() //service 까지 생성, kotlin extension 파일을 보면 알아서 제네릭 타입으로 넣어줌, 확장함수의 형태로
    }

    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    //디버그일때만 open
                    level = if(BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
                //builder()니까 build()로 마무리
            .build()
}