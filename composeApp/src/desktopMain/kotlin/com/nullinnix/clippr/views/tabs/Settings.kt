package com.nullinnix.clippr.views.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.CheckBox

@Composable
fun Settings (
    settingsViewModel: SettingsViewModel
) {
    val settingsState = settingsViewModel.settings.collectAsState().value
    val recordingEnabled = settingsState.recordingEnabled
    val enableMetaShiftVPopup = settingsState.enableMetaShiftVPopup
    val clearAllUnpinnedClipsOnDeviceStart = settingsState.clearAllUnpinnedClipsOnDeviceStart
    val deleteUnpinnedClipsAfter30Days = settingsState.deleteUnpinnedClipsAfter30Days
    val startAtLogin = settingsState.startAtLogin

    val scrollState = rememberScrollState()

    Column (
        modifier = Modifier
            .verticalScroll(state = scrollState)
            .padding(10.dp)
    ){
        Spacer(Modifier.height(15.dp))

        SettingsCheckBoxElement(
            title = "Recording enabled",
            description = "Enable recording to save copied items",
            isChecked = recordingEnabled
        ) {
            settingsViewModel.onAction(SettingsAction.ToggleEnableClipping)
        }

        Spacer(Modifier.height(20.dp))

        SettingsCheckBoxElement(
            title = "Start recording on login. Read more below",
            description = "Start recording clips automatically whenever the device is started (and recording is enabled)",
            isChecked = startAtLogin,
            enabled = startAtLogin,
            extras = "Go to System Settings -> General -> Login Items and Extensions. Click + then add Clippr"
        ) {
            settingsViewModel.onAction(SettingsAction.ToggleStartAtLogin)
        }

        Spacer(Modifier.height(20.dp))

        SettingsCheckBoxElement(
            title = "Enable ⌘ + ⇧ + V (Command + Shift + V) shortcut",
            description = "Enable the shortcut to show/hide the app when it's running. (Requires app restart)",
            isChecked = enableMetaShiftVPopup
        ) {
            settingsViewModel.onAction(SettingsAction.ToggleEnableMetaShiftV)
        }

        Spacer(Modifier.height(20.dp))

        SettingsCheckBoxElement(
            title = "Clear unpinned on login",
            description = "Delete all unpinned clips when the device is started",
            isChecked = clearAllUnpinnedClipsOnDeviceStart
        ) {
            settingsViewModel.onAction(SettingsAction.ToggleClearAllUnpinnedDevicesOnStart)
        }

        Spacer(Modifier.height(20.dp))

        SettingsCheckBoxElement(
            title = "Clear unpinned after 30 days",
            description = "Delete any unpinned clip after 30 days",
            isChecked = deleteUnpinnedClipsAfter30Days
        ) {
            settingsViewModel.onAction(SettingsAction.ToggleDeleteUnpinnedAfter30)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SettingsCheckBoxElement (
    title: String,
    description: String,
    extras: String? = null,
    isChecked: Boolean,
    enabled: Boolean = true,
    onClick: (Boolean) -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(10.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(corners(10.dp))
            .background(Color.White)
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderColor)
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            CheckBox(
                enabled = enabled,
                isChecked = isChecked
            ) {
                onClick(it)
            }
        }

        Canvas (
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            drawLine(color = Color.Black.copy(0.25f), start = Offset.Zero, end = Offset(this.size.width, 0f))
        }

        if (extras != null) {
            Text(
                text = extras,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Text(
            text = description,
            modifier = Modifier
                .padding(20.dp),
            color = Color.DarkGray
        )
    }
}