package com.example.composesample.presentation.example.component.version

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TargetSDK34Example(
    onBackButtonClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                onBackButtonClick.invoke()
                            }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "")
                        }
                    }
                }
            }
        }

        item {
            Button(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                        Uri.parse(
                            "package:${context.packageName}"
                        )
                    )
                )
            }) {
                Text(text = "Go to Permission Setting Screen")
            }
        }
    }
}

@Composable
fun PermissionButton() {
    val permissionType = remember { mutableStateOf("") }
    val permissionDialog = remember { mutableStateOf(false) }

    val addPhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.data?.let { uri ->
                        uri.let {
                            // Logic
                        }
                    } ?: run {
                        Unit
                    }
                }

                Activity.RESULT_CANCELED -> Unit
            }
        }

    val addCameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { _ ->
                // logic
            } ?: run {
                Unit
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.data?.let { uri ->
                        // logic
                    }
                }

                Activity.RESULT_CANCELED -> Unit
            }
        }

    // sdk 34 permission Logic
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        val isGrantedPermission = checkPermissionGranted(
            isGranted = isGranted,
            permissionType = permissionType.value
        )

        if (!isGrantedPermission) {
            permissionDialog.value = true
        } else {
            when (permissionType.value) {
                PermissionConstValue.Photo -> {
                    addPhotoLauncher.launch(imageAlbumIntent)
                }

                PermissionConstValue.Video -> {
                    videoLauncher.launch(videoAlbumIntent)
                }

                PermissionConstValue.Camera -> {
                    addCameraLauncher.launch()
                }
            }
        }
    }
}