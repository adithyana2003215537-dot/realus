package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserMood
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimarySienna
import com.example.ui.theme.SecondaryGold

data class ChartPoint(
  val label: String,
  val userScore: Float,
  val userMoodLabel: String,
  val userMoodIcon: String,
  val partnerScore: Float,
  val partnerMoodLabel: String,
  val partnerMoodIcon: String,
  val synergyScore: Int,
  val note: String
)

/**
 * Interactive Collective Mood Trend Line Chart for Jetpack Compose.
 * Featuring D3/Recharts-inspired smooth cubic Bézier curves, dual partner series,
 * gradient area fills, dashed guideline overlays, and interactive scrubbing tooltip.
 */
@Composable
fun MoodTrendLineChart(
  moods: List<UserMood>,
  userName: String = "You",
  partnerName: String = "Jamie",
  timeRange: String = "7D",
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  val userColor = PrimarySienna // Warm Terracotta Sienna
  val partnerColor = Color(0xFF00B0FF) // Azure Cyan
  val synergyColor = SecondaryGold // Gold

  // Transform UserMood entities to chronological chart points
  val chartPoints = remember(moods, timeRange) {
    val items = moods.sortedBy { it.timestamp }
    val points = if (items.isNotEmpty()) {
      items.takeLast(if (timeRange == "7D") 7 else if (timeRange == "14D") 14 else 30).mapIndexed { index, m ->
        val shortDate = when {
          m.dateLabel.isNotBlank() -> m.dateLabel
          else -> "D${index + 1}"
        }
        ChartPoint(
          label = shortDate,
          userScore = m.moodScore.coerceIn(1f, 10f),
          userMoodLabel = m.moodLabel,
          userMoodIcon = m.moodIcon,
          partnerScore = m.partnerMoodScore.coerceIn(1f, 10f),
          partnerMoodLabel = m.partnerMoodLabel,
          partnerMoodIcon = m.partnerMoodIcon,
          synergyScore = m.synergyScore,
          note = m.note
        )
      }
    } else {
      emptyList()
    }
    points
  }

  var selectedIndex by remember(chartPoints) { mutableIntStateOf(chartPoints.lastIndex.coerceAtLeast(0)) }
  var isScrubbing by remember { mutableStateOf(false) }

  val activePoint = chartPoints.getOrNull(selectedIndex) ?: chartPoints.lastOrNull()

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
      .testTag("mood_trend_line_chart")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Chart Header & Series Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Mood Sync Trajectory",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = appColors.textPrimary
          )
          Text(
            text = "Past 7-Day Resonance & Sync",
            fontSize = 11.sp,
            color = appColors.textMuted
          )
        }

        // Legend Pills
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // User Legend
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(userColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = userName, fontSize = 11.sp, color = appColors.textPrimary)
          }

          // Partner Legend
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(partnerColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = partnerName, fontSize = 11.sp, color = appColors.textPrimary)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tooltip Card for Scrubber Selection
      if (activePoint != null) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(bottom = 8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = activePoint.label,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = SecondaryGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Synergy Badge
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimarySienna.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${activePoint.synergyScore}% Synergy ✨",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimarySienna
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // User state
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = activePoint.userMoodIcon, fontSize = 14.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$userName: ${activePoint.userMoodLabel} (${activePoint.userScore})",
                    fontSize = 11.sp,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                  )
                }

                // Partner state
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = activePoint.partnerMoodIcon, fontSize = 14.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$partnerName: ${activePoint.partnerMoodLabel} (${activePoint.partnerScore})",
                    fontSize = 11.sp,
                    color = partnerColor,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              if (activePoint.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "💬 \"${activePoint.note}\"",
                  fontSize = 10.sp,
                  color = appColors.textMuted,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // Main Interactive Canvas Chart or Empty State
      if (chartPoints.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💖", fontSize = 26.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "No moods logged yet",
              fontWeight = FontWeight.Medium,
              fontSize = 14.sp,
              color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Log moods together to unlock your 7-day emotional resonance curve",
              fontSize = 12.sp,
              color = appColors.textMuted,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(chartPoints) {
                detectTapGestures { offset ->
                  val count = chartPoints.size
                  if (count > 1) {
                    val paddingX = size.width * 0.08f
                    val usableWidth = size.width - (paddingX * 2)
                    val step = usableWidth / (count - 1)
                    val index = ((offset.x - paddingX + step / 2) / step)
                      .toInt()
                      .coerceIn(0, count - 1)
                    selectedIndex = index
                  }
                }
              }
              .pointerInput(chartPoints) {
                detectDragGestures(
                  onDragStart = { offset ->
                    isScrubbing = true
                    val count = chartPoints.size
                    if (count > 1) {
                      val paddingX = size.width * 0.08f
                      val usableWidth = size.width - (paddingX * 2)
                      val step = usableWidth / (count - 1)
                      selectedIndex = ((offset.x - paddingX + step / 2) / step)
                        .toInt()
                        .coerceIn(0, count - 1)
                    }
                  },
                  onDragEnd = { isScrubbing = false },
                  onDragCancel = { isScrubbing = false },
                  onDrag = { change, _ ->
                    val count = chartPoints.size
                    if (count > 1) {
                      val paddingX = size.width * 0.08f
                      val usableWidth = size.width - (paddingX * 2)
                      val step = usableWidth / (count - 1)
                      val index = ((change.position.x - paddingX + step / 2) / step)
                        .toInt()
                        .coerceIn(0, count - 1)
                      selectedIndex = index
                    }
                  }
                )
              }
          ) {
            drawCollectiveTrendChart(
              points = chartPoints,
              selectedIndex = selectedIndex,
              userColor = userColor,
              partnerColor = partnerColor
            )
          }
        }
      }

      // X-Axis Labels Row
      if (chartPoints.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          chartPoints.forEachIndexed { idx, pt ->
            val isSelected = idx == selectedIndex
            Text(
              text = if (pt.label.length > 5) pt.label.take(3) else pt.label,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) SecondaryGold else appColors.textMuted
            )
          }
        }
      }
    }
  }
}

