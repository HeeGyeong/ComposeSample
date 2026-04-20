package com.example.composesample.presentation.example.component.architecture.development.di

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun KoinCompilerPluginExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Koin Compiler Plugin",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { ManualVsAnnotationCard() }
            item { AnnotationComparisonCard() }
            item { GeneratedCodeCard() }
            item { GradleSetupCard() }
            item { MigrationStrategyCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Koin Compiler Plugin 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Koin Annotations(KSP)로 DI를 컴파일 타임에 검증합니다. " +
                        "기존 수동 DSL은 누락된 의존성이 런타임에 NoDefinitionFoundException으로 터지는 반면, " +
                        "애노테이션 방식은 KSP가 빌드 중 그래프를 검사해 빌드 오류로 드러냅니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val points = listOf(
                Triple("검증 시점", "런타임", "컴파일 타임 (KSP)"),
                Triple("누락 의존성", "실행 후 예외", "빌드 오류"),
                Triple("리팩토링", "수동 바인딩 수정", "애노테이션 유지"),
                Triple("코드 작성", "module { ... } DSL", "@Single/@Factory/@KoinViewModel")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "항목", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
                Text(text = "수동 DSL", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f))
                Text(text = "애노테이션", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f))
            }
            points.forEach { (label, manual, annotated) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.3f))
                    Text(text = manual, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.35f))
                    Text(text = annotated, fontSize = 10.sp, color = Color(0xFF388E3C), modifier = Modifier.weight(0.35f))
                }
            }
        }
    }
}

@Composable
private fun ManualVsAnnotationCard() {
    var selected by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "수동 DSL vs 애노테이션 방식",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("수동 DSL", "Annotations").forEachIndexed { index, label ->
                    Button(
                        onClick = { selected = index },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selected == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (selected == index) Color.White else Color(0xFF424242),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selected == 0) {
                CodeBlock(
                    code = "// ❌ 수동 DSL — 누락 시 런타임에만 오류\n" +
                            "val apiModule = module {\n" +
                            "    single<Retrofit>(named(\"post\")) {\n" +
                            "        Retrofit.Builder()\n" +
                            "            .baseUrl(BASE)\n" +
                            "            .client(get())  // ← OkHttpClient 바인딩\n" +
                            "            .build()        //   누락 시 런타임 예외\n" +
                            "    }\n" +
                            "}\n\n" +
                            "val viewModelModule = module {\n" +
                            "    viewModel {\n" +
                            "        ApiExampleViewModel(\n" +
                            "            get(),\n" +
                            "            get(named(\"post\")),\n" +
                            "            get()\n" +
                            "        )\n" +
                            "    }\n" +
                            "}",
                    borderColor = Color(0xFFD32F2F)
                )
            } else {
                CodeBlock(
                    code = "// ✅ Koin Annotations — 컴파일 타임 검증\n" +
                            "@Module\n" +
                            "@ComponentScan(\"com.example.sample\")\n" +
                            "class AppModule\n\n" +
                            "@Single\n" +
                            "class OkHttpProvider { fun client() = OkHttpClient() }\n\n" +
                            "@Single\n" +
                            "@Named(\"post\")\n" +
                            "fun provideRetrofit(client: OkHttpClient): Retrofit =\n" +
                            "    Retrofit.Builder()\n" +
                            "        .baseUrl(BASE)\n" +
                            "        .client(client)\n" +
                            "        .build()\n\n" +
                            "@KoinViewModel\n" +
                            "class ApiExampleViewModel(\n" +
                            "    repo: ExampleRepository,\n" +
                            "    @Named(\"post\") retrofit: Retrofit,\n" +
                            "    logger: Logger\n" +
                            ")",
                    borderColor = Color(0xFF388E3C)
                )
            }
        }
    }
}

