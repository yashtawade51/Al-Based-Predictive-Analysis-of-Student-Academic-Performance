package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectStrengthScore
import com.example.data.model.TrendPoint
import com.example.ui.theme.*

@Composable
fun ActualVsPredictedChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(primaryColor, CircleShape))
            Text(" Actual Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(14.dp))
            Box(modifier = Modifier.size(10.dp).background(tertiaryColor, CircleShape))
            Text(" AI Predicted", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Custom Canvas Line/Bar Trend
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingBottom = 40f
            val chartHeight = height - paddingBottom

            // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
            for (i in 0..4) {
                val y = (chartHeight / 4) * i
                drawLine(
                    color = surfaceVariant.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.isEmpty()) return@Canvas

            val stepX = width / (points.size.coerceAtLeast(1))

            // Draw Bars / Points
            points.forEachIndexed { index, pt ->
                val centerX = (stepX * index) + (stepX / 2f)

                // Predicted bar
                val predPct = (pt.predictedScore / 100.0).coerceIn(0.0, 1.0).toFloat()
                val predBarHeight = chartHeight * predPct
                val predTopY = chartHeight - predBarHeight

                drawRoundRect(
                    color = tertiaryColor.copy(alpha = 0.35f),
                    topLeft = Offset(centerX - 18f, predTopY),
                    size = Size(36f, predBarHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Actual point / bar if available
                if (pt.actualScore != null) {
                    val actPct = (pt.actualScore / 100.0).coerceIn(0.0, 1.0).toFloat()
                    val actBarHeight = chartHeight * actPct
                    val actTopY = chartHeight - actBarHeight

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        topLeft = Offset(centerX - 10f, actTopY),
                        size = Size(20f, actBarHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Dot on top
                    drawCircle(
                        color = primaryColor,
                        radius = 5.dp.toPx(),
                        center = Offset(centerX, actTopY)
                    )
                } else {
                    // Predicted Peak Indicator
                    drawCircle(
                        color = tertiaryColor,
                        radius = 6.dp.toPx(),
                        center = Offset(centerX, predTopY)
                    )
                }
            }
        }

        // Labels underneath
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SubjectStrengthBarChart(
    strengths: List<SubjectStrengthScore>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        strengths.forEach { item ->
            val scorePct = (item.score / 100.0).coerceIn(0.0, 1.0).toFloat()
            val animatedScore by animateFloatAsState(
                targetValue = scorePct,
                animationSpec = tween(700),
                label = "bar"
            )

            val barColor = when (item.status) {
                "Strong" -> RiskLowGreen
                "Average" -> RiskModerateAmber
                else -> RiskHighRed
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subjectName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = barColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.status,
                                color = barColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${item.score.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedScore)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceGauge(
    percentage: Double,
    sizeDp: Int = 120,
    modifier: Modifier = Modifier
) {
    val isWarning = percentage < 75.0
    val progressColor = if (isWarning) RiskHighRed else RiskLowGreen
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val radius = (size.minDimension - stroke) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background Circle
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Progress Arc
            val sweepAngle = ((percentage / 100.0).coerceIn(0.0, 1.0) * 360.0).toFloat()
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isWarning) "AT RISK" else "REGULAR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }
    }
}

@Composable
fun RiskDistributionBar(
    lowCount: Int,
    mediumCount: Int,
    highCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (lowCount + mediumCount + highCount).coerceAtLeast(1)
    val lowRatio = lowCount.toFloat() / total
    val medRatio = mediumCount.toFloat() / total
    val highRatio = highCount.toFloat() / total

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            if (lowRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(lowRatio)
                        .fillMaxHeight()
                        .background(RiskLowGreen)
                )
            }
            if (medRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(medRatio)
                        .fillMaxHeight()
                        .background(RiskModerateAmber)
                )
            }
            if (highRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(highRatio)
                        .fillMaxHeight()
                        .background(RiskHighRed)
                )
            }
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(RiskLowGreen, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Low Risk: $lowCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(RiskModerateAmber, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Medium: $mediumCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(RiskHighRed, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("High Risk: $highCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
