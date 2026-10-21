package com.example.composesample.presentation.example.component.system.platform.clipboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 텍스트 선택 메뉴의 'ComposeSample 변환' 항목을 누르면 열리는 액티비티(ACTION_PROCESS_TEXT).
 * 이 앱의 예제 화면뿐 아니라 다른 앱이 띄울 수도 있으므로 받은 값은 프로세스 안 ProcessTextLog 에만 남긴다.
 *
 * - EXTRA_PROCESS_TEXT: 선택된 문자열(CharSequence)
 * - EXTRA_PROCESS_TEXT_READONLY: true 면 결과를 돌려줘도 원문이 바뀌지 않는다는 뜻
 * - 결과는 setResult(RESULT_OK, EXTRA_PROCESS_TEXT) — 호출한 쪽이 startActivityForResult 로 열었을 때만 전달된다
 */
class ProcessTextDemoActivity : ComponentActivity() {

    private var receiptId = 0
    private var finishedWithOutcome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        val readonly =
            if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT_READONLY)) intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false) else null
        // startActivityForResult 로 열렸을 때만 채워진다 — null 이면 setResult 를 받을 곳이 없다
        val caller = callingActivity?.shortClassName
        // referrer 는 startActivity 로 열려도 채워진다(android-app://패키지)
        val from = referrer?.host

        // 재생성(다크 모드 전환 등) 때 같은 intent 를 다시 기록하지 않는다
        receiptId = savedInstanceState?.getInt(KEY_RECEIPT) ?: ProcessTextLog.add(text, readonly, caller, from)

        setContent {
            ProcessTextDemoScreen(
                text = text,
                readonly = readonly,
                caller = caller,
                from = from,
                onReturn = { returnResult(text) },
                onClose = { closeWithoutResult() },
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_RECEIPT, receiptId)
    }

    override fun onDestroy() {
        if (isFinishing && !finishedWithOutcome) ProcessTextLog.setOutcome(receiptId, "뒤로 닫음(결과 없음)")
        super.onDestroy()
    }

    private fun returnResult(text: String) {
        val result = processText(text)
        setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, result))
        ProcessTextLog.setOutcome(receiptId, "돌려줌 → $result")
        finishedWithOutcome = true
        finish()
    }

    private fun closeWithoutResult() {
        setResult(RESULT_CANCELED)
        ProcessTextLog.setOutcome(receiptId, "결과 없이 닫음")
        finishedWithOutcome = true
        finish()
    }

    private companion object {
        const val KEY_RECEIPT = "receipt_id"
    }
}

@Composable
private fun ProcessTextDemoScreen(
    text: String,
    readonly: Boolean?,
    caller: String?,
    from: String?,
    onReturn: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("ComposeSample 변환", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(
            "다른 앱(또는 이 앱)의 텍스트 선택 메뉴에서 열렸다. 받은 값을 그대로 보여 준다.",
            fontSize = 13.sp, color = Color(0xFF616161), lineHeight = 18.sp
        )
        ValueBox("EXTRA_PROCESS_TEXT", "\"$text\"")
        ValueBox(
            "EXTRA_PROCESS_TEXT_READONLY",
            when (readonly) {
                null -> "없음"
                true -> "true — 결과를 돌려줘도 원문은 바뀌지 않는다는 뜻"
                false -> "false — 결과로 선택 부분을 바꿀 수 있다는 뜻"
            }
        )
        ValueBox(
            "callingActivity",
            caller?.let { "$it — startActivityForResult 로 열림, 결과가 전달된다" }
                ?: "null — startActivity 로 열림, setResult 를 받을 곳이 없다"
        )
        ValueBox("referrer", from ?: "없음")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "돌려줄 결과: \"${processText(text)}\"",
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)
        )
        Button(
            onClick = onReturn,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text(if (readonly == true) "읽기 전용이지만 그래도 돌려주기" else "변환해서 돌려주기", color = Color.White)
        }
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
        ) {
            Text("결과 없이 닫기", color = Color.White)
        }
    }
}

@Composable
private fun ValueBox(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))
        Text(value, fontSize = 14.sp, color = Color(0xFF212121), lineHeight = 19.sp)
    }
}
