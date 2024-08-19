package com.example.composesample.presentation.example.component.version

class PermissionConstValue {
    companion object {
        const val Photo = "READ_MEDIA_IMAGES"
        const val Video = "READ_MEDIA_VIDEO"
        const val Camera = "CAMERA"
        const val Record = "RECORD_AUDIO"
        const val MediaSelect = "READ_MEDIA_VISUAL_USER_SELECTED" // targetSDK 34
    }
}