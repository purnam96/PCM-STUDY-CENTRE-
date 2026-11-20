package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.ForumAuthGateScreen
import com.example.auth.attemptSilentGoogleSignIn
import com.example.auth.authStateFlow
import com.example.auth.signInWithGoogleInteractive
import com.example.auth.signOutGoogle
import com.example.model.AcademicClass
import com.example.model.DiscussionReply
import com.example.model.DiscussionThread
import com.example.model.StudyMaterial
import com.example.model.Subject
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.VioletDark
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletSecondary
import com.example.viewmodel.DiscussionViewModel
import com.example.viewmodel.ForumUiState
import com.example.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Auth-gated entry point for the Cloud Firestore Discussion Board.
 * Prevents any Firestore queries/listeners from running until FirebaseAuth has a non-blank UID,
 * and scopes DiscussionViewModel lifecycle to the active user's UID.
 */
@Composable
fun DiscussionForumRoute(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
    val currentUser by if (auth != null) {
        remember(auth) { auth.authStateFlow() }
            .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    } else {
        remember { mutableStateOf(null) }
    }

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid, auth) {
        if (auth != null && currentUser == null) {
            attemptSilentGoogleSignIn(context, auth)
        }
    }

    val activeUser = currentUser
    if (auth == null || activeUser == null || activeUser.uid.isBlank()) {
        ForumAuthGateScreen(
            isSigningIn = isSigningIn,
            errorMessage = authError ?: if (auth == null) "Firebase Auth is not initialized." else null,
            onGoogleSignInClick = {
                if (auth == null) {
                    authError = "Firebase Auth is not initialized."
                } else {
                    scope.launch {
                        isSigningIn = true
                        authError = null
                        signInWithGoogleInteractive(context, auth).onFailure { err ->
                            authError = err.localizedMessage ?: "Sign-in could not be completed."
                        }
                        isSigningIn = false
                    }
                }
            },
            modifier = modifier
        )
    } else {
        key(activeUser.uid) {
            val discussionViewModel: DiscussionViewModel = viewModel(
                key = activeUser.uid,
                factory = DiscussionViewModel.Factory(context.applicationContext)
            )
            DiscussionForumScreen(
                mainViewModel = mainViewModel,
                discussionViewModel = discussionViewModel,
                currentUser = activeUser,
                onSignOut = {
                    scope.launch {
                        signOutGoogle(context, auth)
                    }
                },
                modifier = modifier
            )
        }
    }
}

