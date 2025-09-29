package com.nullinnix.clippr

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.close
import clippr.composeapp.generated.resources.full_screen
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.name
import com.nullinnix.clippr.misc.noGleamCombinedClickable
import com.nullinnix.clippr.misc.noGleamTaps
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