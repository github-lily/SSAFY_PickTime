package com.example.picktimeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

//image 관련
import androidx.compose.ui.res.painterResource



// font 관련
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.picktimeapp.R
import androidx.compose.ui.unit.sp

//font
val TitleFont = FontFamily(
    Font(R.font.hakgyoansimalrimche_regular, FontWeight.Normal),
    Font(R.font.hakgyoansimalrimche_bold, FontWeight.Bold)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickTimeAppTheme(dynamicColor = false) {
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
    var showMyPage by remember { mutableStateOf(false) }

    if (showMyPage) {
        val viewModel = MyPageViewModel()
        MyPageScreen(viewModel = viewModel)
    } else {
        // 전체 화면을 좌우로 나누는 Row
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 텍스트 + 버튼 Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 타이틀
                Text(
                    text = "피크 타임",
                    fontFamily = TitleFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 130.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 서브타이틀
                Text(
                    text = "즐겁고 재미있는 기타 학습",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 로그인 버튼
                Button(
                    onClick = { /* TODO: 로그인 화면 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(60.dp)
                ) {
                    Text("로그인")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 회원가입 버튼
                Button(
                    onClick = { /* TODO: 회원가입 화면 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(60.dp)
                ) {
                    Text("회원가입")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 마이페이지 버튼
                Button(
                    onClick = { showMyPage = true },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(60.dp)
                ) {
                    Text("마이페이지 확인용")
                }
            }

            // 우측: 기린 이미지
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.girinduo),
                contentDescription = "기타 치는 기린들",
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            )
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