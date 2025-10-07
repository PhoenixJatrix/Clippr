package com.nullinnix.clippr.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.back
import clippr.composeapp.generated.resources.check
import clippr.composeapp.generated.resources.close
import clippr.composeapp.generated.resources.filter
import clippr.composeapp.generated.resources.full_screen
import clippr.composeapp.generated.resources.search
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.SearchAction
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.name
import com.nullinnix.clippr.misc.noGleamCombinedClickable
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.theme.Transparent
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import java.awt.MouseInfo
import java.awt.Window

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
                .noGleamTaps {
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
                    text = "Search/filter",
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
            Icon(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    containerColor: Color = Color.White,
    backgroundColor: Color = Color.Black.copy(0.75f),
    closeFromParent: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { new ->
            true
        }
    )

    LaunchedEffect(closeFromParent) {
        if (closeFromParent) {
            sheetState.hide()
            delay(300)
            onDismiss()
        }
    }

    ModalBottomSheet(
        shape = RoundedCornerShape(20.dp),
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = containerColor,
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, brush = Brush.verticalGradient(listOf(Color.Black.copy(0.2f), Color.Transparent)), shape = RoundedCornerShape(20.dp))
                    .padding(vertical = 15.dp), contentAlignment = Alignment.Center
            ) {
                Canvas (
                    modifier = Modifier
                        .height(10.dp)
                        .width(30.dp)
                ) {
                    drawLine(color = Color.Black, start = Offset.Zero, end = Offset(y = 0f, x = this.size.width), cap = StrokeCap.Round, strokeWidth = this.size.height)
                }
            }
        },
        scrimColor = backgroundColor,
        tonalElevation = 15.dp
    ) {
        content()
    }
}