@Composable
private fun AnnotationComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "주요 애노테이션 매핑",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = listOf(
                Triple("@Module", "module { ... }", "모듈 선언. @ComponentScan과 함께 사용"),
                Triple("@ComponentScan", "—", "지정 패키지 내 애노테이션 스캔"),
                Triple("@Single", "single { ... }", "싱글톤 인스턴스"),
                Triple("@Factory", "factory { ... }", "호출마다 새 인스턴스"),
                Triple("@KoinViewModel", "viewModel { ... }", "ViewModel 생명주기 연동"),
                Triple("@Named", "named(\"...\")", "한정자 — 같은 타입 구분"),
                Triple("@InjectedParam", "parametersOf(...)", "런타임 파라미터 주입"),
                Triple("@Property", "getProperty(\"...\")", "Koin properties 값 주입")
            )
            rows.forEach { (anno, dsl, desc) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = anno, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2))
                        Text(text = dsl, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))
                    }
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFF424242))
                }
            }
        }
    }
}

@Composable
private fun GeneratedCodeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "KSP가 생성하는 코드 (개념 미리보기)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "KSP는 빌드 시점에 애노테이션을 스캔하여 기존 Koin module DSL과 동등한 코드를 생성합니다. " +
                        "다음은 생성 결과의 개념적 형태입니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// build/generated/ksp/.../AppModuleGen.kt (개념)\n" +
                        "public val AppModule.module : Module get() = module {\n" +
                        "    single { OkHttpProvider() }\n" +
                        "    single(qualifier = named(\"post\")) {\n" +
                        "        provideRetrofit(client = get())\n" +
                        "    }\n" +
                        "    viewModel {\n" +
                        "        ApiExampleViewModel(\n" +
                        "            repo = get(),\n" +
                        "            retrofit = get(qualifier = named(\"post\")),\n" +
                        "            logger = get()\n" +
                        "        )\n" +
                        "    }\n" +
                        "}\n\n" +
                        "// startKoin에 주입\n" +
                        "startKoin {\n" +
                        "    modules(AppModule().module)\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "누락된 get() 바인딩이 있으면 KSP가 컴파일 오류로 리포트합니다. " +
                        "→ 런타임에 앱을 실행하지 않아도 그래프 무결성 확인이 가능합니다.",
                fontSize = 11.sp,
                color = Color(0xFF388E3C),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GradleSetupCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gradle 설정",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(
                code = "// build.gradle.kts (app)\n" +
                        "plugins {\n" +
                        "    id(\"com.google.devtools.ksp\")\n" +
                        "}\n\n" +
                        "dependencies {\n" +
                        "    implementation(\n" +
                        "        \"io.insert-koin:koin-annotations:<ver>\"\n" +
                        "    )\n" +
                        "    ksp(\n" +
                        "        \"io.insert-koin:koin-ksp-compiler:<ver>\"\n" +
                        "    )\n" +
                        "}\n\n" +
                        "// KSP 옵션 (선택) — 안전성 강화\n" +
                        "ksp {\n" +
                        "    arg(\"KOIN_CONFIG_CHECK\", \"true\")\n" +
                        "}",
                borderColor = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "`KOIN_CONFIG_CHECK=true`를 주면 KSP가 선언되지 않은 의존성을 더 엄격히 검사합니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun MigrationStrategyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "전환 전략 (ComposeSample 적용 시)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val strategies = listOf(
                "1단계: 새 모듈부터 @Module + @ComponentScan 도입. 기존 수동 module과 공존 가능",
                "2단계: ViewModelModule 등 목록이 긴 모듈을 @KoinViewModel로 이전 → 수동 get() 누락 탐지",
                "3단계: named() 한정자가 많은 ApiModule은 @Named(\"...\") + @Single 함수 패턴으로 치환",
                "4단계: 런타임 분기가 필요한 바인딩(디버그/릴리스 분기 등)은 수동 DSL로 유지"
            )
            strategies.forEach { line ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = line, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ 현재 프로젝트는 수동 DSL(InjectModules.kt)로 구성되어 있습니다. " +
                            "애노테이션 도입 시 KSP 적용과 리플렉션 비용 증가 여부를 먼저 벤치마크하세요.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "Koin 수동 DSL은 런타임 검증 → 미정의 바인딩은 앱 실행 중에만 노출",
                "Koin Annotations(KSP)는 컴파일 타임 검증 → 빌드 단계에서 그래프 누락 감지",
                "@Single/@Factory/@KoinViewModel은 기존 single/factory/viewModel DSL과 1:1 매핑",
                "@Named로 같은 타입의 한정자를 컴파일 타임에 고정 — 오타/누락 방지",
                "기존 수동 모듈과 공존 가능하므로 점진적 전환이 안전한 도입 경로"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
