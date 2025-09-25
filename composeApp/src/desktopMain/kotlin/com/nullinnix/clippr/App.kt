package com.nullinnix.clippr

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Baloo2_Regular
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.Urbanist_Regular
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.getIconForContent
import com.nullinnix.clippr.model.ClipsViewModel
import com.nullinnix.clippr.theme.Transparent
import com.nullinnix.clippr.theme.White
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    clipsViewModel: ClipsViewModel
) {
    MaterialTheme {
        val pinnedClips = clipsViewModel.clipsState.collectAsState().value.pinnedClips
        val otherClips = clipsViewModel.clipsState.collectAsState().value.otherClips
        val pagerState = rememberPagerState { 2 }

        var currentTab by remember { mutableStateOf<Tab>(Tab.ClipsTab) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(pagerState.currentPage) {
            currentTab = when (pagerState.currentPage) {
                0 -> Tab.ClipsTab
                else -> Tab.SettingsTab
            }
        }

        Column (
            modifier = Modifier
                .background(White)
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Tabs(
                currentTab = currentTab
            ) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = when (it) {
                            Tab.ClipsTab -> 0
                            Tab.SettingsTab -> 1
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState
            ) {
                when (currentTab) {
                    Tab.ClipsTab -> {
                        if (pinnedClips.isNotEmpty() || otherClips.isNotEmpty()) {
                            LazyColumn {
                                item {
                                    Spacer(Modifier.height(15.dp))
                                }

                                item {
                                    if (pinnedClips.isNotEmpty()) {
                                        Text(
                                            text = "Pinned Clips",
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                items(pinnedClips) { clip ->
                                    ClipTemplate(clip) { action ->
                                        clipsViewModel.onAction(action)
                                    }

                                    Spacer(Modifier.height(50.dp))
                                }


                                item {
                                    if (pinnedClips.isNotEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "Other Clips",
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                items(otherClips) { clip ->
                                    ClipTemplate(clip) { action ->
                                        clipsViewModel.onAction(action)
                                    }

                                    Spacer(Modifier.height(50.dp))
                                }

                                item {
                                    Spacer(Modifier.height(15.dp))
                                }
                            }
                        } else {
                            Text(
                                text = "Try copying something...",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 25.sp
                            )
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }
}

@Composable
fun ClipTemplate(
    clip: Clip,
    onAction: (ClipAction) -> Unit
) {
    Column (
        modifier = Modifier
    ){
        val interactionSource = remember { MutableInteractionSource() }
//        val isHovered by interactionSource.collectIsHoveredAsState()
        val isHovered = false

        val timeCopiedTextAnim by animateColorAsState(if (isHovered) Transparent else Color.Gray)
        val timeCopiedBackgroundAnim by animateColorAsState(if (isHovered) Transparent else Color.White)
        val onHoverShadow by animateColorAsState(if (isHovered) Transparent else Color.Black)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.pin),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(corners(90.dp))
                    .clickable {
                        onAction(ClipAction.OnTogglePin(clip))
                    }
                    .padding(5.dp),
                tint = if (clip.isPinned) Color.Black else Color.LightGray
            )

            Spacer(Modifier.width(10.dp))

            Box (
                modifier = Modifier
                    .fillMaxSize()
//                    .clip(corners(10.dp))
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxHeight()
                        .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                        .clip(corners(15.dp))
                        .clickable {
                            onAction(ClipAction.OnAddClip(clip))
                        }
                        .hoverable(interactionSource)
                        .background(Color.White)
                        .padding(horizontal = 3.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(getIconForContent(clip.mimeType, clip.exists)),
                        contentDescription = "",
                        modifier = Modifier
                            .height(65.dp)
                            .clip(corners(7.dp))
                            .padding(3.dp),
                        contentScale = ContentScale.FillHeight
                    )

                    Spacer(Modifier.width(7.dp))

                    Canvas(
                        modifier = Modifier
                            .height(50.dp)
                    ) {
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset.Zero,
                            end = Offset(x = 0f, y = this.size.height),
                            cap = StrokeCap.Round,
                            strokeWidth = 6f
                        )
                    }

                    Spacer(Modifier.width(15.dp))

                    Text(
                        text = formatText(clip.content),
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily(Font(Res.font.Urbanist_Regular))
                    )
                }

                Text(
                    text = epochToReadableTime(clip.copiedAt),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 10.dp)
                        .shadow(10.dp, RoundedCornerShape(10.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                        .clip(RoundedCornerShape(10.dp))
                        .background(timeCopiedBackgroundAnim)
                        .padding(horizontal = 10.dp),
                    fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                    color = timeCopiedTextAnim,
                    fontSize = 12.sp
                )
            }
        }
    }
}