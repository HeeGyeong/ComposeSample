package com.example.composesample.presentation.example.component.system.platform.file

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import java.util.zip.ZipInputStream

class SafFileSelectionViewModel : ViewModel() {
    
    private val _extractedText = MutableStateFlow<String?>(null)
    val extractedText: StateFlow<String?> = _extractedText.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun readFileContent(context: Context, uri: Uri, fileExtension: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                when (fileExtension.lowercase()) {
                    "txt" -> readTxtFile(context, uri)
                    "doc", "docx" -> readDocxFile(context, uri)
                    else -> _errorMessage.value = "지원하지 않는 파일 형식입니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "파일 읽기 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun readTxtFile(context: Context, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            
            _extractedText.value = content.toString()
        } ?: run {
            _errorMessage.value = "파일을 열 수 없습니다."
        }
    }
    
    private fun readDocxFile(context: Context, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val textContent = StringBuilder()
            val zipInputStream = ZipInputStream(inputStream)
            var zipEntry = zipInputStream.nextEntry
            
            // docx 파일 내의 모든 항목을 순회
            while (zipEntry != null) {
                // word/document.xml 파일에는 문서의 텍스트 내용이 저장되어 있음
                if (zipEntry.name == "word/document.xml") {
                    // XML 파싱을 통해 텍스트 추출
                    textContent.append(parseDocumentXml(zipInputStream))
                    break
                }
                zipInputStream.closeEntry()
                zipEntry = zipInputStream.nextEntry
            }
            
            zipInputStream.close()
            
            if (textContent.isEmpty()) {
                _errorMessage.value = "문서에서 텍스트를 추출할 수 없습니다."
            } else {
                _extractedText.value = textContent.toString()
            }
        } ?: run {
            _errorMessage.value = "파일을 열 수 없습니다."
        }
    }
    
    private fun parseDocumentXml(inputStream: InputStream): String {
        val textBuilder = StringBuilder()
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            
            var eventType = parser.eventType
            var isInTextTag = false
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        // MS Word의 텍스트는 주로 w:t 태그 안에 있음
                        if (parser.name.endsWith(":t")) {
                            isInTextTag = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (isInTextTag) {
                            textBuilder.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.endsWith(":t")) {
                            isInTextTag = false
                            // 단락 구분을 위해 공백 추가
                            textBuilder.append(" ")
                        } else if (parser.name.endsWith(":p")) {
                            // 문단 구분을 위해 줄바꿈 추가
                            textBuilder.append("\n")
                        }
                    }
                }
                eventType = parser.next()
            }
            
            return textBuilder.toString()
        } catch (e: Exception) {
            return "XML 파싱 오류: ${e.message}"
        }
    }
    
    fun clearData() {
        _extractedText.value = null
        _errorMessage.value = null
    }
} 