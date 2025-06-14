package com.example.composesample.util

import android.os.Build

/**
 * 에뮬레이터 감지를 위한 유틸리티 클래스
 */
object EmulatorUtil {
    /**
     * 현재 디바이스가 에뮬레이터인지 확인
     * @return 에뮬레이터 여부
     */
    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) // 기본 Android 에뮬레이터
                || Build.FINGERPRINT.startsWith("generic") // 제네릭 에뮬레이터
                || Build.FINGERPRINT.startsWith("unknown") // 알 수 없는 에뮬레이터
                || Build.HARDWARE.contains("goldfish") // Android 에뮬레이터 하드웨어
                || Build.HARDWARE.contains("ranchu") // Android 에뮬레이터 하드웨어
                || Build.HARDWARE.contains("vbox") // VirtualBox 기반 에뮬레이터
                || Build.HARDWARE.contains("qemu") // QEMU 에뮬레이터
                || Build.HARDWARE.contains("intel") // Intel 기반 에뮬레이터
                || Build.HARDWARE.contains("amd") // AMD 기반 에뮬레이터
                || Build.MODEL.contains("google_sdk") // Google SDK 에뮬레이터
                || Build.MODEL.contains("Emulator") // 일반 에뮬레이터
                || Build.MODEL.contains("Android SDK built for x86") // x86 SDK 에뮬레이터
                || Build.MODEL.contains("Android SDK built for arm") // ARM 기반 SDK 에뮬레이터
                || Build.MODEL.contains("Android SDK built for arm64") // ARM64 기반 SDK 에뮬레이터
                || Build.MODEL.contains("Android SDK built for x86_64") // x86_64 기반 SDK 에뮬레이터
                || Build.MODEL.contains("generic_x86") // x86 제네릭 에뮬레이터
                || Build.MANUFACTURER.contains("Genymotion") // Genymotion 에뮬레이터
                || Build.MANUFACTURER.contains("unknown") // 알 수 없는 제조사 에뮬레이터
                || Build.MANUFACTURER.contains("Google") // Google 제조 에뮬레이터
                || Build.MANUFACTURER.contains("Android") // Android 제조 에뮬레이터
                || Build.PRODUCT.contains("sdk_gphone") // Google Phone 에뮬레이터
                || Build.PRODUCT.contains("google_sdk") // Google SDK 에뮬레이터
                || Build.PRODUCT.contains("sdk") // 일반 SDK 에뮬레이터
                || Build.PRODUCT.contains("sdk_x86") // x86 SDK 에뮬레이터
                || Build.PRODUCT.contains("sdk_x86_64") // 64비트 SDK 에뮬레이터
                || Build.PRODUCT.contains("vbox86p") // VirtualBox x86 에뮬레이터
                || Build.PRODUCT.contains("emulator") // 일반 에뮬레이터
                || Build.PRODUCT.contains("simulator") // 시뮬레이터
                || Build.PRODUCT.contains("x86_64") // 64비트 에뮬레이터
                || Build.PRODUCT.contains("arm64-v8a") // ARM64 아키텍처 에뮬레이터
                || Build.PRODUCT.contains("armv7") // ARMv7 아키텍처 에뮬레이터
                || Build.PRODUCT.contains("qemu") // QEMU 에뮬레이터
                || Build.BOARD.contains("goldfish") // Android 에뮬레이터 보드
                || Build.BOOTLOADER.contains("unknown") // 알 수 없는 부트로더
                || Build.DEVICE.contains("generic_arm") // ARM 제네릭 디바이스
                || Build.DEVICE.contains("generic_arm64") // ARM64 제네릭 디바이스
    }
} 