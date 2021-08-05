package kr.ac.konkuk.finedust

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kr.ac.konkuk.finedust.data.Repository
import kr.ac.konkuk.finedust.data.models.airquality.Grade
import kr.ac.konkuk.finedust.data.models.airquality.MeasuredValue
import kr.ac.konkuk.finedust.data.models.monitoringstation.MonitoringStation
import kr.ac.konkuk.finedust.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val scope = MainScope()

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        initVariables()
        requestLocationPermission()

    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        //습관적으로 작성해줄 것
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

//        val locationPermissionGranted =
//            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED //접근이 허용되었다.
        val locationPermissionGranted =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED


        if(!locationPermissionGranted) {
            finish()
        } else {
            //권한이 있을 경우에
            fetchAirQualityData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {
        //fetchData
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient
            .getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
            //location 자체가 task 를 반환하므로
            //location 은 nullable
        ).addOnSuccessListener { location ->
//            binding.textView.text = "${location.latitude}, ${location.longitude}"
            scope.launch {
                binding.errorDescriptionTextView.visibility = View.GONE
                try{
                    val monitoringStation =
                        Repository.getNearbyMonitoringStation(location.longitude, location.latitude)

//                binding.textView.text = monitoringStation?.stationName
                    val measuredValue =
                        Repository.getLatestAirQualityData(monitoringStation!!.stationName!!)

//                binding.textView.text = measuredValue.toString()

                    displayAirQualityData(monitoringStation, measuredValue!!)
                } catch (exception: Exception) {
                    //모든 exception 을 처리
                    binding.errorDescriptionTextView.visibility = View.VISIBLE
                    //정상적으로 data 를 받아왔다가 refresh 하고 exception 이 발생하였을 때 기존의 contents 가 밑에 깔려 겹칠 수 있음
                    binding.contentsLayout.alpha = 0F
                }
                finally {
                    binding.progressBar.visibility = View.GONE
                    binding.refresh.isRefreshing = false
                }

            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun displayAirQualityData(monitoringStation: MonitoringStation, measuredValue: MeasuredValue) {
        //fade in
        binding.contentsLayout.animate()
            .alpha(1F)
            .start()

        binding.measuringStationNameTextView.text = monitoringStation.stationName
        binding.measuringStationAddressTextView.text = "측정소 위치: ${monitoringStation.addr}"

        //data 를 가져오지 못해 null 값이 반환된 경우 이를 unknown 으로 바꾸는 과정이 필요
        (measuredValue.khaiGrade ?: Grade.UNKNOWN).let { grade ->
            binding.root.setBackgroundResource(grade.colorResId)
            binding.totalGradeLabelTextView.text = grade.label
            binding.totalGradleEmojiTextView.text = grade.emoji
        }

        with(measuredValue) {
            binding.fineDustInformationTextView.text =
                "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: Grade.UNKNOWN).emoji}"
            binding.ultraFineDustInformationTextView.text =
                "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: Grade.UNKNOWN).emoji}"

            with(binding.so2Item) {
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }

            with(binding.coItem) {
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$coValue ppm"
            }

            with(binding.o3Item) {
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$o3Value ppm"
            }

            with(binding.no2Item) {
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$no2Value ppm"
            }
        }
    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            fetchAirQualityData()
        }
    }

    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}