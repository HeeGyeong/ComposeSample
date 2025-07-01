package com.example.composesample.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.example.composesample.presentation.example.component.system.platform.version.PermissionConstValue

enum class PermissionType {
    MEDIA,
    IMAGE,
    VIDEO,
    STORAGE,
    CAMERA_STORAGE
}

/**
 * 단일 권한이 허용되었는지 확인합니다.
 *
 * @param context 권한 확인에 사용할 컨텍스트.
 * @param permission 확인할 권한.
 * @param onDenied 권한이 거부되었을 때 호출되는 콜백.
 * @param onGranted 권한이 허용되었을 때 호출되는 콜백.
 */
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

/**
 * 주어진 권한 맵을 기반으로 특정 권한 유형이 허용되었는지 확인합니다.
 *
 * @param isGranted 권한과 그 허용 상태의 맵.
 * @param permissionType 확인할 권한 유형.
 * @return 권한 유형이 허용되었으면 true, 아니면 false.
 */
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

/**
 * 미디어 유형 권한이 허용되었는지 확인합니다.
 *
 * @param context 권한 확인에 사용할 컨텍스트.
 * @param permissionType 확인할 미디어 유형 권한.
 * @return 미디어 유형 권한이 허용되었으면 true, 아니면 false.
 */
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

/**
 * 이하 앨범 관련 인텐트
 */
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


/**
 * 이하 OS 버전에 따른 Permission List
 */
val CameraPermission = listOf(Manifest.permission.CAMERA) // 카메라 사용 권한

val ImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    listOf(
        Manifest.permission.READ_MEDIA_IMAGES, // 이미지 읽기 권한
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED // 사용자가 선택한 미디어 읽기 권한
    )
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.READ_MEDIA_IMAGES, // 이미지 읽기 권한
    )
} else {
    listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, // 외부 저장소 읽기 권한
        Manifest.permission.WRITE_EXTERNAL_STORAGE, // 외부 저장소 쓰기 권한
    )
}

val VideoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    listOf(
        Manifest.permission.READ_MEDIA_VIDEO, // 비디오 읽기 권한
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED // 사용자가 선택한 미디어 읽기 권한
    )
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.READ_MEDIA_VIDEO, // 비디오 읽기 권한
    )
} else {
    listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, // 외부 저장소 읽기 권한
        Manifest.permission.WRITE_EXTERNAL_STORAGE, // 외부 저장소 쓰기 권한
    )
}