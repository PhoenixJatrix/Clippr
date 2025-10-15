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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import clippr.composeapp.generated.resources.Baloo2_Regular
import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.back
import clippr.composeapp.generated.resources.clippr_status_icon_thicker
import clippr.composeapp.generated.resources.finder
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipMenuAction
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.MacApp
import com.nullinnix.clippr.misc.SearchAction
import com.nullinnix.clippr.misc.clipTypeToColor
import com.nullinnix.clippr.misc.clipTypeToDesc
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.misc.desc
import com.nullinnix.clippr.misc.epochToReadableTime
import com.nullinnix.clippr.misc.getClipMenuActions
import com.nullinnix.clippr.misc.hash
import com.nullinnix.clippr.misc.noGleamTaps
import com.nullinnix.clippr.misc.shortcut
import com.nullinnix.clippr.misc.toClipType
import com.nullinnix.clippr.theme.HeaderColor
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun ClipPreview (
    clip: Clip?,
    macApp: MacApp?,
    icon: ImageBitmap?,
    secondsBeforePaste: Int,
    onClipMenuAction: (ClipMenuAction) -> Unit,
    onClose: () -> Unit
) {
    PopupMenu (
        onClose = {
           onClose()
        },
        content = {
            if (clip != null) {
                val scrollState = rememberScrollState()
                val contentScrollState = rememberScrollState()
                var copiedAt by remember { mutableStateOf(epochToReadableTime(clip.copiedAt)) }
                var clipContentEdit by remember { mutableStateOf(clip.content) }
                var edited by remember { mutableStateOf(false) }
                val originalHash by remember { mutableStateOf(clip.content.hash()) }

                LaunchedEffect(Unit) {
                    while (true) {
                        copiedAt = epochToReadableTime(clip.copiedAt)
                        delay(3000)
                    }
                }

                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .noGleamTaps {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(Color.White)
                            .padding(25.dp)
                            .padding(end = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                            onClose()
                                        }
                                        .padding(10.dp)
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    text = "Edit",
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Box (
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .shadow(10.dp, RoundedCornerShape(15.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                                .clip(corners(15.dp))
                                .background(HeaderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(contentScrollState)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                Spacer(Modifier.height(20.dp))

                                BasicTextField(
                                    value = clipContentEdit,
                                    onValueChange = {
                                        clipContentEdit = it

                                        edited = it.hash() != originalHash
                                    },
                                    textStyle = TextStyle (
                                        color = Color.Black,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(Color.Black),
                                    decorationBox = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {
                                            it()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                )

                                Spacer(Modifier.height(20.dp))
                            }

                            VerticalScrollbar (
                                adapter = rememberScrollbarAdapter(contentScrollState),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .padding(bottom = 10.dp, top = 10.dp, end = 10.dp),
                                style = LocalScrollbarStyle.current.copy(minimalHeight = 35.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row (
                            Modifier
                                .fillMaxWidth(), horizontalArrangement = Arrangement.End
                        ){
                            Row (
                                modifier = Modifier
                                    .shadow(5.dp, RoundedCornerShape(90.dp), clip = false, ambientColor = Color.Gray, spotColor = Color.Gray)
                                    .clip(RoundedCornerShape(90.dp))
                                    .height(30.dp)
                                    .background(Color.Black)
                                    .clickable(false) {}
                                    .padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.clippr_status_icon_thicker),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .clip(corners(90.dp)),
                                    tint = Color.White
                                )
                            }

                            Spacer(Modifier.width(7.dp))

                            if (macApp != null) {
                                clip.source?.let {
                                    ClipPreviewInfo(
                                        content = macApp.name.coerce(30),
                                        enabled = false,
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
                                        }
                                    )

                                    Spacer(Modifier.width(7.dp))
                                }
                            }

                            ClipPreviewInfo(
                                content = clipTypeToDesc(clip.associatedIcon),
                                enabled = false,
                                prefix = {
                                    Canvas(
                                        modifier = Modifier
                                            .size(7.dp)
                                    ) {
                                        drawCircle(color = clipTypeToColor(clip.associatedIcon))
                                    }
                                }
                            )

                            Spacer(Modifier.width(7.dp))

                            if (clip.associatedIcon.toClipType() == ClipType.PLAIN_TEXT) {
                                ClipPreviewInfo(
                                    content = "${clip.content.length} ${if (clip.content.length == 1) "character" else "characters"}",
                                    enabled = false,
                                    prefix = {
                                        val lines = clip.content.lines().size
                                        if (lines > 1) {
                                            Text(
                                                text = "$lines lines",
                                                fontFamily = FontFamily(Font(Res.font.Baloo2_Regular)),
                                                color = Color.Gray,
                                                fontSize = 13.sp
                                            )

                                            Spacer(Modifier.width(5.dp))

                                            Canvas(
                                                modifier = Modifier
                                                    .size(5.dp)
                                            ) {
                                                drawCircle(color = Color.Gray)
                                            }
                                        }
                                    }
                                )

                                Spacer(Modifier.width(7.dp))
                            }

                            ClipPreviewInfo(
                                content = "Copied $copiedAt",
                                enabled = false,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        getClipMenuActions(clip).minus(ClipMenuAction.Preview).forEach { option ->
                            Box (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(5.dp, RoundedCornerShape(10.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                                    .clip(corners(10.dp))
                                    .background(Color.White)
                                    .clickable {
                                        onClipMenuAction(option)
                                    }
                                    .padding(20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = option.desc(secondsBeforePaste),
                                        fontSize = 16.sp,
                                        color = if (option == ClipMenuAction.Delete) Color.Red else Color.Black
                                    )

                                    Text(
                                        text = option.shortcut(),
                                        fontSize = 16.sp,
                                        color = Color.Black.copy(0.5f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
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
                }
            }
        }
    )
}