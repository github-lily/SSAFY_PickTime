package com.example.picktimeapp.ui.game

import com.example.picktimeapp.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .width(300.dp)
            .height(800.dp)
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.padding(30.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 음향 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 5.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sound),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onSoundClick(song) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 이미지 async는 uri를 받아올 때 사용함
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.songThumbnailUri)
                        .crossfade(true) // 서서히 보여지는 효과
                        .build(), // 이미지 요청을 최종적으로 완성하라는 뜻
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 별점
                Row {
                    repeat(3) { index ->
                        Icon(
                            painter = painterResource(
                                id = if (index < song.star) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                            ),
                            contentDescription = "Star",
                            modifier = Modifier
                                .size(if (index < song.star) 50.dp else 55.dp),
                                tint = Color.Unspecified //
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 제목
                Text(
                    text = song.title,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 코드
                Text(
                    text = song.songChords.joinToString(" "),
                    fontSize = 35.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // BPM
                Text(
                    text = "${song.bpm} BPM",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray70
                )

                Spacer(modifier = Modifier.height(40.dp))

                // PLAY 버튼
                Button(
                    onClick = { onPlayClick(song) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown20
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(90.dp)
                ) {
                    Text(text = "PLAY", fontSize = 35.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
