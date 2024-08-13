package com.example.composesample.presentation.example.component.version

import android.content.Intent
import android.provider.MediaStore


fun checkPermissionGranted(
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