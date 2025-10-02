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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Baloo2_Regular
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.Urbanist_Regular
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.misc.BROKEN
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.drawableMap
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.theme.Translucent
import com.nullinnix.clippr.theme.Transparent
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun Clips (
    clipsViewModel: ClipsViewModel
) {
    val clipState = clipsViewModel.clipsState.collectAsState().value
    val pinnedClips = clipState.pinnedClips
    val otherClips = clipState.otherClips

    val showAllInteractionSource = remember { MutableInteractionSource() }
    val isHovered by showAllInteractionSource.collectIsHoveredAsState()
    var allPinnedClipsExpanded by remember { mutableStateOf(false) }

    if (pinnedClips.isNotEmpty() || otherClips.isNotEmpty()) {
        LazyColumn {
            item {
                Spacer(Modifier.height(15.dp))
            }

            item {
                if (pinnedClips.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pinned Clips",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (pinnedClips.size > 5) {
                            Text(
                                text = if (!allPinnedClipsExpanded) "Show All (${pinnedClips.size})" else "Show Less",
                                color = Color.Gray,
                                modifier = Modifier
                                    .noGleamTaps {
                                        allPinnedClipsExpanded = !allPinnedClipsExpanded
                                    }
                                    .hoverable(showAllInteractionSource),
                                textDecoration = if (isHovered) TextDecoration.Underline else TextDecoration.None
                            )
                        }
                    }
                }
            }

            items(if (!allPinnedClipsExpanded && pinnedClips.size > 5) pinnedClips.subList(0, 5) else pinnedClips) { clip ->
                ClipTemplate(clip = clip) { action ->
                    clipsViewModel.onAction(action)
                }

                Spacer(Modifier.height(20.dp))
            }


            item {
                if (pinnedClips.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Other Clips",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            items(otherClips) { clip ->
                ClipTemplate(clip = clip) { action ->
                    clipsViewModel.onAction(action)
                }

                Spacer(Modifier.height(20.dp))
            }

            item {
                Spacer(Modifier.height(15.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Try copying something...",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun ClipTemplate (
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
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(10.dp))

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
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 10.dp)
                        .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                        .clip(corners(15.dp))
                        .clickable {
                            onAction(ClipAction.OnCopyToClipboard(clip))
                        }
                        .hoverable(interactionSource)
                        .background(Color.White)
                        .padding(start = 65.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatText(clip.content),
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily(Font(Res.font.Urbanist_Regular)),
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .weight(1f)
                    )
                    //transparent time text to force text weight
                    Text (
                        text = epochToReadableTime(clip.copiedAt),
                        modifier = Modifier
                            .align(Alignment.Bottom)
//                            .weight(1f)
                            .padding(end = 10.dp, bottom = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(timeCopiedBackgroundAnim)
                            .padding(horizontal = 10.dp),
                        fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                        color = Transparent,
                        fontSize = 12.sp
                    )
                }

                //icon and canvas bar
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(drawableMap[if (clip.exists) clip.associatedIcon else BROKEN]!!),
                        contentDescription = "",
                        modifier = Modifier
                            .height(65.dp)
                            .width(55.dp)
                            .padding(5.dp)
                            .shadow(25.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow),
                        contentScale = ContentScale.FillHeight
                    )

                    Spacer(Modifier.width(3.dp))

                    Canvas(
                        modifier = Modifier
                            .height(50.dp)
                    ) {
                        drawLine(
                            color = Translucent,
                            start = Offset.Zero,
                            end = Offset(x = 0f, y = this.size.height),
                            cap = StrokeCap.Round,
                            strokeWidth = 6f
                        )
                    }
                }

                //time copied
                Text(
                    text = epochToReadableTime(clip.copiedAt),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 10.dp)
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