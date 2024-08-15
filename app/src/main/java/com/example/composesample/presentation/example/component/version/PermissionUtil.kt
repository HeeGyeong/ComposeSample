package com.example.composesample.presentation.example.component.version

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat

enum class PermissionType {
    MEDIA,
    IMAGE,
    VIDEO,
    STORAGE,
    CAMERA_STORAGE
}

fun checkSinglePermissionGranted(
    context: Context,
    permission: String,
    onDenied: () -> Unit,
    onGranted: () -> Unit,
) {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        onGranted.invoke()
    } else {
        onDenied.invoke()
    }
}

fun checkPermissionTypeGranted(
    isGranted: Map<String, @JvmSuppressWildcards Boolean>,
    permissionType: String,
): Boolean {
    var isPermissionGranted = false
    if (isGranted.isNotEmpty()) {
        when (permissionType) {
            PermissionConstValue.Photo -> {
                var usePermission = false

                for (item in isGranted) {
                    if (item.key.contains(PermissionConstValue.Photo)
                        || item.key.contains(PermissionConstValue.MediaSelect)
                    ) {
                        if (!usePermission) {
                            usePermission =
                                (item.key.contains(PermissionConstValue.Photo) && item.value)
                                        || (item.key.contains(PermissionConstValue.MediaSelect) && item.value)
                        }
                    }
                }

                isPermissionGranted = usePermission
            }

            PermissionConstValue.Video -> {
                var usePermission = false

                for (item in isGranted) {
                    if (item.key.contains(PermissionConstValue.Video)
                        || item.key.contains(PermissionConstValue.MediaSelect)
                    ) {
                        if (!usePermission) {
                            usePermission =
                                (item.key.contains(PermissionConstValue.Video) && item.value)
                                        || (item.key.contains(PermissionConstValue.MediaSelect) && item.value)
                        }
                    }
                }

                isPermissionGranted = usePermission
            }

            PermissionConstValue.Camera -> {
                var usePermission = false

                for (item in isGranted) {
                    if (item.key.contains(PermissionConstValue.Camera)
                        || item.key.contains(PermissionConstValue.MediaSelect)
                    ) {
                        if (!usePermission) {
                            usePermission =
                                (item.key.contains(PermissionConstValue.Camera) && item.value)
                                        || (item.key.contains(PermissionConstValue.MediaSelect) && item.value)
                        }
                    }
                }

                isPermissionGranted = usePermission
            }

            PermissionConstValue.Record -> {
                var usePermission = false

                for (item in isGranted) {
                    if (item.key.contains(PermissionConstValue.Record)) {
                        if (!usePermission) {
                            usePermission =
                                (item.key.contains(PermissionConstValue.Record) && item.value)
                        }
                    }
                }

                isPermissionGranted = usePermission
            }
        }
    } else {
        isPermissionGranted = false
    }

    return isPermissionGranted
}

fun checkMediaTypePermissionGranted(
    context: Context,
    permissionType: PermissionType,
): Boolean {
    var selectPermission = false
    when (permissionType) {
        PermissionType.MEDIA -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                selectPermission = true
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectPermission = true
            } else {
                selectPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }

        PermissionType.IMAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES,
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                selectPermission = true
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
                || (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES,
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                selectPermission = true
            } else {
                selectPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }

        PermissionType.VIDEO -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO,
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                selectPermission = true
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
                || (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO,
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                selectPermission = true
            } else {
                selectPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }

        PermissionType.STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectPermission = true
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectPermission = true
            } else {
                selectPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }

        PermissionType.CAMERA_STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectPermission = true
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectPermission = true
            } else {
                selectPermission =
                    (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    return selectPermission
}

val imageAlbumIntent =
    Intent(Intent.ACTION_PICK).apply {
        type = MediaStore.Images.Media.CONTENT_TYPE
        data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/jpeg", "image/png", "image/bmp", "image/webp")
        )
    }

val videoAlbumIntent =
    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "video/*"
        putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("video/webm", "video/ogg")
        )
    }


val CameraPermission = listOf(Manifest.permission.CAMERA)

val ImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
    )
} else {
    listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}

val VideoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    listOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.READ_MEDIA_VIDEO,
    )
} else {
    listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}