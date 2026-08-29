package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.CoupleSettings
import com.example.ui.components.RealUsLogo
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalThemeController
import com.example.ui.theme.paperBackground

@Composable
fun ProfileScreen(
  coupleSettings: CoupleSettings?,
  onSwitchTheme: (String) -> Unit,
  onToggleSetting: (String) -> Unit,
  onUpdateProfile: (name1: String, name2: String, anniversary: String) -> Unit,
  onShowWelcome: () -> Unit,
  firebaseUser: FirebaseUser? = null,
  isFirebaseConfigured: Boolean = false,
  firebaseSyncStatus: String = "Ready",
  onSignInWithGoogle: (() -> Unit)? = null,
  onSignOutFirebase: (() -> Unit)? = null,
  onSyncAllToCloud: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val appColors = AppTheme.colors
  val themeController = LocalThemeController.current
  var showEditProfileDialog by remember { mutableStateOf(false) }

  val currentTheme = coupleSettings?.themeName ?: "Night"

  Column(
    modifier = modifier
      .fillMaxSize()
      .paperBackground()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 90.dp)
  ) {
    // Hero Banner Image with Overlapping Dual Avatars
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
    ) {
      AsyncImage(
        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDmydn-lgIJYZMjmW800qyus6DudIJZel3er1PpM_HbK-dyMpATBt_nfNI2Ys46-02eSU27KymepeX_q6E50cRdPJoXt35TnQXuPqofqcSrN4YodKgEirDcWa9EOszjxexBHJ98g-plsWWAexp5LwRxX7ODchYVBN_zCD9KQJySJ7dHfXpoPa_97cF29-dt7dwzi_kVfyzb4kVWroLUhyA0Udt6W7HWrduebp8p4PtSCQbgLMi84w",
        contentDescription = "Couple Sunset Banner",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                appColors.background.copy(alpha = 0.7f),
                appColors.background
              )
            )
          )
      )
    }

    // Overlapping Avatars & Title
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .offset(y = (-40).dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        AsyncImage(
          model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCr3fVoQ3DuG0CGaMULkrVwYXnqw6pJ5HUcX2EdI7iqeF9Fn6_ajHYQ2ZLv1i3HhrkI4H-96sP18wDGIU0oxFPDEZD357n0OHCOGu6ggMr_vRsiyXPFGf4_OHLfVRFE2xvDZaE23woLUmY2DHXnpkYJlszIE0y7Y1Ak1zN7Axp2tgmCYSpCXvyqZGjqhEWe5WQHEbHRgFcZimZEwwnU3K5Dl5lzFHvJVUDqA2jo8HC2X2A1UXhVNg",
          contentDescription = "User Avatar",
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .border(3.dp, appColors.background, CircleShape)
            .zIndex(1f),
          contentScale = ContentScale.Crop
        )

        AsyncImage(
          model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
          contentDescription = "Partner Avatar",
          modifier = Modifier
            .offset(x = (-16).dp)
            .size(76.dp)
            .clip(CircleShape)
            .border(3.dp, appColors.background, CircleShape)
            .zIndex(2f),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      val p1DisplayName = coupleSettings?.partner1Name?.ifBlank { "You" } ?: "You"
      val p2DisplayName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner"
      val annDate = coupleSettings?.anniversaryDate ?: ""
      val daysTogether = coupleSettings?.daysTogether ?: 0
      val coupleCode = coupleSettings?.coupleCode ?: "REALUS-PAIR"

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "$p1DisplayName & $p2DisplayName",
          fontFamily = FontFamily.Serif,
          fontSize = 28.sp,
          fontWeight = FontWeight.SemiBold,
          color = appColors.textPrimary
        )
        IconButton(
          onClick = { showEditProfileDialog = true },
          modifier = Modifier.size(32.dp).testTag("edit_couple_profile_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Names",
            tint = appColors.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Text(
        text = if (annDate.isNotBlank()) "Together since $annDate • $daysTogether Days"
               else if (daysTogether > 0) "$daysTogether Days Together"
               else "Connected Together",
        fontSize = 13.sp,
        color = appColors.textMuted,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Couple Code Capsule (Copyable)
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(appColors.surfaceContainerHigh)
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
          .clickable {
            clipboardManager.setText(AnnotatedString(coupleCode))
            Toast.makeText(context, "Pairing Code copied to clipboard!", Toast.LENGTH_SHORT).show()
          }
          .padding(horizontal = 14.dp, vertical = 6.dp)
          .testTag("couple_code_capsule"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = coupleCode,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = appColors.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy",
          tint = appColors.textMuted,
          modifier = Modifier.size(13.dp)
        )
      }
    }

    // Sections
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .offset(y = (-20).dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Day & Night Mode Selection Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = appColors.primary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "DAY & NIGHT DISPLAY THEMES",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
              color = appColors.textMuted
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Switch between bright daytime clarity and soothing evening warmth designed for shared comfort.",
            fontSize = 12.sp,
            color = appColors.textSecondary,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 3 Major Mode Visual Cards: Day Mode, Night Mode, Evening Comfort Mode
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ModeVisualCard(
              title = "Day Mode",
              subtitle = "Sunlit Warmth",
              icon = Icons.Default.WbSunny,
              primaryPreviewColor = Color(0xFFB85920),
              bgPreviewColor = Color(0xFFFBF8F3),
              isSelected = themeController.currentMode == AppThemeMode.DAY,
              onClick = {
                themeController.setMode(AppThemeMode.DAY)
                onSwitchTheme("Day")
              },
              modifier = Modifier.weight(1f)
            )

            ModeVisualCard(
              title = "Night Mode",
              subtitle = "Midnight Glow",
              icon = Icons.Default.DarkMode,
              primaryPreviewColor = Color(0xFFFFB68E),
              bgPreviewColor = Color(0xFF131313),
              isSelected = themeController.currentMode == AppThemeMode.NIGHT,
              onClick = {
                themeController.setMode(AppThemeMode.NIGHT)
                onSwitchTheme("Night")
              },
              modifier = Modifier.weight(1f)
            )

            ModeVisualCard(
              title = "Evening",
              subtitle = "Candlelight",
              icon = Icons.Default.Bedtime,
              primaryPreviewColor = Color(0xFFFFAD7A),
              bgPreviewColor = Color(0xFF140F0B),
              isSelected = themeController.currentMode == AppThemeMode.EVENING,
              onClick = {
                themeController.setMode(AppThemeMode.EVENING)
                onSwitchTheme("Evening")
              },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = appColors.outlineVariant.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(14.dp))

          // Shared Evening Comfort Feature Spotlight Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(appColors.surfaceContainerHigh)
              .border(1.dp, appColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFA050).copy(alpha = 0.18f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Nightlight,
                contentDescription = null,
                tint = Color(0xFFFFA050),
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Shared Bedtime Comfort",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
              )
              Text(
                text = "Softens contrast and emits cozy amber undertones for comfortable bedtime use.",
                fontSize = 11.sp,
                color = appColors.textMuted,
                lineHeight = 15.sp
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
              checked = themeController.currentMode == AppThemeMode.EVENING,
              onCheckedChange = { isChecked ->
                if (isChecked) {
                  themeController.setMode(AppThemeMode.EVENING)
                  onSwitchTheme("Evening")
                } else {
                  themeController.setMode(AppThemeMode.NIGHT)
                  onSwitchTheme("Night")
                }
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF451900),
                checkedTrackColor = Color(0xFFFFAD7A),
                uncheckedThumbColor = appColors.textSecondary,
                uncheckedTrackColor = appColors.surfaceContainerHighest
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Auto Day/Night schedule row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(appColors.surfaceContainerHigh.copy(alpha = 0.6f))
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = appColors.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Auto Sun/Sunset Schedule",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary
              )
              Text(
                text = "Switches to Day mode at 7 AM, and Evening mode at 7 PM automatically",
                fontSize = 10.5.sp,
                color = appColors.textMuted
              )
            }
            Switch(
              checked = currentTheme.equals("Auto", ignoreCase = true),
              onCheckedChange = { isChecked ->
                if (isChecked) {
                  themeController.setMode(AppThemeMode.AUTO)
                  onSwitchTheme("Auto")
                } else {
                  themeController.setMode(AppThemeMode.NIGHT)
                  onSwitchTheme("Night")
                }
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF451900),
                checkedTrackColor = appColors.primary,
                uncheckedThumbColor = appColors.textSecondary,
                uncheckedTrackColor = appColors.surfaceContainerHighest
              )
            )
          }
        }
      }

      // 2. Preferences & Switches Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "INTIMACY & SECURITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = appColors.textMuted
          )

          Spacer(modifier = Modifier.height(12.dp))

          SettingSwitchRow(
            icon = Icons.Default.Sync,
            title = "Real-Time Partner Sync",
            subtitle = "Keep music, mood, and notes synced",
            isChecked = coupleSettings?.syncWithPartner ?: true,
            onCheckedChange = { onToggleSetting("sync") }
          )

          HorizontalDivider(
            color = appColors.outlineVariant.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 8.dp)
          )

          SettingSwitchRow(
            icon = Icons.Default.Notifications,
            title = "Intimacy Alerts",
            subtitle = "Hugs, kisses, and love note pings",
            isChecked = coupleSettings?.intimacyNotifications ?: true,
            onCheckedChange = { onToggleSetting("intimacy_notif") }
          )

          HorizontalDivider(
            color = appColors.outlineVariant.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 8.dp)
          )

          SettingSwitchRow(
            icon = Icons.Default.Security,
            title = "Biometric Passcode Lock",
            subtitle = "Protect private memories with Face / Fingerprint",
            isChecked = coupleSettings?.biometricLock ?: false,
            onCheckedChange = { onToggleSetting("biometric") }
          )
        }
      }

      // Firebase Cloud Sync & Google Auth Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, appColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
          .testTag("firebase_cloud_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "Firebase Sync",
                tint = appColors.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "FIREBASE CLOUD & MULTIPLAYER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = appColors.textMuted
              )
            }

            // Status Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isFirebaseConfigured) appColors.primary.copy(alpha = 0.15f)
                  else appColors.surfaceContainerHighest
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = if (isFirebaseConfigured) "FIREBASE ENABLED" else "ROOM OFFLINE MODE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFirebaseConfigured) appColors.primary else appColors.textMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (firebaseUser != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(appColors.surfaceContainerHigh)
                .padding(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = firebaseUser.displayName ?: "Partner Connected",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = appColors.textPrimary
                )
                Text(
                  text = firebaseUser.email ?: firebaseUser.uid,
                  fontSize = 11.sp,
                  color = appColors.textMuted
                )
              }
              if (onSignOutFirebase != null) {
                IconButton(
                  onClick = onSignOutFirebase,
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = appColors.textMuted,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (onSyncAllToCloud != null) {
              Button(
                onClick = onSyncAllToCloud,
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("sync_cloud_now_btn"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync All Memories to Firestore Cloud", fontSize = 12.5.sp)
              }
            }
          } else {
            Text(
              text = "Connect Google Sign-In & Firebase Firestore to backup love notes, voice memos, and timeline chapters across devices safely.",
              fontSize = 12.sp,
              color = appColors.textSecondary,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (onSignInWithGoogle != null) {
              Button(
                onClick = onSignInWithGoogle,
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("google_signin_firebase_btn"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CloudQueue,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign in with Google (Firebase)", fontSize = 12.5.sp)
              }
            }
          }

          if (firebaseSyncStatus.isNotBlank() && firebaseSyncStatus != "Ready") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Status: $firebaseSyncStatus",
              fontSize = 10.5.sp,
              color = appColors.primary,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // 3. Welcome / Intro re-launcher
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onShowWelcome() }
          .padding(bottom = 16.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RealUsLogo(
            size = 42.dp,
            showText = false,
            isAnimated = true
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "RealUs v2.5.0",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = appColors.textPrimary
            )
            Text(
              text = "Tap to replay romantic onboarding experience",
              fontSize = 12.sp,
              color = appColors.textMuted
            )
          }
        }
      }
    }
  }

  // Edit Profile Modal Dialog
  if (showEditProfileDialog) {
    var p1Name by remember { mutableStateOf(coupleSettings?.partner1Name ?: "") }
    var p2Name by remember { mutableStateOf(coupleSettings?.partner2Name ?: "") }
    var anniversary by remember { mutableStateOf(coupleSettings?.anniversaryDate ?: "") }

    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      containerColor = appColors.surfaceContainer,
      title = {
        Text(
          text = "Edit Couple Profile",
          fontFamily = FontFamily.Serif,
          color = appColors.textPrimary
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = p1Name,
            onValueChange = { p1Name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = p2Name,
            onValueChange = { p2Name = it },
            label = { Text("Partner's Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = anniversary,
            onValueChange = { anniversary = it },
            label = { Text("Anniversary Date") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (p1Name.isNotBlank() && p2Name.isNotBlank()) {
              onUpdateProfile(p1Name, p2Name, anniversary)
              showEditProfileDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryDark)
        ) {
          Text("Save Changes", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel", color = appColors.textSecondary)
        }
      }
    )
  }
}

@Composable
private fun ModeVisualCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  primaryPreviewColor: Color,
  bgPreviewColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(if (isSelected) appColors.surfaceClay else appColors.surfaceContainerHigh)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) appColors.primary else appColors.outlineVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(14.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 12.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Color Preview Pill
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(bgPreviewColor)
          .border(1.5.dp, primaryPreviewColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = primaryPreviewColor,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) appColors.primary else appColors.textPrimary
      )

      Text(
        text = subtitle,
        fontSize = 9.5.sp,
        color = appColors.textMuted
      )
    }
  }
}

@Composable
private fun SettingSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  val appColors = AppTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = appColors.primary,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = appColors.textPrimary
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = appColors.textMuted
      )
    }
    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = appColors.onPrimary,
        checkedTrackColor = appColors.primary,
        uncheckedThumbColor = appColors.textSecondary,
        uncheckedTrackColor = appColors.surfaceContainerHighest
      )
    )
  }
}
