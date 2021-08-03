package kr.ac.konkuk.finedust.data.models.monitoringstation


import com.google.gson.annotations.SerializedName

//원래 item data class 였음
data class MonitoringStation(
    @SerializedName("addr")
    val addr: String?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("tm")
    val tm: Double?
)