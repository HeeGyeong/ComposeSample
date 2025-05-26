package com.example.composesample.presentation.example.component.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SafFileSelectionUI(onBackButtonClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SafFileSelectionViewModel = viewModel()
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileExtension by remember { mutableStateOf<String?>(null) }
    
    // ViewModel에서 데이터 수집
    val extractedText by viewModel.extractedText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    
    // ViewModel 에러 메시지를 UI 에러 메시지로 설정
    LaunchedEffect(viewModelError) {
        viewModelError?.let {
            errorMessage = it
        }
    }

    // 파일 선택기 런처 설정
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // 파일 크기 확인
            val fileSize = getFileSize(context, uri)

            if (fileSize > 200 * 1024) { // 200KB = 200 * 1024 bytes
                errorMessage = "파일 크기가 200KB를 초과합니다."
                selectedFileName = null
                selectedFileSize = null
                selectedFileUri = null
                selectedFileExtension = null
                return@let
            }

            // 파일 이름 가져오기
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(nameIndex)

                    // 파일 확장자 확인
                    val extension = fileName.substringAfterLast(".", "")
                    if (extension !in listOf("txt", "doc", "docx")) {
                        errorMessage = "지원되지 않는 파일 형식입니다. (txt, doc, docx만 가능)"
                        selectedFileName = null
                        selectedFileSize = null
                        selectedFileUri = null
                        selectedFileExtension = null
                        return@use
                    }

                    selectedFileName = fileName
                    selectedFileSize = fileSize
                    selectedFileUri = uri
                    selectedFileExtension = extension
                    errorMessage = null
                    
                    // 파일 내용 읽기
                    viewModel.readFileContent(context, uri, extension)
                }
            }
        } ?: run {
            // 파일 선택이 취소된 경우
            errorMessage = "파일 선택이 취소되었습니다."
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SAF 파일 선택 예제",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        fileLauncher.launch(
                            arrayOf(
                                "text/plain",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )

                        // 파일 확장자 관련 없이 가져오기.
                        // fileLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Text("파일 선택하기 (txt, doc, docx)")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 에러 메시지 표시
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 선택된 파일 정보 표시
                if (selectedFileName != null && selectedFileSize != null) {
                    Text(
                        text = "파일 이름: $selectedFileName",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "파일 크기: ${formatFileSize(selectedFileSize!!)}",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // 로딩 인디케이터
                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // 추출된 텍스트 표시
                extractedText?.let { text ->
                    Text(
                        text = "파일 내용:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// 파일 크기 가져오기
private fun getFileSize(context: Context, uri: Uri): Long {
    var fileSize: Long = 0

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
    }

    return fileSize
}

// 파일 크기를 읽기 쉬운 형식으로 포맷팅
private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 KB"

    val kbSize = size / 1024.0
    val formatter = DecimalFormat("#,##0.00")
    return "${formatter.format(kbSize)} KB"
} 