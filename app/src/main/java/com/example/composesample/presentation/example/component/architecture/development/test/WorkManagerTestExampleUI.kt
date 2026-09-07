package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * WorkManager 테스트 하네스 예제
 * - androidx.work:work-testing 의 세 도구(테스트 모드 초기화 / TestDriver / 격리 실행 빌더)를 대조한다.
 * - 제약·초기 지연을 **실제로 만족시키지 않고** 통과시키는 방법과, 그때 상태가 어떻게 바뀌는지를
 *   실기기에서 측정한 값으로 보여준다.
 * - 이 화면이 설명하는 코드는 `app/src/androidTest/.../WorkManagerTestExampleTest.kt` 에 실제로 들어 있고,
 *   5개 테스트가 모두 통과한 상태다. 화면이 실행 주체가 아닌 이유는 6번 카드 첫 항목 참조.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

@Composable
fun WorkManagerTestExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "WorkManager 테스트 하네스",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WhyHardCard() }
            item { TestRuleCard() }
            item { ConstraintGatingCard() }
            item { DelayCard() }
            item { IsolationCard() }
            item { WorkTestPitfallCard() }
        }
    }
}

// ==================== 1. 무엇이 어려운가 ====================

@Composable
private fun WhyHardCard() {
    WorkTestSectionCard(title = "1. WorkManager 는 왜 테스트하기 어려운가") {
        BodyText(
            "작업이 언제 실행될지를 정하는 주체가 앱이 아니라 시스템이다. 충전 중·네트워크 연결 같은 " +
                "제약은 실제로 그 조건이 될 때까지 실행되지 않고, 주기 작업의 최소 간격은 15분이다. " +
                "테스트에서 이걸 그대로 기다릴 수는 없다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        TableRow("도구", "무엇을 대신해 주는가", isHeader = true)
        TableRow("initializeTestWorkManager", "실제 스케줄러 대신 테스트용 구현으로 교체")
        TableRow("SynchronousExecutor", "작업을 호출 스레드에서 즉시 실행 → 대기/idling 코드가 사라짐")
        TableRow("TestDriver", "제약·초기 지연·주기 지연을 \"충족됨\"으로 표시")
        TableRow("TestListenableWorkerBuilder", "WorkManager 없이 워커 하나만 떼어내 실행")

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "의존성은 한 줄이면 된다 — androidTestImplementation(\"androidx.work:work-testing\"). " +
                "런타임 work 와 같은 버전을 쓰면 되고, 이 프로젝트는 상향 없이 2.9.1 을 그대로 썼다."
        )
    }
}

// ==================== 2. 규칙으로 보일러플레이트 걷어내기 ====================

@Composable
private fun TestRuleCard() {
    WorkTestSectionCard(title = "2. 초기화는 규칙 하나로 끝낸다") {
        BodyText(
            "테스트마다 초기화 + TestDriver 확보 + 정리를 반복하는 대신 JUnit 규칙에 넣는다. " +
                "TestDriver 는 초기화 이후에만 유효하므로 규칙이 함께 들고 있는 편이 안전하다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            "class WorkManagerTestRule(\n" +
                "    private val context: Context = ApplicationProvider.getApplicationContext()\n" +
                ") : TestRule {\n" +
                "    lateinit var workManager: WorkManager; private set\n" +
                "    lateinit var testDriver: TestDriver; private set\n\n" +
                "    override fun apply(base: Statement, description: Description) =\n" +
                "        object : Statement() {\n" +
                "            override fun evaluate() {\n" +
                "                val config = Configuration.Builder()\n" +
                "                    .setMinimumLoggingLevel(Log.DEBUG)\n" +
                "                    .setExecutor(SynchronousExecutor())\n" +
                "                    .build()\n" +
                "                WorkManagerTestInitHelper.initializeTestWorkManager(context, config)\n" +
                "                workManager = WorkManager.getInstance(context)\n" +
                "                testDriver = requireNotNull(\n" +
                "                    WorkManagerTestInitHelper.getTestDriver(context)\n" +
                "                )\n" +
                "                try { base.evaluate() } finally { workManager.cancelAllWork() }\n" +
                "            }\n" +
                "        }\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "사용하는 쪽은 @get:Rule val workRule = WorkManagerTestRule() 한 줄이면 되고, " +
                "테스트 본문에는 workRule.workManager / workRule.testDriver 만 남는다."
        )
    }
}

// ==================== 3. 제약 게이팅 ====================

@Composable
private fun ConstraintGatingCard() {
    WorkTestSectionCard(title = "3. 제약은 충족시키지 않고 통과시킨다 (실측)") {
        BodyText(
            "네트워크 연결 + 충전 중을 요구하는 작업을 큐에 넣고, 기기 상태는 전혀 건드리지 않은 채 " +
                "TestDriver 로만 제약을 만족시켰을 때의 상태 변화다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        CodeText(
            "val request = OneTimeWorkRequestBuilder<EchoWorker>()\n" +
                "    .setInputData(workDataOf(\"echo\" to \"hello\"))\n" +
                "    .setConstraints(\n" +
                "        Constraints.Builder()\n" +
                "            .setRequiredNetworkType(NetworkType.CONNECTED)\n" +
                "            .setRequiresCharging(true)\n" +
                "            .build()\n" +
                "    ).build()\n" +
                "workManager.enqueue(request).result.get()\n" +
                "// ...상태 확인...\n" +
                "testDriver.setAllConstraintsMet(request.id)"
        )

        Spacer(modifier = Modifier.height(12.dp))
        TableRow("시점", "getWorkInfoById(id).state", isHeader = true)
        TableRow("enqueue 직후", "ENQUEUED  ← 제약이 걸려 실행되지 않음")
        TableRow("setAllConstraintsMet 후", "SUCCEEDED / output echo = \"HELLO\"")

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "실측 기기 SM-A725F(API 33). 충전기를 꽂지도, 네트워크를 조작하지도 않았다 — " +
                "TestDriver 가 스케줄러에 \"조건이 만족됐다\"고 알려주는 것만으로 작업이 실행된다."
        )
    }
}

