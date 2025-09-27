package com.nullinnix.clippr

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.name

@Composable
fun Tabs (
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
            isSelected = currentTab == Tab.ClipsTab,
            tab = Tab.ClipsTab
        ) {
            onTabChanged(it)
        }

        TabElement(
            isSelected = currentTab == Tab.SettingsTab,
            tab = Tab.SettingsTab
        ) {
            onTabChanged(it)
        }
    }
}

@Composable
fun TabElement (
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
            color = textColorAnim,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}