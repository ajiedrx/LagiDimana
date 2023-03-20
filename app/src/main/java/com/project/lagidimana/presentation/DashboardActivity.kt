package com.project.lagidimana.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.lagidimana.R
import com.project.lagidimana.databinding.ActivityDashboardBinding
import com.project.lagidimana.isMyServiceRunning
import com.project.lagidimana.service.location.LocationService
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private var _binding: ActivityDashboardBinding? = null
    private val binding by lazy { _binding!! }

    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val logAdapter: LocationLogAdapter by lazy {
        LocationLogAdapter(
            this,
            onMapsButtonClicked = { latitude: Double, longitude: Double ->
                val uri: String =
                    java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                this.startActivity(intent)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()
        initObserver()
        initAction()
        initUI()
    }

    private fun initUI(){
        val isLocationServiceRunning = isMyServiceRunning(LocationService::class.java)
        with(binding) {
            setUIState(isLocationServiceRunning)
            rvLocationLog.apply {
                adapter = logAdapter
                layoutManager = LinearLayoutManager(this@DashboardActivity)
            }
        }
    }

    private fun initAction(){
        with(binding){
            btnStart.setOnClickListener {
                dashboardViewModel.startService()
            }
        }
    }

    private fun initObserver(){
        dashboardViewModel.getLocationLog().observe(this){
            logAdapter.setData(it)
        }
        dashboardViewModel.isServiceRunning.observe(this){
            setUIState(it)
        }
    }

    private fun setUIState(isServiceRunning: Boolean){
        with(binding){
            tvMessage.text = getString(
                if(isServiceRunning)
                    R.string.message_service_running
                else
                    R.string.message_start_service
            )
            btnStart.visibility = if(isServiceRunning) View.INVISIBLE else View.VISIBLE
            btnStart.isEnabled = !isServiceRunning
            pbCircular.isVisible = isServiceRunning
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        binding.btnStart.isEnabled = true
                    }

                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please enable Location permission on app settings to use this app")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            ) {
                                startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", this.packageName, null),
                                    ),
                                )
                            }
                        }
                        .setNegativeButton(
                            "DECLINE"
                        ) { _, _ ->
                            finish()
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    companion object{
        fun start(context: Context){
            val intent = Intent(context, DashboardActivity::class.java)
            context.startActivity(intent)
        }

        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}