/**
 * Real-time Cloud Firestore Peer Discussion Board where authenticated students can browse
 * Physics & Chemistry threads, create new question threads, reply to classmates, upvote helpful
 * posts, and mark accepted solutions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionForumScreen(
    mainViewModel: MainViewModel,
    discussionViewModel: DiscussionViewModel,
    currentUser: FirebaseUser,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val threadsState by discussionViewModel.threadsState.collectAsStateWithLifecycle()
    val activeThreadId by discussionViewModel.activeThreadId.collectAsStateWithLifecycle()
    val activeThread by discussionViewModel.activeThread.collectAsStateWithLifecycle()
    val repliesState by discussionViewModel.repliesState.collectAsStateWithLifecycle()
    val isActiveThreadUpvoted by discussionViewModel.isActiveThreadUpvoted.collectAsStateWithLifecycle()
    val sessionUpvotedThreads by discussionViewModel.sessionUpvotedThreadIds.collectAsStateWithLifecycle()
    val sessionUpvotedReplies by discussionViewModel.sessionUpvotedReplyIds.collectAsStateWithLifecycle()
    val isSubmitting by discussionViewModel.isSubmitting.collectAsStateWithLifecycle()
    val actionMessage by discussionViewModel.actionMessage.collectAsStateWithLifecycle()

    val allMaterials by mainViewModel.materials.collectAsState()
    val studentProfile by mainViewModel.studentProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<Subject?>(null) }
    var solvedFilterOnly by remember { mutableStateOf<Boolean?>(null) }

    var showAskSheet by remember { mutableStateOf(false) }
    val askSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            discussionViewModel.clearActionMessage()
        }
    }

    BackHandler(enabled = activeThreadId != null) {
        discussionViewModel.closeThread()
    }

    val allThreads = (threadsState as? ForumUiState.Success)?.data ?: emptyList()

    val filteredThreads = remember(
        allThreads,
        searchQuery,
        selectedSubjectFilter,
        solvedFilterOnly
    ) {
        allThreads.filter { thread ->
            val matchSubject = selectedSubjectFilter == null || thread.resolvedSubject() == selectedSubjectFilter
            val matchSolved = when (solvedFilterOnly) {
                true -> thread.solved
                false -> !thread.solved
                null -> true
            }
            val matchQuery = if (searchQuery.isBlank()) true else {
                thread.title.contains(searchQuery, ignoreCase = true) ||
                    thread.content.contains(searchQuery, ignoreCase = true) ||
                    thread.chapterName.contains(searchQuery, ignoreCase = true) ||
                    thread.authorName.contains(searchQuery, ignoreCase = true) ||
                    thread.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchSubject && matchSolved && matchQuery
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("discussion_forum_screen"),
        floatingActionButton = {
            if (activeThread == null) {
                FloatingActionButton(
                    onClick = { showAskSheet = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier
                        .testTag("fab_ask_question")
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ask Question"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val selectedThread = activeThread
            if (selectedThread != null) {
                ThreadDetailView(
                    thread = selectedThread,
                    repliesState = repliesState,
                    currentUid = currentUser.uid,
                    isThreadUpvoted = isActiveThreadUpvoted || sessionUpvotedThreads.contains(selectedThread.id),
                    upvotedReplyIds = sessionUpvotedReplies,
                    isSubmitting = isSubmitting,
                    onBack = { discussionViewModel.closeThread() },
                    onUpvoteThread = { discussionViewModel.toggleThreadUpvote(selectedThread.id) },
                    onDeleteThread = { discussionViewModel.deleteThread(selectedThread.id) },
                    onUpvoteReply = { replyId -> discussionViewModel.toggleReplyUpvote(selectedThread.id, replyId) },
                    onDeleteReply = { replyId -> discussionViewModel.deleteReply(selectedThread.id, replyId) },
                    onToggleAcceptedSolution = { reply ->
                        discussionViewModel.toggleAcceptedSolution(selectedThread, reply)
                    },
                    onPostReply = { replyText ->
                        discussionViewModel.postReply(
                            threadId = selectedThread.id,
                            content = replyText,
                            authorName = currentUser.displayName?.ifBlank { null } ?: studentProfile.name,
                            authorSchool = studentProfile.schoolName
                        )
                    },
                    onRetryReplies = { discussionViewModel.retry() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Slate100),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    // Gradient Hero Banner + Account Bar
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(NavyDark, NavyPrimary)
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Connected to Cloud Discussion",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Signed in as ${currentUser.displayName?.ifBlank { null } ?: studentProfile.name}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = CyanAccent,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    TextButton(
                                        onClick = onSignOut,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("btn_forum_sign_out")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = "Sign Out",
                                            tint = Slate300,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sign Out",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate300,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Forum,
                                                contentDescription = null,
                                                tint = CyanAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "PEER STUDY FORUM",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = CyanAccent,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Physics & Chemistry Q&A",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        )
                                        Text(
                                            text = "Live Firestore discussion board for board exam doubts & solutions",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = GoldAccent,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("btn_header_ask_question")
                                            .clickable { showAskSheet = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ask Question",
                                                tint = NavyDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Ask Doubt",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    color = NavyDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CommunityStatCard(
                                        label = "Threads",
                                        value = "${allThreads.size}",
                                        color = PureWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CommunityStatCard(
                                        label = "Solved",
                                        value = "${allThreads.count { it.solved }}",
                                        color = TealAccent,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CommunityStatCard(
                                        label = "Replies",
                                        value = "${allThreads.sumOf { it.repliesCount }}",
                                        color = GoldAccent,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search by topic, question, formula, or chapter...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Forum",
                                        tint = Slate400
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = Slate500
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Slate300,
                                    focusedContainerColor = PureWhite,
                                    unfocusedContainerColor = PureWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forum_search_input")
                            )
                        }
                    }

                    // Filter Chips Row
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedSubjectFilter == null,
                                onClick = { selectedSubjectFilter = null },
                                label = { Text("All Subjects") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_forum_all")
                            )

                            FilterChip(
                                selected = selectedSubjectFilter == Subject.PHYSICS,
                                onClick = {
                                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.PHYSICS) null else Subject.PHYSICS
                                },
                                label = { Text("Physics") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PhysicsBlue,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_forum_physics")
                            )

                            FilterChip(
                                selected = selectedSubjectFilter == Subject.CHEMISTRY,
                                onClick = {
                                    selectedSubjectFilter = if (selectedSubjectFilter == Subject.CHEMISTRY) null else Subject.CHEMISTRY
                                },
                                label = { Text("Chemistry") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChemistryOrange,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_forum_chemistry")
                            )

                            FilterChip(
                                selected = solvedFilterOnly == true,
                                onClick = {
                                    solvedFilterOnly = if (solvedFilterOnly == true) null else true
                                },
                                label = { Text("Solved") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealAccent,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_forum_solved")
                            )

                            FilterChip(
                                selected = solvedFilterOnly == false,
                                onClick = {
                                    solvedFilterOnly = if (solvedFilterOnly == false) null else false
                                },
                                label = { Text("Needs Help") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletSecondary,
                                    selectedLabelColor = PureWhite
                                ),
                                modifier = Modifier.testTag("filter_forum_unsolved")
                            )
                        }
                    }

                    // Feed Header
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Discussions (${filteredThreads.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Live Sync • Latest First",
                                style = MaterialTheme.typography.labelMedium.copy(color = Slate500)
                            )
                        }
                    }

                    // State Handling: Loading / Error / Empty / Threads List
                    when (val state = threadsState) {
                        is ForumUiState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp)
                                        .testTag("forum_loading_state"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = NavyPrimary)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Syncing Physics & Chemistry threads...",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                                        )
                                    }
                                }
                            }
                        }

                        is ForumUiState.Error -> {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .testTag("forum_error_state")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Could not load discussions",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        OutlinedButton(
                                            onClick = { discussionViewModel.retry() },
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("btn_retry_forum")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Retry", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        is ForumUiState.Success -> {
                            if (filteredThreads.isEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .testTag("forum_empty_state")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Slate100,
                                                modifier = Modifier.size(54.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.HelpOutline,
                                                        contentDescription = null,
                                                        tint = Slate400,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = if (allThreads.isEmpty()) "Start the First Thread" else "No Matching Threads",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Post a Physics or Chemistry question and collaborate with peers!",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { showAskSheet = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .testTag("btn_empty_ask_question")
                                                    .height(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Ask a Question", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(filteredThreads, key = { it.id }) { thread ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DiscussionThreadCard(
                                        thread = thread,
                                        isAuthor = thread.authorId == currentUser.uid,
                                        isUpvotedInSession = sessionUpvotedThreads.contains(thread.id),
                                        onOpenDetail = { discussionViewModel.openThread(thread.id) },
                                        onToggleUpvote = { discussionViewModel.toggleThreadUpvote(thread.id) },
                                        onDelete = { discussionViewModel.deleteThread(thread.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAskSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!isSubmitting) showAskSheet = false },
            sheetState = askSheetState,
            containerColor = PureWhite,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            AskQuestionSheetContent(
                availableMaterials = allMaterials,
                defaultClass = studentProfile.selectedClass,
                isSubmitting = isSubmitting,
                onSubmit = { title, content, subject, academicClass, chapter, tags ->
                    discussionViewModel.createThread(
                        title = title,
                        content = content,
                        subject = subject,
                        academicClass = academicClass,
                        chapterName = chapter,
                        tags = tags,
                        authorName = currentUser.displayName?.ifBlank { null } ?: studentProfile.name,
                        authorSchool = studentProfile.schoolName,
                        onSuccess = { newThreadId ->
                            showAskSheet = false
                            discussionViewModel.openThread(newThreadId)
                        }
                    )
                },
                onCancel = { if (!isSubmitting) showAskSheet = false }
            )
        }
    }
}

@Composable
private fun CommunityStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PureWhite.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun DiscussionThreadCard(
    thread: DiscussionThread,
    isAuthor: Boolean,
    isUpvotedInSession: Boolean,
    onOpenDetail: () -> Unit,
    onToggleUpvote: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = thread.resolvedSubject()
    val academicClass = thread.resolvedClass()
    val subjectColor = if (subject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .testTag("question_card_${thread.id}")
            .clickable { onOpenDetail() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = subjectColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = subject.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = subjectColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = academicClass.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate600,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (thread.solved) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Solved",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Solved",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    if (isAuthor) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_delete_thread_${thread.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Thread",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = thread.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 22.sp
                )
            )

            Text(
                text = "Chapter: ${thread.chapterName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate500,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = thread.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate700,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (thread.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    thread.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Slate100
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate600,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = Slate200, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NavyLight,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = thread.authorName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = thread.authorName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Slate700,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dateLabel = formatTimestamp(thread.createdAt?.toDate())
                        if (dateLabel.isNotBlank()) {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUpvotedInSession) TealAccent.copy(alpha = 0.15f) else Slate100,
                        modifier = Modifier
                            .clickable { onToggleUpvote() }
                            .testTag("btn_upvote_q_${thread.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isUpvotedInSession) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote",
                                tint = if (isUpvotedInSession) TealAccent else Slate600,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${thread.upvotesCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isUpvotedInSession) TealAccent else Slate700,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VioletLight,
                        modifier = Modifier
                            .clickable { onOpenDetail() }
                            .testTag("btn_replies_${thread.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Replies",
                                tint = VioletDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${thread.repliesCount} Replies",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = VioletDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadDetailView(
    thread: DiscussionThread,
    repliesState: ForumUiState<List<DiscussionReply>>,
    currentUid: String,
    isThreadUpvoted: Boolean,
    upvotedReplyIds: Set<String>,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onUpvoteThread: () -> Unit,
    onDeleteThread: () -> Unit,
    onUpvoteReply: (String) -> Unit,
    onDeleteReply: (String) -> Unit,
    onToggleAcceptedSolution: (DiscussionReply) -> Unit,
    onPostReply: (String) -> Unit,
    onRetryReplies: () -> Unit,
    modifier: Modifier = Modifier
) {
    var replyText by remember { mutableStateOf("") }
    val subject = thread.resolvedSubject()
    val academicClass = thread.resolvedClass()
    val subjectColor = if (subject == Subject.PHYSICS) PhysicsBlue else ChemistryOrange
    val isThreadAuthor = thread.authorId == currentUid
    val repliesList = (repliesState as? ForumUiState.Success)?.data ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
            .testTag("forum_detail_view")
    ) {
        Surface(
            color = NavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_back_to_forum")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Forum",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Problem Discussion",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${subject.displayName} • ${academicClass.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent, fontSize = 11.sp)
                        )
                    }
                }

                if (isThreadAuthor) {
                    IconButton(
                        onClick = onDeleteThread,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_detail_delete_thread")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Thread",
                            tint = Slate300
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = subjectColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = thread.chapterName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = subjectColor,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            if (thread.solved) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Solved",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Solved",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = thread.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                lineHeight = 26.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = thread.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate800,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = NavyLight,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = thread.authorName.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = thread.authorName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                    val subline = listOfNotNull(
                                        thread.authorSchool.takeIf { it.isNotBlank() },
                                        formatTimestamp(thread.createdAt?.toDate()).takeIf { it.isNotBlank() }
                                    ).joinToString(" • ")
                                    if (subline.isNotBlank()) {
                                        Text(
                                            text = subline,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate500,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isThreadUpvoted) TealAccent.copy(alpha = 0.15f) else Slate100,
                                modifier = Modifier
                                    .clickable { onUpvoteThread() }
                                    .testTag("btn_detail_upvote_question")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isThreadUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Upvote",
                                        tint = if (isThreadUpvoted) TealAccent else Slate600,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${thread.upvotesCount} Upvotes",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isThreadUpvoted) TealAccent else Slate700,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Peer Replies (${repliesList.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = if (isThreadAuthor) "Tap Mark Solution on best answer" else "Live Peer Answers",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }
            }

            when (repliesState) {
                is ForumUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NavyPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                is ForumUiState.Error -> {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = repliesState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRetryReplies) {
                                    Text("Retry Loading Replies")
                                }
                            }
                        }
                    }
                }

                is ForumUiState.Success -> {
                    if (repliesList.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No replies posted yet",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Share your derivation or explanation below to help a classmate!",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(repliesList, key = { it.id }) { reply ->
                            Spacer(modifier = Modifier.height(10.dp))
                            ReplyCard(
                                reply = reply,
                                isThreadAuthor = isThreadAuthor,
                                isReplyAuthor = reply.authorId == currentUid,
                                isUpvotedInSession = upvotedReplyIds.contains(reply.id),
                                onUpvote = { onUpvoteReply(reply.id) },
                                onToggleAccept = { onToggleAcceptedSolution(reply) },
                                onDelete = { onDeleteReply(reply.id) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Compose Reply Area
        Surface(
            color = PureWhite,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = "Quick Equation Insert:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickFormulas = listOf(
                        "1/f = 1/v + 1/u",
                        "V = I·R",
                        "P = V·I",
                        "2H₂ + O₂ → 2H₂O",
                        "pH = -log[H⁺]",
                        "Fe + CuSO₄ → FeSO₄ + Cu",
                        "F = m·a"
                    )
                    quickFormulas.forEach { formula ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate100,
                            modifier = Modifier.clickable {
                                replyText = if (replyText.isBlank()) formula else "$replyText $formula"
                            }
                        ) {
                            Text(
                                text = formula,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate700,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { if (it.length <= 3000) replyText = it },
                        placeholder = { Text("Write a reply or step-by-step solution...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate300
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_peer_reply")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank() && !isSubmitting) {
                                onPostReply(replyText)
                                replyText = ""
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (replyText.isNotBlank() && !isSubmitting) NavyPrimary else Slate200)
                            .testTag("btn_post_peer_reply")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Post Reply",
                                tint = if (replyText.isNotBlank()) PureWhite else Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyCard(
    reply: DiscussionReply,
    isThreadAuthor: Boolean,
    isReplyAuthor: Boolean,
    isUpvotedInSession: Boolean,
    onUpvote: () -> Unit,
    onToggleAccept: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reply.acceptedSolution) TealAccent.copy(alpha = 0.06f) else PureWhite
        ),
        border = if (reply.acceptedSolution) {
            BorderStroke(1.5.dp, TealAccent)
        } else {
            BorderStroke(0.8.dp, Slate200)
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reply_card_${reply.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (reply.acceptedSolution) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TealAccent,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Accepted Solution",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Accepted Solution",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VioletSecondary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = reply.authorName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = reply.authorName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = VioletLight
                            ) {
                                Text(
                                    text = reply.authorBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = VioletDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val subline = listOfNotNull(
                            reply.authorSchool.takeIf { it.isNotBlank() },
                            formatTimestamp(reply.createdAt?.toDate()).takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (subline.isNotBlank()) {
                            Text(
                                text = subline,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 10.sp)
                            )
                        }
                    }
                }

                if (isThreadAuthor) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (reply.acceptedSolution) TealAccent.copy(alpha = 0.15f) else Slate100,
                        modifier = Modifier
                            .clickable { onToggleAccept() }
                            .testTag("btn_accept_solution_${reply.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Accept Solution",
                                tint = if (reply.acceptedSolution) TealAccent else Slate500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (reply.acceptedSolution) "Accepted" else "Mark Solution",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (reply.acceptedSolution) TealAccent else Slate700,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate800,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isReplyAuthor) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_delete_reply_${reply.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Reply",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUpvotedInSession) TealAccent.copy(alpha = 0.15f) else Slate100,
                    modifier = Modifier
                        .clickable { onUpvote() }
                        .testTag("btn_upvote_reply_${reply.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isUpvotedInSession) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Helpful",
                            tint = if (isUpvotedInSession) TealAccent else Slate600,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reply.upvotesCount} Helpful",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isUpvotedInSession) TealAccent else Slate700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskQuestionSheetContent(
    availableMaterials: List<StudyMaterial>,
    defaultClass: AcademicClass,
    isSubmitting: Boolean,
    onSubmit: (title: String, content: String, subject: Subject, academicClass: AcademicClass, chapter: String, tags: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var subject by remember { mutableStateOf(Subject.PHYSICS) }
    var targetClass by remember { mutableStateOf(defaultClass) }

    val chaptersForSubject = remember(availableMaterials, subject, targetClass) {
        availableMaterials
            .filter { it.subject == subject && it.academicClass == targetClass }
            .map { it.chapterName }
            .distinct()
            .ifEmpty {
                if (subject == Subject.PHYSICS) {
                    listOf("Light - Reflection and Refraction", "Electricity & Circuits", "Magnetic Effects of Electric Current")
                } else {
                    listOf("Chemical Reactions and Equations", "Acids, Bases and Salts", "Metals and Non-Metals")
                }
            }
    }

    var selectedChapter by remember { mutableStateOf(chaptersForSubject.firstOrNull() ?: "General Syllabus") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    LaunchedEffect(subject, targetClass) {
        val newChapters = availableMaterials
            .filter { it.subject == subject && it.academicClass == targetClass }
            .map { it.chapterName }
            .distinct()
        if (newChapters.isNotEmpty()) {
            selectedChapter = newChapters.first()
        }
    }

    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("ask_question_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "New Discussion Thread",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Post a Physics or Chemistry question for peer solutions",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = Slate400)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Subject",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Subject.entries.forEach { subj ->
                val isSelected = subject == subj
                val color = if (subj == Subject.PHYSICS) PhysicsBlue else ChemistryOrange
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) color else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { subject = subj }
                        .testTag("sheet_forum_subject_${subj.name.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = subj.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isSelected) PureWhite else Slate700,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Class Level",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AcademicClass.entries.forEach { cls ->
                val isSelected = targetClass == cls
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NavyPrimary else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { targetClass = cls }
                        .testTag("sheet_forum_class_${cls.name.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = cls.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isSelected) PureWhite else Slate700,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Syllabus Chapter",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = chapterDropdownExpanded,
            onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedChapter,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Slate300
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("dropdown_forum_chapter")
            )

            ExposedDropdownMenu(
                expanded = chapterDropdownExpanded,
                onDismissRequest = { chapterDropdownExpanded = false }
            ) {
                chaptersForSubject.forEach { ch ->
                    DropdownMenuItem(
                        text = { Text(ch, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            selectedChapter = ch
                            chapterDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Question Title (${title.length}/160)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 160) title = it },
            placeholder = { Text("e.g., Why is a convex lens called a converging lens?") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Slate300
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_forum_title")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Question Details & Steps Attempted",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { if (it.length <= 4000) content = it },
            placeholder = { Text("Describe the concept, numerical values, or formula steps you tried...") },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Slate300
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_forum_content")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tags (up to 5, comma separated)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            placeholder = { Text("e.g., optics, numericals, bosem-2024") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Slate300
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_forum_tags")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && content.isNotBlank() && !isSubmitting) {
                    val parsedTags = tagsInput.split(",")
                        .map { it.trim().take(32) }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(5)
                    onSubmit(title, content, subject, targetClass, selectedChapter, parsedTags)
                }
            },
            enabled = title.isNotBlank() && content.isNotBlank() && !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_forum_question")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = PureWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Posting Thread...",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PureWhite)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post Thread to Board",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PureWhite)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatTimestamp(date: Date?): String {
    if (date == null) return "Just now"
    val diffMs = System.currentTimeMillis() - date.time
    val minutes = diffMs / 60_000L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 24 * 60 -> "${minutes / 60}h ago"
        else -> SimpleDateFormat("MMM d", Locale.US).format(date)
    }
}