// ==================== 4. 지연 ====================

@Composable
private fun DelayCard() {
    WorkTestSectionCard(title = "4. 24시간 지연을 0초로 (실측)") {
        BodyText(
            "초기 지연도 같은 방식이다. 24시간 뒤 실행하도록 예약한 작업을 setInitialDelayMet 으로 " +
                "즉시 실행시킨다. 주기 작업에는 setPeriodDelayMet 을 쓴다(최소 주기 15분을 기다릴 필요가 없다)."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            "val request = OneTimeWorkRequestBuilder<EchoWorker>()\n" +
                "    .setInitialDelay(24, TimeUnit.HOURS)\n" +
                "    .build()\n" +
                "workManager.enqueue(request).result.get()\n" +
                "testDriver.setInitialDelayMet(request.id)"
        )
        Spacer(modifier = Modifier.height(12.dp))
        TableRow("시점", "state", isHeader = true)
        TableRow("24시간 지연 설정 후", "ENQUEUED")
        TableRow("setInitialDelayMet 후", "SUCCEEDED")

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "TestDriver 의 세 메서드는 모두 UUID 를 받는다 — setAllConstraintsMet / setInitialDelayMet / " +
                "setPeriodDelayMet. 존재하지 않는 id 를 넘기면 예외가 난다."
        )
    }
}

// ==================== 5. 격리 실행 ====================

@Composable
private fun IsolationCard() {
    WorkTestSectionCard(title = "5. 워커 하나만 떼어내 실행 (실측)") {
        BodyText(
            "스케줄링 전체가 아니라 doWork() 안의 로직만 확인하고 싶을 때는 WorkManager 자체가 필요 없다. " +
                "TestListenableWorkerBuilder 로 워커를 직접 만들어 실행한다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            "val worker = TestListenableWorkerBuilder<EchoWorker>(context)\n" +
                "    .setInputData(workDataOf(\"echo\" to \"isolated\"))\n" +
                "    .build()\n" +
                "val result = worker.startWork().get()"
        )
        Spacer(modifier = Modifier.height(12.dp))
        TableRow("입력 / 설정", "결과(toString 그대로)", isHeader = true)
        TableRow("echo=\"isolated\"", "Success {mOutputData=Data {echo : ISOLATED, }}")
        TableRow("입력 없음", "Failure {mOutputData=Data {}}")
        TableRow("runAttemptCount = 0", "Retry")
        TableRow("runAttemptCount = 2", "Success {mOutputData=Data {attempt : 2, }}")

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "마지막 두 줄이 이 빌더의 진짜 쓸모다. 재시도 분기는 보통 \"몇 번째 시도인가\"로 갈리는데, " +
                "runAttemptCount 를 직접 주입하면 실제로 실패를 반복시키지 않고 그 분기만 검증할 수 있다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "setInputData 외에 setTags/setRunAttemptCount/setTriggeredContentUris/setNetwork/" +
                "setWorkerFactory/setProgressUpdater/setForegroundUpdater 를 넣을 수 있다."
        )
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun WorkTestPitfallCard() {
    WorkTestSectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "이 화면이 직접 실행하지 않는 이유",
            "initializeTestWorkManager 는 프로세스의 WorkManager 싱글턴을 테스트 구현으로 갈아끼운다. " +
                "앱에서 부르면 실제 WorkManager 예제가 쓰는 인스턴스까지 바뀌므로, 실행 코드는 " +
                "androidTest 에 두고 화면은 설명과 측정 결과만 싣는다."
        )
        PitfallRow(
            "ExecutorsMode 3종",
            "initializeTestWorkManager 는 ExecutorsMode 를 받는 오버로드가 있다 — " +
                "LEGACY_OVERRIDE_WITH_SYNCHRONOUS_EXECUTORS(기본 동작) / PRESERVE_EXECUTORS(설정한 executor 유지) / " +
                "USE_TIME_BASED_SCHEDULING(시간 기반 스케줄링). 동기 실행이 기본이라는 점을 모르면 " +
                "\"왜 코루틴 테스트가 이상하게 도나\"로 헤맨다."
        )
        PitfallRow(
            "get() 은 블로킹이다",
            "enqueue(...).result.get() / getWorkInfoById(id).get() 은 ListenableFuture 를 블로킹으로 기다린다. " +
                "SynchronousExecutor 와 함께 쓰면 그 자리에서 끝나지만, 메인 스레드에서 부르면 안 된다."
        )
        PitfallRow(
            "테스트 간 격리",
            "WorkManager 는 프로세스 단위 싱글턴이라 앞 테스트가 남긴 작업이 다음 테스트에 보인다. " +
                "규칙의 finally 에서 cancelAllWork() 로 정리한다."
        )
        PitfallRow(
            "실제 제약 동작은 검증되지 않는다",
            "TestDriver 는 \"조건이 만족됐다\"고 알려줄 뿐, 조건 판정 자체를 테스트하지는 않는다. " +
                "제약이 올바르게 걸렸는지는 WorkRequest 의 Constraints 를 단언하는 쪽이 맞다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun WorkTestSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun CodeText(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
            .padding(10.dp),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 15.sp
    )
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(120.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = if (isHeader) FontFamily.Default else FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}
