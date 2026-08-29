package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.CoupleSettings
import com.example.data.model.JournalEntry
import com.example.data.model.LoveNote
import com.example.data.model.SharedMusic
import com.example.data.model.StoryMilestone
import com.example.data.model.UserMood
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

data class UserProfile(
  val uid: String = "",
  val phoneNumber: String = "",
  val name: String = "",
  val profilePicUrl: String = "",
  val partnerCode: String = "",
  val coupleId: String? = null,
  val partnerUid: String? = null,
  val partnerName: String? = null,
  val anniversaryDate: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

data class CoupleData(
  val coupleId: String = "",
  val partner1Uid: String = "",
  val partner2Uid: String = "",
  val partner1Name: String = "",
  val partner2Name: String = "",
  val anniversaryDate: String = "",
  val partner1Mood: String = "Loved",
  val partner2Mood: String = "Loved",
  val coupleCode: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

class FirebaseManager private constructor(private val context: Context) {

  companion object {
    private const val TAG = "FirebaseManager"
    @Volatile
    private var INSTANCE: FirebaseManager? = null

    fun getInstance(context: Context): FirebaseManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: FirebaseManager(context.applicationContext).also { INSTANCE = it }
      }
    }

    fun generateUniquePartnerCode(): String {
      val letters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
      val randomStr = (1..6).map { letters.random() }.joinToString("")
      return "US-$randomStr"
    }
  }

  val isFirebaseInitialized: Boolean
    get() = runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)

  val auth: FirebaseAuth?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseAuth.getInstance() }.getOrNull()
    } else null

  val firestore: FirebaseFirestore?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    } else null

  private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
  val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

  private val _syncStatus = MutableStateFlow<String>("Ready")
  val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

  init {
    if (isFirebaseInitialized) {
      auth?.let { firebaseAuth ->
        _currentUserState.value = firebaseAuth.currentUser
        firebaseAuth.addAuthStateListener { fa ->
          _currentUserState.value = fa.currentUser
        }
      }
    }
  }

  // --- Real Firebase Phone Authentication ---

  fun sendPhoneOtp(
    activity: Activity,
    phoneNumber: String,
    onCodeSent: (verificationId: String, resendToken: PhoneAuthProvider.ForceResendingToken?) -> Unit,
    onVerificationCompleted: (FirebaseUser) -> Unit,
    onVerificationFailed: (Exception) -> Unit
  ) {
    val firebaseAuth = auth ?: run {
      onVerificationFailed(IllegalStateException("Firebase is not initialized."))
      return
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
      override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Log.d(TAG, "onVerificationCompleted instant auto-verify with credential")
        firebaseAuth.signInWithCredential(credential)
          .addOnSuccessListener { authResult ->
            authResult.user?.let { user ->
              _currentUserState.value = user
              onVerificationCompleted(user)
            }
          }
          .addOnFailureListener { error ->
            Log.e(TAG, "Instant signInWithCredential failed", error)
            onVerificationFailed(error)
          }
      }

      override fun onVerificationFailed(e: FirebaseException) {
        Log.e(TAG, "onVerificationFailed: ${e.message}", e)
        onVerificationFailed(e)
      }

      override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
      ) {
        Log.d(TAG, "onCodeSent: verificationId=$verificationId")
        onCodeSent(verificationId, token)
      }
    }

    try {
      val options = PhoneAuthOptions.newBuilder(firebaseAuth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .build()
      PhoneAuthProvider.verifyPhoneNumber(options)
    } catch (e: Exception) {
      Log.e(TAG, "PhoneAuthProvider.verifyPhoneNumber failed to build/start", e)
      onVerificationFailed(e)
    }
  }

  fun resendPhoneOtp(
    activity: Activity,
    phoneNumber: String,
    token: PhoneAuthProvider.ForceResendingToken,
    onCodeSent: (verificationId: String, resendToken: PhoneAuthProvider.ForceResendingToken?) -> Unit,
    onVerificationCompleted: (FirebaseUser) -> Unit,
    onVerificationFailed: (Exception) -> Unit
  ) {
    val firebaseAuth = auth ?: run {
      onVerificationFailed(IllegalStateException("Firebase is not initialized."))
      return
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
      override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
          .addOnSuccessListener { authResult ->
            authResult.user?.let { user ->
              _currentUserState.value = user
              onVerificationCompleted(user)
            }
          }
          .addOnFailureListener { error ->
            onVerificationFailed(error)
          }
      }

      override fun onVerificationFailed(e: FirebaseException) {
        onVerificationFailed(e)
      }

      override fun onCodeSent(
        verificationId: String,
        newToken: PhoneAuthProvider.ForceResendingToken
      ) {
        onCodeSent(verificationId, newToken)
      }
    }

    try {
      val options = PhoneAuthOptions.newBuilder(firebaseAuth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .setForceResendingToken(token)
        .build()
      PhoneAuthProvider.verifyPhoneNumber(options)
    } catch (e: Exception) {
      onVerificationFailed(e)
    }
  }

  suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): Result<FirebaseUser> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase not initialized"))
    return try {
      val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
      val result = firebaseAuth.signInWithCredential(credential).await()
      val user = result.user ?: throw IllegalStateException("FirebaseUser is null after sign in")
      _currentUserState.value = user
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "verifyPhoneOtp failed", e)
      Result.failure(e)
    }
  }

  suspend fun signInWithGoogle(
    activity: Activity,
    webClientId: String = ""
  ): Result<FirebaseUser> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase not initialized"))
    return try {
      val credentialManager = CredentialManager.create(activity)
      val clientId = if (webClientId.isNotBlank()) webClientId else "default-client-id"
      val googleIdOption = GetSignInWithGoogleOption.Builder(clientId).build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(request = request, context = activity)
      val credential = result.credential

      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val firebaseAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseAuthCredential).await()
        val user = authResult.user ?: throw IllegalStateException("No user returned")
        _currentUserState.value = user
        Result.success(user)
      } else {
        Result.failure(Exception("Unrecognized credential type"))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Google Sign-In failed", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      auth?.signOut()
      _currentUserState.value = null
    } catch (e: Exception) {
      Log.e(TAG, "Sign out failed", e)
    }
  }

  // --- Real User Profile & Partner Pairing ---

  suspend fun saveUserProfile(
    uid: String,
    phoneNumber: String,
    name: String,
    profilePicUrl: String = "",
    anniversaryDate: String
  ): Result<UserProfile> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val docRef = db.collection("users").document(uid)
      val snapshot = docRef.get().await()
      
      val existingCode = snapshot.getString("partnerCode")
      val partnerCode = if (!existingCode.isNullOrBlank()) existingCode else generateUniquePartnerCode()
      val coupleId = snapshot.getString("coupleId")
      val partnerUid = snapshot.getString("partnerUid")
      val partnerName = snapshot.getString("partnerName")

      val profile = UserProfile(
        uid = uid,
        phoneNumber = phoneNumber,
        name = name,
        profilePicUrl = profilePicUrl,
        partnerCode = partnerCode,
        coupleId = coupleId,
        partnerUid = partnerUid,
        partnerName = partnerName,
        anniversaryDate = anniversaryDate,
        createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
      )

      val data = hashMapOf(
        "uid" to profile.uid,
        "phoneNumber" to profile.phoneNumber,
        "name" to profile.name,
        "profilePicUrl" to profile.profilePicUrl,
        "partnerCode" to profile.partnerCode,
        "coupleId" to profile.coupleId,
        "partnerUid" to profile.partnerUid,
        "partnerName" to profile.partnerName,
        "anniversaryDate" to profile.anniversaryDate,
        "createdAt" to profile.createdAt
      )

      docRef.set(data, SetOptions.merge()).await()
      Result.success(profile)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save user profile", e)
      Result.failure(e)
    }
  }

  suspend fun updateUserProfilePic(uid: String, profilePicUrl: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      db.collection("users").document(uid)
        .set(mapOf("profilePicUrl" to profilePicUrl), SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update profile pic", e)
      Result.failure(e)
    }
  }

  suspend fun getUserProfile(uid: String): Result<UserProfile?> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val doc = db.collection("users").document(uid).get().await()
      if (!doc.exists()) {
        Result.success(null)
      } else {
        val profile = UserProfile(
          uid = doc.getString("uid") ?: uid,
          phoneNumber = doc.getString("phoneNumber") ?: "",
          name = doc.getString("name") ?: "",
          profilePicUrl = doc.getString("profilePicUrl") ?: "",
          partnerCode = doc.getString("partnerCode") ?: "",
          coupleId = doc.getString("coupleId"),
          partnerUid = doc.getString("partnerUid"),
          partnerName = doc.getString("partnerName"),
          anniversaryDate = doc.getString("anniversaryDate") ?: "",
          createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
        Result.success(profile)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get user profile", e)
      Result.failure(e)
    }
  }

  suspend fun connectWithPartnerCode(
    currentUid: String,
    currentName: String,
    enteredPartnerCode: String,
    anniversaryDate: String
  ): Result<Pair<UserProfile, CoupleData>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val formattedCode = enteredPartnerCode.trim().uppercase()
      
      // Find partner by partnerCode
      val querySnapshot = db.collection("users")
        .whereEqualTo("partnerCode", formattedCode)
        .limit(1)
        .get()
        .await()

      if (querySnapshot.isEmpty) {
        return Result.failure(IllegalArgumentException("No user found with code $formattedCode. Please check and try again."))
      }

      val partnerDoc = querySnapshot.documents.first()
      val partnerUid = partnerDoc.id
      val partnerName = partnerDoc.getString("name") ?: "Partner"

      if (partnerUid == currentUid) {
        return Result.failure(IllegalArgumentException("You cannot connect with your own pairing code! Share this code with your partner."))
      }

      // Check if partner is already in a couple
      val existingCoupleId = partnerDoc.getString("coupleId")
      val coupleId = if (!existingCoupleId.isNullOrBlank()) {
        existingCoupleId
      } else {
        // Generate a new deterministic or unique coupleId
        "couple_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
      }

      val coupleDocRef = db.collection("couples").document(coupleId)
      val coupleData = CoupleData(
        coupleId = coupleId,
        partner1Uid = partnerUid,
        partner2Uid = currentUid,
        partner1Name = partnerName,
        partner2Name = currentName,
        anniversaryDate = if (anniversaryDate.isNotBlank()) anniversaryDate else (partnerDoc.getString("anniversaryDate") ?: ""),
        coupleCode = formattedCode,
        createdAt = System.currentTimeMillis()
      )

      val coupleMap = hashMapOf(
        "coupleId" to coupleData.coupleId,
        "partner1Uid" to coupleData.partner1Uid,
        "partner2Uid" to coupleData.partner2Uid,
        "partner1Name" to coupleData.partner1Name,
        "partner2Name" to coupleData.partner2Name,
        "anniversaryDate" to coupleData.anniversaryDate,
        "partner1Mood" to coupleData.partner1Mood,
        "partner2Mood" to coupleData.partner2Mood,
        "coupleCode" to coupleData.coupleCode,
        "createdAt" to coupleData.createdAt
      )
      coupleDocRef.set(coupleMap, SetOptions.merge()).await()

      // Update both user profiles
      db.collection("users").document(currentUid).set(
        hashMapOf(
          "coupleId" to coupleId,
          "partnerUid" to partnerUid,
          "partnerName" to partnerName
        ),
        SetOptions.merge()
      ).await()

      db.collection("users").document(partnerUid).set(
        hashMapOf(
          "coupleId" to coupleId,
          "partnerUid" to currentUid,
          "partnerName" to currentName
        ),
        SetOptions.merge()
      ).await()

      val updatedCurrentUserProfile = UserProfile(
        uid = currentUid,
        name = currentName,
        partnerCode = "",
        coupleId = coupleId,
        partnerUid = partnerUid,
        partnerName = partnerName,
        anniversaryDate = coupleData.anniversaryDate
      )

      Result.success(Pair(updatedCurrentUserProfile, coupleData))
    } catch (e: Exception) {
      Log.e(TAG, "connectWithPartnerCode failed", e)
      Result.failure(e)
    }
  }

  suspend fun getCoupleData(coupleId: String): Result<CoupleData?> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val doc = db.collection("couples").document(coupleId).get().await()
      if (!doc.exists()) {
        Result.success(null)
      } else {
        val couple = CoupleData(
          coupleId = doc.getString("coupleId") ?: coupleId,
          partner1Uid = doc.getString("partner1Uid") ?: "",
          partner2Uid = doc.getString("partner2Uid") ?: "",
          partner1Name = doc.getString("partner1Name") ?: "",
          partner2Name = doc.getString("partner2Name") ?: "",
          anniversaryDate = doc.getString("anniversaryDate") ?: "",
          partner1Mood = doc.getString("partner1Mood") ?: "Loved",
          partner2Mood = doc.getString("partner2Mood") ?: "Loved",
          coupleCode = doc.getString("coupleCode") ?: "",
          createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
        Result.success(couple)
      }
    } catch (e: Exception) {
      Log.e(TAG, "getCoupleData failed", e)
      Result.failure(e)
    }
  }

  // --- Realtime Firestore Listeners ---

  fun listenToUserProfile(uid: String, onUpdate: (UserProfile?) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("users").document(uid)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "listenToUserProfile error", error)
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          val profile = UserProfile(
            uid = snapshot.getString("uid") ?: uid,
            phoneNumber = snapshot.getString("phoneNumber") ?: "",
            name = snapshot.getString("name") ?: "",
            profilePicUrl = snapshot.getString("profilePicUrl") ?: "",
            partnerCode = snapshot.getString("partnerCode") ?: "",
            coupleId = snapshot.getString("coupleId"),
            partnerUid = snapshot.getString("partnerUid"),
            partnerName = snapshot.getString("partnerName"),
            anniversaryDate = snapshot.getString("anniversaryDate") ?: "",
            createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
          )
          onUpdate(profile)
        } else {
          onUpdate(null)
        }
      }
  }

  fun listenToCouple(coupleId: String, onUpdate: (CoupleData?) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "listenToCouple error", error)
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          val couple = CoupleData(
            coupleId = snapshot.getString("coupleId") ?: coupleId,
            partner1Uid = snapshot.getString("partner1Uid") ?: "",
            partner2Uid = snapshot.getString("partner2Uid") ?: "",
            partner1Name = snapshot.getString("partner1Name") ?: "",
            partner2Name = snapshot.getString("partner2Name") ?: "",
            anniversaryDate = snapshot.getString("anniversaryDate") ?: "",
            partner1Mood = snapshot.getString("partner1Mood") ?: "Loved",
            partner2Mood = snapshot.getString("partner2Mood") ?: "Loved",
            coupleCode = snapshot.getString("coupleCode") ?: "",
            createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
          )
          onUpdate(couple)
        } else {
          onUpdate(null)
        }
      }
  }

  fun listenToChatMessages(coupleId: String, onUpdate: (List<ChatMessage>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("chat_messages")
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "listenToChatMessages error", error)
          return@addSnapshotListener
        }
        val messages = snapshot?.documents?.mapNotNull { doc ->
          try {
            val sender = doc.getString("sender") ?: "partner"
            val text = doc.getString("text") ?: ""
            val timestamp = doc.getString("timestamp") ?: ""
            val isAudio = doc.getBoolean("isAudio") ?: false
            val audioDuration = doc.getString("audioDuration") ?: ""
            val isImage = doc.getBoolean("isImage") ?: false
            val imageUrl = doc.getString("imageUrl") ?: ""
            val imageCaption = doc.getString("imageCaption") ?: ""
            val isRead = doc.getBoolean("isRead") ?: true
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val senderUid = doc.getString("senderUid")

            val currentUid = auth?.currentUser?.uid
            val normalizedSender = if (senderUid != null && currentUid != null) {
              if (senderUid == currentUid) "you" else "partner"
            } else {
              sender
            }

            val rawId = doc.getLong("id")
            val idVal = if (rawId == null || rawId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawId

            ChatMessage(
              id = idVal,
              sender = normalizedSender,
              text = text,
              timestamp = timestamp,
              isAudio = isAudio,
              audioDuration = audioDuration,
              isImage = isImage,
              imageUrl = imageUrl,
              imageCaption = imageCaption,
              isRead = isRead,
              createdAt = createdAt
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(messages)
      }
  }

  fun listenToLoveNotes(coupleId: String, onUpdate: (List<LoveNote>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("love_notes")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "listenToLoveNotes error", error)
          return@addSnapshotListener
        }
        val notes = snapshot?.documents?.mapNotNull { doc ->
          try {
            val author = doc.getString("author") ?: "YOU"
            val text = doc.getString("text") ?: ""
            val timeAgo = doc.getString("timeAgo") ?: "Just now"
            val bgType = doc.getString("bgType") ?: "clay"
            val isPinned = doc.getBoolean("isPinned") ?: false
            val rotation = (doc.getDouble("rotation") ?: 0.0).toFloat()
            val isAudioNote = doc.getBoolean("isAudioNote") ?: false
            val audioFilePath = doc.getString("audioFilePath") ?: ""
            val audioDurationSec = (doc.getLong("audioDurationSec") ?: 0L).toInt()
            val audioAmplitudes = doc.getString("audioAmplitudes") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            val rawNoteId = doc.getLong("id")
            val noteIdVal = if (rawNoteId == null || rawNoteId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawNoteId

            LoveNote(
              id = noteIdVal,
              author = author,
              text = text,
              timeAgo = timeAgo,
              bgType = bgType,
              isPinned = isPinned,
              rotation = rotation,
              isAudioNote = isAudioNote,
              audioFilePath = audioFilePath,
              audioDurationSec = audioDurationSec,
              audioAmplitudes = audioAmplitudes,
              createdAt = createdAt
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(notes)
      }
  }

  fun listenToStoryMilestones(coupleId: String, onUpdate: (List<StoryMilestone>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("story_milestones")
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        val milestones = snapshot?.documents?.mapNotNull { doc ->
          try {
            val rawMsId = doc.getLong("id")
            val msIdVal = if (rawMsId == null || rawMsId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawMsId
            StoryMilestone(
              id = msIdVal,
              dateStr = doc.getString("dateStr") ?: "",
              title = doc.getString("title") ?: "",
              description = doc.getString("description") ?: "",
              imageUrl = doc.getString("imageUrl") ?: "",
              rotation = (doc.getDouble("rotation") ?: 0.0).toFloat(),
              isHighlighted = doc.getBoolean("isHighlighted") ?: false,
              createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(milestones)
      }
  }

  fun listenToJournalEntries(coupleId: String, onUpdate: (List<JournalEntry>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("journal_entries")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        val entries = snapshot?.documents?.mapNotNull { doc ->
          try {
            val rawJId = doc.getLong("id")
            val jIdVal = if (rawJId == null || rawJId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawJId
            JournalEntry(
              id = jIdVal,
              dateStr = doc.getString("dateStr") ?: "",
              category = doc.getString("category") ?: "Memory",
              title = doc.getString("title") ?: "",
              body = doc.getString("body") ?: "",
              imageUrl = doc.getString("imageUrl") ?: "",
              weatherIcon = doc.getString("weatherIcon") ?: "sunny",
              isFavorite = doc.getBoolean("isFavorite") ?: false,
              isAudioAttached = doc.getBoolean("isAudioAttached") ?: false,
              audioFilePath = doc.getString("audioFilePath") ?: "",
              audioDurationSec = (doc.getLong("audioDurationSec") ?: 0L).toInt(),
              createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(entries)
      }
  }

  fun listenToCalendarEvents(coupleId: String, onUpdate: (List<CalendarEvent>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("calendar_events")
      .orderBy("dayOfMonth", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        val events = snapshot?.documents?.mapNotNull { doc ->
          try {
            val rawEId = doc.getLong("id")
            val eIdVal = if (rawEId == null || rawEId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawEId
            CalendarEvent(
              id = eIdVal,
              title = doc.getString("title") ?: "",
              dateStr = doc.getString("dateStr") ?: "",
              daysRemainingText = doc.getString("daysRemainingText") ?: "",
              dayOfMonth = (doc.getLong("dayOfMonth") ?: 1L).toInt(),
              monthYear = doc.getString("monthYear") ?: "",
              iconType = doc.getString("iconType") ?: "heart",
              createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(events)
      }
  }

  fun listenToMoods(coupleId: String, onUpdate: (List<UserMood>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("user_moods")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        val moods = snapshot?.documents?.mapNotNull { doc ->
          try {
            val rawMId = doc.getLong("id")
            val mIdVal = if (rawMId == null || rawMId == 0L) doc.id.hashCode().toLong().let { if (it <= 0L) kotlin.math.abs(it) + 100L else it } else rawMId
            UserMood(
              id = mIdVal,
              moodKey = doc.getString("moodKey") ?: "loved",
              moodLabel = doc.getString("moodLabel") ?: "Loved",
              moodIcon = doc.getString("moodIcon") ?: "💖",
              moodScore = (doc.getDouble("moodScore") ?: 8.5).toFloat(),
              partnerMoodLabel = doc.getString("partnerMoodLabel") ?: "Loved",
              partnerMoodIcon = doc.getString("partnerMoodIcon") ?: "💖",
              partnerMoodScore = (doc.getDouble("partnerMoodScore") ?: 9.0).toFloat(),
              note = doc.getString("note") ?: "",
              synergyScore = (doc.getLong("synergyScore") ?: 92L).toInt(),
              dateLabel = doc.getString("dateLabel") ?: "Today",
              partnerName = doc.getString("partnerName") ?: "Partner",
              timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()
        onUpdate(moods)
      }
  }

  // --- Sync Operations to Firestore Cloud ---

  suspend fun syncLoveNoteToCloud(note: LoveNote, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val noteData = hashMapOf(
        "id" to note.id,
        "text" to note.text,
        "author" to note.author,
        "timeAgo" to note.timeAgo,
        "bgType" to note.bgType,
        "isPinned" to note.isPinned,
        "rotation" to note.rotation,
        "isAudioNote" to note.isAudioNote,
        "audioDurationSec" to note.audioDurationSec,
        "audioFilePath" to note.audioFilePath,
        "audioAmplitudes" to note.audioAmplitudes,
        "createdAt" to note.createdAt,
        "syncedByUid" to user.uid,
        "syncedAt" to System.currentTimeMillis()
      )
      db.collection("couples")
        .document(coupleId)
        .collection("love_notes")
        .document(note.id.toString())
        .set(noteData, SetOptions.merge())
        .await()
      _syncStatus.value = "Synced note: ${note.id}"
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync love note", e)
      false
    }
  }

  suspend fun deleteLoveNoteFromCloud(noteId: Long, coupleId: String): Boolean {
    val db = firestore ?: return false
    return try {
      db.collection("couples")
        .document(coupleId)
        .collection("love_notes")
        .document(noteId.toString())
        .delete()
        .await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to delete love note from cloud", e)
      false
    }
  }

  suspend fun syncChatMessageToCloud(message: ChatMessage, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val data = hashMapOf(
        "id" to message.id,
        "sender" to message.sender,
        "senderUid" to user.uid,
        "text" to message.text,
        "timestamp" to message.timestamp,
        "isAudio" to message.isAudio,
        "audioDuration" to message.audioDuration,
        "isImage" to message.isImage,
        "imageUrl" to message.imageUrl,
        "imageCaption" to message.imageCaption,
        "isRead" to message.isRead,
        "createdAt" to message.createdAt,
        "syncedByUid" to user.uid,
        "syncedAt" to System.currentTimeMillis()
      )
      db.collection("couples")
        .document(coupleId)
        .collection("chat_messages")
        .document(message.id.toString())
        .set(data, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync chat message", e)
      false
    }
  }

  suspend fun syncJournalEntryToCloud(entry: JournalEntry, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val entryData = hashMapOf(
        "id" to entry.id,
        "title" to entry.title,
        "category" to entry.category,
        "body" to entry.body,
        "imageUrl" to entry.imageUrl,
        "dateStr" to entry.dateStr,
        "weatherIcon" to entry.weatherIcon,
        "isFavorite" to entry.isFavorite,
        "isAudioAttached" to entry.isAudioAttached,
        "audioDurationSec" to entry.audioDurationSec,
        "audioFilePath" to entry.audioFilePath,
        "createdAt" to entry.createdAt,
        "syncedByUid" to user.uid,
        "syncedAt" to System.currentTimeMillis()
      )
      db.collection("couples")
        .document(coupleId)
        .collection("journal_entries")
        .document(entry.id.toString())
        .set(entryData, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync journal entry", e)
      false
    }
  }

  suspend fun syncMilestoneToCloud(milestone: StoryMilestone, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val data = hashMapOf(
        "id" to milestone.id,
        "title" to milestone.title,
        "dateStr" to milestone.dateStr,
        "description" to milestone.description,
        "imageUrl" to milestone.imageUrl,
        "rotation" to milestone.rotation,
        "isHighlighted" to milestone.isHighlighted,
        "createdAt" to milestone.createdAt,
        "syncedByUid" to user.uid,
        "syncedAt" to System.currentTimeMillis()
      )
      db.collection("couples")
        .document(coupleId)
        .collection("story_milestones")
        .document(milestone.id.toString())
        .set(data, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync milestone", e)
      false
    }
  }

  suspend fun syncCalendarEventToCloud(event: CalendarEvent, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val data = hashMapOf(
        "id" to event.id,
        "title" to event.title,
        "dateStr" to event.dateStr,
        "daysRemainingText" to event.daysRemainingText,
        "dayOfMonth" to event.dayOfMonth,
        "monthYear" to event.monthYear,
        "iconType" to event.iconType,
        "createdAt" to event.createdAt,
        "syncedByUid" to user.uid,
        "syncedAt" to System.currentTimeMillis()
      )
      db.collection("couples")
        .document(coupleId)
        .collection("calendar_events")
        .document(event.id.toString())
        .set(data, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync calendar event", e)
      false
    }
  }

  suspend fun syncMoodToCloud(mood: UserMood, coupleId: String): Boolean {
    val db = firestore ?: return false
    val user = auth?.currentUser ?: return false
    return try {
      val data = hashMapOf(
        "id" to mood.id,
        "moodKey" to mood.moodKey,
        "moodLabel" to mood.moodLabel,
        "moodIcon" to mood.moodIcon,
        "moodScore" to mood.moodScore,
        "partnerMoodLabel" to mood.partnerMoodLabel,
        "partnerMoodIcon" to mood.partnerMoodIcon,
        "partnerMoodScore" to mood.partnerMoodScore,
        "note" to mood.note,
        "synergyScore" to mood.synergyScore,
        "dateLabel" to mood.dateLabel,
        "partnerName" to mood.partnerName,
        "timestamp" to mood.timestamp,
        "syncedByUid" to user.uid
      )
      db.collection("couples")
        .document(coupleId)
        .collection("user_moods")
        .document(mood.id.toString())
        .set(data, SetOptions.merge())
        .await()

      runCatching {
        val coupleDoc = db.collection("couples").document(coupleId).get().await()
        if (coupleDoc.exists()) {
          val p1 = coupleDoc.getString("partner1Uid")
          val fieldToUpdate = if (user.uid == p1) "partner1Mood" else "partner2Mood"
          db.collection("couples").document(coupleId).update(fieldToUpdate, mood.moodLabel)
        }
      }

      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to sync mood", e)
      false
    }
  }

  fun listenToSharedMusic(coupleId: String, onUpdate: (SharedMusic?) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("couples").document(coupleId)
      .collection("music").document("current")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "listenToSharedMusic error", error)
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          val videoId = snapshot.getString("videoId") ?: ""
          if (videoId.isBlank()) {
            onUpdate(null)
          } else {
            val music = SharedMusic(
              videoId = videoId,
              videoUrl = snapshot.getString("videoUrl") ?: "https://www.youtube.com/watch?v=$videoId",
              title = snapshot.getString("title") ?: "YouTube Video",
              artist = snapshot.getString("artist") ?: "YouTube Channel",
              thumbnailUrl = snapshot.getString("thumbnailUrl") ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
              isPlaying = snapshot.getBoolean("isPlaying") ?: false,
              positionMs = snapshot.getLong("positionMs") ?: 0L,
              moodSync = (snapshot.getDouble("moodSync") ?: 0.5).toFloat(),
              addedBy = snapshot.getString("addedBy") ?: "Partner",
              updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
            )
            onUpdate(music)
          }
        } else {
          onUpdate(null)
        }
      }
  }

  suspend fun updateSharedMusic(coupleId: String, music: SharedMusic?): Boolean {
    val db = firestore ?: return false
    return try {
      val docRef = db.collection("couples").document(coupleId).collection("music").document("current")
      if (music == null) {
        docRef.delete().await()
      } else {
        val data = hashMapOf(
          "videoId" to music.videoId,
          "videoUrl" to music.videoUrl,
          "title" to music.title,
          "artist" to music.artist,
          "thumbnailUrl" to music.thumbnailUrl,
          "isPlaying" to music.isPlaying,
          "positionMs" to music.positionMs,
          "moodSync" to music.moodSync,
          "addedBy" to music.addedBy,
          "updatedAt" to music.updatedAt
        )
        docRef.set(data, SetOptions.merge()).await()
      }
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update shared music", e)
      false
    }
  }

  suspend fun updateSharedMusicPlayback(coupleId: String, isPlaying: Boolean, positionMs: Long = 0L): Boolean {
    val db = firestore ?: return false
    return try {
      val docRef = db.collection("couples").document(coupleId).collection("music").document("current")
      val data = hashMapOf<String, Any>(
        "isPlaying" to isPlaying,
        "positionMs" to positionMs,
        "updatedAt" to System.currentTimeMillis()
      )
      docRef.set(data, SetOptions.merge()).await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update music playback", e)
      false
    }
  }

  suspend fun updateSharedMusicMoodSync(coupleId: String, moodSync: Float): Boolean {
    val db = firestore ?: return false
    return try {
      val docRef = db.collection("couples").document(coupleId).collection("music").document("current")
      val data = hashMapOf<String, Any>(
        "moodSync" to moodSync,
        "updatedAt" to System.currentTimeMillis()
      )
      docRef.set(data, SetOptions.merge()).await()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update music mood sync", e)
      false
    }
  }
}

