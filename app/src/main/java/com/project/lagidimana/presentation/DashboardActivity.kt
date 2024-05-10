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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.lagidimana.R
import com.project.lagidimana.databinding.ActivityDashboardBinding
import com.project.lagidimana.formatDate
import com.project.lagidimana.presentation.model.LocationLog
import com.project.lagidimana.theme.LagiDimanaTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class DashboardActivity : ComponentActivity() {

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
//        _binding = ActivityDashboardBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        setContent {
            LagiDimanaTheme {
                DashboardPage()
            }
        }

        checkLocationPermission()
        initObserver()
        initAction()
        initUI()
    }

    private fun initUI() {
        with(binding) {
            rvLocationLog.apply {
                adapter = logAdapter
                layoutManager = LinearLayoutManager(this@DashboardActivity)
            }
        }
    }

    private fun initAction() {
        with(binding) {
            btnStart.setOnClickListener {
                dashboardViewModel.startService()
            }
        }
    }

    private fun initObserver() {
//        dashboardViewModel.getLocationLog().observe(this) {
//            logAdapter.setData(it)
//        }
//
//        dashboardViewModel.isServiceRunning.observe(this) {
//            setUIState(it)
//        }
    }

    private fun setUIState(isServiceRunning: Boolean) {
        with(binding) {
            tvMessage.text = getString(
                if (isServiceRunning)
                    R.string.message_service_running
                else
                    R.string.message_start_service
            )
            btnStart.visibility = if (isServiceRunning) View.INVISIBLE else View.VISIBLE
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
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

    @Composable
    fun DashboardPage() {
        val trackingLogList = dashboardViewModel.getLocationLog().collectAsState(listOf())

        Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp)) {
            Text(
                text = getString(R.string.title_dashboard),
                modifier = Modifier.padding(top = 64.dp),
                style = MaterialTheme.typography.headlineMedium
            )

            ServiceOperations()


            TrackingLogList(trackingLogList)
        }
    }

    @Composable
    fun ServiceOperations() {
        val dashboardVM = this@DashboardActivity.dashboardViewModel
        val isServiceRunningState by dashboardVM.isServiceRunning.collectAsState()

        Row(modifier = Modifier.padding(top = 32.dp)) {
            when {
                isServiceRunningState -> CircularProgressIndicator(color = Color.Black)

                else -> Button(onClick = { dashboardVM.startService() }) {
                    Text(text = getString(R.string.action_start))
                }
            }

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = if (isServiceRunningState) getString(R.string.message_service_running) else getString(
                    R.string.message_start_service
                )
            )
        }
    }

    @Composable
    fun TrackingLogList(data: State<List<LocationLog>>) {
        LazyColumn {
            items(data.value) { log ->
                TrackingLogItem(item = log)
            }
        }
    }

    @Composable
    fun TrackingLogItem(item: LocationLog) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = item.time.formatDate(), style = MaterialTheme.typography.bodySmall)
                Text(modifier = Modifier.padding(top = 12.dp), text = getString(R.string.format_latitude, item.latitude), style = MaterialTheme.typography.labelSmall)
                Text(modifier = Modifier.padding(top = 12.dp), text = getString(R.string.format_longitude, item.longitude), style = MaterialTheme.typography.labelSmall)
            }

            IconButton(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        java.lang.String.format(
                            Locale.ENGLISH,
                            "geo:%f,%f",
                            item.latitude,
                            item.longitude
                        )
                    )
                )
                this@DashboardActivity.startActivity(intent)
            }, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(painter = painterResource(id = R.drawable.ic_map), contentDescription = null)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DashboardPreview() {
        LagiDimanaTheme {
            DashboardPage()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DashboardActivity::class.java)
            context.startActivity(intent)
        }

        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}