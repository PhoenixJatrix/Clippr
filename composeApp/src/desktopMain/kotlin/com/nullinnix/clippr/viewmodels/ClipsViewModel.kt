package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip
import com.nullinnix.clippr.misc.ClipAction
import com.nullinnix.clippr.misc.ClipMenuAction
import com.nullinnix.clippr.misc.ClipType
import com.nullinnix.clippr.misc.ClipsState
import com.nullinnix.clippr.misc.Filters
import com.nullinnix.clippr.misc.MIME_TYPE_PLAIN_TEXT
import com.nullinnix.clippr.misc.MergeAction
import com.nullinnix.clippr.misc.MergeOptions
import com.nullinnix.clippr.misc.MultiSelectClipMenuAction
import com.nullinnix.clippr.misc.Notification
import com.nullinnix.clippr.misc.NotificationType
import com.nullinnix.clippr.misc.Tab
import com.nullinnix.clippr.misc.coerce
import com.nullinnix.clippr.misc.desc
import com.nullinnix.clippr.misc.focusWindow
import com.nullinnix.clippr.misc.log
import com.nullinnix.clippr.misc.copyMultipleToClipboard
import com.nullinnix.clippr.misc.copyToClipboard
import com.nullinnix.clippr.misc.pasteMultipleFilesWithRobot
import com.nullinnix.clippr.misc.pasteWithRobot
import com.nullinnix.clippr.misc.search
import com.nullinnix.clippr.misc.showMacConfirmDialog
import com.nullinnix.clippr.misc.toClip
import com.nullinnix.clippr.misc.toClipEntity
import com.nullinnix.clippr.showMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class ClipsViewModel(
    private val clipsDao: ClipsDao,
    private val settingsViewModel: SettingsViewModel,
    private val miscViewModel: MiscViewModel,
    private val notificationsViewModel: NotificationsViewModel
): ViewModel() {
    private val _clipsState = MutableStateFlow(ClipsState())
    val clipsState = _clipsState.asStateFlow()
    var job: Job? = null

    init {
        monitorOldClips()

        clipsDao
            .getOtherClips()
            .onEach {
                _clipsState.update { state ->
                    state.copy(otherClips = it.toClip())
                }

                if (it.size > settingsViewModel.state.value.maximumRememberableUnpinnedClips) {
                    val maxClips = settingsViewModel.state.value.maximumRememberableUnpinnedClips
                    val toDelete = it.subList(maxClips, it.size)

                    clipsDao.deleteSelected (
                        clipsToDelete = toDelete.map { clip ->
                            clip.clipID
                        }
                    )
                }
            }
            .launchIn(viewModelScope)

        clipsDao
            .getPinnedClips()
            .onEach {
                _clipsState.update { state ->
                    state.copy(pinnedClips = it.toClip())
                }
            }
            .launchIn(viewModelScope)
    }

    fun monitorOldClips () {
        job?.cancel()

        job = CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                //delay 10 seconds before start
                delay(10000)

                val settingsState = settingsViewModel.state.value

                log("monitor clips", "monitorOldClips")
                println("still running the job every ${settingsState.clipDeleteTime.unit * settingsState.clipDeleteTime.timeCode.secondsPer} second")

                deleteOldUnpinnedClips()

                delay((settingsState.clipDeleteTime.unit * settingsState.clipDeleteTime.timeCode.secondsPer).toLong() * 1000)
            }
        }
    }

    fun onAction (action: ClipAction) {
        when (action) {
            is ClipAction.OnCopyToClipboard -> {
                copyToClipboard(clip = action.clip, pasteAsFile = action.altHeldDown)
            }

            is ClipAction.OnDelete -> {
                deleteClip(action.clip)
            }

            is ClipAction.OnTogglePin -> {
                togglePinnedClip(action.clip)
            }

            is ClipAction.OnAddClip -> {
                if (settingsViewModel.state.value.recordingEnabled) {
                    addClip(action.clip)
                }
            }

            is ClipAction.ToggleSelectClip -> {
                toggleSelectClip(action.clip)
            }

            is ClipAction.Search -> {
                searchAndFilter()
            }

            is ClipAction.FilterBySource -> {
                _clipsState.update {
                    it.copy(
                        searchParams = "",
                        protoFilters = Filters(sources = setOf(action.source), types = it.protoFilters.types),
                        selectedClips = emptySet()
                    )
                }

                searchAndFilter(true)
            }

            is ClipAction.FilterByType -> {
                _clipsState.update {
                    it.copy(
                        searchParams = "",
                        protoFilters = Filters(types = setOf(action.type), sources = it.protoFilters.sources),
                        selectedClips = emptySet()
                    )
                }

                searchAndFilter(true)
            }
        }
    }

    fun onClipMenuAction(action: ClipMenuAction, clip: Clip) {
        println(action.desc(3))

        when (action) {
            ClipMenuAction.PasteAsText -> {
                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "",
                        type = NotificationType.DelayedOperation(delay = settingsViewModel.state.value.secondsBeforePaste, action = "Pasting text")
                    )
                )

                pasteWithRobot(clip = clip, pasteAsFile = false, wait = settingsViewModel.state.value.secondsBeforePaste)
            }

            ClipMenuAction.PasteAsFile -> {
                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "",
                        type = NotificationType.DelayedOperation(delay = settingsViewModel.state.value.secondsBeforePaste, action = "Pasting file")
                    )
                )

                pasteWithRobot(clip = clip, pasteAsFile = true, wait = settingsViewModel.state.value.secondsBeforePaste)
            }

            ClipMenuAction.CopyAsText -> {
                copyToClipboard(clip = clip, pasteAsFile = false)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "Clip copied as text",
                        type = NotificationType.Info()
                    )
                )
            }

            ClipMenuAction.CopyAsFile -> {
                copyToClipboard(clip = clip, pasteAsFile = true)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "Clip copied as file",
                        type = NotificationType.Info()
                    )
                )
            }

            ClipMenuAction.Pin -> {
                togglePinnedClip(clip)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "Clip pinned",
                        type = NotificationType.Info()
                    )
                )
            }

            ClipMenuAction.Unpin -> {
                togglePinnedClip(clip)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "Clip unpinned",
                        type = NotificationType.Info()
                    )
                )
            }

            ClipMenuAction.Preview -> {

            }

            ClipMenuAction.OpenAsLink -> {
                ProcessBuilder("open", clip.content).start()
            }

            ClipMenuAction.RevealInFinder -> {
                val file = File(clip.content)

                if (file.exists()) {
                    ProcessBuilder("open", "-R", file.absolutePath).start()
                }
            }

            ClipMenuAction.Delete -> {
                if (showMacConfirmDialog("Delete clip", "'${clip.content.coerce(50)}' will be deleted")) {
                    deleteClip(clip)
                    searchAndFilter(true)
                }
            }
        }
    }
    
    fun onMultiSelectAction(action: MultiSelectClipMenuAction) {
        when (action) {
            MultiSelectClipMenuAction.Paste -> {
                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "",
                        type = NotificationType.DelayedOperation(delay = settingsViewModel.state.value.secondsBeforePaste, action = "Pasting ${clipsState.value.selectedClips.size} files")
                    )
                )

                pasteMultipleFilesWithRobot(clips = clipsState.value.selectedClips, wait = settingsViewModel.state.value.secondsBeforePaste)
            }

            MultiSelectClipMenuAction.Copy -> {
                copyMultipleToClipboard(clips = clipsState.value.selectedClips)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "Copied ${clipsState.value.selectedClips.size} clips",
                        type = NotificationType.Info()
                    )
                )
            }

            MultiSelectClipMenuAction.Merge -> {}

            MultiSelectClipMenuAction.PinAll -> {
                setMultiplePinState(clips = clipsState.value.selectedClips.toList(), state = true)
            }

            MultiSelectClipMenuAction.UnpinAll -> {
                setMultiplePinState(clips = clipsState.value.selectedClips.toList(), state = false)
            }

            MultiSelectClipMenuAction.DeleteAll -> {
                if (showMacConfirmDialog("Delete selected clips?", "${clipsState.value.selectedClips.size }${if (clipsState.value.selectedClips.size == 1) " clip" else " clips"} will be deleted")) {
                    deleteSpecified(clips = clipsState.value.selectedClips.toList())

                    searchAndFilter(true)
                }
            }
        }
    }
    
    fun onMergeAction(action: MergeAction, options: MergeOptions) {
        var content = ""
        val clips = clipsState.value.selectedClips.toList()

        var normalizedClips = clips.map {
            var content = it.content

            if (options.trim) {
                content = content.trim().trimIndent().trimMargin()
            }

            content
        }

        if (options.removeDuplicates) {
            normalizedClips = normalizedClips.toSet().toList()
        }

        when (action) {
            MergeAction.CommaSeparated -> {
                for (idx in normalizedClips.indices) {
                    content += normalizedClips[idx]

                    if (idx != normalizedClips.size - 1) {
                        content += ","
                    }
                }
            }

            MergeAction.NewLineSeparated -> {
                for (idx in normalizedClips.indices) {
                    content += normalizedClips[idx]

                    if (idx != normalizedClips.size - 1) {
                        content += "\n"
                    }
                }
            }

            MergeAction.NumberSeparated -> {
                for (idx in normalizedClips.indices) {
                    content += "${idx + 1}. ${normalizedClips[idx]}"

                    if (idx != normalizedClips.size - 1) {
                        content += "\n"
                    }
                }
            }

            MergeAction.SpaceSeparated -> {
                for (idx in normalizedClips.indices) {
                    content += normalizedClips[idx]

                    if (idx != normalizedClips.size - 1) {
                        content += " "
                    }
                }
            }

            MergeAction.NoSeparation -> {
                for (idx in normalizedClips.indices) {
                    content += normalizedClips[idx]
                }
            }
        }

        val newClip = Clip (
            clipID = UUID.randomUUID().toString(),
            content = content,
            copiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            isPinned = false,
            mimeType = MIME_TYPE_PLAIN_TEXT,
            isImage = false,
            exists = true,
            pinnedAt = 0L,
            associatedIcon = ClipType.PLAIN_TEXT.id,
            source = "com.nullinnix.clippr"
        )

        addClip(newClip)

        if (options.copyAfterMerge) {
            copyToClipboard(newClip, false)
        }

        if (options.deleteOriginal) {
            deleteSpecified(clips)
        }

        if (options.saveToDesktop) {
            val desktopPath = Paths.get(System.getProperty("user.home"), "Desktop")
            var fileName = newClip.content.coerce(50, false)
            var idx = 1

            while (desktopPath.resolve("$fileName.txt").toFile().exists()) {
                fileName = newClip.content.coerce(50, false) + " $idx"
                idx += 1
            }

            desktopPath.resolve("$fileName.txt").toFile().writeText(newClip.content)
        }

        notificationsViewModel.postNotification(
            Notification(
                duration = 6,
                content = "Merged ${clipsState.value.selectedClips.size} clips",
                type = NotificationType.Info()
            )
        )
    }

    fun addClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.upsert(clip.toClipEntity().copy(source = if (clip.source in miscViewModel.state.value.allApps.keys) clip.source else null))
        }
    }

    fun deleteClip(clip: Clip) {
        viewModelScope.launch {
            clipsDao.delete(clip.toClipEntity())
            
            if (clipsState.value.isSearching) {
                searchAndFilter(true)
            }

            notificationsViewModel.postNotification(
                Notification(
                    duration = 6,
                    content = "Deleted ${clip.content.coerce(15)}",
                    type = NotificationType.Info()
                )
            )
        }
    }

    fun togglePinnedClip(clip: Clip) {
        addClip(clip.copy(isPinned = !clip.isPinned, pinnedAt = if (!clip.isPinned) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) else 0L))

        notificationsViewModel.postNotification(
            Notification(
                duration = 6, 
                content = if (clip.isPinned) "Unpinned ${clip.content.coerce(15)}" else "Pinned ${clip.content.coerce(15)}",
                type = NotificationType.Info()
            )
        )
    }

    fun setShowMainApp(value: Boolean) {
        showMain.update {
            value
        }
    }

    fun forceShowMainApp() {
        viewModelScope.launch {
            log("force show app", "forceShowMainApp")
            setShowMainApp(false)

            delay(100)

            setShowMainApp(true)

            delay(100)

            focusWindow()
        }
    }

    fun switchTab(value: Tab) {
        _clipsState.update {
            it.copy(currentTab = value)
        }
    }

    fun deleteAllUnpinned() {
        viewModelScope.launch {
            log("delete all unpinned clips", "deleteAllUnpinned")
            clipsDao.deleteAllUnpinned()

            notificationsViewModel.postNotification(
                Notification(
                    duration = 6,
                    content = "Deleted all unpinned clips",
                    type = NotificationType.Info()
                )
            )
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            log("delete all selected clips", "deleteSelected")
            clipsDao.deleteSelected(
                clipsToDelete = clipsState.value.selectedClips.map {
                    it.clipID
                }
            )

            notificationsViewModel.postNotification(
                Notification(
                    duration = 6,
                    content = "Deleted ${clipsState.value.selectedClips.size} clips",
                    type = NotificationType.Info()
                )
            )

            if (clipsState.value.isSearching) {
                searchAndFilter(true)
            }
        }
    }

    fun deleteSpecified(clips: List<Clip>) {
        viewModelScope.launch {
            log("delete all specified clips", "deleteSpecified")

            notificationsViewModel.postNotification(
                Notification(
                    duration = 6,
                    content = "Deleted ${clips.size} clips",
                    type = NotificationType.Info()
                )
            )
        }
    }

    fun deleteOldUnpinnedClips() {
        viewModelScope.launch {
            val settingsState = settingsViewModel.state.value
            log("delete all unpinned clips older ${settingsState.clipDeleteTime.unit * settingsState.clipDeleteTime.timeCode.secondsPer}", "deleteOldUnpinnedClips")
            clipsDao.deleteOldUnpinnedClips(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), (settingsState.clipDeleteTime.unit * settingsState.clipDeleteTime.timeCode.secondsPer).toLong())
        }
    }

    fun setIsSearching (value: Boolean) {
        _clipsState.update {
            it.copy(isSearching = value)
        }

        if (!value) {
            _clipsState.update {
                it.copy(
                    searchParams = "",
                    selectedClips = emptySet(),
                    isOnGoingSearch = false,
                    protoFilters = Filters(sources = miscViewModel.state.value.allApps.keys),
                    searchFilter = Filters(sources = miscViewModel.state.value.allApps.keys),
                    searchResults = emptyList()
                )
            }
        }
    }

    fun setShowFilters (value: Boolean) {
        _clipsState.update {
            it.copy(isShowingFilters = value)
        }
    }

    fun setSearchParams (value: String) {
        _clipsState.update {
            it.copy(searchParams = value, searchResults = if (value.isBlank()) emptyList() else it.searchResults)
        }

        searchAndFilter()
    }
    
    fun toggleSelectClip(clip: Clip) {
        _clipsState.update {
            if (clip in it.selectedClips) {
                it.copy(selectedClips = it.selectedClips - clip)
            } else {
                it.copy(selectedClips = it.selectedClips + clip)
            }
        }
    }

    fun setSelectedClips (value: Set<Clip>) {
        _clipsState.update {
            it.copy(selectedClips = value)
        }
    }

    fun searchAndFilter (searchWithFilters: Boolean = false) {
        viewModelScope.launch {
            _clipsState.update {
                val customFilterApplied = it.protoFilters.sources.size != miscViewModel.state.value.allApps.size || it.protoFilters.pinState != null || it.protoFilters.types.size != ClipType.entries.size || it.protoFilters.copyTime != null || it.protoFilters.lineCount != null

                it.copy(searchFilter = it.protoFilters, isShowingFilters = false, isSearching = true, customFilterApplied = customFilterApplied, selectedClips = emptySet())
            }

            if (clipsState.value.searchParams.isNotEmpty() || searchWithFilters) {
                println("searching start")

                _clipsState.update {
                    it.copy(isOnGoingSearch = true)
                }
                val searchResults = search(searchParams = clipsState.value.searchParams, filters = clipsState.value.searchFilter, clips = clipsState.value.pinnedClips + clipsState.value.otherClips, allApps = miscViewModel.state.value.allApps)

                delay(300)

                _clipsState.update {
                    it.copy(searchResults = searchResults, isOnGoingSearch = false)
                }

                println("searching end")
            }
        }
    }

    fun setMultiplePinState (clips: List<Clip>, state: Boolean) {
        viewModelScope.launch {
            if (clips.isNotEmpty()) {
                clipsDao.setMultiplePinnedState(clips.map {it.clipID}, state, if (state) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) else 0L)

                notificationsViewModel.postNotification(
                    Notification(
                        duration = 6,
                        content = "${if (state) "Pinned" else "Unpinned"} ${clips.size} clips",
                        type = NotificationType.Info()
                    )
                )
            }
        }
    }

    fun setFilters(filters: Filters) {
        _clipsState.update {
            it.copy(protoFilters = filters)
        }
    }

    fun setSearchFilters(filters: Filters) {
        _clipsState.update {
            it.copy(searchFilter = filters)
        }
    }
}