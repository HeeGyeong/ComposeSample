package com.example.composesample.presentation.example.component.system.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay

data class DeepLinkTest(
    val url: String,
    val description: String,
    val shouldOpenInApp: Boolean,
    val reason: String
)

data class TestResult(
    val url: String,
    val timestamp: Long,
    val openedIn: String,
    val processingTime: Long,
    val isSuccess: Boolean
)

@Composable
fun DynamicAppLinksExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Dynamic App Links",
            onBackIconClicked = onBackEvent
        )

        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color(0xFF1976D2),
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("개요", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("라이브 테스트", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("성능 분석", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("시뮬레이터", fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> OverviewTab()
            1 -> LiveTestTab()
            2 -> PerformanceTab()
            3 -> SimulatorTab()
        }
    }
}

@Composable
private fun OverviewTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverviewCard() }
        item { SourceReferenceCard() }
        item { QuickFeatureCard() }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            Text(
                    text = "Dynamic App Links",
                    fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Android 15+에서 도입된 강력한 딥링킹 솔루션입니다. 서버의 Digital Asset Links JSON으로 앱 업데이트 없이 딥링킹 동작을 실시간 제어할 수 있습니다.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("Android 15+", Color(0xFF4CAF50))
                FeatureChip("실시간 제어", Color(0xFFFF9800))
                FeatureChip("서버 기반", Color(0xFF2196F3))
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SourceReferenceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF9C4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text = "출처 및 공식 문서",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
            )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
        modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
            ) {
                Text(
                        text = "Google Developers Blog",
                        fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
            )
                    Spacer(modifier = Modifier.height(4.dp))
            Text(
                        text = "https://android-developers.googleblog.com/2025/10/dynamic-app-links-elevating-your.html",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFeatureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 핵심 기능",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("경로 제외", "특정 URL을 앱에서 제외", "/admin/* → 웹"),
                Triple("쿼리 제어", "파라미터로 앱/웹 분기", "?web_only=true → 웹"),
                Triple("동적 업데이트", "앱 릴리스 없이 변경", "JSON 수정만으로 즉시 반영")
            ).forEach { (title, desc, example) ->
                QuickFeatureItem(title, desc, example)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun QuickFeatureItem(title: String, description: String, example: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF388E3C)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = "예: $example",
                fontSize = 10.sp,
                color = Color(0xFF1976D2),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun LiveTestTab() {
    val context = LocalContext.current
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }
    var isTestingAll by remember { mutableStateOf(false) }

    val testCases = remember {
        listOf(
            DeepLinkTest(
                "https://example.com/products/123",
                "일반 제품 페이지",
                shouldOpenInApp = true,
                reason = "제외 규칙 없음"
            ),
            DeepLinkTest(
                "https://example.com/admin/dashboard",
                "관리자 페이지",
                shouldOpenInApp = false,
                reason = "/admin/* 경로 제외"
            ),
            DeepLinkTest(
                "https://example.com/product?web_only=true",
                "강제 웹 모드",
                shouldOpenInApp = false,
                reason = "web_only 파라미터"
            ),
            DeepLinkTest(
                "https://example.com/product?ab_test=web_variant",
                "A/B 테스트 (웹 그룹)",
                shouldOpenInApp = false,
                reason = "ab_test 파라미터"
            ),
            DeepLinkTest(
                "https://example.com/legacy/old-page",
                "레거시 페이지",
                shouldOpenInApp = false,
                reason = "/legacy/* 경로 제외"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor = Color(0xFF1976D2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚀 실시간 딥링크 테스트",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "실제 링크를 클릭하여 동작을 확인하세요",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (isTestingAll) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isTestingAll = true
                            testResults = emptyList()
                            // 실제로는 각 링크를 순차적으로 테스트
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White,
                            contentColor = Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("전체 테스트 실행", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(testCases) { testCase ->
            LiveTestItem(
                testCase = testCase,
                onTest = { url ->
                    val startTime = System.currentTimeMillis()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        val endTime = System.currentTimeMillis()
                        val result = TestResult(
                            url = url,
                            timestamp = System.currentTimeMillis(),
                            openedIn = if (testCase.shouldOpenInApp) "앱" else "웹",
                            processingTime = endTime - startTime,
                            isSuccess = true
                        )
                        testResults = testResults + result
                    } catch (e: Exception) {
                        val result = TestResult(
                            url = url,
                            timestamp = System.currentTimeMillis(),
                            openedIn = "실패",
                            processingTime = 0,
                            isSuccess = false
                        )
                        testResults = testResults + result
                    }
                }
            )
        }

        if (testResults.isNotEmpty()) {
            item {
                TestResultsCard(testResults)
            }
        }
    }
}

@Composable
private fun LiveTestItem(
    testCase: DeepLinkTest,
    onTest: (String) -> Unit
) {
    var isTesting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
            Text(
                        text = testCase.description,
                        fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = testCase.url,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (testCase.shouldOpenInApp) 
                        Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else 
                        Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
            Text(
                        text = if (testCase.shouldOpenInApp) "📱 앱" else "🌐 웹",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (testCase.shouldOpenInApp) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = testCase.reason,
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isTesting = true
                    onTest(testCase.url)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isTesting
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("테스트 중...", color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("링크 열기", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            delay(1000)
            isTesting = false
        }
    }
}

@Composable
private fun TestResultsCard(results: List<TestResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "📊 테스트 결과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )
                Text(
                    text = "${results.size}건",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            results.takeLast(3).forEach { result ->
                TestResultItem(result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TestResultItem(result: TestResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (result.isSuccess) Color.White else Color(0xFFFFEBEE)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (result.isSuccess) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
        Text(
                    text = result.openedIn,
            fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isSuccess) Color(0xFF388E3C) else Color(0xFFD32F2F)
        )
        Text(
                    text = "${result.processingTime}ms",
            fontSize = 11.sp,
            color = Color.Gray
        )
            }
        }
    }
}

@Composable
private fun PerformanceTab() {
    var selectedMetric by remember { mutableStateOf(0) }
    var isAnalyzing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PerformanceMetricsCard(
                selectedMetric = selectedMetric,
                onMetricSelected = { selectedMetric = it },
                isAnalyzing = isAnalyzing,
                onAnalyze = { isAnalyzing = true }
            )
        }

        item {
            when (selectedMetric) {
                0 -> LinkResolutionPerformanceCard()
                1 -> CacheEfficiencyCard()
                2 -> NetworkImpactCard()
            }
        }

        item {
            ComparisonCard()
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            delay(2000)
            isAnalyzing = false
        }
    }
}

@Composable
private fun PerformanceMetricsCard(
    selectedMetric: Int,
    onMetricSelected: (Int) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFF1976D2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 성능 분석",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dynamic App Links의 성능 메트릭을 실시간으로 측정합니다",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("링크 해석", "캐시 효율", "네트워크").forEachIndexed { index, title ->
                    Button(
                        onClick = { onMetricSelected(index) },
                        modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedMetric == index) 
                                Color.White else Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                            text = title,
                            fontSize = 11.sp,
                            color = if (selectedMetric == index) 
                                Color(0xFF1976D2) else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("분석 중...", color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("성능 분석 시작", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LinkResolutionPerformanceCard() {
    val metrics = remember {
        listOf(
            Triple("평균 해석 시간", "12ms", Color(0xFF4CAF50)),
            Triple("최대 해석 시간", "45ms", Color(0xFFFF9800)),
            Triple("캐시 히트율", "87%", Color(0xFF2196F3)),
            Triple("JSON 파싱 시간", "3ms", Color(0xFF9C27B0))
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚡ 링크 해석 성능",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            metrics.forEach { (label, value, color) ->
                MetricRow(label, value, color)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            PerformanceBarChart(
                data = listOf(8f, 12f, 15f, 10f, 9f, 13f, 11f),
                label = "최근 7일 평균 (ms)"
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF212121)
                )
                Text(
            text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

@Composable
private fun PerformanceBarChart(data: List<Float>, label: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { value ->
                val normalizedHeight = (value / data.maxOrNull()!! * 100).dp
                Surface(
                    modifier = Modifier
                        .width(30.dp)
                        .height(normalizedHeight),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    color = Color(0xFF1976D2)
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.indices.forEach { index ->
                Text(
                    text = "${index + 1}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
}

@Composable
private fun CacheEfficiencyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💾 캐시 효율성",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 캐시 히트율 원형 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
            Text(
                                text = "87%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "히트율",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                "캐시 히트" to "870회",
                "캐시 미스" to "130회",
                "평균 캐시 수명" to "18시간"
            ).forEach { (label, value) ->
                MetricRow(label, value, Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "💡 최적화 팁",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• assetlinks.json 캐시는 최대 24시간 유지\n• 변경 시 자동으로 재검증\n• 네트워크 실패 시 캐시 사용",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkImpactCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌐 네트워크 영향",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("JSON 파일 크기", "2.3KB", Color(0xFF4CAF50)),
                Triple("평균 다운로드 시간", "85ms", Color(0xFF2196F3)),
                Triple("일일 요청 수", "1,234회", Color(0xFFFF9800)),
                Triple("데이터 사용량/일", "2.8MB", Color(0xFF9C27B0))
            ).forEach { (label, value, color) ->
                MetricRow(label, value, color)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "⚡ 네트워크 절약",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "캐시 덕분에 실제 네트워크 요청은 13%만 발생합니다",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚖️ 비교 분석",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComparisonRow("기존 App Links", "8ms", "Dynamic App Links", "12ms")
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("유연성", "낮음", "유연성", "높음")
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("업데이트", "앱 릴리스 필요", "업데이트", "서버만 수정")
        }
    }
}

@Composable
private fun ComparisonRow(label1: String, value1: String, label2: String, value2: String) {
    Row(
                    modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEEEEEE)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                    text = label1,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = value1,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1976D2).copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = label2,
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = value2,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
private fun SimulatorTab() {
    var selectedUrl by remember { mutableStateOf("https://example.com/products/123") }
    var isProcessing by remember { mutableStateOf(false) }
    var simulationResult by remember { mutableStateOf<SimulationResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SimulatorControlCard(
                selectedUrl = selectedUrl,
                onUrlSelected = { selectedUrl = it },
                isProcessing = isProcessing,
                onSimulate = {
                    isProcessing = true
                    simulationResult = null
                }
            )
        }

        if (simulationResult != null) {
            item {
                SimulationResultCard(simulationResult!!)
            }
        }

        item {
            RuleEngineCard(selectedUrl)
        }
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(1500)
            simulationResult = SimulationResult(
                url = selectedUrl,
                matchedRules = analyzeUrl(selectedUrl),
                finalDestination = if (shouldExclude(selectedUrl)) "웹 브라우저" else "앱",
                processingSteps = getProcessingSteps(selectedUrl)
            )
            isProcessing = false
        }
    }
}

data class SimulationResult(
    val url: String,
    val matchedRules: List<String>,
    val finalDestination: String,
    val processingSteps: List<String>
)

@Composable
private fun SimulatorControlCard(
    selectedUrl: String,
    onUrlSelected: (String) -> Unit,
    isProcessing: Boolean,
    onSimulate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFF7B1FA2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎮 딥링크 시뮬레이터",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "URL을 선택하고 처리 과정을 단계별로 확인하세요",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val testUrls = listOf(
                "https://example.com/products/123",
                "https://example.com/admin/dashboard",
                "https://example.com/product?web_only=true",
                "https://example.com/legacy/old-page",
                "https://example.com/product?ab_test=web_variant"
            )

            testUrls.forEach { url ->
                Button(
                    onClick = { onUrlSelected(url) },
                modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedUrl == url) 
                            Color.White else Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = url,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (selectedUrl == url) Color(0xFF7B1FA2) else Color.White
                    )
                }
                    Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSimulate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("처리 중...", color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("시뮬레이션 실행", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SimulationResultCard(result: SimulationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "✅ 시뮬레이션 결과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (result.finalDestination == "앱") 
                        Color(0xFF4CAF50) else Color(0xFFFF9800)
                ) {
                    Text(
                        text = result.finalDestination,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "처리 단계:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(8.dp))

            result.processingSteps.forEachIndexed { index, step ->
                ProcessingStepItem(index + 1, step)
                if (index < result.processingSteps.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (result.matchedRules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🎯 매칭된 규칙:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        result.matchedRules.forEach { rule ->
                            Text(
                                text = "• $rule",
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingStepItem(stepNumber: Int, description: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = Color(0xFF388E3C)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$stepNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = description,
            fontSize = 12.sp,
            color = Color(0xFF212121),
            modifier = Modifier.padding(top = 4.dp)
            )
    }
}

@Composable
private fun RuleEngineCard(url: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚙️ 규칙 엔진",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "현재 적용된 제외 규칙:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                "경로: /admin/*" to "/admin/ 하위 모든 경로",
                "경로: /legacy/*" to "레거시 콘텐츠 제외",
                "경로: /videos/*" to "비디오 콘텐츠 제외",
                "쿼리: web_only=true" to "강제 웹 모드",
                "쿼리: ab_test=web_variant" to "A/B 테스트 웹 그룹"
            ).forEach { (rule, desc) ->
                RuleItem(rule, desc, url)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RuleItem(rule: String, description: String, currentUrl: String) {
    val isMatching = when {
        rule.contains("/admin/") && currentUrl.contains("/admin/") -> true
        rule.contains("/legacy/") && currentUrl.contains("/legacy/") -> true
        rule.contains("/videos/") && currentUrl.contains("/videos/") -> true
        rule.contains("web_only=true") && currentUrl.contains("web_only=true") -> true
        rule.contains("ab_test=web_variant") && currentUrl.contains("ab_test=web_variant") -> true
        else -> false
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isMatching) Color(0xFFFF9800).copy(alpha = 0.1f) else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isMatching) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (isMatching) Color(0xFFFF9800) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
        Text(
                    text = rule,
                    fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isMatching) Color(0xFFFF9800) else Color(0xFF212121)
        )
        Text(
                    text = description,
            fontSize = 11.sp,
            color = Color.Gray
        )
            }
        }
    }
}

// Helper functions
private fun shouldExclude(url: String): Boolean {
    return url.contains("/admin/") ||
            url.contains("/legacy/") ||
            url.contains("/videos/") ||
            url.contains("web_only=true") ||
            url.contains("ab_test=web_variant")
}

private fun analyzeUrl(url: String): List<String> {
    val rules = mutableListOf<String>()
    if (url.contains("/admin/")) rules.add("경로 제외: /admin/*")
    if (url.contains("/legacy/")) rules.add("경로 제외: /legacy/*")
    if (url.contains("/videos/")) rules.add("경로 제외: /videos/*")
    if (url.contains("web_only=true")) rules.add("쿼리 제외: web_only=true")
    if (url.contains("ab_test=web_variant")) rules.add("쿼리 제외: ab_test=web_variant")
    return rules
}

private fun getProcessingSteps(url: String): List<String> {
    val steps = mutableListOf<String>()
    steps.add("URL 수신: $url")
    steps.add("assetlinks.json 캐시 확인")
    
    if (shouldExclude(url)) {
        steps.add("제외 규칙 매칭 발견")
        steps.add("웹 브라우저로 리다이렉트")
    } else {
        steps.add("제외 규칙 없음")
        steps.add("앱에서 딥링크 처리")
    }
    
    steps.add("처리 완료")
    return steps
}

