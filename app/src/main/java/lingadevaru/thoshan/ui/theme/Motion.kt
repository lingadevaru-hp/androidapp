package lingadevaru.thoshan.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Shape and motion tokens from spec:
// Radius (default) 14.dp
// Radius (large)   22.dp
// Easing           cubic-bezier(.22, 1, .36, 1)
// Motion           soft and quick ~160–240 ms
val PortfolioRadiusDefault = 14.dp
val PortfolioRadiusLarge = 22.dp

val PortfolioShapeDefault = RoundedCornerShape(PortfolioRadiusDefault)
val PortfolioShapeLarge = RoundedCornerShape(PortfolioRadiusLarge)
val PortfolioShapePill = RoundedCornerShape(999.dp)

val PortfolioEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

fun <T> portfolioTween(
    durationMillis: Int = 200,
    delayMillis: Int = 0
) = tween<T>(
    durationMillis = durationMillis,
    delayMillis = delayMillis,
    easing = PortfolioEasing
)
