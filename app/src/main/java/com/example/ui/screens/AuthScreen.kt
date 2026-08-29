package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.UserProfile
import com.example.ui.components.RealUsLogo
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimarySienna
import com.example.ui.theme.PrimarySiennaDark
import com.example.ui.theme.SecondaryGold
import com.example.ui.theme.paperBackground
import com.example.ui.viewmodel.AppAuthState
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
  authState: AppAuthState,
  userProfile: UserProfile?,
  onSendPhoneOtp: (activity: Activity, phoneNumber: String, onCodeSent: (String) -> Unit, onError: (String) -> Unit) -> Unit,
  onVerifyPhoneOtp: (verificationId: String, otpCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onResendPhoneOtp: (activity: Activity, phoneNumber: String, onError: (String) -> Unit) -> Unit,
  onSaveProfile: (name: String, profilePicUrl: String, anniversaryDate: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onConnectPartner: (partnerCode: String, anniversaryDate: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onSkipPairing: () -> Unit,
  onSignInWithGoogle: ((Activity) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val appColors = AppTheme.colors

  // Local UI step state within Auth flow
  var authStep by remember { mutableStateOf(AuthFlowStep.PHONE_INPUT) }
  var countryCode by remember { mutableStateOf("+1") }
  var phoneNumber by remember { mutableStateOf("") }
  var verificationId by remember { mutableStateOf("") }
  var otpCode by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("") }
  var profilePicUrl by remember { mutableStateOf("") }
  var anniversaryDate by remember { mutableStateOf("") }
  var partnerCodeInput by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Trigger Snackbar when error or success occurs
  LaunchedEffect(errorMessage) {
    errorMessage?.let { rawErr ->
      val formatted = formatAuthError(rawErr) ?: rawErr
      scope.launch {
        snackbarHostState.showSnackbar(
          message = formatted,
          actionLabel = "Dismiss"
        )
      }
    }
  }

  LaunchedEffect(successMessage) {
    successMessage?.let { msg ->
      scope.launch {
        snackbarHostState.showSnackbar(
          message = msg,
          actionLabel = "OK"
        )
      }
    }
  }

  var resendCountdown by remember { mutableIntStateOf(60) }
  var isResendEnabled by remember { mutableStateOf(false) }

  // Sync with ViewModel authState
  LaunchedEffect(authState, userProfile) {
    when (authState) {
      AppAuthState.WELCOME, AppAuthState.UNAUTHENTICATED -> {
        if (authStep != AuthFlowStep.OTP_VERIFY) {
          authStep = AuthFlowStep.PHONE_INPUT
        }
      }
      AppAuthState.PROFILE_SETUP -> {
        authStep = AuthFlowStep.PROFILE_SETUP
        if (userProfile?.name?.isNotBlank() == true) {
          userName = userProfile.name
        }
        if (userProfile?.profilePicUrl?.isNotBlank() == true) {
          profilePicUrl = userProfile.profilePicUrl
        }
        if (userProfile?.anniversaryDate?.isNotBlank() == true) {
          anniversaryDate = userProfile.anniversaryDate
        }
      }
      AppAuthState.PENDING_PAIRING -> {
        authStep = AuthFlowStep.PARTNER_PAIRING
      }
      AppAuthState.AUTHENTICATED_AND_PAIRED -> {
        // App handles navigation to main screen
      }
    }
  }

  // Resend Countdown Timer
  LaunchedEffect(authStep, resendCountdown) {
    if (authStep == AuthFlowStep.OTP_VERIFY && resendCountdown > 0) {
      delay(1000)
      resendCountdown -= 1
      if (resendCountdown == 0) {
        isResendEnabled = true
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            PrimaryContainer.copy(alpha = 0.25f),
            DarkSurfaceContainer,
            DarkSurface
          ),
          radius = 1200f
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {

      // Top Navigation / Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (authStep == AuthFlowStep.OTP_VERIFY) {
          IconButton(
            onClick = {
              authStep = AuthFlowStep.PHONE_INPUT
              errorMessage = null
            },
            modifier = Modifier.testTag("auth_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = appColors.textPrimary
            )
          }
        } else {
          Spacer(modifier = Modifier.size(48.dp))
        }

        RealUsLogo(
          size = 44.dp,
          showText = false,
          isAnimated = true
        )

        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Form Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = PrimarySienna.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerLow)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {

          // Step Content
          when (authStep) {
            AuthFlowStep.PHONE_INPUT -> {
              PhoneInputView(
                countryCode = countryCode,
                phoneNumber = phoneNumber,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onCountryCodeChange = { countryCode = it },
                onPhoneNumberChange = {
                  phoneNumber = it.filter { char -> char.isDigit() }
                  errorMessage = null
                },
                onSubmit = {
                  val fullNumber = "$countryCode$phoneNumber".trim()
                  if (phoneNumber.length < 7) {
                    errorMessage = "Please enter a valid phone number"
                    return@PhoneInputView
                  }
                  if (activity == null) {
                    errorMessage = "Unable to start phone verification in current window"
                    return@PhoneInputView
                  }
                  isLoading = true
                  errorMessage = null
                  onSendPhoneOtp(
                    activity,
                    fullNumber,
                    { vId ->
                      isLoading = false
                      verificationId = vId
                      authStep = AuthFlowStep.OTP_VERIFY
                      resendCountdown = 60
                      isResendEnabled = false
                      successMessage = "Verification code sent via SMS"
                    },
                    { err ->
                      isLoading = false
                      errorMessage = err
                    }
                  )
                },
                onDismissError = { errorMessage = null },
                onGoogleSignIn = if (onSignInWithGoogle != null && activity != null) {
                  { onSignInWithGoogle(activity) }
                } else null
              )
            }

            AuthFlowStep.OTP_VERIFY -> {
              OtpVerifyView(
                fullPhoneNumber = "$countryCode $phoneNumber",
                otpCode = otpCode,
                isLoading = isLoading,
                errorMessage = errorMessage,
                successMessage = successMessage,
                resendCountdown = resendCountdown,
                isResendEnabled = isResendEnabled,
                onOtpCodeChange = {
                  if (it.length <= 6) {
                    otpCode = it.filter { c -> c.isDigit() }
                    errorMessage = null
                  }
                },
                onVerify = {
                  if (otpCode.length < 6) {
                    errorMessage = "Please enter the complete 6-digit OTP code"
                    return@OtpVerifyView
                  }
                  isLoading = true
                  errorMessage = null
                  onVerifyPhoneOtp(
                    verificationId,
                    otpCode,
                    {
                      isLoading = false
                      authStep = AuthFlowStep.PROFILE_SETUP
                    },
                    { err ->
                      isLoading = false
                      errorMessage = err
                    }
                  )
                },
                onResend = {
                  if (activity != null && isResendEnabled) {
                    val fullNumber = "$countryCode$phoneNumber".trim()
                    isLoading = true
                    errorMessage = null
                    onResendPhoneOtp(
                      activity,
                      fullNumber,
                      { err ->
                        isLoading = false
                        errorMessage = err
                      }
                    )
                    resendCountdown = 60
                    isResendEnabled = false
                    isLoading = false
                    successMessage = "New code sent"
                  }
                },
                onDismissError = { errorMessage = null },
                onDismissSuccess = { successMessage = null }
              )
            }

            AuthFlowStep.PROFILE_SETUP -> {
              ProfileSetupView(
                userName = userName,
                profilePicUrl = profilePicUrl,
                anniversaryDate = anniversaryDate,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onUserNameChange = {
                  userName = it
                  errorMessage = null
                },
                onProfilePicUrlChange = {
                  profilePicUrl = it
                },
                onAnniversaryChange = {
                  anniversaryDate = it
                },
                onSubmit = {
                  if (userName.isBlank()) {
                    errorMessage = "Please enter your name"
                    return@ProfileSetupView
                  }
                  isLoading = true
                  errorMessage = null
                  onSaveProfile(
                    userName.trim(),
                    profilePicUrl.trim(),
                    anniversaryDate.trim(),
                    {
                      isLoading = false
                      authStep = AuthFlowStep.PARTNER_PAIRING
                    },
                    { err ->
                      isLoading = false
                      errorMessage = err
                    }
                  )
                },
                onDismissError = { errorMessage = null }
              )
            }

            AuthFlowStep.PARTNER_PAIRING -> {
              PartnerPairingView(
                myPartnerCode = userProfile?.partnerCode ?: "GENERATING...",
                partnerCodeInput = partnerCodeInput,
                anniversaryDate = anniversaryDate.ifBlank { userProfile?.anniversaryDate ?: "" },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onPartnerCodeInputChange = {
                  partnerCodeInput = it.uppercase()
                  errorMessage = null
                },
                onConnectPartner = {
                  if (partnerCodeInput.isBlank()) {
                    errorMessage = "Please enter partner's pairing code"
                    return@PartnerPairingView
                  }
                  isLoading = true
                  errorMessage = null
                  onConnectPartner(
                    partnerCodeInput.trim(),
                    anniversaryDate.trim(),
                    {
                      isLoading = false
                    },
                    { err ->
                      isLoading = false
                      errorMessage = err
                    }
                  )
                },
                onSkip = onSkipPairing,
                onDismissError = { errorMessage = null }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Bottom Security Footer
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = appColors.textMuted,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Real Firebase Auth & End-to-End Encrypted Couple Space",
          fontSize = 11.sp,
          color = appColors.textMuted
        )
      }
    }

    // Interactive Snackbar Overlay
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp)
        .navigationBarsPadding()
        .testTag("auth_snackbar_host"),
      snackbar = { data ->
        val isError = errorMessage != null
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isError) DarkSurfaceContainer else PrimarySiennaDark,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isError) ErrorRed.copy(alpha = 0.6f) else SecondaryGold.copy(alpha = 0.6f)
          ),
          shadowElevation = 10.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (isError) ErrorRed else SecondaryGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = data.visuals.message,
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.weight(1f)
            )
            data.visuals.actionLabel?.let { label ->
              Spacer(modifier = Modifier.width(8.dp))
              TextButton(onClick = { data.dismiss() }) {
                Text(label, color = SecondaryGold, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    )
  }
}

/** Sanitizes technical exception messages into clear user-friendly guidance. */
fun formatAuthError(rawError: String?): String? {
  if (rawError.isNullOrBlank()) return null
  val err = rawError.lowercase()
  return when {
    err.contains("invalid-phone-number") || err.contains("invalid phone") || err.contains("format") ->
      "Invalid phone number format. Please include your country code and phone number."
    err.contains("quota-exceeded") || err.contains("too-many-requests") || err.contains("blocked") ->
      "SMS quota limit reached. Please wait a few minutes or sign in with Google."
    err.contains("invalid-verification-code") || err.contains("invalid_otp") || err.contains("code entered is invalid") ->
      "Incorrect verification code. Please check your SMS and try again."
    err.contains("session-expired") ->
      "Verification session expired. Please request a new SMS code."
    err.contains("network") || err.contains("unavailable") || err.contains("connection") ->
      "Network error. Please check your internet connection."
    err.contains("credential") || err.contains("canceled") || err.contains("cancelled") ->
      "Google Sign-In canceled or failed. Please try again."
    err.contains("code not found") || err.contains("invalid_code") || err.contains("no user found") ->
      "Pairing code not found. Please double-check the 6-character code with your partner."
    err.contains("already paired") || err.contains("already linked") ->
      "This code has already been paired with another user."
    else -> rawError
  }
}

@Composable
private fun ErrorFeedbackCard(
  message: String,
  onDismiss: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val formattedMsg = remember(message) { formatAuthError(message) ?: message }
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = ErrorRed.copy(alpha = 0.12f)
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.ErrorOutline,
        contentDescription = "Error",
        tint = ErrorRed,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = formattedMsg,
        color = ErrorRed,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.weight(1f)
      )
      if (onDismiss != null) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss error",
            tint = ErrorRed.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun SuccessFeedbackCard(
  message: String,
  onDismiss: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val appColors = AppTheme.colors
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = PrimarySienna.copy(alpha = 0.12f)
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, PrimarySienna.copy(alpha = 0.5f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Success",
        tint = appColors.primary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = message,
        color = appColors.textPrimary,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.weight(1f)
      )
      if (onDismiss != null) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = appColors.textMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

enum class AuthFlowStep {
  PHONE_INPUT,
  OTP_VERIFY,
  PROFILE_SETUP,
  PARTNER_PAIRING
}

data class CountryCodeInfo(val name: String, val code: String, val flag: String)

val ALL_COUNTRY_CODES = listOf(
  CountryCodeInfo("United States", "+1", "🇺🇸"),
  CountryCodeInfo("Canada", "+1", "🇨🇦"),
  CountryCodeInfo("India", "+91", "🇮🇳"),
  CountryCodeInfo("United Kingdom", "+44", "🇬🇧"),
  CountryCodeInfo("Australia", "+61", "🇦🇺"),
  CountryCodeInfo("Germany", "+49", "🇩🇪"),
  CountryCodeInfo("France", "+33", "🇫🇷"),
  CountryCodeInfo("Japan", "+81", "🇯🇵"),
  CountryCodeInfo("China", "+86", "🇨🇳"),
  CountryCodeInfo("Brazil", "+55", "🇧🇷"),
  CountryCodeInfo("Mexico", "+52", "🇲🇽"),
  CountryCodeInfo("United Arab Emirates", "+971", "🇦🇪"),
  CountryCodeInfo("Singapore", "+65", "🇸🇬"),
  CountryCodeInfo("Malaysia", "+60", "🇲🇾"),
  CountryCodeInfo("Indonesia", "+62", "🇮🇩"),
  CountryCodeInfo("Philippines", "+63", "🇵🇭"),
  CountryCodeInfo("South Korea", "+82", "🇰🇷"),
  CountryCodeInfo("Italy", "+39", "🇮🇹"),
  CountryCodeInfo("Spain", "+34", "🇪🇸"),
  CountryCodeInfo("Russia", "+7", "🇷🇺"),
  CountryCodeInfo("Egypt", "+20", "🇪🇬"),
  CountryCodeInfo("Nigeria", "+234", "🇳🇬"),
  CountryCodeInfo("Kenya", "+254", "🇰🇪"),
  CountryCodeInfo("Pakistan", "+92", "🇵🇰"),
  CountryCodeInfo("Bangladesh", "+880", "🇧🇩"),
  CountryCodeInfo("Vietnam", "+84", "🇻🇳"),
  CountryCodeInfo("Thailand", "+66", "🇹🇭"),
  CountryCodeInfo("Turkey", "+90", "🇹🇷"),
  CountryCodeInfo("Saudi Arabia", "+966", "🇸🇦"),
  CountryCodeInfo("South Africa", "+27", "🇿🇦"),
  CountryCodeInfo("Netherlands", "+31", "🇳🇱"),
  CountryCodeInfo("Sweden", "+46", "🇸🇪"),
  CountryCodeInfo("Norway", "+47", "🇳🇴"),
  CountryCodeInfo("Denmark", "+45", "🇩🇰"),
  CountryCodeInfo("Finland", "+358", "🇫🇮"),
  CountryCodeInfo("Ireland", "+353", "🇮🇪"),
  CountryCodeInfo("New Zealand", "+64", "🇳🇿"),
  CountryCodeInfo("Argentina", "+54", "🇦🇷"),
  CountryCodeInfo("Chile", "+56", "🇨🇱"),
  CountryCodeInfo("Colombia", "+57", "🇨🇴"),
  CountryCodeInfo("Peru", "+51", "🇵🇪"),
  CountryCodeInfo("Kuwait", "+965", "🇰🇼"),
  CountryCodeInfo("Qatar", "+974", "🇶🇦"),
  CountryCodeInfo("Oman", "+968", "🇴🇲"),
  CountryCodeInfo("Bahrain", "+973", "🇧🇭"),
  CountryCodeInfo("Jordan", "+962", "🇯🇴"),
  CountryCodeInfo("Lebanon", "+961", "🇱🇧"),
  CountryCodeInfo("Israel", "+972", "🇮🇱"),
  CountryCodeInfo("Iraq", "+964", "🇮🇶"),
  CountryCodeInfo("Greece", "+30", "🇬🇷"),
  CountryCodeInfo("Poland", "+48", "🇵🇱"),
  CountryCodeInfo("Hungary", "+36", "🇭🇺"),
  CountryCodeInfo("Czech Republic", "+420", "🇨🇿"),
  CountryCodeInfo("Romania", "+40", "🇷🇴"),
  CountryCodeInfo("Ukraine", "+380", "🇺🇦"),
  CountryCodeInfo("Portugal", "+351", "🇵🇹"),
  CountryCodeInfo("Belgium", "+32", "🇧🇪"),
  CountryCodeInfo("Austria", "+43", "🇦🇹"),
  CountryCodeInfo("Switzerland", "+41", "🇨🇭"),
  CountryCodeInfo("Morocco", "+212", "🇲🇦"),
  CountryCodeInfo("Tunisia", "+216", "🇹🇳"),
  CountryCodeInfo("Algeria", "+213", "🇩🇿"),
  CountryCodeInfo("Ghana", "+233", "🇬🇭"),
  CountryCodeInfo("Ethiopia", "+251", "🇪🇹"),
  CountryCodeInfo("Tanzania", "+255", "🇹🇿"),
  CountryCodeInfo("Uganda", "+256", "🇺🇬"),
  CountryCodeInfo("Zambia", "+260", "🇿🇲"),
  CountryCodeInfo("Zimbabwe", "+263", "🇿🇼"),
  CountryCodeInfo("Sri Lanka", "+94", "🇱🇰"),
  CountryCodeInfo("Nepal", "+977", "🇳🇵"),
  CountryCodeInfo("Myanmar", "+95", "🇲🇲"),
  CountryCodeInfo("Hong Kong", "+852", "🇭🇰"),
  CountryCodeInfo("Taiwan", "+886", "🇹🇼"),
  CountryCodeInfo("Macau", "+853", "🇲🇴"),
  CountryCodeInfo("Iceland", "+354", "🇮🇸"),
  CountryCodeInfo("Luxembourg", "+352", "🇱🇺"),
  CountryCodeInfo("Malta", "+356", "🇲🇹"),
  CountryCodeInfo("Cyprus", "+357", "🇨🇾"),
  CountryCodeInfo("Croatia", "+385", "🇭🇷"),
  CountryCodeInfo("Slovakia", "+421", "🇸🇰"),
  CountryCodeInfo("Slovenia", "+386", "🇸🇮"),
  CountryCodeInfo("Bulgaria", "+359", "🇧🇬"),
  CountryCodeInfo("Estonia", "+372", "🇪🇪"),
  CountryCodeInfo("Latvia", "+371", "🇱🇻"),
  CountryCodeInfo("Lithuania", "+370", "🇱🇹"),
  CountryCodeInfo("Costa Rica", "+506", "🇨🇷"),
  CountryCodeInfo("Panama", "+507", "🇵🇦"),
  CountryCodeInfo("Ecuador", "+593", "🇪🇨"),
  CountryCodeInfo("Venezuela", "+58", "🇻🇪"),
  CountryCodeInfo("Bolivia", "+591", "🇧🇴"),
  CountryCodeInfo("Paraguay", "+595", "🇵🇾"),
  CountryCodeInfo("Uruguay", "+598", "🇺🇾")
)

@Composable
fun CountryCodePickerDialog(
  onDismiss: () -> Unit,
  onSelectCountry: (CountryCodeInfo) -> Unit
) {
  val appColors = AppTheme.colors
  var searchQuery by remember { mutableStateOf("") }

  val filteredList = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      ALL_COUNTRY_CODES
    } else {
      ALL_COUNTRY_CODES.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.code.contains(searchQuery)
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = appColors.primary)
      }
    },
    title = {
      Text(
        text = "Select Country Code",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = appColors.textPrimary
      )
    },
    text = {
      Column(modifier = Modifier.height(380.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search country or code...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = appColors.primary,
            unfocusedBorderColor = appColors.outlineVariant
          )
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredList) { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onSelectCountry(item)
                  onDismiss()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.flag, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = item.name,
                  fontSize = 14.sp,
                  color = appColors.textPrimary,
                  fontWeight = FontWeight.Medium
                )
              }
              Text(
                text = item.code,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.primary
              )
            }
          }
        }
      }
    },
    containerColor = appColors.surface,
    shape = RoundedCornerShape(20.dp)
  )
}

