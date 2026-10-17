package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentSoft

/**
 * 5-petal orange lotus flower emblem mark recreated as a Compose Canvas vector.
 * Features a soft radial ambient glow behind it and graceful curved petals.
 */
@Composable
fun LotusMark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    petalColor: Color = DarkAccent,
    petalHighlightColor: Color = DarkAccentSoft,
    glowColor: Color = DarkAccent.copy(alpha = 0.28f),
    showGlow: Boolean = true,
    isRotating: Boolean = false,
    scale: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lotus_anim")
    val rotation by if (isRotating) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "lotus_rotation"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Radial ambient glow
            if (showGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor,
                            glowColor.copy(alpha = glowColor.alpha * 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = (w * 0.7f) * scale
                    ),
                    radius = (w * 0.7f) * scale
                )
            }

            rotate(degrees = rotation, pivot = center) {
                val petalBrush = Brush.verticalGradient(
                    colors = listOf(petalHighlightColor, petalColor),
                    startY = cy - (h * 0.38f * scale),
                    endY = cy + (h * 0.22f * scale)
                )
                val outerPetalBrush = Brush.verticalGradient(
                    colors = listOf(petalHighlightColor.copy(alpha = 0.9f), petalColor),
                    startY = cy - (h * 0.25f * scale),
                    endY = cy + (h * 0.22f * scale)
                )

                val baseOffsetY = cy + (h * 0.16f * scale)
                val topOffsetY = cy - (h * 0.36f * scale)

                // 1. Central tall petal
                val centerPath = Path().apply {
                    moveTo(cx, topOffsetY)
                    cubicTo(
                        cx + (w * 0.11f * scale), cy - (h * 0.12f * scale),
                        cx + (w * 0.08f * scale), cy + (h * 0.06f * scale),
                        cx, baseOffsetY
                    )
                    cubicTo(
                        cx - (w * 0.08f * scale), cy + (h * 0.06f * scale),
                        cx - (w * 0.11f * scale), cy - (h * 0.12f * scale),
                        cx, topOffsetY
                    )
                    close()
                }
                drawPath(path = centerPath, brush = petalBrush)

                // 2. Left inner petal
                val leftInnerPath = Path().apply {
                    val tipX = cx - (w * 0.18f * scale)
                    val tipY = cy - (h * 0.24f * scale)
                    moveTo(tipX, tipY)
                    cubicTo(
                        cx - (w * 0.07f * scale), cy - (h * 0.06f * scale),
                        cx - (w * 0.04f * scale), cy + (h * 0.08f * scale),
                        cx, baseOffsetY
                    )
                    cubicTo(
                        cx - (w * 0.14f * scale), cy + (h * 0.02f * scale),
                        cx - (w * 0.22f * scale), cy - (h * 0.12f * scale),
                        tipX, tipY
                    )
                    close()
                }
                drawPath(path = leftInnerPath, brush = petalBrush)

                // 3. Right inner petal
                val rightInnerPath = Path().apply {
                    val tipX = cx + (w * 0.18f * scale)
                    val tipY = cy - (h * 0.24f * scale)
                    moveTo(tipX, tipY)
                    cubicTo(
                        cx + (w * 0.07f * scale), cy - (h * 0.06f * scale),
                        cx + (w * 0.04f * scale), cy + (h * 0.08f * scale),
                        cx, baseOffsetY
                    )
                    cubicTo(
                        cx + (w * 0.14f * scale), cy + (h * 0.02f * scale),
                        cx + (w * 0.22f * scale), cy - (h * 0.12f * scale),
                        tipX, tipY
                    )
                    close()
                }
                drawPath(path = rightInnerPath, brush = petalBrush)

                // 4. Left outer petal
                val leftOuterPath = Path().apply {
                    val tipX = cx - (w * 0.32f * scale)
                    val tipY = cy - (h * 0.08f * scale)
                    moveTo(tipX, tipY)
                    cubicTo(
                        cx - (w * 0.16f * scale), cy + (h * 0.04f * scale),
                        cx - (w * 0.08f * scale), cy + (h * 0.12f * scale),
                        cx, baseOffsetY
                    )
                    cubicTo(
                        cx - (w * 0.20f * scale), cy + (h * 0.14f * scale),
                        cx - (w * 0.34f * scale), cy + (h * 0.04f * scale),
                        tipX, tipY
                    )
                    close()
                }
                drawPath(path = leftOuterPath, brush = outerPetalBrush)

                // 5. Right outer petal
                val rightOuterPath = Path().apply {
                    val tipX = cx + (w * 0.32f * scale)
                    val tipY = cy - (h * 0.08f * scale)
                    moveTo(tipX, tipY)
                    cubicTo(
                        cx + (w * 0.16f * scale), cy + (h * 0.04f * scale),
                        cx + (w * 0.08f * scale), cy + (h * 0.12f * scale),
                        cx, baseOffsetY
                    )
                    cubicTo(
                        cx + (w * 0.20f * scale), cy + (h * 0.14f * scale),
                        cx + (w * 0.34f * scale), cy + (h * 0.04f * scale),
                        tipX, tipY
                    )
                    close()
                }
                drawPath(path = rightOuterPath, brush = outerPetalBrush)

                // Base node
                drawCircle(
                    color = petalColor,
                    radius = (w * 0.045f) * scale,
                    center = androidx.compose.ui.geometry.Offset(cx, baseOffsetY)
                )
            }
        }
    }
}
