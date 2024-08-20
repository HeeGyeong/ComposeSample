package com.example.composesample.presentation.example.component.version

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.composesample.util.component.PermissionDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TargetSDK34Example(
    onBackButtonClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val permissionType = remember { mutableStateOf("") }
    val permissionDialog = remember { mutableStateOf(false) }

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
            PermissionButton(
                context = context,
                permissionDialog = permissionDialog,
                permissionType = permissionType,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (permissionDialog.value) {
            PermissionDialog(
                context = context,
                permissionDialog = permissionDialog,
                permissionText = when (permissionType.value) {
                    PermissionConstValue.Camera -> {
                        "카메라 권한"
                    }

                    else -> {
                        "사진 및 동영상 권한"
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionButton(
    context: Context,
    permissionType: MutableState<String>,
    permissionDialog: MutableState<Boolean>,
) {
    var selectPermission by rememberSaveable { mutableStateOf(false) }

    val albumLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.data?.let { uri ->
                        uri.let {
                            // Logic
                            Log.d("TargetSDK", "imageUri - selected : $uri")
                        }
                    }
                }

                Activity.RESULT_CANCELED -> Unit
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { _ ->
                // logic
                Log.d("TargetSDK", "photoBitmap - photo : $bitmap")
            } ?: run {
                Unit
            }
        }

    // sdk 34 permission Logic
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedPermissionMap ->
        val isGrantedPermission = checkPermissionTypeGranted(
            isGranted = grantedPermissionMap,
            permissionType = permissionType.value
        )

        if (!isGrantedPermission) {
            permissionDialog.value = true
        } else {
            when (permissionType.value) {
                PermissionConstValue.Photo -> {
                    albumLauncher.launch(imageAlbumIntent)
                }

                PermissionConstValue.Video -> {
                    albumLauncher.launch(videoAlbumIntent)
                }

                PermissionConstValue.Camera -> {
                    cameraLauncher.launch()
                }
            }
        }
    }

    Column {
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            permissionType.value =
                PermissionConstValue.Photo

            selectPermission = checkMediaTypePermissionGranted(
                context = context,
                permissionType = PermissionType.IMAGE
            )

            if (selectPermission) {
                albumLauncher.launch(imageAlbumIntent)
            } else {
                storagePermissionLauncher.launch(
                    ImagePermission.toTypedArray()
                )
            }
        }) {
            Text(text = "Photo")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            permissionType.value =
                PermissionConstValue.Camera

            checkSinglePermissionGranted(
                context,
                Manifest.permission.CAMERA,
                onDenied = {
                    storagePermissionLauncher.launch(
                        CameraPermission.toTypedArray()
                    )
                },
                onGranted = {
                    cameraLauncher.launch()
                }
            )
        }) {
            Text(text = "Camera")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            permissionType.value =
                PermissionConstValue.Video

            selectPermission = checkMediaTypePermissionGranted(
                context = context,
                permissionType = PermissionType.VIDEO
            )

            if (selectPermission) {
                albumLauncher.launch(videoAlbumIntent)
            } else {
                storagePermissionLauncher.launch(
                    VideoPermission.toTypedArray()
                )
            }
        }) {
            Text(text = "Video")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}