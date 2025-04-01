package com.example.picktimeapp.ui.game

import com.example.picktimeapp.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.picktimeapp.ui.theme.Brown20
import com.example.picktimeapp.ui.theme.Gray70
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.picktimeapp.ui.theme.Brown40

data class SongData(
    val songId: Int,
    val title: String,
    val bpm: Int,
    val songUri: String,
    val songThumbnailUri: String,
    val songChords: List<String>,
    val star: Int
)

@Composable
fun GameCard(
    song: SongData,
    onPlayClick: (SongData) -> Unit,
    onSoundClick: (SongData) -> Unit
) {
    BoxWithConstraints {

        // 음악 재생중인지 확인하는 상태
        var isPlaying by remember { mutableStateOf(false) }

        val boxWidth = maxWidth
        val boxheight = maxHeight

        // 전체 카드 묶음
        val cardWidth = boxWidth * 1f
        val cardHeight = boxheight * 1.5f

        // 각각의 카드
        val cardpadding = boxWidth * 0.07f
        val playBtn = cardWidth * 0.08f

        // 각각의 카드 요소 사이즈
        val playBtnSize = cardWidth * 0.08f
        val smallMargin = cardHeight * 0.05f
        val imageSize = cardWidth * 0.6f
        val starSize = cardWidth * 0.15f
        val titleFont = cardWidth.value * 0.09f
        val chordFont = cardWidth.value * 0.07f
        val bpmFont = cardWidth.value * 0.06f
        val buttonHeight = cardHeight * 0.1f
        val buttonFont = cardWidth.value * 0.08f

        val spacingSmall = cardHeight * 0.03f

        Card(
            shape = RoundedCornerShape(cardWidth * 0.07f),
            elevation = CardDefaults.cardElevation(cardWidth * 0.02f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
        ) {
            Box(modifier = Modifier.padding(cardpadding)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 음향 버튼
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.speaker else R.drawable.play_btn),
                            contentDescription = "Sound",
                            tint = Brown40,
                            modifier = Modifier
                                .size(playBtnSize)
                                .clickable {
                                    isPlaying = !isPlaying
                                    onSoundClick(song) }
                        )
                    }

                    Spacer(modifier = Modifier.height(smallMargin))

                    // 이미지 async는 uri를 받아올 때 사용함
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.songThumbnailUri)
                            .crossfade(true) // 서서히 보여지는 효과
                            .build(), // 이미지 요청을 최종적으로 완성하라는 뜻
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 별점
                    Row {
                        repeat(3) { index ->
                            Icon(
                                painter = painterResource(
                                    id = if (index < song.star) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                                ),
                                contentDescription = "Star",
                                modifier = Modifier
                                    .size(starSize),
                                tint = Color.Unspecified //
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 제목
                    Text(
                        text = song.title,
                        fontSize = titleFont.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(spacingSmall))

                    // 코드
                    Text(
                        text = song.songChords.joinToString(" "),
                        fontSize =  chordFont.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // BPM
                    Text(
                        text = "${song.bpm} BPM",
                        fontSize = bpmFont.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray70
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // PLAY 버튼
                    Button(
                        onClick = { onPlayClick(song) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown20
                        ),
                        shape = RoundedCornerShape(cardWidth * 0.2f),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(buttonHeight)
                    ) {
                        Text(text = "PLAY", fontSize = buttonFont.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


    }


}
