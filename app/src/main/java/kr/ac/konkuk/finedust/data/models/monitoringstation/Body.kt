package kr.ac.konkuk.finedust.data.models.monitoringstation


import com.google.gson.annotations.SerializedName

data class Body(

    //item data class 의 이름을 바꿔도 SerializedName 은 변하지 않기 때문에 데이터를 가져오는 데에 문제는 없음
    @SerializedName("items")
    val monitoringStations: List<MonitoringStation>?,
    @SerializedName("numOfRows")
    val numOfRows: Int?,
    @SerializedName("pageNo")
    val pageNo: Int?,
    @SerializedName("totalCount")
    val totalCount: Int?
)