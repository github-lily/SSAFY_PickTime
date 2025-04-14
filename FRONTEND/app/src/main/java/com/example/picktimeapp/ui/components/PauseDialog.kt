package com.example.picktimeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown80
import com.example.picktimeapp.ui.theme.Gray50
import com.example.picktimeapp.util.CameraAnalyzerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun PauseDialogCustom(
    screenWidth: Dp,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {

        BoxWithConstraints(
            modifier = Modifier
                .width(screenWidth * 0.45f)
                .padding(horizontal = screenWidth * 0.02f) // 좌우 여백
        ) {
            val maxWidthDp = maxWidth
            val cornerRadius = maxWidthDp * 0.04f
            val titleFontSize = (maxWidthDp * 0.06f).value.sp
            val subFontSize = (maxWidthDp * 0.035f).value.sp
            val buttonHeight = maxWidthDp * 0.12f
            val buttonFontSize = (maxWidthDp * 0.04f).value.sp

            Box(
                modifier = Modifier
                    .width(maxWidthDp * 1.0f) // 진짜 너비 조절은 여기!
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
                                append("종료")
                            }
                            append("하시겠습니까?")
                        },
                        fontSize = titleFontSize,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.01f))

                    // 서브 텍스트
                    Text(
                        text = "지금까지의 내용들은 저장되지 않습니다.",
                        fontSize = subFontSize,
                        color = Gray50
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.08f))

                    // 버튼 영역
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(maxWidthDp * 0.04f)
                    ) {
                        // 이어하기 버튼
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
                                text = "이어하기",
                                color = Brown80,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 종료하기 버튼
                        val cameraAnalyzerViewModel : CameraAnalyzerViewModel = hiltViewModel()
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                cameraAnalyzerViewModel.deleteSession(context)
                                onExit()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown40
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "종료하기",
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
