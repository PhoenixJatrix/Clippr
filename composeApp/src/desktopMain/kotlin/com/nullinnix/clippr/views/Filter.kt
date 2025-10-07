package com.nullinnix.clippr.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.corners
import com.nullinnix.clippr.theme.HeaderColor
import com.nullinnix.clippr.viewmodels.ClipsViewModel

@Composable
fun FilterView (
    clipsViewModel: ClipsViewModel
) {
    val state = rememberScrollState()
    val clipState = clipsViewModel.clipsState.collectAsState().value
    val filters = clipState.filters

    CustomBottomSheet(
        onDismiss = {

        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(state)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                FilterTemplate(
                    title = "Type"
                ) {
                    LazyColumn {
                        items (ClipType.entries.toTypedArray()) {
                            Row(

                            ) {
                                RadioButton(
                                    isSelected = filters.types.contains(it)
                                ) {

                                }
                            }
                        }
                    }
                }
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