@Composable
private fun PhoneInputView(
  countryCode: String,
  phoneNumber: String,
  isLoading: Boolean,
  errorMessage: String?,
  onCountryCodeChange: (String) -> Unit,
  onPhoneNumberChange: (String) -> Unit,
  onSubmit: () -> Unit,
  onDismissError: () -> Unit,
  onGoogleSignIn: (() -> Unit)? = null
) {
  val appColors = AppTheme.colors
  val focusManager = LocalFocusManager.current
  var showCountryPicker by remember { mutableStateOf(false) }

  val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
  val isValidPhone = digitsOnly.length in 7..15
  val isInvalidPhone = phoneNumber.isNotBlank() && !isValidPhone

  if (showCountryPicker) {
    CountryCodePickerDialog(
      onDismiss = { showCountryPicker = false },
      onSelectCountry = { item ->
        onCountryCodeChange(item.code)
      }
    )
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "Welcome to RealUs",
      fontFamily = FontFamily.Serif,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = appColors.textPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Enter your phone number to receive a secure login code",
      fontSize = 13.sp,
      color = appColors.textSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Phone Number Input Row with Country Code Selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Country Code Picker Button & Input
      Box(modifier = Modifier.width(110.dp)) {
        OutlinedTextField(
          value = countryCode,
          onValueChange = onCountryCodeChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("country_code_input"),
          label = { Text("Code") },
          singleLine = true,
          trailingIcon = {
            IconButton(
              onClick = { showCountryPicker = true },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Country",
                tint = appColors.primary
              )
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = appColors.primary,
            unfocusedBorderColor = appColors.outlineVariant
          )
        )
      }

      OutlinedTextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChange,
        modifier = Modifier
          .weight(1f)
          .testTag("phone_number_input"),
        label = { Text("Phone Number") },
        placeholder = { Text("e.g. 5551234567") },
        isError = isInvalidPhone || (errorMessage != null && phoneNumber.length < 7),
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = if (isInvalidPhone) ErrorRed else if (isValidPhone) Color(0xFF2E7D32) else appColors.primary
          )
        },
        trailingIcon = {
          if (isValidPhone) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Valid Phone Number",
              tint = Color(0xFF2E7D32)
            )
          } else if (isInvalidPhone) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Invalid Format",
              tint = ErrorRed
            )
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Phone,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
          if (isValidPhone) {
            focusManager.clearFocus()
            onSubmit()
          }
        }),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = if (isValidPhone) Color(0xFF2E7D32) else appColors.primary,
          unfocusedBorderColor = if (isValidPhone) Color(0xFF2E7D32).copy(alpha = 0.6f) else appColors.outlineVariant,
          errorBorderColor = ErrorRed
        )
      )
    }

    // Helper Text & Validation Visual Cues
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp, start = 4.dp, end = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isValidPhone) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Valid phone format",
            fontSize = 11.5.sp,
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Medium
          )
        }
      } else if (isInvalidPhone) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Enter 7–15 digits",
            fontSize = 11.5.sp,
            color = ErrorRed,
            fontWeight = FontWeight.Medium
          )
        }
      } else {
        Text(
          text = "Enter phone number without country code",
          fontSize = 11.5.sp,
          color = appColors.textMuted
        )
      }

      Text(
        text = "${digitsOnly.length}/10 digits",
        fontSize = 11.5.sp,
        color = if (isValidPhone) Color(0xFF2E7D32) else appColors.textMuted,
        fontWeight = if (isValidPhone) FontWeight.Bold else FontWeight.Normal
      )
    }

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      ErrorFeedbackCard(
        message = errorMessage,
        onDismiss = onDismissError
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onSubmit,
      enabled = !isLoading && isValidPhone,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("send_otp_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimarySiennaDark,
        disabledContainerColor = PrimarySiennaDark.copy(alpha = 0.45f)
      ),
      shape = RoundedCornerShape(26.dp)
    ) {
      if (isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Sending Code...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Send Verification Code", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
      }
    }

    if (onGoogleSignIn != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = appColors.outlineVariant.copy(alpha = 0.4f))
        Text(
          text = " OR ",
          fontSize = 11.sp,
          color = appColors.textMuted,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = appColors.outlineVariant.copy(alpha = 0.4f))
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedButton(
        onClick = onGoogleSignIn,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("google_auth_btn"),
        shape = RoundedCornerShape(24.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(appColors.primary, appColors.secondary)))
      ) {
        Text("Continue with Google", fontSize = 13.5.sp, color = appColors.textPrimary)
      }
    }
  }
}

