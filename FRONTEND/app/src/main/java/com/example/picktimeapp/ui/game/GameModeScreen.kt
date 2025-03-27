package com.example.picktimeapp.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.SideNavigation
import com.example.picktimeapp.ui.nav.Routes

@Composable
fun GameModeScreen(navController: NavController) {
    // 임시 데이터
    val dummySongs = listOf(
        SongData(1, "Winter Christmas", 120, "", "https://picsum.photos/id/101/200", listOf("C", "D", "E", "Am"), 2),
        SongData(2, "Summer Vibes", 90, "", "https://picsum.photos/id/102/200", listOf("A", "G", "E", "Am"), 3),
        SongData(3, "Rainy Mood", 70, "", "https://picsum.photos/id/103/200", listOf("Am", "F", "G", "C"), 1),
        SongData(4, "Morning Light", 100, "", "https://picsum.photos/id/104/200", listOf("C", "G", "Am", "F"), 3),
        SongData(5, "Night Walk", 110, "", "https://picsum.photos/id/105/200", listOf("Dm", "G", "C", "Am"), 2),
        SongData(6, "Island Breeze", 95, "", "https://picsum.photos/id/106/200", listOf("G", "D", "Em", "C"), 1),
        SongData(7, "Dreamer's Sky", 85, "", "https://picsum.photos/id/107/200", listOf("F", "C", "Dm", "G"), 3),
        SongData(8, "Golden Hour", 105, "", "https://picsum.photos/id/108/200", listOf("C", "Am", "F", "G"), 2),
        SongData(9, "Lazy Sunday", 75, "", "https://picsum.photos/id/109/200", listOf("Em", "C", "G", "D"), 2),
        SongData(10, "Firefly Night", 100, "", "https://picsum.photos/id/110/200", listOf("Am", "C", "F", "G"), 1),
        SongData(11, "Spring Bloom", 115, "", "https://picsum.photos/id/111/200", listOf("C", "D", "E", "F"), 3),
        SongData(12, "Ocean Flow", 80, "", "https://picsum.photos/id/112/200", listOf("G", "Em", "C", "D"), 2),
    )

    Row(modifier = Modifier.fillMaxSize()) {

        //네비게이션 만들기
        SideNavigation(navController = navController)

        // 오른쪽 메인 콘텐츠
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 80.dp, end = 80.dp, top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 텍스트 영역
            item {
                Column {
                    Text(
                        text = "GAME",
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 20.dp, start = 10.dp)
                    )
                    Text(
                        text = "기타 챌린지! 얼마나 정확하게 연주할 수 있을까요?",
                        fontSize = 40.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 15.dp)
                    )
                }
            }

            // 카드 그리드
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 4000.dp)
                ) {
                    items(dummySongs) { song ->
                        GameCard(
                            song = song,
                            onPlayClick = {
                                navController.navigate(Routes.GAME_PLAY)
                            },
                            onSoundClick = {
                                println("Sound ${it.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}