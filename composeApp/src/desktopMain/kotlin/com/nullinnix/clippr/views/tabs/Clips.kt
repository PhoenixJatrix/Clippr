package com.nullinnix.clippr.views.tabs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.ImageBitmap
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
import com.nullinnix.clippr.misc.MacApp
import com.nullinnix.clippr.misc.clipTypeToColor
import com.nullinnix.clippr.misc.clipTypeToDesc
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.drawableMap
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.theme.Translucent
import com.nullinnix.clippr.theme.Transparent
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.views.CheckBox
import com.nullinnix.clippr.views.RadioButton
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun Clips (
    isSearching: Boolean,
    clipsViewModel: ClipsViewModel,
    miscViewModel: MiscViewModel
) {
    val clipState = clipsViewModel.clipsState.collectAsState().value
    val pinnedClips = clipState.pinnedClips
    val otherClips = clipState.otherClips

    val selectedPinnedClips = clipState.selectedPinnedClips
    val selectedOtherClips = clipState.selectedOtherClips

    val loadedIcns = miscViewModel.state.collectAsState().value.loadedIcns
    val allApps = miscViewModel.state.collectAsState().value.allApps

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
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(isSearching) {
                                RadioButton(
                                    isSelected = pinnedClips.size == selectedPinnedClips.size
                                ) {
                                    if (pinnedClips.size == selectedPinnedClips.size) {
                                        clipsViewModel.setSelectedPinnedClips(emptySet())
                                    } else {
                                        clipsViewModel.setSelectedPinnedClips(pinnedClips.toSet())
                                    }
                                }
                            }

                            Text(
                                text = "Pinned Clips",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }


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
                ClipTemplate (
                    clip = clip,
                    icns = loadedIcns[clip.source ?: ""],
                    macApp = allApps[clip.source ?: ""],
                    isSelected = clip in selectedPinnedClips,
                    isSearching = isSearching
                ) { action ->
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(isSearching) {
                            RadioButton(
                                isSelected = otherClips.size == selectedOtherClips.size
                            ) {
                                if (otherClips.size == selectedOtherClips.size) {
                                    clipsViewModel.setSelectedOtherClips(emptySet())
                                } else {
                                    clipsViewModel.setSelectedOtherClips(otherClips.toSet())
                                }
                            }
                        }

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
                ClipTemplate(
                    clip = clip,
                    icns = loadedIcns[clip.source ?: ""],
                    macApp = allApps[clip.source ?: ""],
                    isSelected = clip in selectedOtherClips,
                    isSearching = isSearching
                ) { action ->
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
    isSearching: Boolean,
    isSelected: Boolean,
    clip: Clip,
    icns: ImageBitmap?,
    macApp: MacApp?,
    onAction: (ClipAction) -> Unit
) {
    Column (
        modifier = Modifier
    ) {
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

            if (isSearching) {
                RadioButton(
                    isSelected = isSelected
                ) {
                    onAction(ClipAction.ToggleSelectClip(clip))
                }
            } else {
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
            }

            Spacer(Modifier.width(10.dp))

            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 10.dp)
                    .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                    .clip(corners(15.dp))
                    .background(Color.White)
            ) {
                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onAction(ClipAction.OnCopyToClipboard(clip))
                        }
                        .hoverable(interactionSource)
                ) {

                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp), verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row (
                        modifier = Modifier
                            .weight(1f), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatText(clip.content),
                            color = Color.Black,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontFamily = FontFamily(Font(Res.font.Urbanist_Regular)),
                            modifier = Modifier
                        )
                    }

                    Row (
                        Modifier
                            .align(Alignment.End)
                    ){
                        if (icns != null || macApp != null) {
                            clip.source?.let {
                                Row (
                                    modifier = Modifier
                                        .shadow(7.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                                        .clip(RoundedCornerShape(90.dp))
                                        .background(timeCopiedBackgroundAnim)
                                        .padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically
                                ){
                                    icns?.let {
                                        Image (
                                            bitmap = it,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(20.dp)

                                        )
                                    }

                                    macApp?.let {
                                        Spacer(Modifier.width(5.dp))

                                        Text (
                                            text = macApp.name,
                                            fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                            color = timeCopiedTextAnim,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.width(7.dp))
                            }
                        }

                        Row (
                            modifier = Modifier
                                .shadow(7.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                                .clip(RoundedCornerShape(90.dp))
                                .background(clipTypeToColor(clip.associatedIcon))
                                .padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically
                        ){
                            Text (
                                text = clipTypeToDesc(clip.associatedIcon),
                                fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                color = Color.Black,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(Modifier.width(7.dp))

                        Row (
                            modifier = Modifier
                                .shadow(7.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                                .clip(RoundedCornerShape(90.dp))
                                .background(timeCopiedBackgroundAnim)
                                .padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically
                        ){
                            val lines = clip.content.lines().size
                            if (lines > 1) {
                                Text (
                                    text = "$lines lines",
                                    fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                    color = timeCopiedTextAnim,
                                    fontSize = 11.sp
                                )

                                Spacer(Modifier.width(5.dp))

                                Canvas(
                                    modifier = Modifier
                                        .size(5.dp)
                                ) {
                                    drawCircle(color = timeCopiedTextAnim)
                                }

                                Spacer(Modifier.width(5.dp))
                            }

                            Text (
                                text = "${clip.content.length} ${if (clip.content.length == 1) "char" else "chars"}",
                                fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                color = timeCopiedTextAnim,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(Modifier.width(7.dp))

                        Text(
                            text = epochToReadableTime(clip.copiedAt),
                            modifier = Modifier
                                .shadow(7.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = onHoverShadow, spotColor = onHoverShadow)
                                .clip(RoundedCornerShape(90.dp))
                                .background(timeCopiedBackgroundAnim)
                                .padding(horizontal = 7.dp),
                            fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                            color = timeCopiedTextAnim,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}