package com.nullinnix.clippr.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.back
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.MacApp
import com.nullinnix.clippr.misc.clipTypeToColor
import com.nullinnix.clippr.misc.clipTypeToDesc
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.viewmodels.ClipsViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun FilterView (
    clipsViewModel: ClipsViewModel,
    allApps: Map<String, MacApp>,
    loadedIcns: Map<String, ImageBitmap>
) {
    val state = rememberScrollState()
    val clipState = clipsViewModel.clipsState.collectAsState().value
    val filters = clipState.searchFilter

    PopupMenu (
        onClose = {
            clipsViewModel.setShowFilters(false)
        },
        content = {
            Box (
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ){
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .padding(end = 15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon (
                                painter = painterResource(Res.drawable.back),
                                contentDescription = "",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(10.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                                    .clip(corners(90.dp))
                                    .background(Color.White)
                                    .clickable {
                                        clipsViewModel.setShowFilters(false)
                                    }
                                    .padding(10.dp)
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "Filters",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(corners(90.dp))
                                .background(Color.Black)
                                .padding(horizontal = 15.dp)
                                .clickable {

                                }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Apply ⏎",
                                color = Color.White
                            )
                        }
                    }

//                    Spacer(Modifier.height(20.dp))

                    Column (
                        modifier = Modifier
                            .verticalScroll(state)
                            .padding(end = 15.dp)
                            .padding(horizontal = 10.dp),
                    ) {
                        Spacer(Modifier.height(10.dp))

                        FilterTemplate(
                            title = "Type"
                        ) {
                            Column (
                                modifier = Modifier
                                    .padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                            ){
                                RadioButton(
                                    isSelected = filters.types.size == ClipType.entries.size
                                ) {
                                    if (filters.types.size == ClipType.entries.size) {
                                        clipsViewModel.setFilters(filters.copy(types = emptySet()))
                                    } else {
                                        clipsViewModel.setFilters(filters.copy(types = ClipType.entries.toSet()))
                                    }
                                }

                                for (clipType in ClipType.entries.toTypedArray()) {
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            isSelected = filters.types.contains(clipType)
                                        ) {
                                            if (filters.types.contains(clipType)) {
                                                clipsViewModel.setFilters(filters.copy(types = filters.types - clipType))
                                            } else {
                                                clipsViewModel.setFilters(filters.copy(types = filters.types + clipType))
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

                        Spacer(Modifier.height(20.dp))

                        FilterTemplate(
                            title = "Pin status"
                        ) {
                            Column (
                                modifier = Modifier
                                    .padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                            ){
                                RadioButton(
                                    isSelected = filters.pinState == null
                                ) {
                                    clipsViewModel.setFilters(filters.copy(pinState = null))
                                }

                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        isSelected = filters.pinState == true || filters.pinState == null
                                    ) {
                                        if (filters.pinState != null) {
                                            if (filters.pinState) {
                                                clipsViewModel.setFilters(filters.copy(pinState = false))
                                            } else {
                                                clipsViewModel.setFilters(filters.copy(pinState = true))
                                            }
                                        } else {
                                            clipsViewModel.setFilters(filters.copy(pinState = false))
                                        }
                                    }

                                    Text(
                                        text = "Pinned clips only"
                                    )
                                }

                                Row (
                                    modifier = Modifier
                                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        isSelected = filters.pinState == false || filters.pinState == null
                                    ) {
                                        if (filters.pinState != null) {
                                            if (filters.pinState) {
                                                clipsViewModel.setFilters(filters.copy(pinState = false))
                                            } else {
                                                clipsViewModel.setFilters(filters.copy(pinState = true))
                                            }
                                        } else {
                                            clipsViewModel.setFilters(filters.copy(pinState = true))
                                        }
                                    }

                                    Text(
                                        text = "UnPinned clips only"
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        FilterTemplate(
                            title = "Sources"
                        ) {
                            Column (
                                modifier = Modifier
                                    .padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)
                            ){
                                RadioButton(
                                    isSelected = allApps.size == filters.sources.size
                                ) {
                                    if (allApps.size == filters.sources.size) {
                                        clipsViewModel.setFilters(filters.copy(sources = emptySet()))
                                    } else {
                                        clipsViewModel.setFilters(filters.copy(sources = allApps.keys))
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
                                            isSelected = filters.sources.contains(app.key)
                                        ) {
                                            if (filters.sources.contains(app.key)) {
                                                clipsViewModel.setFilters(filters.copy(sources = filters.sources - app.key))
                                            } else {
                                                clipsViewModel.setFilters(filters.copy(sources = filters.sources + app.key))
                                            }
                                        }

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
                                                drawRoundRect(color = Color.DarkGray)
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

                        Spacer(Modifier.height(10.dp))
                    }
                }

                VerticalScrollbar (
                    adapter = rememberScrollbarAdapter(state),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 10.dp, bottom = 15.dp, top = 25.dp),
                    style = LocalScrollbarStyle.current.copy(minimalHeight = 35.dp)
                )
            }
        }
    )
}

@Composable
fun FilterTemplate (
    title: String,
    content: @Composable () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(10.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(corners(10.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderColor)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            drawLine(
                color = Color.Black.copy(0.25f),
                start = Offset.Zero,
                end = Offset(this.size.width, 0f)
            )
        }

        content()
    }
}