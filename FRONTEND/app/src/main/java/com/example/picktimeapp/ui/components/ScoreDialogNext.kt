package com.example.picktimeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown80

@Composable
fun NextScoreDialogCustom(
    score: Int,
    screenWidth: Dp,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(
            modifier = Modifier
                .width(screenWidth * 0.45f)
                .padding(horizontal = screenWidth * 0.02f)
        ) {
            val maxWidthDp = maxWidth
            val cornerRadius = maxWidthDp * 0.04f
            val titleFontSize = (maxWidthDp * 0.05f).value.sp
            val messageFontSize = (maxWidthDp * 0.035f).value.sp
            val buttonHeight = maxWidthDp * 0.12f
            val buttonFontSize = (maxWidthDp * 0.04f).value.sp
            val starSize = maxWidthDp * 0.15f

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .background(Color.White, shape = RoundedCornerShape(cornerRadius))
                    .padding(maxWidthDp * 0.06f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // ⭐ 별 점수 렌더링
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val starRes = if (index < score) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                            Image(
                                painter = painterResource(id = starRes),
                                contentDescription = "Star ${index + 1}",
                                modifier = Modifier.size(starSize)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 메시지
                    Text(
                        text = "잘했습니다! 다음으로 넘어가볼까요?",
                        fontSize = messageFontSize,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.08f))

                    // 버튼들
                    Row(horizontalArrangement = Arrangement.spacedBy(maxWidthDp * 0.04f)) {
                        // 다시하기
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
                                text = "다시하기",
                                color = Brown80,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 다음으로 넘어가기
                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown40
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "다음으로",
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