package com.nullinnix.clippr.views.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.finder
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.MiscViewModelState
import com.nullinnix.clippr.misc.SettingsAction
import com.nullinnix.clippr.misc.clipTypeToColor
import com.nullinnix.clippr.misc.clipTypeToDesc
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.viewmodels.SettingsViewModel
import com.nullinnix.clippr.views.CheckBox
import com.nullinnix.clippr.views.RadioButton
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun Settings (
    settingsViewModel: SettingsViewModel,
    miscViewModelState: MiscViewModelState
) {
    val settingsState = settingsViewModel.settings.collectAsState().value
    val recordingEnabled = settingsState.recordingEnabled
    val enableMetaShiftVPopup = settingsState.enableMetaShiftVPopup
    val clearAllUnpinnedClipsOnDeviceStart = settingsState.clearAllUnpinnedClipsOnDeviceStart
    val deleteUnpinnedClipsAfter30Days = settingsState.deleteUnpinnedClipsAfter30Days
    val startAtLogin = settingsState.startAtLogin
    val sourcesExceptions = settingsState.sourcesExceptions
    val clipTypesExceptions = settingsState.clipTypesExceptions

    val allApps = miscViewModelState.allApps
    val loadedIcns = miscViewModelState.loadedIcns
    val scrollState = rememberScrollState()

    Box (
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column (
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(10.dp)
                .padding(end = 20.dp)
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
                extras = if (!settingsState.startAtLogin) "Go to System Settings -> General -> Login Items and Extensions. Click + then add Clippr" else null
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

            SettingsElement (
                title = "Exceptions",
                content = {
                    SettingsElement (
                        title = "Clip type",
                        description = "Any clip determined as any of these selected types will be ignored",
                        isMainHeading = false
                    ) {
                        Column (
                            modifier = Modifier
                                .padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                        ){
                            RadioButton(
                                isSelected = clipTypesExceptions.size == ClipType.entries.size
                            ) {
                                if (sourcesExceptions.size == ClipType.entries.size) {
                                    settingsViewModel.onAction(SettingsAction.SetClipTypes(emptySet()))
                                } else {
                                    settingsViewModel.onAction(SettingsAction.SetClipTypes(ClipType.entries.toSet()))
                                }
                            }

                            for (clipType in ClipType.entries.toTypedArray()) {
                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        isSelected = clipTypesExceptions.contains(clipType)
                                    ) {
                                        if (clipTypesExceptions.contains(clipType)) {
                                            settingsViewModel.onAction(SettingsAction.SetClipTypes(clipTypesExceptions - clipType))
                                        } else {
                                            settingsViewModel.onAction(SettingsAction.SetClipTypes(clipTypesExceptions + clipType))
                                        }
                                    }

                                    Canvas(
                                        modifier = Modifier
                                            .size(10.dp)
                                    ) {
                                        drawCircle(color = clipTypeToColor(clipType.id))
                                    }

                                    Spacer(Modifier.width(5.dp))

                                    Text(
                                        text = clipTypeToDesc(clipType.id)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    SettingsElement(
                        title = "Sources",
                        description = "Anything copied from these selected sources will be ignored",
                        isMainHeading = false
                    ) {
                        Column (
                            modifier = Modifier
                                .padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                        ){
                            RadioButton(
                                isSelected = sourcesExceptions.size == allApps.size
                            ) {
                                if (sourcesExceptions.size == allApps.size) {
                                    settingsViewModel.onAction(SettingsAction.SetSourceExceptions(emptySet()))
                                } else {
                                    settingsViewModel.onAction(SettingsAction.SetSourceExceptions(allApps.keys))
                                }
                            }

                            for (app in allApps) {
                                var showIcon by remember { mutableStateOf(false) }

                                LaunchedEffect(Unit) {
                                    delay(500)
                                    showIcon = true
                                }

                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        isSelected = sourcesExceptions.contains(app.key)
                                    ) {
                                        if (sourcesExceptions.contains(app.key)) {
                                            settingsViewModel.onAction(SettingsAction.SetSourceExceptions(sourcesExceptions - app.key))
                                        } else {
                                            settingsViewModel.onAction(SettingsAction.SetSourceExceptions(sourcesExceptions + app.key))
                                        }
                                    }

                                    if (app.value.bundleId == "com.apple.finder") {
                                        Image(
                                            painter = painterResource(Res.drawable.finder),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(25.dp)
                                                .padding(2.dp)
                                        )
                                    } else {
                                        if (app.value.iconPath != null && loadedIcns[app.key] != null && showIcon) {
                                            Image(
                                                bitmap = loadedIcns[app.key]!!,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .clip(corners(7.dp))
                                            )
                                        } else {
                                            Canvas(
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .padding(3.dp)
                                                    .clip(corners(5.dp))
                                            ) {
                                                drawRoundRect(color = if (app.key == "unknown") Color.Yellow else Color.DarkGray)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(5.dp))

                                    Text(
                                        text = if (app.value.name.contains("default pair of")) app.key else app.value.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(20.dp))
        }

        VerticalScrollbar (
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 10.dp, bottom = 15.dp, top = 25.dp),
            style = LocalScrollbarStyle.current.copy(minimalHeight = 35.dp)
        )
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

@Composable
fun SettingsElement (
    title: String,
    description: String? = null,
    isMainHeading: Boolean = true,
    content: @Composable () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .shadow( if (isMainHeading) 10.dp else 2.dp, RoundedCornerShape(10.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
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
                fontSize = if (isMainHeading) 17.sp else 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Canvas (
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            drawLine(color = Color.Black.copy(0.25f), start = Offset.Zero, end = Offset(this.size.width, 0f))
        }

        if (description != null) {
            Text(
                text = description,
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                color = Color.Black,
                fontSize = 16.sp
            )
        }

        Column (
            modifier = Modifier
                .padding(10.dp)
        ) {
            content()
        }
    }
}