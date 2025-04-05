package com.example.picktimeapp.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.SideNavigation
import com.example.picktimeapp.ui.nav.Routes

@Composable
fun GameModeScreen(navController: NavController, viewModel: GameListsViewModel = hiltViewModel()) {

    val gameLists = viewModel.gameLists
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            // 화면 벗어날 때 음악 멈춤
            PreviewSoundPlayer.stopSound()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = maxWidth
        val titleFontSize = maxW.value * 0.04f
        val subTitleFontSize = maxW.value * 0.015f
        val loadingFontSize = maxW.value * 0.025f
        val paddingHorizontal = maxW * 0.05f
        val spacingVertical = maxW * 0.015f
        val gridItemSpacing = maxW * 0.03f

        Row(modifier = Modifier.fillMaxSize()) {

            //네비게이션 만들기
            SideNavigation(navController = navController)

            // 오른쪽 메인 콘텐츠
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = paddingHorizontal, end = paddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacingVertical)
            ) {
                // 텍스트 영역
                item {
                    Column {
                        Text(
                            text = "GAME",
                            fontSize = titleFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 10.dp)
                        )
                        Text(
                            text = "기타 챌린지! 얼마나 정확하게 연주할 수 있을까요?",
                            fontSize = subTitleFontSize.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                // 로딩 중 - 없어도 됨
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text("로딩 중...", fontSize = loadingFontSize.sp)
                        }
                    }
                }

                // 에러 메시지 - 없어도 됨
                errorMessage?.let { message ->
                    item {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = loadingFontSize.sp
                        )
                    }
                }


                // 카드 그리드
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(gridItemSpacing),
                        horizontalArrangement = Arrangement.spacedBy(gridItemSpacing),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 0.dp, max = 4000.dp)
                    ) {
                        items(gameLists) { song ->
                            GameCard(
                                song = SongData(
                                    songId = song.songId,
                                    title = song.title,
                                    bpm = song.bpm,
                                    songUri = song.songUri,
                                    songThumbnailUri = song.songThumbnailUri,
                                    songChords = song.chords,
                                    star = song.star
                                ),
                                onPlayClick = {
                                    navController.navigate("gameguitartunning/${song.songId}")
                                },
                                onSoundClick = {
                                    PreviewSoundPlayer.toggleSound(context, song.songUri)
                                }
                            )
                        }
                    }
                }
            }
        }
    }


}