package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (Bitmap) -> Unit,
    onClear: () -> Unit
) {
    val points = remember { mutableStateListOf<Offset?>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ग्राहक डिजिटल स्वाक्षरी (Client Signature Canvas)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
            
            TextButton(
                onClick = {
                    points.clear()
                    onClear()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "साफ करा",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("साफ करा (Clear)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(12.dp))
        ) {
            ComposeCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                points.add(offset)
                            },
                            onDragEnd = {
                                points.add(null)
                                val map = createSignatureBitmap(points, 300, 140)
                                if (map != null) {
                                    onSignatureCaptured(map)
                                }
                            },
                            onDragCancel = {
                                points.add(null)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                points.add(change.position)
                            }
                        )
                    }
            ) {
                val path = Path()
                var isFirst = true

                for (p in points) {
                    if (p == null) {
                        isFirst = true
                    } else {
                        if (isFirst) {
                            path.moveTo(p.x, p.y)
                            isFirst = false
                        } else {
                            path.lineTo(p.x, p.y)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = Color(103, 80, 164),
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "इथे तुमचे बोट फिरवून सही करा (Draw Signature Here)",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun createSignatureBitmap(points: List<Offset?>, widthDp: Int, heightDp: Int): Bitmap? {
    if (points.isEmpty()) return null
    val bitmap = Bitmap.createBitmap(400, 160, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.TRANSPARENT)

    val paint = Paint().apply {
        color = android.graphics.Color.rgb(103, 80, 164)
        isAntiAlias = true
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    val androidPath = AndroidPath()
    var isFirst = true

    val scaleX = 400f / 320f
    val scaleY = 160f / 140f

    for (p in points) {
        if (p == null) {
            isFirst = true
        } else {
            val px = p.x * scaleX
            val py = p.y * scaleY
            if (isFirst) {
                androidPath.moveTo(px, py)
                isFirst = false
            } else {
                androidPath.lineTo(px, py)
            }
        }
    }

    canvas.drawPath(androidPath, paint)
    return bitmap
}
