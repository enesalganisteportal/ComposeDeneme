package com.algan.composedeneme.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.algan.composedeneme.presentation.viewmodel.VideoViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Eksik olan import
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.algan.composedeneme.R
import com.algan.composedeneme.presentation.state.VideoState
import com.algan.composedeneme.service.ForegroundService


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(viewModel: VideoViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    Box {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated visibility for logo to show/hide based on search focus
                AnimatedVisibility(visible = !isSearchFocused) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp) // Adjust size as needed
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // Space between logo and SearchBar

                // Search bar with focus tracking
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearch = { viewModel.searchVideos(searchQuery) },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isSearchFocused = focusState.isFocused
                        }
                )
            }


            when (val state = viewModel.videoState.value) {
                is VideoState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is VideoState.Success -> {
                    LazyColumn {
                        items(state.data) { video ->
                            VideoCard(
                                video,
                                onAudioDownloadClick = {
                                    viewModel.startYouTubeToMP3ConversionService(
                                        context,
                                        video.urlSuffix
                                    )
                                },
                                onVideoDownloadClick = {

                                }
                            )
                        }
                    }
                }

                is VideoState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

