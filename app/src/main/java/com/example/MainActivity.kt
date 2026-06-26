package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.JournalEntry
import com.example.ui.Nook
import com.example.ui.StudyTab
import com.example.ui.StudyViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userStats by viewModel.userStats.collectAsState()
            val isDarkTheme = userStats?.isDarkTheme ?: false

            MyApplicationTheme(darkTheme = isDarkTheme) {
                StudySpaceApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySpaceApp(viewModel: StudyViewModel) {
    val context = LocalContext.current
    val userStats by viewModel.userStats.collectAsState()
    val journalEntries by viewModel.allJournalEntries.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var activePreStudyNook by remember { mutableStateOf<Nook?>(null) }

    val currentTab = viewModel.currentTab
    val activeNook = viewModel.activeNook

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!viewModel.showSessionSummary) {
                BottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == StudyTab.SANCTUARY && viewModel.activeNook == null) {
                            Toast.makeText(context, "Select a room first!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.selectTab(tab)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Main Screen Switcher
            when (currentTab) {
                StudyTab.LIBRARY -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onEnterNookClicked = { nook ->
                            activePreStudyNook = nook
                        },
                        onEditProfileClicked = { showEditProfileDialog = true }
                    )
                }
                StudyTab.SANCTUARY -> {
                    SanctuaryScreen(
                        viewModel = viewModel,
                        onExitNookClicked = { viewModel.selectNook(null) }
                    )
                }
                StudyTab.CHRONICLE -> {
                    ChronicleScreen(
                        viewModel = viewModel,
                        journalEntries = journalEntries,
                        onEditProfileClicked = { showEditProfileDialog = true }
                    )
                }
            }

            // Room Setup bottom dialog / modal
            activePreStudyNook?.let { nook ->
                RoomSetupDialog(
                    nook = nook,
                    onDismiss = { activePreStudyNook = null },
                    onStartFocus = { microIntention, subject ->
                        viewModel.microIntention = microIntention
                        viewModel.selectedSubject = subject
                        viewModel.selectNook(nook)
                        activePreStudyNook = null
                    }
                )
            }

            // Profile Edit Dialog
            if (showEditProfileDialog) {
                ProfileEditDialog(
                    currentUsername = userStats?.username ?: "Arlo",
                    onDismiss = { showEditProfileDialog = false },
                    onSave = { newName ->
                        viewModel.updateUsername(newName)
                        showEditProfileDialog = false
                    }
                )
            }

            // Pomodoro Completed Overlay Summary
            AnimatedVisibility(
                visible = viewModel.showSessionSummary,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                SessionSummaryOverlay(
                    viewModel = viewModel,
                    onReturnClicked = {
                        viewModel.closeSummary()
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. LIBRARY SCREEN
// ==========================================
@Composable
fun LibraryScreen(
    viewModel: StudyViewModel,
    onEnterNookClicked: (Nook) -> Unit,
    onEditProfileClicked: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val filteredNooks = viewModel.getFilteredNooks()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Custom Styled Header matching mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar Clickable to edit profile
            Row(
                modifier = Modifier
                    .clickable { onEditProfileClicked() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCS2vM_U17_kOXdEZaVLZE9I2TOvj70afCRko7YEJp-il1y7hbOQASfnrQAgq_A_Nw5-rGXo9Sp3y0Q-aPudadHYD-NqR9ajZCmSYh6Vl5HS0tKggjE-HvKn7nE7jthR2d3tBR3mWV_iJVb3csiSR0QKAYgXMM89wIf4E1nHUQB09qrVIx-ONag_k-YU96rUQ7OewpQzYRb-MOzx9c9qw6LPLfAMP3WISdYRaQhL_hEQMZtl_qLOSxPx5QmcTTw5Nw26HakQWDnX_ni",
                        contentDescription = "User Avatar",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Hello,",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userStats?.username ?: "Arlo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // App Brand Name in display serif
            Text(
                text = "StudySpace",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("app_brand_logo")
            )

            // Dynamic light/dark theme switch and edit button
            Row {
                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Text(
                        text = if (userStats?.isDarkTheme == true) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }
                IconButton(onClick = onEditProfileClicked) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Hero Title
        Text(
            text = "Find Your Nook",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Typewriter Ledger Bottom-Border Input style search bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 24.dp)
                .border(
                    width = 0.dp,
                    color = Color.Transparent
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehindBottomBorder(MaterialTheme.colorScheme.outline)
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (viewModel.searchQuery.isEmpty()) {
                        Text(
                            text = "Finding a specific nook...",
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nook_search_bar"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Search",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Rooms Polaroid Grid / List
        if (filteredNooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🕯️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No nooks found.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredNooks) { nook ->
                    PolaroidRoomCard(
                        nook = nook,
                        onEnterClick = { onEnterNookClicked(nook) }
                    )
                }
            }
        }
    }
}

// Custom Draw Helper for bottom border on inputs
@Composable
fun Modifier.drawBehindBottomBorder(color: Color): Modifier {
    return this.drawBehind {
        val strokeWidth = 1.dp.toPx()
        val y = size.height - strokeWidth / 2
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, y),
            end = androidx.compose.ui.geometry.Offset(size.width, y),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun PolaroidRoomCard(nook: Nook, onEnterClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEnterClick() }
            .testTag("nook_card_${nook.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Styled Clean Image Frame with Rounded Corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = nook.imageUrl,
                    contentDescription = nook.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Headcount Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥  ${nook.presentCount}/${nook.maxPresent} present",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Featured Badge
                if (nook.isFeatured) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Featured",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = nook.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📚 ${nook.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ambient Audio badge
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎧 ${nook.ambientSound}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ROOM SETUP MODAL / DIALOG
// ==========================================
@Composable
fun RoomSetupDialog(
    nook: Nook,
    onDismiss: () -> Unit,
    onStartFocus: (String, String) -> Unit
) {
    var microIntention by remember { mutableStateOf("Drafting Intro") }
    var selectedSubject by remember { mutableStateOf("Design") }
    val subjects = listOf("Mathematics", "Physics", "Design", "General")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Study Session",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Entering: ${nook.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Input field for Micro-Intention
                Text(
                    text = "What is your micro-intention?",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = microIntention,
                    onValueChange = { microIntention = it },
                    placeholder = { Text("e.g. Drafting Intro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("micro_intention_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Choose Subject Chips
                Text(
                    text = "Select a Subject Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        val isSelected = selectedSubject == subject
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedSubject = subject }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subject,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onStartFocus(microIntention, selectedSubject) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_enter_nook"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Enter Room")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. SANCTUARY SCREEN (TIMER & MIXER)
// ==========================================
@Composable
fun SanctuaryScreen(
    viewModel: StudyViewModel,
    onExitNookClicked: () -> Unit
) {
    val activeNook = viewModel.activeNook ?: return
    val userStats by viewModel.userStats.collectAsState()

    // Animation transition variables for Pomodoro Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Format Pomodoro Time MM:SS
    val minutes = viewModel.timerSecondsRemaining / 60
    val seconds = viewModel.timerSecondsRemaining % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App header inside Sanctuary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExitNookClicked,
                    modifier = Modifier.testTag("exit_sanctuary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Room",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = activeNook.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Skip button for easy playtesting!
                Button(
                    onClick = { viewModel.skipTimer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Skip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Giant countdown timer
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { viewModel.toggleTimer() }
                    .testTag("focus_timer_tap_zone")
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scaleAndPulse(if (viewModel.isTimerRunning) pulseScale else 1f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 44.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("countdown_timer_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (viewModel.isTimerRunning) "Studying..." else "Click to Start",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (viewModel.isTimerRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plant status pill
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🪴  The Oak is Thriving",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Peer virtual study grid matching mockup
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sanctuary Cohort",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "6 Present",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display Cohort Peers (slightly tilted as polaroid cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudyPeerCard(
                        name = "Arlo",
                        task = "Drafting Intro",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAKRBiryvTX16I2pgENcFBqaDmVBLGfRPm_IDaMDtciVP2wju8L3XJPIaMqWuzK_qysQoSAZBR3tJyrqSztCfSpE_Niexr8BGHdDaLYAbaTldXGBJ9cG6qUyRwXCHmpC9VNdssu6fkA8feSV0WU40uk7iMX3AUp7s695Qvgl8rG3t0AXfYBsdLMFmvfe10GuRSf8qVMt6GF5YFllc1t2QeaOzthkh6L3V0zzXSSd7i51pMG5Gl5ATH8zTgoLcxQHruM4wegRXxsPlp6",
                        rotation = -2f,
                        modifier = Modifier.weight(1f)
                    )
                    StudyPeerCard(
                        name = "Clara",
                        task = "Math Sets",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA-d6NLzMoAS0pJFxmP98bSrjL6imd2xJ6aXbMPZ22KqSdxofNMbwuzPaRlJyX03sWbuAng_RdbdqxAetkYzrrtwcWlwVUoONIk6XSlzzrz_CdY6ZRZfs7dnfylpo8tEud0kbuZ5Qe6S7c-kspVZ2eNZaVisZ7BVfZPqsC4Ll_wZaiwS6-KsKEQFPWuZifqcqHOEXtCW5CN8gUaoNAkw4RbLWjU4AZZx34em6Ccfo3AqiYmiWGrC1qleXQR0Ivu9fIUFjKrLsmUcpag",
                        rotation = 1f,
                        modifier = Modifier.weight(1f)
                    )
                    StudyPeerCard(
                        name = "Felix",
                        task = "Reading Ch 4",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCKroXNhM2CsbuOOl9Llh7zB8NW3wfISHnsDSJOVlPSFx2MaBQc8JV8kb5QCvSog2BgQKClW7z7IKZkUuel9nt3ss7SjmkaeuuWsjKY7ruAIXcn6zx7G-4xtX0LzogN14Vnbk5lZbk61DKn2nseVdnkuRD5Gu9MuJp-cdoZln3UEdbAfYExdF6owew6pL_3rZfuX8jz9ZTgf1Ud_YTRaD-IGeyrrbP2wGP0kcHJvIuV7u2o_nyaDxWO2NT1on23_jwzQJ2eQBDqIVOv",
                        rotation = -1f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Atmosphere volume slider controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Atmosphere",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Lo-Fi Slider
                    AtmosphereSliderRow(
                        iconString = "🎧",
                        label = "Lo-Fi",
                        value = viewModel.volumeLofi,
                        onValueChange = { viewModel.volumeLofi = it }
                    )

                    // Rain Slider
                    AtmosphereSliderRow(
                        iconString = "🌧️",
                        label = "Rain",
                        value = viewModel.volumeRain,
                        onValueChange = { viewModel.volumeRain = it }
                    )

                    // Cafe Slider
                    AtmosphereSliderRow(
                        iconString = "☕",
                        label = "Cafe",
                        value = viewModel.volumeCafe,
                        onValueChange = { viewModel.volumeCafe = it }
                    )
                }
            }
        }
    }
}

// ScaleModifier to apply pulse scale
fun Modifier.scaleAndPulse(scale: Float): Modifier = this.then(
    Modifier.rotate(scale * 2f - 2f) // subtle rotational play too!
)

@Composable
fun StudyPeerCard(
    name: String,
    task: String,
    imageUrl: String,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AtmosphereSliderRow(
    iconString: String,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = iconString, fontSize = 20.sp, modifier = Modifier.width(24.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .testTag("volume_slider_${label.lowercase()}"),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

// ==========================================
// 4. CHRONICLE SCREEN (STATS & JOURNAL)
// ==========================================
@Composable
fun ChronicleScreen(
    viewModel: StudyViewModel,
    journalEntries: List<JournalEntry>,
    onEditProfileClicked: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()

    // Calculate subject stats dynamically from the journal
    val totalSessions = journalEntries.size.coerceAtLeast(1)
    val mathCount = journalEntries.count { it.subject.equals("Mathematics", ignoreCase = true) }
    val physicsCount = journalEntries.count { it.subject.equals("Physics", ignoreCase = true) }
    val designCount = journalEntries.count { it.subject.equals("Design", ignoreCase = true) }
    val generalCount = journalEntries.count { it.subject.equals("General", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App header inside Chronicle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onEditProfileClicked() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAucW5onQqCTqmBHfkQgDYF85mzgZqpR1IWQNWx2zvDE_HNPG90tEl2OEwFGE8KpS1oqBeGz5W5wQK5BkOoClH4-1OlDtZLVNyAI5slrSXBE9fVpopOZ4-uNJtvD1572DbN8-T9a1KVFwpAyYZ9O0paljq96XoLrhRUh_iQdmLTfSFYujj3AGWVOzxznsClWjWz838zhmm_kKi9W6DZt02Bm60CbCVex2YVm1MFghsO260WPVStSPjRWVmH17z8Zio6oPR54FAMGhHc",
                            contentDescription = "User Portrait",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The Chronicle",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("chronicle_header_title")
                    )
                }

                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Text(
                        text = if (userStats?.isDarkTheme == true) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Profile Portrait Frame Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Polaroid profile avatar
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDR6-ZUiCYeuiTAD8OjXIjFceEN7lrNitpBjKPqRjFkuEgWngjCs8xvziRC32kvKwUSxq9Klwehlk-f70ofr-pdtUGXDHK5dwpBGlYfHjG0Xe_GuCooIf3Ur3AiVssI1Nc73TFK1eBfRHcnEdQmn5sE-2TKapAyNsneScICy3tpprtcAPMbVNpDE1Fd5O0YesbmamFpELmLwS8DpePpgcsctNSCtFD_jdPUm_BVrHnlHXBkHQcW58o8eh2X1vZtAm-Nn_FB-mdi233h",
                                contentDescription = "Polaroid Profile image",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userStats?.username ?: "Arlo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Stats Right Hand Column matching mockup
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Streak Pill Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CURRENT STREAK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${userStats?.streakDays ?: 12}-Day Focus Streak",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Available Ink Pill Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✒️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AVAILABLE INK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${userStats?.inkDrops ?: 742} Drops",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Stats Overview cards
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Focus Metrics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        label = "DAILY AVERAGE",
                        value = "${userStats?.dailyAverageMinutes ?: 140}m",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "TOTAL FOCUS",
                        value = "${userStats?.totalFocusHours ?: 84}h",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "ROOMS VISITED",
                        value = "${userStats?.roomsVisited ?: 12}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Subject Distribution bar chart matching mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Subject Distribution",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Mathematics
                    SubjectDistributionRow(
                        subjectName = "Mathematics",
                        percentage = (mathCount * 100 / totalSessions).coerceAtLeast(10),
                        displayValue = "${mathCount * 25}h"
                    )

                    // Physics
                    SubjectDistributionRow(
                        subjectName = "Physics",
                        percentage = (physicsCount * 100 / totalSessions).coerceAtLeast(10),
                        displayValue = "${physicsCount * 25}h"
                    )

                    // Design
                    SubjectDistributionRow(
                        subjectName = "Design",
                        percentage = (designCount * 100 / totalSessions).coerceAtLeast(10),
                        displayValue = "${designCount * 25}h"
                    )
                }
            }
        }

        // Weekly Journey Calendar matching mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Weekly Journey",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        days.forEachIndexed { index, day ->
                            // Seed checks on mon, tue, wed like the mockup, plus check for current day!
                            val isChecked = index < 3 || (journalEntries.isNotEmpty() && index == 3)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // The Journal activity log list
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "The Journal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    journalEntries.forEach { entry ->
                        JournalEntryRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubjectDistributionRow(
    subjectName: String,
    percentage: Int,
    displayValue: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = subjectName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage.toFloat() / 100f).coerceIn(0.1f, 1f))
                    .background(
                        color = if (subjectName == "Mathematics") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

@Composable
fun JournalEntryRow(entry: JournalEntry) {
    // Format timestamp to Month string and Day
    val date = Date(entry.timestamp)
    val monthFormat = SimpleDateFormat("MMM", Locale.US)
    val dayFormat = SimpleDateFormat("dd", Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Calendar-style Date box
                Card(
                    modifier = Modifier.width(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = monthFormat.format(date).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dayFormat.format(date),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Room and intention details
                Column {
                    Text(
                        text = entry.nookName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "✍️ ${entry.microIntention} (${entry.subject})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Duration badge
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ ${entry.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==========================================
// 5. SESSION SUMMARY FULL-SCREEN OVERLAY
// ==========================================
@Composable
fun SessionSummaryOverlay(
    viewModel: StudyViewModel,
    onReturnClicked: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)) // Beautiful translucent shadow overlay
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(28.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Focus Complete",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Duration and Stats Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Time Card
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(115.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${viewModel.sessionCompletedDuration}:00",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FOCUS DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Ink & Streak updates
                Column(
                    modifier = Modifier.weight(1.8f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ink earned
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✒️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Ink Earned", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "+${viewModel.sessionEarnedDrops} Drops", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Streak
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Streak", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "12 Days", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Room Plant status card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪴", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Room Plant", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = "The Oak is Thriving",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }

            // Micro-intention review
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MICRO-INTENTION REVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${viewModel.microIntention}\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Primary buttons
            Button(
                onClick = onReturnClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("return_to_library_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Return to Library", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .clickable {
                        Toast.makeText(context, "Shared to Chronicle!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📤  Share to Chronicle", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ==========================================
// 6. PROFILE EDIT DIALOG
// ==========================================
@Composable
fun ProfileEditDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Nickname",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("edit_nickname_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (username.isNotBlank()) onSave(username) },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("save_nickname_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. BOTTOM NAVIGATION BAR
// ==========================================
@Composable
fun BottomNavBar(
    currentTab: StudyTab,
    onTabSelected: (StudyTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Library
            BottomNavItem(
                iconString = "📚",
                label = "Library",
                isSelected = currentTab == StudyTab.LIBRARY,
                onClick = { onTabSelected(StudyTab.LIBRARY) },
                modifier = Modifier.testTag("library_nav_tab")
            )

            // Tab 2: Sanctuary
            BottomNavItem(
                iconString = "🧘",
                label = "Sanctuary",
                isSelected = currentTab == StudyTab.SANCTUARY,
                onClick = { onTabSelected(StudyTab.SANCTUARY) },
                modifier = Modifier.testTag("sanctuary_nav_tab")
            )

            // Tab 3: Chronicle
            BottomNavItem(
                iconString = "📜",
                label = "Chronicle",
                isSelected = currentTab == StudyTab.CHRONICLE,
                onClick = { onTabSelected(StudyTab.CHRONICLE) },
                modifier = Modifier.testTag("chronicle_nav_tab")
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconString: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconString, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
