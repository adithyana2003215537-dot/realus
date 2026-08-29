package com.example.ui.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

private const val TAG = "AudioLoveNoteManager"

enum class RecorderStatus {
  IDLE,
  RECORDING,
  PREVIEW_READY,
  PLAYING_PREVIEW
}

class AudioLoveNoteManager(private val context: Context) {
  private var mediaRecorder: MediaRecorder? = null
  private var previewPlayer: MediaPlayer? = null
  private var activePlaybackPlayer: MediaPlayer? = null
  private var activePlayingPath: String? = null

  private var currentRecordingFile: File? = null
  private var recordingStartTime = 0L

  private val _recorderStatus = MutableStateFlow(RecorderStatus.IDLE)
  val recorderStatus: StateFlow<RecorderStatus> = _recorderStatus.asStateFlow()

  private val _recordingDurationSec = MutableStateFlow(0)
  val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

  private val _previewDurationSec = MutableStateFlow(0)
  val previewDurationSec: StateFlow<Int> = _previewDurationSec.asStateFlow()

  private val _previewProgress = MutableStateFlow(0f)
  val previewProgress: StateFlow<Float> = _previewProgress.asStateFlow()

  private val _liveAmplitudes = MutableStateFlow<List<Float>>(emptyList())
  val liveAmplitudes: StateFlow<List<Float>> = _liveAmplitudes.asStateFlow()

  // Track playback for in-card notes
  private val _currentlyPlayingId = MutableStateFlow<String?>(null)
  val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

  private val _activePlaybackProgress = MutableStateFlow(0f)
  val activePlaybackProgress: StateFlow<Float> = _activePlaybackProgress.asStateFlow()

  private val _activePlaybackCurrentSec = MutableStateFlow(0)
  val activePlaybackCurrentSec: StateFlow<Int> = _activePlaybackCurrentSec.asStateFlow()

  private val _activePlaybackTotalSec = MutableStateFlow(0)
  val activePlaybackTotalSec: StateFlow<Int> = _activePlaybackTotalSec.asStateFlow()

  private var recordingTickerJob: Job? = null
  private var previewTickerJob: Job? = null
  private var cardPlaybackTickerJob: Job? = null

  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  fun startRecording(): Boolean {
    stopAllPlayback()
    try {
      val audioDir = File(context.filesDir, "love_notes_audio").apply {
        if (!exists()) mkdirs()
      }
      val file = File(audioDir, "love_note_${System.currentTimeMillis()}.m4a")
      currentRecordingFile = file

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }

      recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(128000)
        setAudioSamplingRate(44100)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }
      mediaRecorder = recorder
      recordingStartTime = System.currentTimeMillis()
      _recorderStatus.value = RecorderStatus.RECORDING
      _recordingDurationSec.value = 0
      _liveAmplitudes.value = emptyList()