private fun DrawScope.drawCollectiveTrendChart(
  points: List<ChartPoint>,
  selectedIndex: Int,
  userColor: Color,
  partnerColor: Color
) {
  val width = size.width
  val height = size.height
  val paddingLeft = width * 0.06f
  val paddingRight = width * 0.06f
  val paddingTop = height * 0.12f
  val paddingBottom = height * 0.16f

  val chartWidth = width - paddingLeft - paddingRight
  val chartHeight = height - paddingTop - paddingBottom

  val count = points.size
  if (count < 2) return

  val stepX = chartWidth / (count - 1)

  // 1. Draw Horizontal Reference Grid Lines & Y-Axis Labels
  val gridLevels = listOf(10f, 8f, 6f, 4f)
  gridLevels.forEach { score ->
    val normalizedY = 1f - ((score - 3f) / 7.5f).coerceIn(0f, 1f)
    val y = paddingTop + (normalizedY * chartHeight)

    // Grid Line
    drawLine(
      color = Color.White.copy(alpha = 0.07f),
      start = Offset(paddingLeft, y),
      end = Offset(width - paddingRight, y),
      strokeWidth = 1.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    )
  }

  // Helper to map score (3.0 to 10.0) to Y pixel coordinate
  fun getY(score: Float): Float {
    val normalized = 1f - ((score - 3f) / 7.5f).coerceIn(0f, 1f)
    return paddingTop + (normalized * chartHeight)
  }

  fun getX(index: Int): Float = paddingLeft + (index * stepX)

  // 2. Compute Smooth Cubic Bézier Paths for User and Partner
  val userPath = Path()
  val partnerPath = Path()

  val userFillPath = Path()
  val partnerFillPath = Path()

  val userPoints = points.mapIndexed { idx, pt -> Offset(getX(idx), getY(pt.userScore)) }
  val partnerPoints = points.mapIndexed { idx, pt -> Offset(getX(idx), getY(pt.partnerScore)) }

  // Build User Path
  userPath.moveTo(userPoints[0].x, userPoints[0].y)
  userFillPath.moveTo(userPoints[0].x, paddingTop + chartHeight)
  userFillPath.lineTo(userPoints[0].x, userPoints[0].y)

  for (i in 0 until userPoints.size - 1) {
    val p0 = userPoints[i]
    val p1 = userPoints[i + 1]
    val controlX1 = p0.x + (p1.x - p0.x) / 2f
    val controlY1 = p0.y
    val controlX2 = p0.x + (p1.x - p0.x) / 2f
    val controlY2 = p1.y

    userPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
    userFillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
  }
  userFillPath.lineTo(userPoints.last().x, paddingTop + chartHeight)
  userFillPath.close()

  // Build Partner Path
  partnerPath.moveTo(partnerPoints[0].x, partnerPoints[0].y)
  partnerFillPath.moveTo(partnerPoints[0].x, paddingTop + chartHeight)
  partnerFillPath.lineTo(partnerPoints[0].x, partnerPoints[0].y)

  for (i in 0 until partnerPoints.size - 1) {
    val p0 = partnerPoints[i]
    val p1 = partnerPoints[i + 1]
    val controlX1 = p0.x + (p1.x - p0.x) / 2f
    val controlY1 = p0.y
    val controlX2 = p0.x + (p1.x - p0.x) / 2f
    val controlY2 = p1.y

    partnerPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
    partnerFillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
  }
  partnerFillPath.lineTo(partnerPoints.last().x, paddingTop + chartHeight)
  partnerFillPath.close()

  // 3. Draw Gradient Area Fills (D3 / Recharts look)
  drawPath(
    path = partnerFillPath,
    brush = Brush.verticalGradient(
      colors = listOf(partnerColor.copy(alpha = 0.22f), Color.Transparent),
      startY = paddingTop,
      endY = paddingTop + chartHeight
    )
  )

  drawPath(
    path = userFillPath,
    brush = Brush.verticalGradient(
      colors = listOf(userColor.copy(alpha = 0.25f), Color.Transparent),
      startY = paddingTop,
      endY = paddingTop + chartHeight
    )
  )

  // 4. Draw Line Strokes
  // Partner Line
  drawPath(
    path = partnerPath,
    color = partnerColor,
    style = Stroke(
      width = 3.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // User Line
  drawPath(
    path = userPath,
    color = userColor,
    style = Stroke(
      width = 3.2.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // 5. Draw Data Point Dots
  partnerPoints.forEachIndexed { index, pt ->
    val isSelected = index == selectedIndex
    drawCircle(
      color = if (isSelected) Color.White else partnerColor,
      radius = if (isSelected) 5.dp.toPx() else 3.2.dp.toPx(),
      center = pt
    )
    if (isSelected) {
      drawCircle(
        color = partnerColor.copy(alpha = 0.4f),
        radius = 8.dp.toPx(),
        center = pt
      )
    }
  }

  userPoints.forEachIndexed { index, pt ->
    val isSelected = index == selectedIndex
    drawCircle(
      color = if (isSelected) Color.White else userColor,
      radius = if (isSelected) 5.5.dp.toPx() else 3.5.dp.toPx(),
      center = pt
    )
    if (isSelected) {
      drawCircle(
        color = userColor.copy(alpha = 0.4f),
        radius = 9.dp.toPx(),
        center = pt
      )
    }
  }

  // 6. Draw Selected Day Vertical Indicator Guideline
  if (selectedIndex in 0 until count) {
    val selX = getX(selectedIndex)
    drawLine(
      color = SecondaryGold.copy(alpha = 0.8f),
      start = Offset(selX, paddingTop),
      end = Offset(selX, paddingTop + chartHeight),
      strokeWidth = 1.5.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )
  }
}
