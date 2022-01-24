package kr.ac.konkuk.finedust.appwidget

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kr.ac.konkuk.finedust.R
import kr.ac.konkuk.finedust.data.Repository
import kr.ac.konkuk.finedust.data.models.airquality.Grade

class SimpleAirQualityWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // 위젯 갱신하기
        // -> 위젯을 생성해두고 위젯 매니저에 업데이트를 요청하는 방식으로 갱신을 수행함

        // 명시적으로 시작을 해줘야 함
        // Manifest 에 서비스를 명시해줘야
        ContextCompat.startForegroundService(
            context!!,
            Intent(context, UpdateWidgetService::class.java)
        )
    }

    class UpdateWidgetService : LifecycleService() {

        override fun onCreate() {
            super.onCreate()

            createChannelIfNeeded()
            startForeground(
                NOTIFICATION_ID,
                createNotification()
            )
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            //서비스가 시작되면 위치정보를 가져와야 함
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //권한이 없는 경우
                val updateViews = RemoteViews(packageName, R.layout.widget_simple).apply {
                    setTextViewText(
                        R.id.resultTextView,
                        "권한 없음"
                    )
                    setViewVisibility(R.id.labelTextView, View.GONE)
                    setViewVisibility(R.id.gradeLabelTextView, View.GONE)
                }
                updateWidget(updateViews)
                stopSelf()

                return super.onStartCommand(intent, flags, startId)
            }

            //위치를 가져옴
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener { location ->
                    //CoroutineScope
                    lifecycleScope.launch{
                        // 인터넷이 연결되어있지 않을 경우에 예외처리
                        try {
                            //근첩한 측정소를 가져옴
                            val nearbyMonitoringStation = Repository.getNearbyMonitoringStation(location.longitude, location.latitude)
                            Log.d("nearbyMonitoringStation", "${location.latitude}, ${location.longitude}")
                            //측정소를 기반으로 최신 AirQuality 정보를 가져옴
                            val measuredValue = Repository.getLatestAirQualityData(nearbyMonitoringStation!!.stationName!!)
                            Log.d("nearbyMonitoringStation", nearbyMonitoringStation.stationName!!)
                            val updateViews = RemoteViews(packageName, R.layout.widget_simple).apply{
                                setViewVisibility(R.id.labelTextView, View.VISIBLE)
                                setViewVisibility(R.id.gradeLabelTextView, View.VISIBLE)

                                //총 등급
                                val currentGrade = (measuredValue?.khaiGrade ?: Grade.UNKNOWN)

                                setTextViewText(R.id.resultTextView, currentGrade.emoji)
                                setTextViewText(R.id.gradeLabelTextView, currentGrade.label)
                            }
                            updateWidget(updateViews)

                        } catch(e: Exception) {
                            e.printStackTrace()
                        } finally {
                            //서비스를 멈춤
                            stopSelf()
                        }
                    }
                }

            return super.onStartCommand(intent, flags, startId)
        }

        override fun onDestroy() {
            super.onDestroy()
            // remove notification? -> true
            stopForeground(true)
        }

        private fun createChannelIfNeeded() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
                    ?.createNotificationChannel(
                        NotificationChannel(
                            WIDGET_REFRESH_CHANNEL_ID,
                            "위젯 갱신 채널",
                            NotificationManager.IMPORTANCE_LOW
                        )
                    )
            }
        }

        private fun createNotification(): Notification =
            NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                .setChannelId(WIDGET_REFRESH_CHANNEL_ID)
                .build()

        //위젯 갱신 메소드
        private fun updateWidget(updateViews: RemoteViews) {
            val widgetProvider = ComponentName(this, SimpleAirQualityWidgetProvider::class.java)
            AppWidgetManager.getInstance(this).updateAppWidget(widgetProvider, updateViews)
        }
    }

    companion object {
        private const val WIDGET_REFRESH_CHANNEL_ID = "WIDGET_REFRESH_CHANNEL_ID"
        private const val NOTIFICATION_ID = 101

    }
}