package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.FloatingReactionLayer
import com.example.ui.components.RealUsBottomNav
import com.example.ui.components.RealUsDrawer
import com.example.ui.components.RealUsTopBar
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingCallScreen
import com.example.ui.screens.JournalAndLoveNotesScreen
import com.example.ui.screens.JournalSectionTab
import com.example.ui.screens.MoodScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StoryScreen
import com.example.ui.screens.TogetherScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.RealUsTheme
import com.example.ui.viewmodel.AppAuthState
import com.example.ui.viewmodel.RealUsViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: RealUsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val coupleSettings by viewModel.coupleSettings.collectAsState()
      val themeName = coupleSettings?.themeName ?: "Night"

      RealUsTheme(
        themeName = themeName,
        onThemeChanged = { newTheme ->
          viewModel.switchTheme(newTheme)
        }
      ) {
        RealUsApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun RealUsApp(viewModel: RealUsViewModel) {
  val appColors = AppTheme.colors
  val authState by viewModel.authState.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()
  val currentSubScreen by viewModel.currentSubScreen.collectAsState()
  val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
  val coupleSettings by viewModel.coupleSettings.collectAsState()

  val chatMessages by viewModel.chatMessages.collectAsState()
  val loveNotes by viewModel.loveNotes.collectAsState()
  val storyMilestones by viewModel.storyMilestones.collectAsState()
  val journalEntries by viewModel.journalEntries.collectAsState()
  val calendarEvents by viewModel.calendarEvents.collectAsState()
  val moodHistory by viewModel.moodHistory.collectAsState()
  val flyingReactions by viewModel.flyingReactions.collectAsState()
  val selectedMoodCelebration by viewModel.selectedMoodCelebration.collectAsState()

  val isVoiceNotePlaying by viewModel.isVoiceNotePlaying.collectAsState()
  val voiceNoteProgress by viewModel.voiceNoteProgress.collectAsState()

  val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
  val musicMoodSync by viewModel.musicMoodSync.collectAsState()

  val callSeconds by viewModel.callSeconds.collectAsState()
  val isMuted by viewModel.isMuted.collectAsState()
  val isVideoOn by viewModel.isVideoOn.collectAsState()
  val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()

  val firebaseUser by viewModel.firebaseUser.collectAsState()
  val firebaseSyncStatus by viewModel.firebaseSyncStatus.collectAsState()
  val context = LocalContext.current

  // Handle system back navigation
  BackHandler(enabled = currentSubScreen != null || isDrawerOpen) {
    if (isDrawerOpen) {
      viewModel.closeDrawer()
    } else if (currentSubScreen != null) {
      viewModel.navigateToSubScreen(null)
    }
  }

  Box(modifier = Modifier.fillMaxSize().background(appColors.background)) {
    if (authState == AppAuthState.WELCOME) {
      WelcomeScreen(
        onGetStarted = { viewModel.dismissWelcomeScreen() }
      )
    } else if (authState != AppAuthState.AUTHENTICATED_AND_PAIRED) {
      AuthScreen(
        authState = authState,
        userProfile = userProfile,
        onSendPhoneOtp = { activity, phone, onSent, onError ->
          viewModel.sendPhoneOtp(activity, phone, onSent, onError)
        },
        onVerifyPhoneOtp = { vId, otp, onSuccess, onError ->
          viewModel.verifyPhoneOtp(vId, otp, onSuccess, onError)
        },
        onResendPhoneOtp = { activity, phone, onError ->
          viewModel.resendPhoneOtp(activity, phone, onError)
        },
        onSaveProfile = { name, picUrl, ann, onSuccess, onError ->
          viewModel.saveUserProfile(name, picUrl, ann, onSuccess, onError)
        },
        onConnectPartner = { partnerCode, ann, onSuccess, onError ->
          viewModel.connectWithPartnerCode(partnerCode, ann, onSuccess, onError)
        },
        onSkipPairing = { viewModel.skipPairingForNow() },
        onSignInWithGoogle = { activity ->
          viewModel.signInWithGoogle(activity) { _, _ -> }
        }
      )
    } else {
      // Main App Scaffold
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = appColors.background,
        topBar = {
          if (currentSubScreen == null) {
            RealUsTopBar(
              onMenuClick = { viewModel.toggleDrawer() },
              onPartnerClick = { viewModel.navigateToSubScreen("mood_picker") }
            )
          }
        },
        bottomBar = {
          if (currentSubScreen == null) {
            RealUsBottomNav(
              currentTab = currentTab,
              onTabSelected = { tab -> viewModel.navigateToTab(tab) }
            )
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          when (currentSubScreen) {
            "incoming_call" -> {
              IncomingCallScreen(
                partnerName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner",
                onAccept = { viewModel.acceptCall() },
                onDecline = { viewModel.declineCall() }
              )
            }
            "active_call" -> {
              ActiveCallScreen(
                partnerName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner",
                callSeconds = callSeconds,
                isMuted = isMuted,
                isVideoOn = isVideoOn,
                isSpeakerOn = isSpeakerOn,
                onToggleMute = { viewModel.toggleMute() },
                onToggleVideo = { viewModel.toggleVideo() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onEndCall = { viewModel.endCall() }
              )
            }
            "mood_picker" -> {
              MoodScreen(
                currentMoodName = coupleSettings?.partnerMood,
                moodHistory = moodHistory,
                partnerName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner",
                userName = coupleSettings?.partner1Name?.ifBlank { "You" } ?: "You",
                onSelectMood = { key, label, icon, note, score ->
                  viewModel.selectMood(key, label, icon, note, score)
                },
                onAddCustomMood = { label, note ->
                  viewModel.addCustomMood(label, note)
                },
                onSendNudge = { emoji ->
                  viewModel.triggerReaction(emoji)
                },
                onClose = { viewModel.navigateToSubScreen(null) },
                celebrationMood = selectedMoodCelebration
              )
            }
            "music", "listening_together" -> {
              val sharedMusic by viewModel.sharedMusic.collectAsState()
              val isMusicLoading by viewModel.isMusicLoading.collectAsState()
              val musicError by viewModel.musicError.collectAsState()
              val partnerName = coupleSettings?.partner2Name?.ifBlank { "Partner" } ?: "Partner"

              MusicScreen(
                sharedMusic = sharedMusic,
                isLoading = isMusicLoading,
                error = musicError,
                isPlaying = isPlayingMusic,
                moodSync = musicMoodSync,
                partnerName = partnerName,
                onLoadSong = { url -> viewModel.loadAndSaveYouTubeMusic(url) },
                onTogglePlay = { viewModel.toggleMusicPlay() },
                onRemoveSong = { viewModel.removeCurrentMusic() },
                onMoodSyncChanged = { viewModel.setMusicMoodSync(it) },
                onClearError = { viewModel.clearMusicError() },
                onClose = { viewModel.navigateToSubScreen(null) }
              )
            }
            "calendar" -> {
              CalendarScreen(
                events = calendarEvents,
                onAddEvent = { title, date, day, icon ->
                  viewModel.addCalendarEvent(title, date, day, icon)
                }
              )
            }
            "journal_notes" -> {
              JournalAndLoveNotesScreen(
                journalEntries = journalEntries,
                loveNotes = loveNotes,
                coupleSettings = coupleSettings,
                initialTab = JournalSectionTab.ALL,
                onAddJournalEntry = { title, cat, body, img ->
                  viewModel.addJournalEntry(title, cat, body, img)
                },
                onAddLoveNote = { text, bg ->
                  viewModel.addLoveNote(text, bg)
                },
                onAddAudioLoveNote = { audioPath, duration, caption, bg, isPinned ->
                  viewModel.addAudioLoveNote(audioPath, duration, caption, bg, isPinned)
                },
                onAddAudioJournalEntry = { title, cat, body, audioPath, duration, img ->
                  viewModel.addJournalEntry(title, cat, body, img, audioPath, duration)
                },
                onDeleteLoveNote = { id ->
                  viewModel.deleteLoveNote(id)
                },
                onTriggerReaction = { emoji ->
                  viewModel.triggerReaction(emoji)
                },
                onClose = { viewModel.navigateToSubScreen(null) }
              )
            }
            "love_notes" -> {
              JournalAndLoveNotesScreen(
                journalEntries = journalEntries,
                loveNotes = loveNotes,
                coupleSettings = coupleSettings,
                initialTab = JournalSectionTab.LOVE_NOTES,
                onAddJournalEntry = { title, cat, body, img ->
                  viewModel.addJournalEntry(title, cat, body, img)
                },
                onAddLoveNote = { text, bg ->
                  viewModel.addLoveNote(text, bg)
                },
                onAddAudioLoveNote = { audioPath, duration, caption, bg, isPinned ->
                  viewModel.addAudioLoveNote(audioPath, duration, caption, bg, isPinned)
                },
                onAddAudioJournalEntry = { title, cat, body, audioPath, duration, img ->
                  viewModel.addJournalEntry(title, cat, body, img, audioPath, duration)
                },
                onDeleteLoveNote = { id ->
                  viewModel.deleteLoveNote(id)
                },
                onTriggerReaction = { emoji ->
                  viewModel.triggerReaction(emoji)
                },
                onClose = { viewModel.navigateToSubScreen(null) }
              )
            }
            "journal" -> {
              JournalAndLoveNotesScreen(
                journalEntries = journalEntries,
                loveNotes = loveNotes,
                coupleSettings = coupleSettings,
                initialTab = JournalSectionTab.JOURNAL,
                onAddJournalEntry = { title, cat, body, img ->
                  viewModel.addJournalEntry(title, cat, body, img)
                },
                onAddLoveNote = { text, bg ->
                  viewModel.addLoveNote(text, bg)
                },
                onAddAudioLoveNote = { audioPath, duration, caption, bg, isPinned ->
                  viewModel.addAudioLoveNote(audioPath, duration, caption, bg, isPinned)
                },
                onAddAudioJournalEntry = { title, cat, body, audioPath, duration, img ->
                  viewModel.addJournalEntry(title, cat, body, img, audioPath, duration)
                },
                onDeleteLoveNote = { id ->
                  viewModel.deleteLoveNote(id)
                },
                onTriggerReaction = { emoji ->
                  viewModel.triggerReaction(emoji)
                },
                onClose = { viewModel.navigateToSubScreen(null) }
              )
            }
            "settings" -> {
              ProfileScreen(
                coupleSettings = coupleSettings,
                onSwitchTheme = { viewModel.switchTheme(it) },
                onToggleSetting = { viewModel.toggleSetting(it) },
                onUpdateProfile = { n1, n2, ann ->
                  viewModel.updateCoupleProfile(n1, n2, ann)
                },
                onShowWelcome = { viewModel.checkUserSession() },
                firebaseUser = firebaseUser,
                isFirebaseConfigured = viewModel.isFirebaseConfigured,
                firebaseSyncStatus = firebaseSyncStatus,
                onSignInWithGoogle = {
                  (context as? Activity)?.let { activity ->
                    viewModel.signInWithGoogle(activity) { _, _ -> }
                  }
                },
                onSignOutFirebase = { viewModel.signOut() },
                onSyncAllToCloud = { viewModel.syncAllDataToFirebase() }
              )
            }
            else -> {
              // Main Tab Destinations
              when (currentTab) {
                "home" -> {
                  HomeScreen(
                    coupleSettings = coupleSettings,
                    moodHistory = moodHistory,
                    onNavigateTab = { viewModel.navigateToTab(it) },
                    onNavigateSubScreen = { viewModel.navigateToSubScreen(it) },
                    onSendHug = { viewModel.triggerReaction("🫂") },
                    onAnswerDailyPrompt = { answer ->
                      viewModel.addJournalEntry(
                        title = "Favorite Memory Reflection",
                        category = "Reflection",
                        body = answer
                      )
                    }
                  )
                }
                "chat" -> {
                  ChatScreen(
                    messages = chatMessages,
                    coupleSettings = coupleSettings,
                    isVoiceNotePlaying = isVoiceNotePlaying,
                    voiceNoteProgress = voiceNoteProgress,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onTriggerReaction = { viewModel.triggerReaction(it) },
                    onToggleVoiceNote = { viewModel.toggleVoiceNotePlayback() },
                    onStartAudioCall = { viewModel.startOutgoingCall() },
                    onStartVideoCall = { viewModel.startOutgoingCall() }
                  )
                }
                "story" -> {
                  StoryScreen(
                    milestones = storyMilestones,
                    loveNotes = loveNotes,
                    moodHistory = moodHistory,
                    journalEntries = journalEntries,
                    calendarEvents = calendarEvents,
                    coupleSettings = coupleSettings,
                    onAddMilestone = { t, d, desc, img ->
                      viewModel.addStoryMilestone(t, d, desc, img)
                    },
                    onAddLoveNote = { text, bg ->
                      viewModel.addLoveNote(text, bg)
                    },
                    onAddJournalEntry = { title, cat, body, img ->
                      viewModel.addJournalEntry(title, cat, body, img)
                    },
                    onAddCalendarEvent = { title, date, day, icon ->
                      viewModel.addCalendarEvent(title, date, day, icon)
                    },
                    onTriggerReaction = { emoji ->
                      viewModel.triggerReaction(emoji)
                    },
                    onNavigateSubScreen = { screen ->
                      viewModel.navigateToSubScreen(screen)
                    }
                  )
                }
                "together" -> {
                  TogetherScreen(
                    coupleSettings = coupleSettings,
                    isPlayingMusic = isPlayingMusic,
                    onToggleMusic = { viewModel.toggleMusicPlay() },
                    onNavigateSubScreen = { viewModel.navigateToSubScreen(it) },
                    onAnswerDailyQuestion = { ans ->
                      viewModel.addJournalEntry(
                        title = "Daily Question Answer",
                        category = "Our World",
                        body = ans
                      )
                    }
                  )
                }
                "us" -> {
                  ProfileScreen(
                    coupleSettings = coupleSettings,
                    onSwitchTheme = { viewModel.switchTheme(it) },
                    onToggleSetting = { viewModel.toggleSetting(it) },
                    onUpdateProfile = { n1, n2, ann ->
                      viewModel.updateCoupleProfile(n1, n2, ann)
                    },
                    onShowWelcome = { viewModel.checkUserSession() },
                    firebaseUser = firebaseUser,
                    isFirebaseConfigured = viewModel.isFirebaseConfigured,
                    firebaseSyncStatus = firebaseSyncStatus,
                    onSignInWithGoogle = {
                      (context as? Activity)?.let { activity ->
                        viewModel.signInWithGoogle(activity) { _, _ -> }
                      }
                    },
                    onSignOutFirebase = { viewModel.signOut() },
                    onSyncAllToCloud = { viewModel.syncAllDataToFirebase() }
                  )
                }
              }
            }
          }
        }
      }

      // Drawer Overlay
      RealUsDrawer(
        isOpen = isDrawerOpen,
        onClose = { viewModel.closeDrawer() },
        onNavigateTab = { viewModel.navigateToTab(it) },
        onNavigateSubScreen = { viewModel.navigateToSubScreen(it) }
      )

      // Floating Flying Reactions Overlay (renders across all screens)
      FloatingReactionLayer(reactions = flyingReactions)
    }
  }
}
