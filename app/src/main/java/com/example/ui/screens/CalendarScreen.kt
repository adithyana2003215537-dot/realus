package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.ui.theme.AppTheme
import com.example.ui.theme.paperBackground

@Composable
fun CalendarScreen(
  events: List<CalendarEvent>,
  onAddEvent: (title: String, date: String, dayOfMonth: Int, iconType: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  var showAddDialog by remember { mutableStateOf(false) }
  var selectedDay by remember { mutableStateOf(10) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
          Text(
            text = "Our Days",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textPrimary
          )
          Text(
            text = "Shared calendar for special moments and dates",
            fontSize = 13.sp,
            color = appColors.textMuted,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      // Month Header and Days Grid Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Month switcher
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "October 2024",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Row {
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                  Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = appColors.textSecondary)
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                  Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = appColors.textSecondary)
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day labels
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                Text(
                  text = dayLabel,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = appColors.textMuted
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 31 Days Grid
            val specialDays = events.map { it.dayOfMonth }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              for (week in 0..4) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  for (d in 1..7) {
                    val dayNum = week * 7 + d
                    if (dayNum <= 31) {
                      val isSpecial = specialDays.contains(dayNum)
                      val isSelected = selectedDay == dayNum

                      Box(
                        modifier = Modifier
                          .size(36.dp)
                          .clip(CircleShape)
                          .background(
                            when {
                              isSelected -> appColors.primary
                              isSpecial -> appColors.secondary.copy(alpha = 0.2f)
                              else -> Color.Transparent
                            }
                          )
                          .border(
                            width = if (isSpecial && !isSelected) 1.5.dp else 0.dp,
                            color = if (isSpecial) appColors.secondary else Color.Transparent,
                            shape = CircleShape
                          )
                          .clickable { selectedDay = dayNum },
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = "$dayNum",
                          fontSize = 13.sp,
                          fontWeight = if (isSpecial || isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = when {
                            isSelected -> appColors.onPrimary
                            isSpecial -> appColors.secondary
                            else -> appColors.textPrimary
                          }
                        )
                      }
                    } else {
                      Spacer(modifier = Modifier.size(36.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }

      // COMING UP Header
      item {
        Text(
          text = "COMING UP",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = appColors.textMuted,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      items(events, key = { it.id }) { event ->
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .testTag("calendar_event_${event.id}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(appColors.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (event.iconType == "flight") Icons.Default.Flight else Icons.Default.Favorite,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = event.title,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Text(
                text = "${event.dateStr} • ${event.daysRemainingText}",
                fontSize = 12.sp,
                color = appColors.textMuted
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.surfaceContainerHigh)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${event.dayOfMonth}th",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.primary
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(100.dp))
      }
    }

    // Add Date FAB
    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = appColors.primary,
      contentColor = appColors.onPrimary,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 86.dp)
        .testTag("add_calendar_event_fab")
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = "Mark Special Date")
    }
  }

  // Add Event Dialog
  if (showAddDialog) {
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("Oct 28") }
    var dayNum by remember { mutableStateOf("28") }
    var iconType by remember { mutableStateOf("heart") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Mark Special Date",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Name (e.g. Candlelit Dinner)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = dateStr,
            onValueChange = { dateStr = it },
            label = { Text("Date (e.g. Oct 28)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = dayNum,
            onValueChange = { dayNum = it },
            label = { Text("Day of Month (1-31)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              onAddEvent(title, dateStr, dayNum.toIntOrNull() ?: 15, iconType)
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
        ) {
          Text("Save Date", color = appColors.onPrimary)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }
}
