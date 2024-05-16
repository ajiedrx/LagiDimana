package com.project.lagidimana.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.project.lagidimana.Const.isBackgroundLocationPermissionEnabled
import com.project.lagidimana.R
import com.project.lagidimana.formatDate
import com.project.lagidimana.presentation.model.LocationLog
import com.project.lagidimana.theme.LagiDimanaTheme
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

class DashboardActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationPermissionRationaleDialog()
            return@registerForActivityResult
        }

        when {
            !isGranted -> {
                showLocationPermissionRequestDeniedDialog()
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }

            else -> {
                if (!isBackgroundLocationPermissionEnabled()) showBackgroundLocationPermissionRequestDialog()
                checkLocationPermission()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LagiDimanaTheme {
                DashboardPage()
            }
        }

        checkLocationPermission()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showLocationPermissionRequestDeniedDialog()
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() = locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun showLocationPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the Location permission, please accept to use location functionality")
            .setPositiveButton("OK") { _, _ -> requestLocationPermission() }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showLocationPermissionRequestDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the Location permission, please enable Location permission on app settings to use this app")
            .setPositiveButton("OK") { _, _ ->
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", this.packageName, null)))
                }
            }
            .setNegativeButton("DECLINE") { _, _ -> finish() }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showBackgroundLocationPermissionRequestDialog() {
        AlertDialog.Builder(this)
            .setTitle("Background Location Permission Needed")
            .setMessage("This app needs the Background Location permission, please allow app to get location all the time")
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", this.packageName, null)))
            }
            .setNegativeButton("DECLINE") { _, _ -> finish() }
            .setCancelable(false)
            .create()
            .show()
    }

    @Composable
    fun DashboardPage() {
        val context = LocalContext.current
        val dashboardVM: DashboardViewModel = koinViewModel()
        val trackingLogList = dashboardVM.getLocationLog().collectAsState(listOf())

        Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp)) {
            Text(
                text = context.getString(R.string.title_dashboard),
                modifier = Modifier.padding(top = 64.dp),
                style = MaterialTheme.typography.headlineMedium
            )

            ServiceStatus(dashboardVM.isServiceRunning) {
                if (isBackgroundLocationPermissionEnabled()) dashboardVM.startService()
                else showBackgroundLocationPermissionRequestDialog()
            }
            TrackingLogList(Modifier.padding(top = 24.dp), trackingLogList.value)
        }
    }

    @Composable
    fun ServiceStatus(isServiceRunningState: StateFlow<Boolean>, onServiceButtonClick: () -> Unit) {
        val context = LocalContext.current
        val isServiceRunning by isServiceRunningState.collectAsState()

        Row(modifier = Modifier.padding(top = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                isServiceRunning -> CircularProgressIndicator(color = Color.Black)

                else -> Button(
                    onClick = { onServiceButtonClick.invoke() }, colors = ButtonColors(
                        containerColor = Color.Black, contentColor = Color.White, disabledContentColor = Color.LightGray,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(text = context.getString(R.string.action_start))
                }
            }

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = if (isServiceRunning) context.getString(R.string.message_service_running) else context.getString(R.string.message_start_service)
            )
        }
    }

    @Composable
    fun TrackingLogList(modifier: Modifier = Modifier, data: List<LocationLog>) {
        LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(data) { log ->
                TrackingLogItem(item = log)
            }
        }
    }

    @Composable
    fun TrackingLogItem(item: LocationLog) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = item.time.formatDate(), style = MaterialTheme.typography.bodyMedium)
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = context.getString(R.string.format_latitude, item.latitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                )
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = context.getString(R.string.format_longitude, item.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                )
            }

            IconButton(
                onClick = { this@DashboardActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", item.latitude, item.longitude)))) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) { Icon(painter = painterResource(id = R.drawable.ic_map), contentDescription = null) }
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