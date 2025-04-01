package com.example.picktimeapp.ui.practice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.network.*
import com.example.picktimeapp.ui.components.SideNavigation
import com.example.picktimeapp.ui.theme.*


@Composable
fun PracticeListScreen(navController: NavController, viewModel: PracticeListViewModel = hiltViewModel()) {
    val stageList = viewModel.stageList

// ✅ 초기값은 null로
    var selectedStage by remember { mutableStateOf<StageItem?>(null) }

// ✅ stageList가 바뀔 때마다 선택 스테이지 갱신
    LaunchedEffect(stageList) {
        if (stageList.isNotEmpty()) {
            selectedStage = stageList
                .filter { !it.isClear } // isClear == false 값 필터링
                .minByOrNull { it.stageId } // 그 중 stageId가 가장 작은 값 선택
                ?: stageList.maxByOrNull { it.stageId } // 없다면 전체 중 가장 큰 값(마지막 stage)ㅅ 선택
        }
    }


    // ✅ 유저 정보 받아오기
    val userInfo = viewModel.userInfo.value

    // ✅ 프로필 이미지 변경
    val level = userInfo?.level ?: 1
    val profileResId = when (level) {
        1 -> R.drawable.profile_level_1
        2 -> R.drawable.profile_level_2
        3 -> R.drawable.profile_level_3
        4 -> R.drawable.profile_level_4
        5 -> R.drawable.profile_level_5
        6 -> R.drawable.profile_level_6
        else -> R.drawable.profile_level_1
    }



    Row(modifier = Modifier.fillMaxSize()) {
        SideNavigation(navController = navController)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkGreen10
        ) {
            BoxWithConstraints {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val baseWidth = 800.dp // 기준 너비 (예: 800dp 기준으로 디자인)
                val scale = screenWidth / baseWidth

                Box(
                    modifier = Modifier.padding(
                        start = (screenWidth.value * 0.0f).dp,
                        top = (screenHeight.value * 0.04f).dp,
                        end = (screenWidth.value * 0.02f).dp,
                        bottom = (screenHeight.value * 0.01f).dp
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단영역
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = screenWidth * 0.07f)
                                .fillMaxWidth()
                        ) {
                            // 상단 프로필 이미지 영역
                            Box(
                                modifier = Modifier
                                    .weight(0.15f)
                                    .aspectRatio(1f)
                                    .background(color = Color.White, shape = CircleShape)
                                    .border(
                                        width = 2.dp * scale,
                                        color = Brown40,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = profileResId),
                                    contentDescription = "기린 프로필",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // 이미지와 인사말 영역의 간격
                            Spacer(modifier = Modifier.width(screenWidth * 0.05f))

                            // 상단 인사말 영역
                            Box(
                                modifier = Modifier
                                    .weight(0.6f)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("안녕하세요 ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(userInfo?.name ?: "기린이")
                                        }
                                        append("님,\n")
                                        append("오늘도 피크타임을 즐기러 가볼까요?")
                                    },
                                    fontFamily = TitleFont,
                                    fontSize = 20.sp * scale,
                                    lineHeight = 30.sp * scale,
                                    color = Gray90,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
//                                        .wrapContentSize(Alignment.Center)
                                )
                            }
                        }

                        // 상단과 리스트 사이 간격
                        Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                        // 커리큘럼 리스트
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .padding(horizontal = screenWidth * 0.06f)
                                .fillMaxWidth()
                        ) {
                            // 왼쪽 : Stage 전체 목록
                            Column (
                                modifier = Modifier
                                    .weight(0.5f),
                                horizontalAlignment =  Alignment.Start
                            ) {
                                stageList.forEach { stage ->
                                    StageListItem(
                                        stage = stage,
                                        isSelected = stage.stageId == selectedStage?.stageId,
                                        scale = scale,
                                        onClick = { selectedStage = stage }
                                    )
                                }
                            }

                            // 전체 리스트와 상세 리스트 사이 간격
                            Spacer(modifier = Modifier.width(screenWidth * 0.02f))

                            // 상세 리스트
                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .wrapContentHeight()
                            ) {
                                selectedStage?.let { stage ->
                                    StageDetailPanel(
                                        stage = stage,
                                        navController = navController,
                                        scale = scale
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }



        @Composable
        fun IconButton(iconResId: Int, contentDescription: String, scale:Float) {
            Box(
                modifier = Modifier
                    .size(50.dp * scale)
                    .clip(CircleShape)
                    .background(Brown40),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp * scale)
                )
            }
        }

    }
}


@Composable
fun StageListItem(
    stage: StageItem,
    isSelected: Boolean,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 16.dp)
            .background(
                color = if (isSelected) Brown40 else Brown20,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp * scale, horizontal = 24.dp * scale)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stage ${stage.stageId}",
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkGreen10 else Brown60,
                textAlign = TextAlign.Start,
                fontSize =  (20.sp * scale),
            )

            Spacer(modifier = Modifier.width(16.dp * scale))

            Text(
                text = stage.stageDescription,
                color = if (isSelected) Color.White else Brown60,
                modifier = Modifier.fillMaxWidth(0.8f),
                textAlign = TextAlign.Center,
                fontSize =(16.sp * scale) ,

            )

            Spacer(modifier = Modifier.weight(1f))


            if (stage.isClear) {
                Icon(
                    painter = painterResource(id = R.drawable.check_circle),
                    contentDescription = "완료",
                    tint = Brown60,
                    modifier = Modifier
                        .size(20.dp * scale)
                        .offset(-10.dp)
                        .wrapContentSize()

                )
            }
        }
    }
}


@Composable
fun StageDetailPanel(stage: StageItem, scale: Float, navController: NavController) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
//            .padding(bottom = 50.dp * scale)

    ) {
        // 스테이지 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Brown40, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(vertical = 12.dp * scale, horizontal = 24.dp * scale)

        ) {
            Text(
                text = "Stage ${stage.stageId} ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp * scale,

            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = " ${stage.stageDescription}",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp * scale,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(start = 10.dp * scale)

            )
        }

        // 스텝 리스트
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .weight(1f)
                .background(Brown20, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 24.dp)
        ) {
            stage.steps.forEach { step ->
                StepItem(step, scale = scale) { stepId ->
                    navController.navigate("guitarPosition/$stepId")
                }
                if (step.stepNumber < stage.steps.size) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }

            }
        }
    }
}

@Composable
fun StepItem(step: StepItem, scale: Float,  onClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(step.stepId) }
            .padding(vertical = 8.dp * scale, horizontal = 18.dp * scale),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Step ${step.stepNumber}",
            fontSize = (16.sp * scale),
            fontWeight = FontWeight.Medium,
            color = Brown60,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = step.stepDescription,
            color = Brown60,
            fontSize = (14.sp * scale),
            modifier = Modifier.fillMaxWidth(0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // ⭐ Step 자신의 star 값 기준
        val starResId = when (step.star) {
            1 -> R.drawable.ic_star_one
            2 -> R.drawable.ic_star_two
            3 -> R.drawable.ic_star_three
            else -> R.drawable.ic_star_zero
        }

        Icon(
            painter = painterResource(id = starResId),
            contentDescription = "Star",
            tint = Brown60,
            modifier = Modifier.size((44 * scale).dp)
        )
    }
}