      startRecordingTicker()
      return true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start audio recording", e)
      cleanupRecorder()
      _recorderStatus.value = RecorderStatus.IDLE
      return false
    }
  }

  private fun startRecordingTicker() {
    recordingTickerJob?.cancel()
    recordingTickerJob = coroutineScope.launch {
      val sampledList = mutableListOf<Float>()
      while (isActive && _recorderStatus.value == RecorderStatus.RECORDING) {
        delay(100)
        val elapsed = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
        _recordingDurationSec.value = elapsed

        // Read amplitude for live visualizer
        val maxAmp = try {
          mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
          0
        }
        val normalized = (maxAmp / 32767f).coerceIn(0.08f, 1.0f)
        sampledList.add(normalized)
        if (sampledList.size > 28) {
          sampledList.removeAt(0)
        }
        _liveAmplitudes.value = sampledList.toList()

        // Max limit of 120 seconds for love note
        if (elapsed >= 120) {
          stopRecording()
          break
        }
      }
    }
  }

  fun stopRecording(): Pair<String, Int>? {
    recordingTickerJob?.cancel()
    val file = currentRecordingFile
    val duration = _recordingDurationSec.value.coerceAtLeast(1)

    try {
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping recorder", e)
    } finally {
      mediaRecorder = null
    }

    if (file != null && file.exists() && file.length() > 0) {
      _previewDurationSec.value = duration
      _recorderStatus.value = RecorderStatus.PREVIEW_READY
      return Pair(file.absolutePath, duration)
    } else {
      _recorderStatus.value = RecorderStatus.IDLE
      return null
    }
  }

  fun cancelRecording() {
    recordingTickerJob?.cancel()
    cleanupRecorder()
    currentRecordingFile?.let {
      if (it.exists()) it.delete()
    }
    currentRecordingFile = null
    _recorderStatus.value = RecorderStatus.IDLE
    _recordingDurationSec.value = 0
    _liveAmplitudes.value = emptyList()
  }

  fun playPreview() {
    val file = currentRecordingFile ?: return
    if (!file.exists()) return

    stopPreviewPlayback()
    stopCardPlayback()

    try {
      val player = MediaPlayer().apply {
        setDataSource(file.absolutePath)
        prepare()
        setOnCompletionListener {
          _recorderStatus.value = RecorderStatus.PREVIEW_READY
          _previewProgress.value = 0f
          previewTickerJob?.cancel()
        }
        start()
      }
      previewPlayer = player
      _recorderStatus.value = RecorderStatus.PLAYING_PREVIEW

      val totalDurationMs = player.duration.coerceAtLeast(1000)
      _previewDurationSec.value = totalDurationMs / 1000

      previewTickerJob?.cancel()
      previewTickerJob = coroutineScope.launch {
        while (isActive && player.isPlaying) {
          delay(100)
          val currentMs = player.currentPosition
          _previewProgress.value = (currentMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to play preview audio", e)
      _recorderStatus.value = RecorderStatus.PREVIEW_READY
    }
  }

  fun pausePreview() {
    try {
      if (previewPlayer?.isPlaying == true) {
        previewPlayer?.pause()
        previewTickerJob?.cancel()
        _recorderStatus.value = RecorderStatus.PREVIEW_READY
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error pausing preview", e)
    }
  }

  fun getFinalRecordedAudioPath(): String? {
    return currentRecordingFile?.absolutePath
  }

  fun resetRecorderState() {
    stopPreviewPlayback()
    currentRecordingFile = null
    _recorderStatus.value = RecorderStatus.IDLE
    _recordingDurationSec.value = 0
    _previewProgress.value = 0f
    _liveAmplitudes.value = emptyList()
  }

  // Card Playback Methods (for items on corkboard or in journal)
  fun playCardAudio(id: String, filePath: String, fallbackDurationSec: Int = 15) {
    if (_currentlyPlayingId.value == id && activePlaybackPlayer?.isPlaying == true) {
      pauseCardPlayback()
      return
    }

    stopAllPlayback()

    val file = File(filePath)
    if (!file.exists()) {
      // Simulate playback for preloaded/sample audio notes if local file doesn't exist
      simulateSampleAudioPlayback(id, fallbackDurationSec)
      return
    }

    try {
      val player = MediaPlayer().apply {
        setDataSource(filePath)
        prepare()
        setOnCompletionListener {
          stopCardPlayback()
        }
        start()
      }
      activePlaybackPlayer = player
      activePlayingPath = filePath
      _currentlyPlayingId.value = id

      val durationMs = player.duration.coerceAtLeast(fallbackDurationSec * 1000)
      val totalSec = durationMs / 1000
      _activePlaybackTotalSec.value = totalSec

      cardPlaybackTickerJob?.cancel()
      cardPlaybackTickerJob = coroutineScope.launch {
        while (isActive && player.isPlaying) {
          delay(100)
          val curPos = player.currentPosition
          _activePlaybackProgress.value = (curPos.toFloat() / durationMs).coerceIn(0f, 1f)
          _activePlaybackCurrentSec.value = (curPos / 1000)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error playing card audio file, falling back to simulated playback", e)
      simulateSampleAudioPlayback(id, fallbackDurationSec)
    }
  }

  private fun simulateSampleAudioPlayback(id: String, durationSec: Int) {
    _currentlyPlayingId.value = id
    _activePlaybackTotalSec.value = durationSec
    _activePlaybackCurrentSec.value = 0
    _activePlaybackProgress.value = 0f

    cardPlaybackTickerJob?.cancel()
    cardPlaybackTickerJob = coroutineScope.launch {
      val totalSteps = (durationSec * 10).coerceAtLeast(10)
      for (step in 0..totalSteps) {
        if (!isActive || _currentlyPlayingId.value != id) break
        delay(100)
        val progress = step.toFloat() / totalSteps
        _activePlaybackProgress.value = progress
        _activePlaybackCurrentSec.value = (progress * durationSec).toInt()
      }
      if (_currentlyPlayingId.value == id) {
        _currentlyPlayingId.value = null
        _activePlaybackProgress.value = 0f
        _activePlaybackCurrentSec.value = 0
      }
    }
  }

  fun pauseCardPlayback() {
    try {
      if (activePlaybackPlayer?.isPlaying == true) {
        activePlaybackPlayer?.pause()
      }
      cardPlaybackTickerJob?.cancel()
      _currentlyPlayingId.value = null
    } catch (e: Exception) {
      Log.e(TAG, "Error pausing card playback", e)
    }
  }

  fun stopCardPlayback() {
    cardPlaybackTickerJob?.cancel()
    try {
      activePlaybackPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error releasing active playback player", e)
    } finally {
      activePlaybackPlayer = null
      activePlayingPath = null
      _currentlyPlayingId.value = null
      _activePlaybackProgress.value = 0f
      _activePlaybackCurrentSec.value = 0
    }
  }

  private fun stopPreviewPlayback() {
    previewTickerJob?.cancel()
    try {
      previewPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping preview player", e)
    } finally {
      previewPlayer = null
    }
  }

  fun stopAllPlayback() {
    stopPreviewPlayback()
    stopCardPlayback()
  }

  private fun cleanupRecorder() {
    try {
      mediaRecorder?.apply {
        reset()
        release()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error cleaning up recorder", e)
    } finally {
      mediaRecorder = null
    }
  }

  fun release() {
    stopAllPlayback()
    cleanupRecorder()
  }
}
