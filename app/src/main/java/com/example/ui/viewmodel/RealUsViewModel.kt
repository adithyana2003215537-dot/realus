package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.RealUsDatabase
import com.example.data.firebase.CoupleData
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.UserProfile
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.SharedMusic
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood
import com.example.data.repository.RealUsRepository
import com.example.data.util.YouTubeHelper
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class AppAuthState {
  WELCOME,
  UNAUTHENTICATED,
  PROFILE_SETUP,
  PENDING_PAIRING,
  AUTHENTICATED_AND_PAIRED
}

data class FlyingReaction(
  val id: Long = System.currentTimeMillis() + Random.nextLong(1000),
  val emoji: String,
  val startXPercent: Float = Random.nextFloat() * 0.6f + 0.2f, // 20% to 80%
  val scale: Float = Random.nextFloat() * 0.5f + 0.8f
)

class RealUsViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: RealUsRepository

  val chatMessages: StateFlow<List<ChatMessage>>
  val loveNotes: StateFlow<List<LoveNote>>
  val storyMilestones: StateFlow<List<StoryMilestone>>
  val journalEntries: StateFlow<List<JournalEntry>>
  val calendarEvents: StateFlow<List<CalendarEvent>>
  val moodHistory: StateFlow<List<UserMood>>
  val coupleSettings: StateFlow<CoupleSettings?>

  // Firebase Manager & State
  val firebaseManager: FirebaseManager = FirebaseManager.getInstance(application)
  val firebaseUser: StateFlow<FirebaseUser?> = firebaseManager.currentUserState
  val firebaseSyncStatus: StateFlow<String> = firebaseManager.syncStatus
  val isFirebaseConfigured: Boolean get() = firebaseManager.isFirebaseInitialized

  // Auth State & User Profile
  private val _authState = MutableStateFlow<AppAuthState>(AppAuthState.UNAUTHENTICATED)
  val authState: StateFlow<AppAuthState> = _authState.asStateFlow()

  private val _userProfile = MutableStateFlow<UserProfile?>(null)
  val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

  private val _activeCoupleData = MutableStateFlow<CoupleData?>(null)
  val activeCoupleData: StateFlow<CoupleData?> = _activeCoupleData.asStateFlow()

  // Phone Auth Resend Token cache
  private var lastResendToken: PhoneAuthProvider.ForceResendingToken? = null

  // Active Firestore snapshot listeners
  private var userProfileListener: ListenerRegistration? = null
  private var coupleListener: ListenerRegistration? = null
  private var chatListener: ListenerRegistration? = null
  private var loveNotesListener: ListenerRegistration? = null
  private var milestonesListener: ListenerRegistration? = null
  private var journalListener: ListenerRegistration? = null
  private var calendarListener: ListenerRegistration? = null
  private var moodsListener: ListenerRegistration? = null
  private var musicListener: ListenerRegistration? = null

  // Navigation State
  private val _currentTab = MutableStateFlow("home")
  val currentTab: StateFlow<String> = _currentTab.asStateFlow()

  private val _currentSubScreen = MutableStateFlow<String?>(null)
  val currentSubScreen: StateFlow<String?> = _currentSubScreen.asStateFlow()

  private val _isDrawerOpen = MutableStateFlow(false)
  val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

  // Reactions Layer
  private val _flyingReactions = MutableStateFlow<List<FlyingReaction>>(emptyList())
  val flyingReactions: StateFlow<List<FlyingReaction>> = _flyingReactions.asStateFlow()

  // Mood celebration overlay
  private val _selectedMoodCelebration = MutableStateFlow<UserMood?>(null)
  val selectedMoodCelebration: StateFlow<UserMood?> = _selectedMoodCelebration.asStateFlow()

  // Audio / Voice Note playback in chat
  private val _isVoiceNotePlaying = MutableStateFlow(false)
  val isVoiceNotePlaying: StateFlow<Boolean> = _isVoiceNotePlaying.asStateFlow()

  private val _voiceNoteProgress = MutableStateFlow(0.33f)
  val voiceNoteProgress: StateFlow<Float> = _voiceNoteProgress.asStateFlow()

  // Music Player
  private val _sharedMusic = MutableStateFlow<SharedMusic?>(null)
  val sharedMusic: StateFlow<SharedMusic?> = _sharedMusic.asStateFlow()

  private val _isMusicLoading = MutableStateFlow(false)
  val isMusicLoading: StateFlow<Boolean> = _isMusicLoading.asStateFlow()

  private val _musicError = MutableStateFlow<String?>(null)
  val musicError: StateFlow<String?> = _musicError.asStateFlow()

  private val _isPlayingMusic = MutableStateFlow(false)
  val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

  private val _musicMoodSync = MutableStateFlow(0.65f)
  val musicMoodSync: StateFlow<Float> = _musicMoodSync.asStateFlow()

  // Call State
  private val _callSeconds = MutableStateFlow(0)
  val callSeconds: StateFlow<Int> = _callSeconds.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isVideoOn = MutableStateFlow(false)
  val isVideoOn: StateFlow<Boolean> = _isVideoOn.asStateFlow()

  private val _isSpeakerOn = MutableStateFlow(true)
  val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

  init {
    val db = RealUsDatabase.getDatabase(application)
    repository = RealUsRepository(db.realUsDao())

    chatMessages = repository.chatMessages.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    loveNotes = repository.loveNotes.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    storyMilestones = repository.storyMilestones.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    journalEntries = repository.journalEntries.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    calendarEvents = repository.calendarEvents.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    moodHistory = repository.allMoods.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    coupleSettings = repository.coupleSettings.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      CoupleSettings(
        partner1Name = "You",
        partner2Name = "Partner",
        anniversaryDate = "",
        daysTogether = 0,
        coupleCode = ""
      )
    )

    // Check initial Firebase Auth status on launch
    checkUserSession()

    // Run active call timer ticker
    viewModelScope.launch {
      while (true) {
        delay(1000)
        if (_currentSubScreen.value == "active_call") {
          _callSeconds.value += 1
        }
      }
    }
  }

  fun checkUserSession() {
    val currentUser = firebaseManager.auth?.currentUser
    if (currentUser == null) {
      _authState.value = AppAuthState.UNAUTHENTICATED
      _userProfile.value = null
      _activeCoupleData.value = null
    } else {
      loadUserProfile(currentUser.uid)
    }
  }

  private fun loadUserProfile(uid: String) {
    viewModelScope.launch {
      val result = firebaseManager.getUserProfile(uid)
      if (result.isSuccess) {
        val profile = result.getOrNull()
        if (profile == null || profile.name.isBlank()) {
          _authState.value = AppAuthState.PROFILE_SETUP
        } else {
          _userProfile.value = profile
          attachUserProfileListener(uid)
          if (!profile.coupleId.isNullOrBlank()) {
            loadCoupleData(profile.coupleId)
            _authState.value = AppAuthState.AUTHENTICATED_AND_PAIRED
          } else {
            _authState.value = AppAuthState.PENDING_PAIRING
          }
        }
      } else {
        _authState.value = AppAuthState.PROFILE_SETUP
      }
    }
  }

  private fun attachUserProfileListener(uid: String) {
    userProfileListener?.remove()
    userProfileListener = firebaseManager.listenToUserProfile(uid) { updatedProfile ->
      if (updatedProfile != null) {
        _userProfile.value = updatedProfile
        if (!updatedProfile.coupleId.isNullOrBlank() && _authState.value != AppAuthState.AUTHENTICATED_AND_PAIRED) {
          loadCoupleData(updatedProfile.coupleId)
          _authState.value = AppAuthState.AUTHENTICATED_AND_PAIRED
        }
      }
    }
  }

  private fun loadCoupleData(coupleId: String) {
    viewModelScope.launch {
      val result = firebaseManager.getCoupleData(coupleId)
      if (result.isSuccess) {
        val couple = result.getOrNull()
        if (couple != null) {
          _activeCoupleData.value = couple
          val days = calculateDaysTogether(couple.anniversaryDate)
          val settings = CoupleSettings(
            id = 1,
            partner1Name = couple.partner1Name.ifBlank { "You" },
            partner2Name = couple.partner2Name.ifBlank { "Partner" },
            anniversaryDate = couple.anniversaryDate,
            daysTogether = days,
            coupleCode = couple.coupleCode,
            partnerMood = couple.partner2Mood
          )
          repository.saveCoupleSettings(settings)
          attachCoupleRealtimeListeners(coupleId)
        }
      }
    }
  }

  private fun attachCoupleRealtimeListeners(coupleId: String) {
    coupleListener?.remove()
    coupleListener = firebaseManager.listenToCouple(coupleId) { couple ->
      if (couple != null) {
        _activeCoupleData.value = couple
        viewModelScope.launch {
          val days = calculateDaysTogether(couple.anniversaryDate)
          val currentUid = firebaseManager.auth?.currentUser?.uid
          val partnerMood = if (currentUid != null && currentUid == couple.partner1Uid) {
            couple.partner2Mood
          } else {
            couple.partner1Mood
          }
          val current = coupleSettings.value ?: CoupleSettings()
          repository.updateSettings(
            current.copy(
              partner1Name = couple.partner1Name,
              partner2Name = couple.partner2Name,
              anniversaryDate = couple.anniversaryDate,
              daysTogether = days,
              coupleCode = couple.coupleCode,
              partnerMood = partnerMood.ifBlank { "Loved" }
            )
          )
        }
      }
    }

    chatListener?.remove()
    chatListener = firebaseManager.listenToChatMessages(coupleId) { messages ->
      viewModelScope.launch {
        messages.forEach { msg ->
          repository.sendChatMessage(msg)
        }
      }
    }

    loveNotesListener?.remove()
    loveNotesListener = firebaseManager.listenToLoveNotes(coupleId) { notes ->
      viewModelScope.launch {
        notes.forEach { note ->
          repository.addLoveNote(note)
        }
      }
    }

    milestonesListener?.remove()
    milestonesListener = firebaseManager.listenToStoryMilestones(coupleId) { milestones ->
      viewModelScope.launch {
        milestones.forEach { m ->
          repository.addMilestone(m)
        }
      }
    }

    journalListener?.remove()
    journalListener = firebaseManager.listenToJournalEntries(coupleId) { entries ->
      viewModelScope.launch {
        entries.forEach { entry ->
          repository.addJournalEntry(entry)
        }
      }
    }

    calendarListener?.remove()
    calendarListener = firebaseManager.listenToCalendarEvents(coupleId) { events ->
      viewModelScope.launch {
        events.forEach { event ->
          repository.addCalendarEvent(event)
        }
      }
    }

    moodsListener?.remove()
    moodsListener = firebaseManager.listenToMoods(coupleId) { moods ->
      viewModelScope.launch {
        moods.forEach { mood ->
          repository.setMood(mood)
        }
      }
    }

    musicListener?.remove()
    musicListener = firebaseManager.listenToSharedMusic(coupleId) { sharedMusic ->
      _sharedMusic.value = sharedMusic
      if (sharedMusic != null) {
        _isPlayingMusic.value = sharedMusic.isPlaying
        _musicMoodSync.value = sharedMusic.moodSync
      } else {
        _isPlayingMusic.value = false
      }
    }
  }

  private fun calculateDaysTogether(anniversaryStr: String): Int {
    if (anniversaryStr.isBlank()) return 0
    val patterns = listOf(
      "MMM dd, yyyy",
      "MMM d, yyyy",
      "MMMM dd, yyyy",
      "MMMM d, yyyy",
      "yyyy-MM-dd",
      "MM/dd/yyyy",
      "dd/MM/yyyy",
      "yyyy/MM/dd",
      "dd MMM yyyy",
      "d MMM yyyy"
    )
    for (pattern in patterns) {
      try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.isLenient = true
        val date = sdf.parse(anniversaryStr.trim())
        if (date != null) {
          val diff = System.currentTimeMillis() - date.time
          return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        }
      } catch (_: Exception) {
        // Continue trying next format pattern
      }
    }
    return 0
  }

  // --- Real Phone Authentication Operations ---

  fun sendPhoneOtp(
    activity: Activity,
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    firebaseManager.sendPhoneOtp(
      activity = activity,
      phoneNumber = phoneNumber,
      onCodeSent = { verificationId, token ->
        lastResendToken = token
        onCodeSent(verificationId)
      },
      onVerificationCompleted = { user ->
        loadUserProfile(user.uid)
      },
      onVerificationFailed = { error ->
        onError(error.localizedMessage ?: "Failed to send verification SMS. Please check phone number.")
      }
    )
  }

  fun resendPhoneOtp(
    activity: Activity,
    phoneNumber: String,
    onError: (String) -> Unit
  ) {
    val token = lastResendToken
    if (token == null) {
      sendPhoneOtp(activity, phoneNumber, {}, onError)
      return
    }
    firebaseManager.resendPhoneOtp(
      activity = activity,
      phoneNumber = phoneNumber,
      token = token,
      onCodeSent = { _, newToken ->
        lastResendToken = newToken
      },
      onVerificationCompleted = { user ->
        loadUserProfile(user.uid)
      },
      onVerificationFailed = { error ->
        onError(error.localizedMessage ?: "Resend failed")
      }
    )
  }

  fun verifyPhoneOtp(
    verificationId: String,
    otpCode: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = firebaseManager.verifyPhoneOtp(verificationId, otpCode)
      if (result.isSuccess) {
        val user = result.getOrNull()
        if (user != null) {
          loadUserProfile(user.uid)
          onSuccess()
        } else {
          onError("Verification succeeded but user session could not be established")
        }
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Invalid verification code. Please try again.")
      }
    }
  }

  fun saveUserProfile(
    name: String,
    profilePicUrl: String = "",
    anniversaryDate: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val user = firebaseManager.auth?.currentUser
    if (user == null) {
      onError("No active user session")
      return
    }
    viewModelScope.launch {
      val result = firebaseManager.saveUserProfile(
        uid = user.uid,
        phoneNumber = user.phoneNumber ?: "",
        name = name,
        profilePicUrl = profilePicUrl,
        anniversaryDate = anniversaryDate
      )
      if (result.isSuccess) {
        val profile = result.getOrNull()
        _userProfile.value = profile
        attachUserProfileListener(user.uid)
        _authState.value = AppAuthState.PENDING_PAIRING
        coupleSettings.value?.let { current ->
          repository.updateSettings(
            current.copy(
              partner1Name = name,
              anniversaryDate = anniversaryDate,
              coupleCode = profile?.partnerCode ?: ""
            )
          )
        }
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to save profile")
      }
    }
  }

  fun connectWithPartnerCode(
    partnerCode: String,
    anniversaryDate: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val user = firebaseManager.auth?.currentUser
    val profile = _userProfile.value
    if (user == null || profile == null) {
      onError("Please complete profile setup first")
      return
    }
    viewModelScope.launch {
      val result = firebaseManager.connectWithPartnerCode(
        currentUid = user.uid,
        currentName = profile.name,
        enteredPartnerCode = partnerCode,
        anniversaryDate = anniversaryDate
      )
      if (result.isSuccess) {
        val (updatedProfile, coupleData) = result.getOrNull()!!
        _userProfile.value = updatedProfile
        _activeCoupleData.value = coupleData
        _authState.value = AppAuthState.AUTHENTICATED_AND_PAIRED
        loadCoupleData(coupleData.coupleId)
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Partner pairing failed")
      }
    }
  }

  fun skipPairingForNow() {
    viewModelScope.launch {
      val currentProfile = _userProfile.value
      val currentSettings = coupleSettings.value ?: CoupleSettings()
      val updatedSettings = currentSettings.copy(
        id = 1,
        partner1Name = currentProfile?.name?.ifBlank { "You" } ?: "You",
        partner2Name = "Partner"
      )
      repository.saveCoupleSettings(updatedSettings)
      _authState.value = AppAuthState.AUTHENTICATED_AND_PAIRED
    }
  }

  fun signOut() {
    userProfileListener?.remove()
    coupleListener?.remove()
    chatListener?.remove()
    loveNotesListener?.remove()
    milestonesListener?.remove()
    journalListener?.remove()
    calendarListener?.remove()
    moodsListener?.remove()

    firebaseManager.signOut()
    _userProfile.value = null
    _activeCoupleData.value = null
    _authState.value = AppAuthState.UNAUTHENTICATED

    viewModelScope.launch {
      repository.clearAllData()
    }
  }

  // Navigation
  fun showWelcomeScreen() {
    _authState.value = AppAuthState.WELCOME
  }

  fun dismissWelcomeScreen() {
    val user = firebaseManager.auth?.currentUser
    if (user != null) {
      _authState.value = AppAuthState.AUTHENTICATED_AND_PAIRED
    } else {
      _authState.value = AppAuthState.UNAUTHENTICATED
    }
  }

  fun navigateToTab(tab: String) {
    _currentTab.value = tab
    _currentSubScreen.value = null
    _isDrawerOpen.value = false
  }

  fun navigateToSubScreen(screen: String?) {
    _currentSubScreen.value = screen
    _isDrawerOpen.value = false
  }

  fun toggleDrawer() {
    _isDrawerOpen.value = !_isDrawerOpen.value
  }

  fun closeDrawer() {
    _isDrawerOpen.value = false
  }

  // Chat Actions
  fun sendMessage(text: String) {
    if (text.isBlank()) return
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val nowStr = sdf.format(Date())
    val newMsg = ChatMessage(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      sender = "you",
      text = text.trim(),
      timestamp = nowStr,
      isRead = true,
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.sendChatMessage(newMsg)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncChatMessageToCloud(newMsg, coupleId)
      }
    }
  }

  fun triggerReaction(emoji: String) {
    val list = (1..6).map {
      FlyingReaction(
        id = System.currentTimeMillis() + it + Random.nextLong(1000),
        emoji = emoji
      )
    }
    _flyingReactions.value = _flyingReactions.value + list
    viewModelScope.launch {
      delay(2500)
      _flyingReactions.value = _flyingReactions.value.filterNot { item -> list.contains(item) }
    }
  }

  fun toggleVoiceNotePlayback() {
    _isVoiceNotePlaying.value = !_isVoiceNotePlaying.value
    if (_isVoiceNotePlaying.value) {
      viewModelScope.launch {
        while (_isVoiceNotePlaying.value && _voiceNoteProgress.value < 1f) {
          delay(200)
          _voiceNoteProgress.value = (_voiceNoteProgress.value + 0.02f).coerceAtMost(1f)
        }
        if (_voiceNoteProgress.value >= 1f) {
          _isVoiceNotePlaying.value = false
          _voiceNoteProgress.value = 0f
        }
      }
    }
  }

  // Mood Actions
  fun selectMood(
    moodKey: String,
    moodLabel: String,
    moodIcon: String,
    note: String = "",
    moodScore: Float = 9.0f
  ) {
    val partnerCurrentMood = coupleSettings.value?.partnerMood ?: "Loved"
    val partnerScore = when (partnerCurrentMood.lowercase()) {
      "loved", "romantic", "grateful" -> 9.5f
      "happy", "flirty", "cozy" -> 8.8f
      "tired", "need_you" -> 7.2f
      else -> 8.0f
    }
    val diff = kotlin.math.abs(moodScore - partnerScore)
    val synergy = (100 - (diff * 7f)).toInt().coerceIn(75, 100)

    val mood = UserMood(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      moodKey = moodKey,
      moodLabel = moodLabel,
      moodIcon = moodIcon,
      moodScore = moodScore,
      partnerMoodLabel = partnerCurrentMood,
      partnerMoodIcon = when (partnerCurrentMood.lowercase()) {
        "loved" -> "💖"
        "romantic" -> "🥰"
        "happy" -> "😊"
        "cozy" -> "☕"
        "tired" -> "😴"
        "need you" -> "🫂"
        "grateful" -> "✨"
        else -> "💖"
      },
      partnerMoodScore = partnerScore,
      note = note,
      synergyScore = synergy,
      dateLabel = "Today",
      partnerName = coupleSettings.value?.partner2Name ?: "Partner",
      timestamp = System.currentTimeMillis()
    )
    _selectedMoodCelebration.value = mood
    viewModelScope.launch {
      repository.setMood(mood)
      coupleSettings.value?.let { current ->
        repository.updateSettings(current.copy(partnerMood = moodLabel))
      }
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncMoodToCloud(mood, coupleId)
      }
      delay(2200)
      _selectedMoodCelebration.value = null
    }
  }

  fun addCustomMood(label: String, note: String = "") {
    selectMood("custom", label, "✨", note, 9.0f)
  }

  // Love Notes
  fun addLoveNote(text: String, bgType: String = "clay") {
    if (text.isBlank()) return
    val userName = _userProfile.value?.name ?: coupleSettings.value?.partner1Name ?: "YOU"
    val note = LoveNote(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      author = userName,
      text = text.trim(),
      timeAgo = "Just now",
      bgType = bgType,
      isPinned = false,
      rotation = (Random.nextFloat() * 6f - 3f),
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addLoveNote(note)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncLoveNoteToCloud(note, coupleId)
      }
    }
  }

  fun addAudioLoveNote(
    audioFilePath: String,
    durationSec: Int,
    captionText: String = "",
    bgType: String = "ochre",
    isPinned: Boolean = false,
    amplitudes: String = ""
  ) {
    val userName = _userProfile.value?.name ?: coupleSettings.value?.partner1Name ?: "YOU"
    val note = LoveNote(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      author = userName,
      text = if (captionText.isBlank()) "Voice whisper for you 🎙️" else captionText.trim(),
      timeAgo = "Just now",
      bgType = bgType,
      isPinned = isPinned,
      rotation = (Random.nextFloat() * 4f - 2f),
      isAudioNote = true,
      audioFilePath = audioFilePath,
      audioDurationSec = durationSec,
      audioAmplitudes = amplitudes,
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addLoveNote(note)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncLoveNoteToCloud(note, coupleId)
      }
    }
  }

  fun deleteLoveNote(id: Long) {
    viewModelScope.launch {
      repository.deleteLoveNote(id)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.deleteLoveNoteFromCloud(id, coupleId)
      }
    }
  }

  // Milestones
  fun addStoryMilestone(title: String, dateStr: String, description: String, imageUrl: String) {
    if (title.isBlank()) return
    val milestone = StoryMilestone(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      title = title.trim(),
      dateStr = dateStr.trim(),
      description = description.trim(),
      imageUrl = imageUrl,
      rotation = (Random.nextFloat() * 4f - 2f),
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addMilestone(milestone)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncMilestoneToCloud(milestone, coupleId)
      }
    }
  }

  // Journal
  fun addJournalEntry(
    title: String,
    category: String,
    body: String,
    imageUrl: String = "",
    audioFilePath: String = "",
    audioDurationSec: Int = 0
  ) {
    if (title.isBlank() && body.isBlank() && audioFilePath.isBlank()) return
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val entry = JournalEntry(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      title = if (title.isBlank()) "Spoken Memory" else title.trim(),
      category = if (category.isBlank()) "Voice Memo" else category.trim(),
      body = body.trim(),
      dateStr = sdf.format(Date()),
      imageUrl = imageUrl,
      weatherIcon = "sunny",
      isAudioAttached = audioFilePath.isNotBlank(),
      audioFilePath = audioFilePath,
      audioDurationSec = audioDurationSec,
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addJournalEntry(entry)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncJournalEntryToCloud(entry, coupleId)
      }
    }
  }

  // Calendar
  fun addCalendarEvent(title: String, dateStr: String, dayOfMonth: Int, iconType: String = "heart") {
    if (title.isBlank()) return
    val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val event = CalendarEvent(
      id = System.currentTimeMillis() + Random.nextLong(1, 1000),
      title = title.trim(),
      dateStr = dateStr.trim(),
      daysRemainingText = "Upcoming",
      dayOfMonth = dayOfMonth,
      monthYear = sdfMonth.format(Date()),
      iconType = iconType,
      createdAt = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addCalendarEvent(event)
      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        firebaseManager.syncCalendarEventToCloud(event, coupleId)
      }
    }
  }

  // Music Player
  fun loadAndSaveYouTubeMusic(url: String) {
    val cleanUrl = url.trim()
    if (cleanUrl.isBlank()) {
      _musicError.value = "Please enter or paste a valid YouTube link."
      return
    }

    _isMusicLoading.value = true
    _musicError.value = null

    viewModelScope.launch {
      val videoId = YouTubeHelper.extractVideoId(cleanUrl)
      if (videoId == null) {
        _musicError.value = "Invalid YouTube link. Supported formats: youtube.com/watch?v=..., youtu.be/..., shorts/..."
        _isMusicLoading.value = false
        return@launch
      }

      val metaResult = YouTubeHelper.fetchMetadata(videoId)
      if (metaResult.isSuccess) {
        val meta = metaResult.getOrThrow()
        val userName = _userProfile.value?.name?.ifBlank { "Partner" } ?: "Partner"
        val music = SharedMusic(
          videoId = meta.videoId,
          videoUrl = "https://www.youtube.com/watch?v=${meta.videoId}",
          title = meta.title,
          artist = meta.authorName,
          thumbnailUrl = meta.thumbnailUrl,
          isPlaying = true,
          positionMs = 0L,
          moodSync = _musicMoodSync.value,
          addedBy = userName,
          updatedAt = System.currentTimeMillis()
        )

        val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
        if (!coupleId.isNullOrBlank()) {
          val success = firebaseManager.updateSharedMusic(coupleId, music)
          if (!success) {
            _musicError.value = "Could not sync music to cloud. Please check network connection."
          } else {
            _sharedMusic.value = music
            _isPlayingMusic.value = true
          }
        } else {
          _sharedMusic.value = music
          _isPlayingMusic.value = true
        }
      } else {
        val err = metaResult.exceptionOrNull()?.message ?: "Video unavailable or restricted."
        _musicError.value = err
      }
      _isMusicLoading.value = false
    }
  }

  fun toggleMusicPlay() {
    val current = _sharedMusic.value
    if (current != null) {
      val newPlayState = !current.isPlaying
      _isPlayingMusic.value = newPlayState
      _sharedMusic.value = current.copy(isPlaying = newPlayState)

      val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
      if (!coupleId.isNullOrBlank()) {
        viewModelScope.launch {
          firebaseManager.updateSharedMusicPlayback(coupleId, newPlayState)
        }
      }
    } else {
      _isPlayingMusic.value = !_isPlayingMusic.value
    }
  }

  fun removeCurrentMusic() {
    _sharedMusic.value = null
    _isPlayingMusic.value = false
    _musicError.value = null

    val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
    if (!coupleId.isNullOrBlank()) {
      viewModelScope.launch {
        firebaseManager.updateSharedMusic(coupleId, null)
      }
    }
  }

  fun setMusicMoodSync(value: Float) {
    val level = value.coerceIn(0f, 1f)
    _musicMoodSync.value = level
    val current = _sharedMusic.value
    if (current != null) {
      _sharedMusic.value = current.copy(moodSync = level)
    }

    val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId
    if (!coupleId.isNullOrBlank()) {
      viewModelScope.launch {
        firebaseManager.updateSharedMusicMoodSync(coupleId, level)
      }
    }
  }

  fun clearMusicError() {
    _musicError.value = null
  }

  // Call Controls
  fun startIncomingCall() {
    _currentSubScreen.value = "incoming_call"
  }

  fun startOutgoingCall() {
    _callSeconds.value = 0
    _currentSubScreen.value = "active_call"
  }

  fun acceptCall() {
    _callSeconds.value = 0
    _currentSubScreen.value = "active_call"
  }

  fun declineCall() {
    _currentSubScreen.value = null
  }

  fun endCall() {
    _currentSubScreen.value = null
  }

  fun toggleMute() {
    _isMuted.value = !_isMuted.value
  }

  fun toggleVideo() {
    _isVideoOn.value = !_isVideoOn.value
  }

  fun toggleSpeaker() {
    _isSpeakerOn.value = !_isSpeakerOn.value
  }

  // Settings & Theme
  fun switchTheme(theme: String) {
    coupleSettings.value?.let { current ->
      viewModelScope.launch {
        repository.updateSettings(current.copy(themeName = theme))
      }
    }
  }

  fun updateUserAvatar(avatarUrl: String) {
    coupleSettings.value?.let { current ->
      viewModelScope.launch {
        repository.updateSettings(current.copy(userAvatarUrl = avatarUrl))
      }
    } ?: run {
      viewModelScope.launch {
        repository.saveCoupleSettings(CoupleSettings(userAvatarUrl = avatarUrl))
      }
    }
    val uid = firebaseManager.auth?.currentUser?.uid
    if (uid != null) {
      _userProfile.value = _userProfile.value?.copy(profilePicUrl = avatarUrl)
      viewModelScope.launch {
        firebaseManager.updateUserProfilePic(uid, avatarUrl)
      }
    }
  }

  fun updatePartnerAvatar(avatarUrl: String) {
    coupleSettings.value?.let { current ->
      viewModelScope.launch {
        repository.updateSettings(current.copy(partnerAvatarUrl = avatarUrl))
      }
    } ?: run {
      viewModelScope.launch {
        repository.saveCoupleSettings(CoupleSettings(partnerAvatarUrl = avatarUrl))
      }
    }
  }

  fun updateCoupleProfile(name1: String, name2: String, anniversary: String) {
    val days = calculateDaysTogether(anniversary)
    coupleSettings.value?.let { current ->
      viewModelScope.launch {
        repository.updateSettings(
          current.copy(
            partner1Name = name1,
            partner2Name = name2,
            anniversaryDate = anniversary,
            daysTogether = days
          )
        )
      }
    }
  }

  fun toggleSetting(type: String) {
    coupleSettings.value?.let { current ->
      val updated = when (type) {
        "sync" -> current.copy(syncWithPartner = !current.syncWithPartner)
        "biometric" -> current.copy(biometricLock = !current.biometricLock)
        "private" -> current.copy(privateMode = !current.privateMode)
        "intimacy_notif" -> current.copy(intimacyNotifications = !current.intimacyNotifications)
        "calendar_notif" -> current.copy(calendarNotifications = !current.calendarNotifications)
        else -> current
      }
      viewModelScope.launch {
        repository.updateSettings(updated)
      }
    }
  }

  // Firebase Operations
  fun signInWithGoogle(activity: Activity, webClientId: String = "", onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val result = firebaseManager.signInWithGoogle(activity, webClientId)
      if (result.isSuccess) {
        val user = result.getOrNull()
        if (user != null) {
          loadUserProfile(user.uid)
          onResult(true, user.email)
        } else {
          onResult(false, "No user returned")
        }
      } else {
        onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Sign-in failed")
      }
    }
  }

  fun syncAllDataToFirebase() {
    val coupleId = _userProfile.value?.coupleId ?: _activeCoupleData.value?.coupleId ?: return
    viewModelScope.launch {
      loveNotes.value.forEach { note ->
        firebaseManager.syncLoveNoteToCloud(note, coupleId)
      }
      journalEntries.value.forEach { entry ->
        firebaseManager.syncJournalEntryToCloud(entry, coupleId)
      }
      storyMilestones.value.forEach { milestone ->
        firebaseManager.syncMilestoneToCloud(milestone, coupleId)
      }
      chatMessages.value.forEach { msg ->
        firebaseManager.syncChatMessageToCloud(msg, coupleId)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    userProfileListener?.remove()
    coupleListener?.remove()
    chatListener?.remove()
    loveNotesListener?.remove()
    milestonesListener?.remove()
    journalListener?.remove()
    calendarListener?.remove()
    moodsListener?.remove()
    musicListener?.remove()
  }
}
