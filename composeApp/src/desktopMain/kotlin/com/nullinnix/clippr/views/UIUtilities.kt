package com.nullinnix.clippr.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.back
import clippr.composeapp.generated.resources.check
import clippr.composeapp.generated.resources.close
import clippr.composeapp.generated.resources.filter
import clippr.composeapp.generated.resources.full_screen
import clippr.composeapp.generated.resources.right
import clippr.composeapp.generated.resources.search
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipMenuAction
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.MergeAction
import com.nullinnix.clippr.misc.MergeOptions
import com.nullinnix.clippr.misc.MultiSelectClipMenuAction
import com.nullinnix.clippr.misc.Notification
import com.nullinnix.clippr.misc.SearchAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.desc
import com.nullinnix.clippr.misc.getClipMenuActions
import com.nullinnix.clippr.misc.info
import com.nullinnix.clippr.misc.name
import com.nullinnix.clippr.misc.noGleamCombinedClickable
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.misc.shortcut
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.theme.Transparent
import com.nullinnix.clippr.viewmodels.NotificationsViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import java.awt.MouseInfo
import java.awt.Window
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun Tabs (
    isFocused: Boolean,
    currentTab: Tab,
    onTabChanged: (Tab) -> Unit
) {
    Row (
        modifier = Modifier
            .width(200.dp)
            .height(50.dp)
            .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(corners(15.dp))
            .background(Color.White)
    ) {
        TabElement(
            isFocused = isFocused,
            isSelected = currentTab == Tab.ClipsTab,
            tab = Tab.ClipsTab
        ) {
            onTabChanged(it)
        }

        TabElement(
            isFocused = isFocused,
            isSelected = currentTab == Tab.SettingsTab,
            tab = Tab.SettingsTab
        ) {
            onTabChanged(it)
        }
    }
}

@Composable
fun TabElement (
    isFocused: Boolean,
    isSelected: Boolean,
    tab: Tab,
    onClick: (Tab) -> Unit
) {
    val textColorAnim by animateColorAsState(if (isSelected) Color.Black else Color.Gray)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp)
            .clickable(!isSelected) {
                onClick(tab)
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = tab.name(),
            color = textColorAnim.copy(if (isFocused) textColorAnim.alpha else 0.5f),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun WindowBar (
    window: Window,
    isFocused: Boolean,
    onToggleFullScreen: () -> Unit,
    onHideMainApp: () -> Unit
) {
    val hoverSource = remember { MutableInteractionSource() }
    val onHover by hoverSource.collectIsHoveredAsState()

    val closeOpacityAnim by animateFloatAsState(if (onHover) 1f else if (isFocused) 0.45f else 0.15f, animationSpec = tween(500))

    val closeShadowColor by animateColorAsState(if (onHover) Color.Red else Transparent, animationSpec = tween(500))
    val maximizeShadowColor by animateColorAsState(if (onHover) Color.Green else Transparent, animationSpec = tween(500))

    var startX by remember { mutableIntStateOf(0) }
    var startY by remember { mutableIntStateOf(0) }
    var windowStartX by remember { mutableIntStateOf(0) }
    var windowStartY by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(HeaderColor)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ){
            Row (
                modifier = Modifier
                    .hoverable(hoverSource)
                    .padding(15.dp)
            ){
                Box (
                    modifier = Modifier
                        .size(30.dp)
                        .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = closeShadowColor, spotColor = closeShadowColor)
                        .clip(corners())
                        .noGleamTaps {
                            onHideMainApp()
                        }
                        .background(Color.White)
                        .padding(10.dp), contentAlignment = Alignment.Center
                ) {
                    Icon (
                        painter = painterResource(Res.drawable.close),
                        contentDescription = "",
                        tint = Color.DarkGray.copy(closeOpacityAnim)
                    )
                }

                Spacer(Modifier.width(7.dp))

                Box (
                    modifier = Modifier
                        .size(30.dp)
                        .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = maximizeShadowColor, spotColor = maximizeShadowColor)
                        .clip(corners())
                        .noGleamTaps {
                            onToggleFullScreen()
                        }
                        .background(Color.White)
                        .padding(10.dp), contentAlignment = Alignment.Center
                ) {
                    Icon (
                        painter = painterResource(Res.drawable.full_screen),
                        contentDescription = "",
                        tint = Color.DarkGray.copy(closeOpacityAnim)
                    )
                }
            }

            Row (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                val mouse = MouseInfo.getPointerInfo().location
                                startX = mouse.x
                                startY = mouse.y
                                windowStartX = window.x
                                windowStartY = window.y
                            },
                            onDrag = { change, _ ->
                                val mouse = MouseInfo.getPointerInfo().location
                                val dx = mouse.x - startX
                                val dy = mouse.y - startY
                                window.setLocation(windowStartX + dx, windowStartY + dy)
                                change.consume()
                            }
                        )
                    }
                    .noGleamCombinedClickable(
                        onDoubleClick = {
                            onToggleFullScreen()
                        }
                    )
            ){

            }
        }

        Text(
            text = "Clippr",
            color = if (isFocused) Color.Black else Color.LightGray,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }

    Canvas (
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        drawLine(color = Color.Black.copy(0.25f), start = Offset.Zero, end = Offset(this.size.width, 0f))
    }
}

