package com.nullinnix.clippr

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Baloo2_Regular
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.Urbanist_Regular
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.misc.COPY
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipActions
import com.nullinnix.clippr.misc.TOGGLE_PIN
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.getIconForContent
import com.nullinnix.clippr.misc.onCopy
import com.nullinnix.clippr.model.ViewModel
import com.nullinnix.clippr.theme.Black
import com.nullinnix.clippr.theme.Translucent
import com.nullinnix.clippr.theme.Transparent
import org.jetbrains.compose.resources.Font

@Composable
@Preview
fun App() {
    MaterialTheme {
        val onClipActions = {action: ClipActions, clip: Clip ->
            when(action) {
                TOGGLE_PIN -> {
                    ViewModel.clips[clip.clipID]!!.isPinned = !ViewModel.clips[clip.clipID]!!.isPinned
                }

                COPY -> {
                    onCopy(clip.content, clip.mimeType)
                }
            }
        }

        Column(
            modifier = Modifier
                .background(Black)
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn {
                item {
                    Spacer(Modifier.height(15.dp))
                }

                item {
                    if (ViewModel.pinnedClipKeys.isNotEmpty()) {
                        Text(
                            text = "Pinned Clips",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(ViewModel.pinnedClipKeys.size) {clipIndex ->
                    ClipTemplate(ViewModel.clips[ViewModel.pinnedClipKeys[(ViewModel.pinnedClipKeys.size - 1) - clipIndex]]!!) {action ->
                        onClipActions(action, ViewModel.clips[ViewModel.pinnedClipKeys[(ViewModel.pinnedClipKeys.size - 1) - clipIndex]]!!)
                    }

                    Spacer(Modifier.height(5.dp))
                }


                item {
                    if (ViewModel.pinnedClipKeys.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Other Clips",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                items(ViewModel.otherClipKeys.size) {clipIndex ->
                    ClipTemplate(ViewModel.clips[ViewModel.otherClipKeys[(ViewModel.otherClipKeys.size - 1) - clipIndex]]!!) {action ->
                        onClipActions(action, ViewModel.clips[ViewModel.otherClipKeys[(ViewModel.otherClipKeys.size - 1) - clipIndex]]!!)
                    }

                    Spacer(Modifier.height(5.dp))
                }

                item {
                    Spacer(Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
fun ClipTemplate(
    clip: Clip,
    onAction: (ClipActions) -> Unit
) {
    Column (
        modifier = Modifier
    ){
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val timeCopiedTextAnim by animateColorAsState(if (isHovered) Transparent else Color.Gray)
        val timeCopiedBackgroundAnim by animateColorAsState(if (isHovered) Transparent else Black)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clickable {
                    onAction(COPY)
                }
                .hoverable(interactionSource)
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.pin),
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        onAction(TOGGLE_PIN)
                    },
                tint = if (clip.isPinned) Color.White else Color.DarkGray
            )

            Spacer(Modifier.width(10.dp))

            Box (
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(corners(10.dp))
                        .background(Translucent)
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
                            .background(Color.DarkGray)
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
                        color = Color.White,
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
                        .clip(RoundedCornerShape(topStart = 7.dp, bottomEnd = 10.dp))
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