@Composable
private fun OtpVerifyView(
  fullPhoneNumber: String,
  otpCode: String,
  isLoading: Boolean,
  errorMessage: String?,
  successMessage: String?,
  resendCountdown: Int,
  isResendEnabled: Boolean,
  onOtpCodeChange: (String) -> Unit,
  onVerify: () -> Unit,
  onResend: () -> Unit,
  onDismissError: () -> Unit,
  onDismissSuccess: () -> Unit
) {
  val appColors = AppTheme.colors
  val focusManager = LocalFocusManager.current

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "Enter Verification Code",
      fontFamily = FontFamily.Serif,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = appColors.textPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "We sent a 6-digit SMS code to $fullPhoneNumber",
      fontSize = 13.sp,
      color = appColors.textSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    // 6-digit OTP Input field
    OutlinedTextField(
      value = otpCode,
      onValueChange = onOtpCodeChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("otp_code_input"),
      label = { Text("6-Digit Code") },
      placeholder = { Text("• • • • • •") },
      isError = errorMessage != null,
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(onDone = {
        focusManager.clearFocus()
        onVerify()
      }),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors.primary,
        unfocusedBorderColor = appColors.outlineVariant,
        errorBorderColor = ErrorRed
      )
    )

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      ErrorFeedbackCard(
        message = errorMessage,
        onDismiss = onDismissError
      )
    } else if (successMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      SuccessFeedbackCard(
        message = successMessage,
        onDismiss = onDismissSuccess
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onVerify,
      enabled = !isLoading && otpCode.trim().length >= 6,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("verify_otp_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimarySiennaDark,
        disabledContainerColor = PrimarySiennaDark.copy(alpha = 0.45f)
      ),
      shape = RoundedCornerShape(26.dp)
    ) {
      if (isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Verifying Code...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Verify & Continue", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Resend Code Row
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = if (isResendEnabled) "Didn't receive code? " else "Resend code in ${resendCountdown}s",
        fontSize = 12.5.sp,
        color = appColors.textMuted
      )
      if (isResendEnabled) {
        TextButton(
          onClick = onResend,
          modifier = Modifier.testTag("resend_otp_btn")
        ) {
          Text("Resend SMS", color = appColors.primary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun ProfileSetupView(
  userName: String,
  profilePicUrl: String,
  anniversaryDate: String,
  isLoading: Boolean,
  errorMessage: String?,
  onUserNameChange: (String) -> Unit,
  onProfilePicUrlChange: (String) -> Unit,
  onAnniversaryChange: (String) -> Unit,
  onSubmit: () -> Unit,
  onDismissError: () -> Unit
) {
  val appColors = AppTheme.colors
  val focusManager = LocalFocusManager.current

  // Activity Result Launcher for gallery photo selection
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      onProfilePicUrlChange(it.toString())
    }
  }

  val presetAvatars = listOf(
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCr3fVoQ3DuG0CGaMULkrVwYXnqw6pJ5HUcX2EdI7iqeF9Fn6_ajHYQ2ZLv1i3HhrkI4H-96sP18wDGIU0oxFPDEZD357n0OHCOGu6ggMr_vRsiyXPFGf4_OHLfVRFE2xvDZaE23woLUmY2DHXnpkYJlszIE0y7Y1Ak1zN7Axp2tgmCYSpCXvyqZGjqhEWe5WQHEbHRgFcZimZEwwnU3K5Dl5lzFHvJVUDqA2jo8HC2X2A1UXhVNg",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAu_QpVqpQoXxQY_m-0ay9JFv6g_qxsE4rnOrAJDLH3kIwuhwESjtjVPlGs-TzKuCOcNmOW74WOex_9yitbsyfS2zGVWUDbkoBnnDEjvIaHUK-mZcQ9damHM7bl9AuOfGRK0-oI54cl3pvqb_XDub-aJQBmMpiZJYXJge_USqXkEs3hsOk_g1G0oKaXcOJo-joZN17jV9j499ASeqq8tnQWJjaVhjE-pblsv7lf82UXErOmkWjN5Q",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuA6b5w49Dj3A4PVSN5weFyV3OLZ7wOwlVl9I6e1wBqRPCWglTQLeDchOkgf0G3kJ-XWLa6kVXF11puARoddGpodLqMlJiBKz_9tuE9s6gc8wDpynqhAKp1UH0mmA0V46qTXxiGEiEj73TrTJIc2V4a-fr3XHQ9j9kD_dBraWpL9YjMOyO80-wyGCE5UA3lbHtUsxeAd3UIUcvk3UF3j-v6xXdziDkiGCFIAyviEbPvDPp-FCycgjw",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAP-Z15ku6TctaIImaI-hqrJR2rlQXqFPi8fW6i_-KHXftvcXbLhSOArHUa2fokaewUy39Fcn-La4tHOri95Zw9BLhbmEtx7m4G85yg49ncL4Pw_ypdAMFuuUB7G-DPmH9BjNVJWjR-mQ-1C6RPk8esoOZUwrloojxO_S9p7koYOyyKIRAYQXo5Tc-q2MitSbBgxT8tlmHeuvl6WfHEdI-ayoFCnQpJPD-44VfRXiSujLdQrzJDOg",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAP9Zf4hQOH4W_5Kqipd3nHvjhVPD-io8Cr29phiNTUuYqNcr9mx8ZPwb8Mc7S1pAJo2pxGFvQulqFYn9B5Ve4tBWKNLRnrK5eoWJCS2h9rWiusFHYbXKjxOcym5KHrUj2riYSp5d2GaAJiadd0_HIr_WSJ2ldYx1Ybf31JI1_4TkusNbMP2IVtG3by8VmATt4EMc-w5FwaWEq-InqOdzdvtf579h_sALctDqrOwM1zKHl0N887lQ"
  )

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "Create Your Profile",
      fontFamily = FontFamily.Serif,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = appColors.textPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Add your photo and name so your partner recognizes you instantly",
      fontSize = 12.5.sp,
      color = appColors.textSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Interactive Profile Picture Display with Edit Badge
    Box(
      contentAlignment = Alignment.BottomEnd,
      modifier = Modifier
        .size(92.dp)
        .testTag("profile_pic_picker_btn")
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape)
          .border(
            width = 3.dp,
            brush = Brush.linearGradient(
              listOf(PrimarySienna, SecondaryGold)
            ),
            shape = CircleShape
          )
          .background(appColors.surfaceContainerHigh),
        contentAlignment = Alignment.Center
      ) {
        if (profilePicUrl.isNotBlank()) {
          AsyncImage(
            model = profilePicUrl,
            contentDescription = "Selected Profile Picture",
            modifier = Modifier.fillMaxSize().clip(CircleShape)
          )
        } else {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default Avatar",
            tint = appColors.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(52.dp)
          )
        }
      }

      // Small Camera Action Button
      Box(
        modifier = Modifier
          .size(30.dp)
          .offset(x = 2.dp, y = 2.dp)
          .clip(CircleShape)
          .background(PrimarySiennaDark)
          .border(2.dp, Color.White, CircleShape)
          .clickable {
            imagePickerLauncher.launch("image/*")
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = "Pick Photo",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Preset Avatars & Gallery Picker Row
    Text(
      text = "Choose an avatar or pick from gallery",
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Medium,
      color = appColors.textMuted
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Photo Gallery Option
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(appColors.surfaceContainerHigh)
          .border(
            width = if (profilePicUrl.isNotBlank() && !presetAvatars.contains(profilePicUrl)) 2.5.dp else 1.dp,
            color = if (profilePicUrl.isNotBlank() && !presetAvatars.contains(profilePicUrl)) PrimarySienna else appColors.outlineVariant,
            shape = CircleShape
          )
          .clickable {
            imagePickerLauncher.launch("image/*")
          }
          .testTag("pick_gallery_photo_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PhotoLibrary,
          contentDescription = "Pick from gallery",
          tint = appColors.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      // Presets
      presetAvatars.forEachIndexed { index, avatarUrl ->
        val isSelected = profilePicUrl == avatarUrl
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(
              width = if (isSelected) 2.5.dp else 1.dp,
              color = if (isSelected) PrimarySienna else appColors.outlineVariant,
              shape = CircleShape
            )
            .clickable {
              onProfilePicUrlChange(avatarUrl)
            }
            .testTag("preset_avatar_$index"),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = avatarUrl,
            contentDescription = "Preset Avatar ${index + 1}",
            modifier = Modifier.fillMaxSize().clip(CircleShape)
          )
          if (isSelected) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Name / Nickname Input
    OutlinedTextField(
      value = userName,
      onValueChange = onUserNameChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("user_name_input"),
      label = { Text("Your Name / Nickname") },
      placeholder = { Text("e.g. Maya") },
      isError = errorMessage != null && userName.isBlank(),
      leadingIcon = {
        Icon(Icons.Default.Person, contentDescription = null, tint = if (errorMessage != null && userName.isBlank()) ErrorRed else appColors.primary)
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors.primary,
        unfocusedBorderColor = appColors.outlineVariant,
        errorBorderColor = ErrorRed
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Anniversary Date Input
    OutlinedTextField(
      value = anniversaryDate,
      onValueChange = onAnniversaryChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("anniversary_input"),
      label = { Text("Anniversary Date (Optional)") },
      placeholder = { Text("e.g. Jan 12, 2024") },
      leadingIcon = {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = appColors.primary)
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        focusManager.clearFocus()
        onSubmit()
      }),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors.primary,
        unfocusedBorderColor = appColors.outlineVariant
      )
    )

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      ErrorFeedbackCard(
        message = errorMessage,
        onDismiss = onDismissError
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onSubmit,
      enabled = !isLoading && userName.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_profile_button"),
      colors = ButtonDefaults.buttonColors(containerColor = PrimarySiennaDark),
      shape = RoundedCornerShape(26.dp)
    ) {
      if (isLoading) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Save & Find Partner", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
private fun PartnerPairingView(
  myPartnerCode: String,
  partnerCodeInput: String,
  anniversaryDate: String,
  isLoading: Boolean,
  errorMessage: String?,
  onPartnerCodeInputChange: (String) -> Unit,
  onConnectPartner: () -> Unit,
  onSkip: () -> Unit,
  onDismissError: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val appColors = AppTheme.colors
  val focusManager = LocalFocusManager.current

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "Connect With Your Partner",
      fontFamily = FontFamily.Serif,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = appColors.textPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Share your unique pairing code or enter your partner's code to link your accounts.",
      fontSize = 12.5.sp,
      color = appColors.textSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // User's Own Unique Code Box
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = appColors.surfaceContainerHigh),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "YOUR PAIRING CODE",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = appColors.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = myPartnerCode,
          fontSize = 26.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 2.sp,
          fontFamily = FontFamily.Monospace,
          color = appColors.textPrimary,
          modifier = Modifier.testTag("my_partner_code_display")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(myPartnerCode))
              Toast.makeText(context, "Pairing code copied!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f).testTag("copy_pairing_code_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy", fontSize = 12.5.sp)
          }

          Button(
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                  Intent.EXTRA_TEXT,
                  "Connect with me on RealUs! My pairing code is: $myPartnerCode"
                )
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Pairing Code")
              context.startActivity(shareIntent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
            modifier = Modifier.weight(1f).testTag("share_pairing_code_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share", fontSize = 12.5.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      HorizontalDivider(modifier = Modifier.weight(1f), color = appColors.outlineVariant.copy(alpha = 0.4f))
      Text(
        text = " OR ENTER PARTNER'S CODE ",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = appColors.textMuted,
        modifier = Modifier.padding(horizontal = 6.dp)
      )
      HorizontalDivider(modifier = Modifier.weight(1f), color = appColors.outlineVariant.copy(alpha = 0.4f))
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = partnerCodeInput,
      onValueChange = onPartnerCodeInputChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("partner_code_input"),
      label = { Text("Partner's Code") },
      placeholder = { Text("e.g. US-AB12CD") },
      isError = errorMessage != null,
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        focusManager.clearFocus()
        onConnectPartner()
      }),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors.primary,
        unfocusedBorderColor = appColors.outlineVariant,
        errorBorderColor = ErrorRed
      )
    )

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      ErrorFeedbackCard(
        message = errorMessage,
        onDismiss = onDismissError
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Button(
      onClick = onConnectPartner,
      enabled = !isLoading && partnerCodeInput.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("connect_partner_btn"),
      colors = ButtonDefaults.buttonColors(containerColor = PrimarySiennaDark),
      shape = RoundedCornerShape(25.dp)
    ) {
      if (isLoading) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Connect & Start Together", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(
      onClick = onSkip,
      modifier = Modifier.testTag("skip_pairing_for_now_btn")
    ) {
      Text(
        text = "Enter Space (Pairing Pending)",
        fontSize = 12.5.sp,
        color = appColors.textMuted
      )
    }
  }
}
