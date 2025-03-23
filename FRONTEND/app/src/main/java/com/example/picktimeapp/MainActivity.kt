package com.example.picktimeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.theme.PickTimeAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickTimeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    // 마이페이지
    var showMyPage by remember { mutableStateOf(false) }
    
    if (showMyPage) {
        val viewModel = MyPageViewModel()
        MyPageScreen(viewModel = viewModel)
    } else {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ){
            Text(text = "기타 학습 앱에 오신 것을 환영합니다!")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = {
                showMyPage = true
            }) {
                Text("마이페이지 확인용")
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PickTimeAppTheme {
        MainScreen()
    }
}