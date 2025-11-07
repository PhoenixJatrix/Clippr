package com.nullinnix.clippr.views.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Baloo2_Regular
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.Urbanist_Regular
import clippr.composeapp.generated.resources.clippr_status_icon_thicker
import clippr.composeapp.generated.resources.finder
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipMenuAction
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.MacApp
import com.nullinnix.clippr.misc.MergeAction
import com.nullinnix.clippr.misc.MergeOptions
import com.nullinnix.clippr.misc.MultiSelectClipMenuAction
import com.nullinnix.clippr.misc.clipTypeToColor
import com.nullinnix.clippr.misc.clipTypeToDesc
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.emptyClip
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.highlightedAnnotatedString
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.misc.relaxedShadow
import com.nullinnix.clippr.misc.toClipType
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import com.nullinnix.clippr.viewmodels.MiscViewModel
import com.nullinnix.clippr.views.ClipDropDownMenu
import com.nullinnix.clippr.views.ClipEdit
import com.nullinnix.clippr.views.ClipInfo
import com.nullinnix.clippr.views.MultiSelectClipDropDownMenu
import com.nullinnix.clippr.views.RadioButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun Clips (
    secondsBeforePaste: Int,
    clipsViewModel: ClipsViewModel,
    miscViewModel: MiscViewModel,
    scrollStates: Pair<LazyListState, LazyListState>,
    onInterceptEvent: (KeyEvent) -> Unit
) {
    val clipState = clipsViewModel.clipsState.collectAsState().value
    val pinnedClips = clipState.pinnedClips
    val otherClips = clipState.otherClips
    val searchResults = clipState.searchResults
    val isSearching = clipState.isSearching
    val selectedClips = clipState.selectedClips
    val showClipEditView = clipState.showClipEditView
    val currentlyPreviewingClip = clipState.currentlyPreviewingClip
    val allPinnedClipsExpanded = clipState.allPinnedClipsExpanded
    val isNewClip = clipState.isNewClip

    val loadedIcns = miscViewModel.state.collectAsState().value.loadedIcns
    val allApps = miscViewModel.state.collectAsState().value.allApps

    val showAllInteractionSource = remember { MutableInteractionSource() }
    val isHovered by showAllInteractionSource.collectIsHoveredAsState()

    val scrollState = scrollStates.first
    val searchScrollState = scrollStates.second
    val coroutine = rememberCoroutineScope()

    val altHeldDown = miscViewModel.state.value.altHeldDown

    var rightClickedClip by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showClipEditView) {
        delay(250)

        if (!showClipEditView) {
            clipsViewModel.setIsNewClip(false)
        }
    }

    Box {
        if (!isSearching) {
            if (pinnedClips.isNotEmpty() || otherClips.isNotEmpty()) {
                LazyColumn (
                    state = scrollState,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(end = 15.dp)
                ){
                    item {
                        Spacer(Modifier.height(15.dp))
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 15.dp)
                                    .clip(corners(90.dp))
                                    .background(Color.Black)
                                    .padding(horizontal = 25.dp, vertical = 7.dp)
                                    .clickable {
                                        clipsViewModel.setIsNewClip(true)
                                        clipsViewModel.setShowClipEditView(true)
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("New Clip  ")

                                        withStyle(SpanStyle(color = Color.White.copy(0.5f))) {
                                            append("⌘N")
                                        }
                                    },
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
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
                                    Text (
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
                                                clipsViewModel.setAllPinnedClipsExpanded(!allPinnedClipsExpanded)
                                            }
                                            .hoverable(showAllInteractionSource),
                                        textDecoration = if (isHovered) TextDecoration.Underline else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = if ((!allPinnedClipsExpanded) && pinnedClips.size > 5) pinnedClips.subList(0, 5) else pinnedClips,
                        key = {it.clipID}
                    ) { clip ->
                        ClipTemplate (
                            clip = clip,
                            icon = loadedIcns[clip.source ?: ""],
                            macApp = allApps[clip.source ?: ""],
                            isSelected = clip in selectedClips,
                            isSearching = false,
                            searchParams = clipState.searchParams,
                            altHeldDown = altHeldDown,
                            rightClickedClip = rightClickedClip,
                            secondsBeforePaste = secondsBeforePaste,
                            numberOfClips = selectedClips.size,
                            onClipMenuAction = {
                                clipsViewModel.onClipMenuAction(it, clip)
                            },
                            onMenuShowEvent = {
                                rightClickedClip = if (it) clip.clipID else null
                            },
                            onAction = { action ->
                                clipsViewModel.onAction(action)
                            },
                            onHover = {
                                miscViewModel.setLastHoveredClip(if (it) clip else null)
                            },
                            onClearSelected = {
                                clipsViewModel.setSelectedClips(emptySet())
                            },
                            onMultiSelectClipMenuAction = {
                                clipsViewModel.onMultiSelectAction(it)
                            },
                            onMergeAction = {action, options ->
                                clipsViewModel.onMergeAction(action, options)
                            },
                            onInterceptEvent = {
                                onInterceptEvent(it)
                            }
                        )

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
                                Text(
                                    text = "Other Clips",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    items(
                        items = otherClips,
                        key = {it.clipID}
                    ) { clip ->
                        ClipTemplate(
                            clip = clip,
                            icon = loadedIcns[clip.source ?: ""],
                            macApp = allApps[clip.source ?: ""],
                            isSelected = clip in selectedClips,
                            isSearching = false,
                            searchParams = clipState.searchParams,
                            altHeldDown = altHeldDown,
                            rightClickedClip = rightClickedClip,
                            secondsBeforePaste = secondsBeforePaste,
                            numberOfClips = selectedClips.size,
                            onClipMenuAction = {
                                clipsViewModel.onClipMenuAction(it, clip)
                            },
                            onMenuShowEvent = {
                                rightClickedClip = if (it) clip.clipID else null
                            },
                            onAction = { action ->
                                clipsViewModel.onAction(action)
                            },
                            onHover = {
                                miscViewModel.setLastHoveredClip(if (it) clip else null)
                            },
                            onClearSelected = {
                                clipsViewModel.setSelectedClips(emptySet())
                            },
                            onMultiSelectClipMenuAction = {
                                clipsViewModel.onMultiSelectAction(it)
                            },
                            onMergeAction = {action, options ->
                                clipsViewModel.onMergeAction(action, options)
                            },
                            onInterceptEvent = {
                                onInterceptEvent(it)
                            }
                        )

                        Spacer(Modifier.height(20.dp))
                    }

                    item {
                        Spacer(Modifier.height(15.dp))
                    }
                }

                VerticalScrollbar (
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 10.dp, bottom = 15.dp, top = 25.dp),
                    style = LocalScrollbarStyle.current.copy(minimalHeight = 35.dp)
                )
            } else {

                Text(
                    text = "Try copying something...",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        } else {
            if (clipState.isOnGoingSearch) {
                Column (
                    modifier = Modifier
                        .fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LaunchedEffect(Unit) {
                        coroutine.launch {
                            searchScrollState.scrollToItem(0)
                        }
                    }

                    Text(
                        text = "Searching all clips...",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp
                    )

                    CircularProgressIndicator(
                        strokeWidth = 10.dp,
                        color = Color.Black,
                        modifier = Modifier
                            .size(70.dp)
                    )
                }
            } else {
                if (searchResults.isNotEmpty()) {
                    Row (
                        modifier = Modifier
                            .padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            RadioButton (
                                isSelected = searchResults.size == selectedClips.size
                            ) {
                                if (searchResults.size == selectedClips.size) {
                                    clipsViewModel.setSelectedClips(emptySet())
                                } else {
                                    clipsViewModel.setSelectedClips(searchResults.toSet())
                                }
                            }

                            Text (
                                text = "${selectedClips.size}/${searchResults.size} clips selected"
                            )
                        }
                    }

                    LazyColumn (
                        state = searchScrollState,
                        modifier = Modifier
                            .padding(end = 15.dp, top = 40.dp)
                    ){
                        item {
                            Spacer(Modifier.height(25.dp))
                        }

                        items(
                            items = searchResults,
                            key = {it.clipID}
                        ) { clip ->
                            ClipTemplate (
                                clip = clip,
                                icon = loadedIcns[clip.source ?: ""],
                                macApp = allApps[clip.source ?: ""],
                                isSelected = clip in selectedClips,
                                isSearching = true,
                                searchParams = clipState.searchParams,
                                altHeldDown = altHeldDown,
                                rightClickedClip = rightClickedClip,
                                secondsBeforePaste = secondsBeforePaste,
                                numberOfClips = selectedClips.size,
                                onClipMenuAction = {
                                    clipsViewModel.onClipMenuAction(it, clip)
                                },
                                onMenuShowEvent = {
                                    rightClickedClip = if (it) clip.clipID else null
                                },
                                onAction = { action ->
                                    clipsViewModel.onAction(action)
                                },
                                onHover = {
                                    miscViewModel.setLastHoveredClip(if (it) clip else null)
                                },
                                onClearSelected = {
                                    clipsViewModel.setSelectedClips(emptySet())
                                },
                                onMultiSelectClipMenuAction = {
                                    clipsViewModel.onMultiSelectAction(it)
                                },
                                onMergeAction = {action, options ->
                                    clipsViewModel.onMergeAction(action, options)
                                },
                                onInterceptEvent = {
                                    onInterceptEvent(it)
                                }
                            )

                            Spacer(Modifier.height(20.dp))
                        }

                        item {
                            Spacer(Modifier.height(15.dp))
                        }
                    }

                    VerticalScrollbar (
                        adapter = rememberScrollbarAdapter(searchScrollState),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(end = 10.dp, bottom = 15.dp, top = 25.dp),
                        style = LocalScrollbarStyle.current.copy(minimalHeight = 35.dp)
                    )
                } else {
                    Column (
                        modifier = Modifier
                            .fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No clips match keywords",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp,
                            modifier = Modifier
                        )
                    }
                }
            }
        }

        if (showClipEditView) {
            ClipEdit(
                clip = if (isNewClip) emptyClip() else currentlyPreviewingClip,
                icon = if (isNewClip) loadedIcns["com.nullinnix.clippr"] else loadedIcns[currentlyPreviewingClip?.source ?: ""],
                macApp = if (isNewClip) allApps["com.nullinnix.clippr"] else allApps[currentlyPreviewingClip?.source ?: ""],
                isNewClip = isNewClip,
                onSaveAction = {
                    clipsViewModel.onSaveAction(it)
                },
                onClose = {
                    clipsViewModel.setShowClipEditView(false)
                },
                onInterceptEvent = {
                    onInterceptEvent(it)
                },
                onClipEdited = {
                    clipsViewModel.setEditedClip(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClipTemplate (
    isSearching: Boolean,
    isSelected: Boolean,
    numberOfClips: Int,
    searchParams: String,
    clip: Clip,
    icon: ImageBitmap?,
    macApp: MacApp?,
    altHeldDown: Boolean,
    rightClickedClip: String?,
    secondsBeforePaste: Int,
    onAction: (ClipAction) -> Unit,
    onClipMenuAction: (ClipMenuAction) -> Unit,
    onMultiSelectClipMenuAction: (MultiSelectClipMenuAction) -> Unit,
    onMergeAction: (MergeAction, MergeOptions) -> Unit,
    onMenuShowEvent: (Boolean) -> Unit,
    onHover: (Boolean) -> Unit,
    onClearSelected: () -> Unit,
    onInterceptEvent: (KeyEvent) -> Unit
) {
    Column (
        modifier = Modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        var showMenu by remember { mutableStateOf(false) }
        var menuPosition by remember { mutableStateOf(0.dp) }
        var delayedHoverEmits by remember { mutableStateOf(false) }
        val isHovered = interactionSource.collectIsHoveredAsState().value
        var copiedAt by remember { mutableStateOf(epochToReadableTime(clip.copiedAt)) }

        val highLightClip =
            if (rightClickedClip != null) {
                if (isSearching && isSelected) {
                    true
                } else {
                    showMenu
                }
            } else {
                true
            }

        LaunchedEffect(Unit) {
            delay(100)
            delayedHoverEmits = true
        }

        LaunchedEffect(isHovered) {
            if (delayedHoverEmits) {
                if (!showMenu) {
                    onHover(isHovered)
                }
            }
        }

        LaunchedEffect(showMenu) {
            if (showMenu) {
                onHover(true)
            } else {
                onHover(false)
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                copiedAt = epochToReadableTime(clip.copiedAt)
                delay(3000)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .alpha(if (highLightClip) 1f else 0.45f),
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
                    painter = painterResource(Res.drawable.clippr_status_icon_thicker),
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
                    .relaxedShadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = Color.Gray, spotColor = Color.Gray)
                    .clip(corners(15.dp))
                    .background(Color.White)
                    .onPointerEvent(PointerEventType.Press) { event ->
                        if (event.buttons.isSecondaryPressed) {
                            onMenuShowEvent(rightClickedClip == null)
                            menuPosition = event.changes.first().position.x.dp
                            showMenu = true
                        } else {
                            showMenu = false
                        }

                        if (isSearching && !isSelected) {
                            onClearSelected()
                        }
                    }
            ) {
                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onAction(ClipAction.OnCopyToClipboard(clip, altHeldDown))
                        }
                        .hoverable(interactionSource)
                ) {

                }

                if (showMenu) {
                    if (isSearching && numberOfClips > 1) {
                        MultiSelectClipDropDownMenu (
                            menuXPosition = menuPosition,
                            secondsBeforePaste = secondsBeforePaste,
                            numberOfClips = numberOfClips,
                            onAction = {
                                onMultiSelectClipMenuAction(it)
                                onMenuShowEvent(false)
                                showMenu = false
                            },
                            onDismiss = {
                                onMenuShowEvent(false)
                                showMenu = false
                            },
                            onMergeAction = { action, options ->
                                onMergeAction(action, options)
                                onMenuShowEvent(false)
                                showMenu = false
                            },
                            onInterceptEvent = {
                                onInterceptEvent(it)
                            }
                        )
                    } else {
                        ClipDropDownMenu (
                            menuXPosition = menuPosition,
                            secondsBeforePaste = secondsBeforePaste,
                            clip = clip,
                            onDismiss = {
                                onMenuShowEvent(false)
                                showMenu = false
                            },
                            onAction = {
                                onClipMenuAction(it)
                                onMenuShowEvent(false)
                                showMenu = false
                            },
                            onInterceptEvent = {
                                onInterceptEvent(it)
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp), verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row (
                        modifier = Modifier, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = highlightedAnnotatedString(formatText(clip.content), listOf(searchParams)),
                            color = Color.Black,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
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
                        if (isSearching && clip.isPinned) {
                            Row (
                                modifier = Modifier
                                    .relaxedShadow(5.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Gray, spotColor = Color.Gray)
                                    .clip(RoundedCornerShape(90.dp))
                                    .height(22.dp)
                                    .background(Color.Black)
                                    .clickable(false) {}
                                    .padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.clippr_status_icon_thicker),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(16.dp)
                                        .clip(corners(90.dp)),
                                    tint = Color.White
                                )
                            }

                            Spacer(Modifier.width(7.dp))
                        }

                        if (clip.edited == true) {
                            ClipInfo(
                                content = "Edited",
                                enabled = false
                            )
                        }

                        Spacer(Modifier.width(7.dp))

                        if (macApp != null) {
                            clip.source?.let {
                                ClipInfo(
                                    content = highlightedAnnotatedString(macApp.name.coerce(30), listOf(searchParams)),
                                    enabled = !isSearching,
                                    prefix = {
                                        if (icon != null) {
                                            Image(
                                                bitmap = icon,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(20.dp)
                                            )
                                        } else if (clip.source == "com.apple.finder") {
                                            Image(
                                                painter = painterResource(Res.drawable.finder),
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(2.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onAction(ClipAction.FilterBySource(clip.source))
                                    }
                                )

                                Spacer(Modifier.width(7.dp))
                            }
                        }

                        ClipInfo(
                            content = highlightedAnnotatedString(clipTypeToDesc(clip.associatedIcon), listOf(searchParams)),
                            enabled = !isSearching,
                            prefix = {
                                Canvas(
                                    modifier = Modifier
                                        .size(7.dp)
                                ) {
                                    drawCircle(color = clipTypeToColor(clip.associatedIcon))
                                }
                            },
                            onClick = {
                                onAction(ClipAction.FilterByType(clip.associatedIcon.toClipType()))
                            }
                        )

                        Spacer(Modifier.width(7.dp))

                        if (clip.associatedIcon.toClipType() == ClipType.PLAIN_TEXT) {
                            ClipInfo(
                                content = "${clip.content.length} ${if (clip.content.length == 1) "char" else "chars"}",
                                enabled = false,
                                prefix = {
                                    val lines = clip.content.lines().size
                                    if (lines > 1) {
                                        Text (
                                            text = "$lines lines",
                                            fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )

                                        Spacer(Modifier.width(5.dp))

                                        Canvas(
                                            modifier = Modifier
                                                .size(5.dp)
                                        ) {
                                            drawCircle(color = Color.Gray)
                                        }
                                    }
                                },
                                onClick = {
                                    onAction(ClipAction.FilterByType(clip.associatedIcon.toClipType()))
                                }
                            )

                            Spacer(Modifier.width(7.dp))
                        }

                        ClipInfo(
                            content = copiedAt,
                            enabled = false
                        )
                    }
                }

                if (altHeldDown && clip.associatedIcon.toClipType() != ClipType.PLAIN_TEXT) {
                    Text (
                        text = "⌥ + left click to copy as file",
                        fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                        color = Color.Black,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 5.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}