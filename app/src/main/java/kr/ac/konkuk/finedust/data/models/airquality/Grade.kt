package kr.ac.konkuk.finedust.data.models.airquality

import androidx.annotation.ColorRes
import com.google.gson.annotations.SerializedName
import kr.ac.konkuk.finedust.R

enum class Grade(
    val label: String,
    val emoji: String,
    @ColorRes val colorResId: Int
    )    {
    //Grade 값에 1이 들어오면 자동으로 GOOD 으로 매핑
    @SerializedName("1")
    GOOD("좋음", "😁", R.color.blue),
    @SerializedName("2")
    NORMAL("보통", "\uD83D\uDE42", R.color.green),
    @SerializedName("3")
    BAD("나쁨", "😑", R.color.yellow),
    @SerializedName("4")
    AWFUL("매우 나쁨", "🤐", R.color.red),

    UNKNOWN("미측정", "\uD83E\uDDD0", R.color.grey);

    override fun toString(): String {
        return "$label $emoji"
    }
}