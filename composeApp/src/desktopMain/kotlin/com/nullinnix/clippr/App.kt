package com.nullinnix.clippr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import clippr.composeapp.generated.resources.Res
import clippr.composeapp.generated.resources.compose_multiplatform
import clippr.composeapp.generated.resources.pin
import com.nullinnix.clippr.database.Database
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipActions
import com.nullinnix.clippr.misc.formatText
import com.nullinnix.clippr.misc.getClipboard
import com.nullinnix.clippr.model.ViewModel
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.Bitmap

@Composable
@Preview
fun App() {
    MaterialTheme {
        var copiedText by remember { mutableStateOf("") }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

        Column(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn {
                items(ViewModel.clips.values.toList()) {
                    ClipTemplate(it) {

                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(90.dp))
                    .background(Color.Green)
                    .clickable {
//                        copiedText = getClipboard()
//                        bitmap = getImageFromClipboard()
//                        bitmap = getImageFileFromClipboard()

//                        val process = ProcessBuilder("pbpaste").start()
//                        copiedText = process.inputStream.bufferedReader().readText()

//                        val process = ProcessBuilder("pbcopy").start()
//                        process.outputStream.bufferedWriter().use {
//                            it.write("paste this shit")
//                        }

//                        getClipboard()
                    }
                    .padding(10.dp), contentAlignment = Alignment.Center
            ) {
                Text("Get clipboard", color = Color.Black)
            }

            Text(copiedText, color = Color.White)

            AnimatedVisibility(bitmap != null) {
                Image(bitmap!!.asComposeImageBitmap(), null)
            }
        }
    }
}

@Composable
fun ClipTemplate(
    clip: Clip,
    onAction: (ClipActions) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0.1f, 0.1f, 0.1f))
            .padding(15.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.pin),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp),
            tint = Color(0.2f, 0.2f, 0.2f)
        )

        Spacer(Modifier.width(15.dp))

        Canvas(
            modifier = Modifier
                .height(80.dp)
        ) {
            drawLine(
                color = Color(0.5f, 0.5f, 0.5f),
                start = Offset.Zero,
                end = Offset(x = 0f, y = this.size.height),
                cap = StrokeCap.Round,
                strokeWidth = 10f
            )
        }

        Spacer(Modifier.width(15.dp))

        Text(
            text = formatText(clip.text),
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
    }
}