@Composable
fun CheckBox(
    enabled: Boolean = false,
    isChecked: Boolean,
    onClick: (Boolean) -> Unit
) {
    val tickSize = 22
    val checkboxWidth = 50
    val tickPosition by animateIntAsState(
        targetValue = if (isChecked) (checkboxWidth - tickSize) - 3 else 3,
        label = "",
        animationSpec = tween(500)
    )

    val tickColorAnim by animateColorAsState (
        targetValue = if (isChecked) Color.Black else Color.Gray, label = "", animationSpec = tween(500)
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(checkboxWidth.dp)
            .height(28.dp)
            .shadow(10.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(corners(90.dp))
            .hoverable(interactionSource, enabled)
            .background(Color.White)
            .clickable(enabled) {
                onClick(!isChecked)
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = tickPosition.dp)
                .size(tickSize.dp)
                .clip(corners(90.dp))
                .background(tickColorAnim), contentAlignment = Alignment.Center
        ) {

        }
    }
}

@Composable
fun SearchBar (
    window: Window,
    isSearching: Boolean,
    clipState: ClipsState,
    onAction: (SearchAction) -> Unit
) {
    val widthAnim by animateDpAsState(if (isSearching) window.width.dp else 300.dp)
    val searchParams = clipState.searchParams
    val customFiltersApplied = clipState.customFilterApplied

    val focusRequester by remember {
        mutableStateOf(FocusRequester())
    }

    Row (
        modifier = Modifier
            .width(widthAnim)
            .padding(10.dp)
    ){
        if (isSearching) {
            Icon(
                painter = painterResource(Res.drawable.back),
                contentDescription = "",
                tint = Color.Black,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(10.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                    .clip(corners(90.dp))
                    .background(Color.White)
                    .clickable {
                        onAction(SearchAction.OnExit)
                    }
                    .padding(10.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Row (
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .shadow(10.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(corners(90.dp))
                .background(HeaderColor)
                .noGleamTaps (!isSearching) {
                    onAction(SearchAction.OnSearchStart)
                }
                .padding(10.dp)
                .padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(!isSearching) {
                Icon(
                    painter = painterResource(Res.drawable.search),
                    contentDescription = "",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(15.dp)
                )
            }

            if (!isSearching) {
                Spacer(Modifier.width(5.dp))

                Text(
                    text = "Search/filter/select",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.width(15.dp))
            } else {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = searchParams,
                    onValueChange = {
                        onAction(SearchAction.SearchParamsChanged(it.replace("\n", "")))
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.Black),
                    decorationBox = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            if (searchParams.isEmpty()) {
                                Text(
                                    text = "69 ideas for Grindr dates",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            it()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                    ,
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        if (isSearching) {
            Box {
                Icon (
                    painter = painterResource(Res.drawable.filter),
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(10.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                        .clip(corners(90.dp))
                        .background(Color.White)
                        .clickable {
                            onAction(SearchAction.Filter)
                        }
                        .padding(10.dp)
                )

                if (customFiltersApplied) {
                    Canvas (
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        drawCircle(color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun RadioButton (
    isSelected: Boolean,
    onToggle: () -> Unit
){
    Box(
        modifier = Modifier
            .padding(end = 10.dp)
            .size(20.dp)
            .clip(corners(5.dp))
            .background(if (isSelected) Color.Black else Color.LightGray)
            .clickable {
                onToggle()
            }
            .padding(5.dp), contentAlignment = Alignment.Center
    ) {
        Icon (
            painter = painterResource(Res.drawable.check),
            contentDescription = "",
            modifier = Modifier,
            tint = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
fun PopupMenu (
    background: Color = Transparent,
    closeFromChild: Boolean = false,
    showStartAnim: Boolean = true,
    showEndAnim: Boolean = true,
    showAfterAnimate: Boolean = false,
    durationStart: Int = 300,
    durationEnd: Int = 300,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    var showMenu by remember {
        mutableStateOf(false)
    }

    val menuAnim by animateDpAsState(
        targetValue =
            if (showMenu)
                0.dp
            else
                1000.dp,
        label = "",
        animationSpec = tween(
            if (showMenu)
                if (showStartAnim)
                    durationStart
                else
                    0
            else
                if (showEndAnim)
                    durationEnd
                else
                    0
        )
    )

    val bg by animateColorAsState(targetValue = if(showMenu) background else Transparent, label = "")

    if (closeFromChild) {
        LaunchedEffect(key1 = Unit) {
            showMenu = false
            delay(durationEnd.toLong())
            onClose()
        }
    }

    LaunchedEffect(key1 = Unit) {
        showMenu = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .noGleamTaps {

            }
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = menuAnim)
        ) {
            if (showAfterAnimate) {
                if (menuAnim == 0.dp) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

@Composable
fun ClipDropDownMenu (
    menuXPosition: Dp,
    clip: Clip,
    secondsBeforePaste: Int,
    onAction: (ClipMenuAction) -> Unit,
    onDismiss: () -> Unit,
    onInterceptEvent: (KeyEvent) -> Unit
) {
    var currentHoverAction by remember { mutableStateOf<ClipMenuAction?>(null) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            focusRequester.requestFocus()
        }
    }

    DropdownMenu(
        expanded = true,
        onDismissRequest = {
            onDismiss()
        },
        offset = DpOffset(menuXPosition, 0.dp),
        properties = PopupProperties(focusable = true),
        modifier = Modifier
            .padding(7.dp)
            .clip(corners(10.dp))
            .animateContentSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onPreviewKeyEvent { event ->
                onInterceptEvent(event)
                true
            }
    ) {
        getClipMenuActions(clip).forEach { option ->
            val interactionSource = remember { MutableInteractionSource() }
            val isHover = interactionSource.collectIsHoveredAsState().value

            LaunchedEffect(isHover) {
                if (isHover) {
                    currentHoverAction = option
                }
            }

            DropdownMenuItem (
                onClick = {
                    onAction(option)
                },
                content = {
                    Row(
                        modifier = Modifier
                            .widthIn(min = 350.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.desc(secondsBeforePaste),
                            fontSize = 14.sp,
                            color = if (option == ClipMenuAction.Delete) Color.Red else Color.Black
                        )

                        Text(
                            text = option.shortcut(),
                            fontSize = 14.sp,
                            color = Color.Black.copy(0.5f)
                        )
                    }
                },
                modifier = Modifier
                    .clip(corners(10.dp))
                    .hoverable(interactionSource)
            )
        }

        currentHoverAction?.let {
            Text (
                text = it.info(secondsBeforePaste),
                color = Color.Black.copy(0.5f)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiSelectClipDropDownMenu (
    menuXPosition: Dp,
    secondsBeforePaste: Int,
    onAction: (MultiSelectClipMenuAction) -> Unit,
    onMergeAction: (MergeAction, MergeOptions) -> Unit,
    onDismiss: () -> Unit,
    onInterceptEvent: (KeyEvent) -> Unit
) {
    var currentHoverAction by remember { mutableStateOf<MultiSelectClipMenuAction?>(null) }
    var mergeXPosition by remember { mutableStateOf(menuXPosition) }
    var trim by remember { mutableStateOf(true) }
    var saveToDesktop by remember { mutableStateOf(false) }
    var deleteOriginal by remember { mutableStateOf(false) }
    var copyAfterMerge by remember { mutableStateOf(false) }
    var removeDuplicates by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            focusRequester.requestFocus()
        }
    }

    DropdownMenu (
        expanded = true,
        onDismissRequest = {
            onDismiss()
        },
        offset = DpOffset(menuXPosition, 0.dp),
        properties = PopupProperties(focusable = true),
        modifier = Modifier
            .padding(7.dp)
            .clip(corners(10.dp))
            .animateContentSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onPreviewKeyEvent { event ->
                onInterceptEvent(event)
                true
            }
    ) {
        MultiSelectClipMenuAction.entries.forEach { option ->
            val interactionSource = remember { MutableInteractionSource() }
            val isHover = interactionSource.collectIsHoveredAsState().value

            LaunchedEffect(isHover) {
                if (isHover) {
                    currentHoverAction = option
                }
            }

            DropdownMenuItem (
                onClick = {
                    if (option != MultiSelectClipMenuAction.Merge) {
                        onAction(option)
                    }
                },
                content = {
                    Row(
                        modifier = Modifier
                            .widthIn(min = 350.dp)
                            .background(Color.White),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.desc(secondsBeforePaste),
                            fontSize = 14.sp,
                            color = if (option == MultiSelectClipMenuAction.DeleteAll) Color.Red else Color.Black
                        )

                        if (option != MultiSelectClipMenuAction.Merge) {
                            Text(
                                text = option.shortcut(),
                                fontSize = 14.sp,
                                color = Color.Black.copy(0.5f)
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.right),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(20.dp),
                                tint = Color.Black.copy(0.5f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .clip(corners(10.dp))
                    .hoverable(interactionSource)
                    .onPointerEvent(PointerEventType.Press) { event ->
                        mergeXPosition = event.changes.first().position.x.dp
                    }
            )
        }

        currentHoverAction?.let {
            Text (
                text = it.info(secondsBeforePaste),
                color = Color.Black.copy(0.5f)
            )
        }
    }

    if (currentHoverAction == MultiSelectClipMenuAction.Merge) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {},
            offset = DpOffset(x = mergeXPosition + 280.dp, y = 0.dp),
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .padding(7.dp)
                .clip(corners(10.dp))
                .animateContentSize()
        ) {
            var currentHoverMerge by remember { mutableStateOf<MergeAction?>(null) }

            Text (
                text = "Merge all",
                color = Color.Black,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(5.dp))

            MergeAction.entries.forEach { option ->
                val interactionSource = remember { MutableInteractionSource() }
                val isHover = interactionSource.collectIsHoveredAsState().value

                LaunchedEffect(isHover) {
                    if (isHover) {
                        currentHoverMerge = option
                    }
                }

                DropdownMenuItem (
                    onClick = {
                        onMergeAction(option, MergeOptions(removeDuplicates = removeDuplicates, saveToDesktop = saveToDesktop, trim = trim, copyAfterMerge = copyAfterMerge, deleteOriginal = deleteOriginal))
                    },
                    content = {
                        Row(
                            modifier = Modifier
                                .widthIn(min = 350.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.desc(),
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    },
                    modifier = Modifier
                        .clip(corners(10.dp))
                        .hoverable(interactionSource)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text (
                text = "Pre-merge",
                color = Color.Black.copy(0.5f)
            )

            Spacer(Modifier.height(5.dp))

            MergeOption(
                label = "Trim (start/end)",
                isSelected = trim
            ) {
                trim = !trim
            }

            Spacer(Modifier.height(5.dp))

            MergeOption(
                label = "Remove duplicates",
                isSelected = removeDuplicates
            ) {
                removeDuplicates = !removeDuplicates
            }

            Spacer(Modifier.height(10.dp))

            Text (
                text = "Post-merge",
                color = Color.Black.copy(0.5f)
            )

            Spacer(Modifier.height(5.dp))

            MergeOption(
                label = "Save to desktop",
                isSelected = saveToDesktop
            ) {
                saveToDesktop = !saveToDesktop
            }

            MergeOption(
                label = "Delete original clips",
                isSelected = deleteOriginal
            ) {
                deleteOriginal = !deleteOriginal
            }

            MergeOption(
                label = "Copy clip after merge",
                isSelected = copyAfterMerge
            ) {
                copyAfterMerge = !copyAfterMerge
            }

            currentHoverMerge?.let {
                Text (
                    text = it.info(),
                    color = Color.Black.copy(0.5f)
                )
            }
        }
    }
}

@Composable
fun MergeOption (
    label: String,
    isSelected: Boolean,
    onAction: () -> Unit
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(corners(10.dp))
            .clickable {
                onAction()
            }
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ){
        RadioButton(
            isSelected = isSelected
        ) {
            onAction()
        }

        Text(
            text = label
        )
    }
}

@Composable
fun Notifications (
    modifier: Modifier,
    notificationsViewModel: NotificationsViewModel,
    onDismiss: (Notification) -> Unit
){
    val notifications = notificationsViewModel.notificationsState.collectAsState().value.notifications

    LazyColumn(
        modifier = modifier
            .animateContentSize(alignment = Alignment.BottomEnd)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            Notification(
                notification = notification,
                onDismiss = { onDismiss(notification) }
            )
        }
    }
}

@Composable
fun Notification (
    notification: Notification,
    onDismiss: () -> Unit
) {
    var timeLeft by remember { mutableStateOf((notification.startedAt + notification.duration) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) }
    var offset by remember { mutableStateOf(350.dp) }

    val notificationXOffsetAnim by animateDpAsState(offset)

    LaunchedEffect(Unit) {
        delay(300)

        offset = 0.dp

        while (true) {
            delay(500)
            timeLeft = (notification.startedAt + notification.duration) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

            if (timeLeft <= 1) {
                offset = 350.dp
                delay(300)
//                onDismiss()
            }
        }
    }

    Row (
        modifier = Modifier
            .height(75.dp)
            .width(300.dp)
            .offset(x = notificationXOffsetAnim)
            .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(corners())
            .background(notification.type.color)
            .clickable {
                onDismiss()
            }
            .padding(start = 15.dp)
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 15.dp, bottomStart = 15.dp))
                .background(Color.White)
        ) {
            Text (
                text = notification.content
            )

            Box (
                modifier = Modifier
                    .width(((notification.duration * timeLeft) * 300L).toInt().dp)
            )
        }
    }
}