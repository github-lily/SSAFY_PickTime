package com.example.picktimeapp.ui.guitarposition

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.camera.CameraPermissionScreen
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuitarPositionScreen(
    onBackClick: () -> Unit = {} // ← 버튼 클릭 시 동작할 이벤트 전달 가능
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "연주 준비",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 안내 문구
            Text(
                text = "화면 안에 기타가 잘 보이도록 맞춰주세요!",
                fontSize = 28.sp,
                color = Gray50
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 카메라 프리뷰 (권한 확인 포함)
            CameraPermissionScreen {
                Box(
                    modifier = Modifier
                        .width(850.dp)
                        .height(480.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    CameraPreview(modifier = Modifier.fillMaxSize())

                    Image(
                        painter = painterResource(id = R.drawable.guitar_overlay),
                        contentDescription = "기타 위치 안내 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}
