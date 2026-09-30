package com.example.composesample.util

import android.app.Application
import android.content.Context
import android.os.Build
import java.io.File

/**
 * 현재 프로세스 이름.
 * - API 28+ : `Application.getProcessName()` — IPC 없이 바로 돌려준다
 * - 그 아래  : `/proc/self/cmdline` — 프로세스가 뜰 때 ActivityThread 가 argv[0] 을 프로세스 이름으로 바꿔 두므로 같은 값이 나온다.
 *   단, zygote 의 argv 영역 길이에서 잘린다(Galaxy A72/API 33 실측 98자). 격리 프로세스처럼 이름에 서비스 클래스명이 붙어
 *   길어지면 끝이 잘리지만, 짧은 기본 프로세스 이름과 같은지만 보는 [isMainProcess] 판정에는 영향이 없다.
 *   `ActivityManager.getRunningAppProcesses()` 에서 자기 PID 를 찾는 방법도 있지만 IPC 가 들고,
 *   isolatedProcess 에서는 SecurityException 으로 막힌다(실측).
 */
fun currentProcessName(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        processNameFromProcFs()
    }

/** `/proc/self/cmdline` 의 첫 인자(argv[0]). 인자는 NUL 로 구분되므로 첫 NUL 앞까지만 쓴다 */
fun processNameFromProcFs(): String? =
    runCatching { File("/proc/self/cmdline").readText().substringBefore('\u0000').trim() }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }

/**
 * 기본(메인) 프로세스인지. 기본 프로세스 이름은 `applicationInfo.processName`(따로 지정하지 않으면 패키지명)이다.
 * 이름을 읽지 못하면 true — 판정 실패로 메인 프로세스의 초기화를 건너뛰는 쪽이 더 위험하다.
 */
fun Context.isMainProcess(): Boolean {
    val name = currentProcessName() ?: return true
    return name == applicationInfo.processName
}
