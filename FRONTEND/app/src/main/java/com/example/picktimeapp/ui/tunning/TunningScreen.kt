package com.example.picktimeapp.ui.tunning

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.R

@Composable
fun TunningScreen(
    onBackClick: () -> Unit = {}
) {
    // ✅ 전역 선택 인덱스 하나로 통일
    var selectedIndex by remember { mutableStateOf(-1) }

    // 좌측 3개 + 우측 3개 = 전체 페그 리스트
    val leftPegs = listOf(
        Pair(R.drawable.tunning_l_d to R.drawable.tunning_l_d_sel, "D String"),
        Pair(R.drawable.tunning_l_a to R.drawable.tunning_l_a_sel, "A String"),
        Pair(R.drawable.tunning_e to R.drawable.tunning_e_sel, "E String")
    )
    val rightPegs = listOf(
        Pair(R.drawable.tunning_r_g to R.drawable.tunning_r_g_sel, "G String"),
        Pair(R.drawable.tunning_r_b to R.drawable.tunning_r_b_sel, "B String"),
        Pair(R.drawable.tunning_e to R.drawable.tunning_e_sel, "E String")
    )

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // (1) 뒤로가기 버튼 영역
        Box(
            modifier = Modifier
                .weight(0.2f)
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
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // (2) 기린 그림 영역
        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tunning_bar),
                contentDescription = "튜닝 바"
            )

            Image(
                painter = painterResource(id = R.drawable.tunning_girin),
                contentDescription = "튜닝 기린",
                modifier = Modifier.offset(x = -50.dp, y = 100.dp)
            )
        }

        // 기타 영역
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text(text = "test 중입니다.")
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // 왼쪽 페그 (index 0 ~ 2)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    TuningPegsColumn(
                        pegs = leftPegs,
                        baseIndex = 0,
                        selectedIndex = selectedIndex,
                        onSelected = { selectedIndex = it }
                    )
                }

                // 기타 헤드
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.guitar_head),
                        contentDescription = "기타 헤드 및 튜닝 페그",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 오른쪽 페그 (index 3 ~ 5)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    TuningPegsColumn(
                        pegs = rightPegs,
                        baseIndex = 3,
                        selectedIndex = selectedIndex,
                        onSelected = { selectedIndex = it }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}


@Composable
fun TuningPegsColumn(
    pegs: List<Pair<Pair<Int, Int>, String>>, // (normal, selected), description
    baseIndex: Int, // 이 열의 시작 인덱스
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        pegs.forEachIndexed { index, (resIds, description) ->
            val (normal, selected) = resIds
            val globalIndex = baseIndex + index

            Image(
                painter = painterResource(id = if (selectedIndex == globalIndex) selected else normal),
                contentDescription = description,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable { onSelected(globalIndex) }
            )
        }
    }
}
