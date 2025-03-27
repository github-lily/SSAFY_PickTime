package com.example.picktimeapp.ui.tunning

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.R

@Composable
fun TunningScreen(
    onBackClick: () -> Unit = {}
){
    Row(
        modifier = Modifier.fillMaxSize()
    ){
        // (1) 뒤로가기 버튼 영역
        Box(
            modifier = Modifier
                .weight(0.2f) // 특정 너비를 줄 수도 있고, weight로 나눌 수도 있음
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(80.dp)
                )
            }
        }

        // (2) 기린 그림 영역
        Box(
            modifier = Modifier
                .weight(0.5f)         // 기린 영역 비율
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tunning_bar),
                contentDescription = "튜님 바"
            )

            Image(
                painter = painterResource(id = R.drawable.tunning_girin), // 추가 이미지 리소스
                contentDescription = "튜닝 기린",
                modifier = Modifier.offset(x = -50.dp, y = 100.dp)
            )
        }

        // 오른쪽 영역: 기타 영역
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 상단의 동적 피드백 텍스트
            Spacer(modifier = Modifier.height(50.dp))
            Text(text = "test 중입니다.")
            Spacer(modifier = Modifier.height(50.dp))
            // 기타 영역을 좌측 버튼, 중앙 콘텐츠(기타 헤드 및 튜닝 페그), 우측 버튼으로 나눔
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                // 왼쪽 버튼 영역
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.head_left), // ← 실제 리소스 ID로 교체
                        contentDescription = "기타 좌측 페그",
                        modifier = Modifier.offset(y = 30.dp)
                    )
                }
                // 중앙 영역: 기타 헤드 및 튜닝 페그 영역
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.guitar_head), // ← 실제 리소스 ID로 교체
                        contentDescription = "기타 헤드 및 튜닝 페그",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                // 오른쪽 버튼 영역
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.head_right), // ← 실제 리소스 ID로 교체
                        contentDescription = "기타 우측 페그",
                        modifier = Modifier.offset(y = 30.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}