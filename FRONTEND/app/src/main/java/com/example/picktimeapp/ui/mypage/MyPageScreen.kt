package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.SideNavigation
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown80
import com.example.picktimeapp.ui.theme.DarkGreen10
import com.example.picktimeapp.ui.theme.Gray70
import com.example.picktimeapp.ui.theme.Pretendard
import com.example.picktimeapp.ui.theme.TitleFont
import kotlinx.coroutines.launch

@Composable
fun MyPageScreen(viewModel: MyPageViewModel = hiltViewModel(), navController: NavController) {

    // collectAsState (StateFlow를 UI와 연결하는 방법)
    // - StateFlow를 실시간으로 구독해서 값이 바뀌면 자동으로 업데이트 됨, 리액트의 useState + useEffect
    val userInfo by viewModel.userInfo.collectAsState()
    val pickDayData by viewModel.pickDayData.collectAsState()
    val fullpickDays = viewModel.getFullPickDayList()
    val showLogoutDialog = remember { mutableStateOf(false) }
    val logoutViewModel: LogoutViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {
        SideNavigation(navController = navController)
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = DarkGreen10
        ) {
            //BoxWithConstraints - 화면의 최대 너비 / 높이 값을 알아낼 수 있게 해주는 컴포저블
            BoxWithConstraints {
                val screenWidth = maxWidth
                val screenHeight = maxHeight

                Box(
                    modifier = Modifier.padding(
                        start = (screenWidth.value * 0.075f).dp,
                        top = (screenHeight.value * 0.06f).dp,
                        end = (screenWidth.value * 0.02f).dp,
                        bottom = (screenHeight.value * 0.01f).dp
                    )
                ){
                    //텍스트는 sp 형태라서 Float.sp로 바꿔주는 value를 붙여야함
                    val textSizeLarge = screenWidth.value * 0.04f // 약 5%
                    val textSizeSmall = screenWidth.value * 0.025f
                    val buttonWidth = screenWidth * 0.15f
                    val buttonHeight = screenHeight * 0.06f
                    val buttonFontSize = screenWidth.value * 0.015f

                    Column (
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단영역
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = screenWidth * 0.07f)
                                .fillMaxWidth()
                        ) {

                            // 이미지 영역
                            Box(
                                modifier = Modifier
                                    .weight(0.35f) // 전체의 35%차지
                                    .aspectRatio(1f) //정사각형 비율을 유지한다.
                                    .background(color = Color.White, shape = CircleShape)
                                    .border(width = screenWidth * 0.005f, color = Brown40, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ){
                                //Painter - 이미지를 화면에 그릴 준비를 하는 타입
                                val profileImage: Painter  = painterResource(
                                    id = when (userInfo?.level) {
                                        1 -> R.drawable.profile_level_1
                                        2 -> R.drawable.profile_level_2
                                        3 -> R.drawable.profile_level_3
                                        4 -> R.drawable.profile_level_4
                                        5 -> R.drawable.profile_level_5
                                        6 -> R.drawable.profile_level_6
                                        else -> R.drawable.profile_level_1
                                    }
                                )
                                Image(
                                    painter = profileImage,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                            }
                            // 이미지와 오른쪽 텍스트 영역의 간격
                            Spacer(modifier = Modifier.width(screenWidth * 0.02f))

                            // 사진 오른쪽 영역
                            Column(
                                modifier = Modifier
                                    .weight(0.6f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "피크타임",
                                    fontSize = textSizeLarge.sp,
                                    fontFamily = TitleFont,
                                    modifier = Modifier.padding(top = screenHeight * 0.005f)
                                )

                                Text(
                                    text = "${pickDayData?.continued ?: 8}일 연속 유지중",
                                    fontFamily = TitleFont,
                                    fontSize = textSizeLarge.sp,
                                )

                                Spacer(modifier = Modifier.height(screenHeight * 0.07f))

                                // 유저 정보 출력하기
                                Row (
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    userInfo?.let {
                                        Text(
                                            text = it.name,
                                            fontFamily = TitleFont,
                                            fontSize = textSizeSmall.sp,
                                            modifier = Modifier
                                                .padding(top = screenHeight * 0.001f)
                                        )
                                        IconButton(
                                            onClick = {
                                                navController.navigate(Routes.EDIT_NICKNAME)
                                            },
                                            modifier = Modifier
                                                .padding(start = screenWidth * 0.01f)
                                                .size(screenWidth * 0.03f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Nickname",
                                            )
                                        }
                                    }
                                }

                                // 비밀번호 수정 버튼
                                Button(
                                    onClick = {
                                        navController.navigate(Routes.PASSWORD_CHECK)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x33E0CDA8),
                                        contentColor = Gray70
                                    ),
                                    modifier = Modifier
                                        .padding(top = screenHeight * 0.02f)
                                        .width(buttonWidth)
                                        .height(buttonHeight)
                                ){
                                    Text(
                                        text = "비밀번호 수정",
                                        fontSize = buttonFontSize.sp,
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                        }

                        // 피크데이와 프로필 사이 간격
                        Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                        // 피크데이
                        Box (
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column (
                                modifier = Modifier
                                    .padding(start = screenWidth * 0.015f)
                                    .align(Alignment.CenterStart),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "PickDays",
                                    fontFamily = TitleFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = (screenWidth.value * 0.025f).sp,
                                    modifier = Modifier.padding(bottom = screenHeight * 0.01f)
                                )
                                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                                PickDaysGrid(
                                    pickDays = fullpickDays,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logout),
                                contentDescription = "Logout Image",
                                modifier = Modifier
                                    .padding(
                                        end = screenWidth * 0.01f,
                                        bottom = screenHeight * 0.001f
                                    )
                                    .size(screenWidth * 0.07f)
                                    .clickable {
                                        showLogoutDialog.value = true
                                    }
                            )
                        }
                    }

                    // 로그아웃 모달창
                    if (showLogoutDialog.value) {
                        LogoutDialogCustom(
                            screenWidth = screenWidth,
                            onDismiss = { showLogoutDialog.value = false },
                            onLogout = {
                                coroutineScope.launch {
                                    logoutViewModel.logout {
                                        showLogoutDialog.value = false
                                        navController.navigate(Routes.WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        )
                    }



                }

            }
        }

    }
                }


//로그아웃 팝업창
@Composable
fun LogoutDialogCustom(
    screenWidth: Dp,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .width(screenWidth * 0.4f)
                .padding(horizontal = screenWidth * 0.02f)
        ) {
            val maxWidthDp = maxWidth
            val cornerRadius = maxWidthDp * 0.04f
            val titleFontSize = (maxWidthDp * 0.06f).value.sp
            val subFontSize = (maxWidthDp * 0.035f).value.sp
            val buttonHeight = maxWidthDp * 0.12f
            val buttonFontSize = (maxWidthDp * 0.04f).value.sp

            Box(
                modifier = Modifier
                    .width(maxWidthDp)
                    .wrapContentHeight()
                    .background(Color.White, shape = RoundedCornerShape(cornerRadius))
                    .padding(maxWidthDp * 0.06f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 타이틀
                    Text(
                        text = buildAnnotatedString {
                            append("정말로 ")
                            withStyle(
                                style = SpanStyle(
                                    color = Brown40,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("로그아웃")
                            }
                            append("하시겠습니까?")
                        },
                        fontSize = titleFontSize,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.01f))

                    // 서브 텍스트
                    Text(
                        text = "다시 로그인해야 앱을 사용할 수 있어요.",
                        fontSize = subFontSize,
                        color = Gray70
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.08f))

                    // 버튼 영역
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(maxWidthDp * 0.04f)
                    ) {
                        // 취소 버튼
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE6D3B3)
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "취소",
                                color = Brown80,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 로그아웃 버튼
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown40
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "로그아웃",
                                color = Color.White,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
