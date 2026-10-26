package com.example.composesample.presentation.example.component.system.security

/**
 * Security State 예제의 순수 로직 — 화면·ViewModel·단위 테스트가 함께 쓴다.
 *
 * androidx.security:security-state 가 주는 값(SPL 문자열, 심각도별 CVE 집합)을 화면용으로 가공한다.
 * 라이브러리 타입(SecurityPatchLevel·Severity)은 ViewModel 에서 문자열로 바꿔 넘겨, 이 파일은 라이브러리 없이도 테스트된다.
 */

/** 화면이 다루는 컴포넌트 — VENDOR 는 1.1.0 에서 internal(USE_VENDOR_SPL=false)이라 뺀다 */
enum class SpComponent(val id: String, val label: String, val dateBased: Boolean) {
    SYSTEM("SYSTEM", "시스템(ro.build.version.security_patch)", dateBased = true),
    SYSTEM_MODULES("SYSTEM_MODULES", "Mainline 모듈(com.google.android.modulemetadata)", dateBased = true),
    KERNEL("KERNEL", "커널(uname -r)", dateBased = false);

    /** getPatchedCves·areCvesPatched 가 받는 컴포넌트(커널은 SPL 이 버전이라 CVE 집계 대상이 아니다) */
    val cveCapable: Boolean get() = this != KERNEL
}

/** 심각도 표시 순서 — SecurityPatchState.Severity 이름과 같다 */
val SEVERITY_ORDER = listOf("CRITICAL", "HIGH", "MODERATE", "LOW")

/** 컴포넌트 한 줄 — 기기 값과 게시 값, 실패하면 이유 */
data class SplRow(
    val component: SpComponent,
    val device: String? = null,
    val deviceError: String? = null,
    val published: List<String> = emptyList(),
    val publishedError: String? = null
)

/**
 * 날짜형 SPL 두 개의 개월 차이(published − device). "YYYY-MM-DD" 의 연·월만 본다.
 * 형식이 아니면 null — 커널 버전 같은 값은 개월로 비교하지 않는다.
 */
fun monthsBehind(device: String, published: String): Int? {
    val d = yearMonth(device) ?: return null
    val p = yearMonth(published) ?: return null
    return (p.first - d.first) * 12 + (p.second - d.second)
}

private fun yearMonth(spl: String): Pair<Int, Int>? {
    val m = Regex("""^(\d{4})-(\d{2})-\d{2}$""").find(spl.trim()) ?: return null
    return m.groupValues[1].toInt() to m.groupValues[2].toInt()
}

/**
 * 게시 SPL 기준으로 고쳐진 CVE 에서 기기 SPL 기준으로 고쳐진 CVE 를 뺀 것 = 이 기기에 아직 없는 수정(심각도별).
 * getPatchedCves 는 보고서의 날짜 ≤ SPL 인 묶음을 모두 모으므로 두 집합의 차이가 '기기 SPL 뒤에 나온 수정'이다.
 */
fun missingFixes(atPublished: Map<String, Set<String>>, atDevice: Map<String, Set<String>>): Map<String, Set<String>> =
    atPublished.mapValues { (severity, cves) -> cves - atDevice[severity].orEmpty() }
        .filterValues { it.isNotEmpty() }

/** "CRITICAL 2 · HIGH 15" — 심각도 순서대로, 0건은 뺀다 */
fun severitySummary(bySeverity: Map<String, Set<String>>): String {
    val parts = SEVERITY_ORDER.mapNotNull { s -> bySeverity[s]?.size?.takeIf { it > 0 }?.let { "$s $it" } } +
        bySeverity.keys.filter { it !in SEVERITY_ORDER && bySeverity[it].orEmpty().isNotEmpty() }.map { "$it ${bySeverity[it]!!.size}" }
    return if (parts.isEmpty()) "없음" else parts.joinToString(" · ")
}

/** 가장 심각한 쪽에서 하나 — areCvesPatched 시연용(결정적으로 고르려고 정렬) */
fun pickOne(bySeverity: Map<String, Set<String>>): String? =
    SEVERITY_ORDER.firstNotNullOfOrNull { s -> bySeverity[s]?.minOrNull() } ?: bySeverity.values.flatten().minOrNull()

/** getGlobalSecurityState Bundle 키를 화면 라벨로 — 모듈·WebView 는 패키지 이름이 키다 */
fun bundleKeyLabel(key: String): String = when (key) {
    "system_spl" -> "시스템 SPL"
    "vendor_spl" -> "벤더 SPL"
    "kernel_version" -> "커널 버전"
    "system_supplemental_security_patches" -> "시스템 보충 패치(CVE)"
    "vendor_supplemental_security_patches" -> "벤더 보충 패치(CVE)"
    else -> "패키지 버전 · $key"
}

/**
 * 기기 커널 계열(major.minor)이 게시된 LTS 목록에 있는가 — 없으면 지원이 끝난 계열이고,
 * isDeviceFullyUpdated 는 같은 계열의 게시 버전만 비교하므로 커널을 '최신'으로 친다.
 */
fun kernelLineSupported(device: String, published: List<String>): Boolean? {
    val line = kernelLine(device) ?: return null
    return published.mapNotNull { kernelLine(it) }.contains(line)
}

private fun kernelLine(version: String): String? =
    Regex("""^(\d+)\.(\d+)""").find(version.trim())?.let { "${it.groupValues[1]}.${it.groupValues[2]}" }

/** 예외를 원인 사슬까지 한 줄로 — debug(HotSwan)가 감싸는 바깥 타입만 보면 이유를 놓친다 */
fun describeError(throwable: Throwable): String {
    val chain = generateSequence(throwable) { it.cause }.take(4).toList()
    val meaningful = chain.lastOrNull { it.message != null } ?: chain.last()
    return "${meaningful.javaClass.simpleName}: ${meaningful.message.orEmpty()}"
}
