package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.EduRepository
import com.example.ui.screens.ai.AIChatbotScreen
import com.example.ui.screens.admin.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.student.*
import com.example.ui.screens.teacher.*
import com.example.ui.theme.RiskHighRed
import com.example.ui.theme.RiskLowGreen

enum class AppScreen {
    // Shared
    LOGIN,
    ASK_AI,

    // Student
    STUDENT_DASHBOARD,
    STUDENT_ATTENDANCE,
    STUDENT_MARKSHEET,
    STUDENT_FEES,
    STUDENT_TIMETABLE,
    STUDENT_CALENDAR,
    STUDENT_AI_INSIGHTS,

    // Teacher
    TEACHER_DASHBOARD,
    TEACHER_ATTENDANCE,
    TEACHER_MARKS,
    TEACHER_ANALYTICS,
    TEACHER_TIMETABLE,

    // Admin
    ADMIN_DASHBOARD,
    ADMIN_USERS,
    ADMIN_TIMETABLE,
    ADMIN_CALENDAR,
    ADMIN_FEES,
    ADMIN_ANALYTICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduPredictApp(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val currentUser by EduRepository.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.STUDENT_DASHBOARD) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Synchronize default screen based on role
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user == null) {
            currentScreen = AppScreen.LOGIN
        } else {
            currentScreen = when (user.role) {
                UserRole.STUDENT -> AppScreen.STUDENT_DASHBOARD
                UserRole.TEACHER -> AppScreen.TEACHER_DASHBOARD
                UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
            }
        }
    }

    if (currentUser == null || currentScreen == AppScreen.LOGIN) {
        LoginScreen(
            onLoginSuccess = {
                val user = EduRepository.currentUser.value
                currentScreen = when (user?.role) {
                    UserRole.STUDENT -> AppScreen.STUDENT_DASHBOARD
                    UserRole.TEACHER -> AppScreen.TEACHER_DASHBOARD
                    UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
                    else -> AppScreen.STUDENT_DASHBOARD
                }
            }
        )
        return
    }

    val user = currentUser!!

    // Back handler for secondary screens
    val defaultHome = when (user.role) {
        UserRole.STUDENT -> AppScreen.STUDENT_DASHBOARD
        UserRole.TEACHER -> AppScreen.TEACHER_DASHBOARD
        UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
    }

    if (currentScreen != defaultHome) {
        BackHandler {
            currentScreen = defaultHome
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "EduPredict",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${user.role.name} • ${user.id}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Notification Bell with badge
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        BadgedBox(
                            badge = {
                                Badge { Text("3") }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Dark / Light Theme Toggle
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // User Profile & Logout
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                when (user.role) {
                    UserRole.STUDENT -> {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STUDENT_DASHBOARD,
                            onClick = { currentScreen = AppScreen.STUDENT_DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Home", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_student_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STUDENT_ATTENDANCE,
                            onClick = { currentScreen = AppScreen.STUDENT_ATTENDANCE },
                            icon = { Icon(Icons.Default.EventAvailable, contentDescription = null) },
                            label = { Text("Attendance", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_student_attendance")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STUDENT_MARKSHEET,
                            onClick = { currentScreen = AppScreen.STUDENT_MARKSHEET },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            label = { Text("Marksheet", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_student_marks")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.STUDENT_AI_INSIGHTS,
                            onClick = { currentScreen = AppScreen.STUDENT_AI_INSIGHTS },
                            icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                            label = { Text("AI Insights", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_student_ai")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ASK_AI,
                            onClick = { currentScreen = AppScreen.ASK_AI },
                            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                            label = { Text("Ask AI", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_student_chat")
                        )
                    }

                    UserRole.TEACHER -> {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TEACHER_DASHBOARD,
                            onClick = { currentScreen = AppScreen.TEACHER_DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Home", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_teacher_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TEACHER_ATTENDANCE,
                            onClick = { currentScreen = AppScreen.TEACHER_ATTENDANCE },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            label = { Text("Attendance", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_teacher_attendance")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TEACHER_MARKS,
                            onClick = { currentScreen = AppScreen.TEACHER_MARKS },
                            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            label = { Text("Marks Entry", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_teacher_marks")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TEACHER_ANALYTICS,
                            onClick = { currentScreen = AppScreen.TEACHER_ANALYTICS },
                            icon = { Icon(Icons.Default.Insights, contentDescription = null) },
                            label = { Text("Analytics", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_teacher_analytics")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ASK_AI,
                            onClick = { currentScreen = AppScreen.ASK_AI },
                            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                            label = { Text("Ask AI", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_teacher_chat")
                        )
                    }

                    UserRole.ADMIN -> {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ADMIN_DASHBOARD,
                            onClick = { currentScreen = AppScreen.ADMIN_DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Home", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_admin_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ADMIN_USERS,
                            onClick = { currentScreen = AppScreen.ADMIN_USERS },
                            icon = { Icon(Icons.Default.Group, contentDescription = null) },
                            label = { Text("Directory", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_admin_users")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ADMIN_TIMETABLE,
                            onClick = { currentScreen = AppScreen.ADMIN_TIMETABLE },
                            icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            label = { Text("Timetable", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_admin_timetable")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ADMIN_FEES,
                            onClick = { currentScreen = AppScreen.ADMIN_FEES },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                            label = { Text("Fees", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_admin_fees")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ASK_AI,
                            onClick = { currentScreen = AppScreen.ASK_AI },
                            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                            label = { Text("Ask AI", fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_admin_chat")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                // Student Screens
                AppScreen.STUDENT_DASHBOARD -> StudentDashboardScreen(
                    studentId = user.id,
                    onNavigateToAI = { currentScreen = AppScreen.STUDENT_AI_INSIGHTS },
                    onNavigateToAttendance = { currentScreen = AppScreen.STUDENT_ATTENDANCE },
                    onNavigateToMarksheet = { currentScreen = AppScreen.STUDENT_MARKSHEET },
                    onNavigateToFees = { currentScreen = AppScreen.STUDENT_FEES },
                    onNavigateToTimetable = { currentScreen = AppScreen.STUDENT_TIMETABLE }
                )
                AppScreen.STUDENT_ATTENDANCE -> StudentAttendanceScreen(studentId = user.id)
                AppScreen.STUDENT_MARKSHEET -> StudentMarksheetScreen(studentId = user.id)
                AppScreen.STUDENT_FEES -> StudentFeesScreen(studentId = user.id)
                AppScreen.STUDENT_TIMETABLE -> StudentTimetableScreen()
                AppScreen.STUDENT_CALENDAR -> StudentCalendarScreen()
                AppScreen.STUDENT_AI_INSIGHTS -> StudentAIInsightsScreen(
                    studentId = user.id,
                    onOpenChatbot = { currentScreen = AppScreen.ASK_AI }
                )

                // Teacher Screens
                AppScreen.TEACHER_DASHBOARD -> TeacherDashboardScreen(
                    teacherId = user.id,
                    onNavigateToAttendance = { currentScreen = AppScreen.TEACHER_ATTENDANCE },
                    onNavigateToMarks = { currentScreen = AppScreen.TEACHER_MARKS },
                    onNavigateToAnalytics = { currentScreen = AppScreen.TEACHER_ANALYTICS },
                    onNavigateToTimetable = { currentScreen = AppScreen.TEACHER_TIMETABLE }
                )
                AppScreen.TEACHER_ATTENDANCE -> TeacherAttendanceScreen(teacherId = user.id)
                AppScreen.TEACHER_MARKS -> TeacherMarksEntryScreen(teacherId = user.id)
                AppScreen.TEACHER_ANALYTICS -> TeacherAnalyticsScreen(teacherId = user.id)
                AppScreen.TEACHER_TIMETABLE -> StudentTimetableScreen() // Shared weekly grid

                // Admin Screens
                AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(
                    onNavigateToUsers = { currentScreen = AppScreen.ADMIN_USERS },
                    onNavigateToTimetable = { currentScreen = AppScreen.ADMIN_TIMETABLE },
                    onNavigateToCalendar = { currentScreen = AppScreen.ADMIN_CALENDAR },
                    onNavigateToFees = { currentScreen = AppScreen.ADMIN_FEES },
                    onNavigateToAnalytics = { currentScreen = AppScreen.ADMIN_ANALYTICS }
                )
                AppScreen.ADMIN_USERS -> AdminUsersScreen()
                AppScreen.ADMIN_TIMETABLE -> AdminTimetableScreen()
                AppScreen.ADMIN_CALENDAR -> AdminCalendarScreen()
                AppScreen.ADMIN_FEES -> AdminFeesScreen()
                AppScreen.ADMIN_ANALYTICS -> AdminAnalyticsScreen()

                // Common
                AppScreen.ASK_AI -> AIChatbotScreen()
                AppScreen.LOGIN -> {}
            }
        }
    }

    // User Profile Dialog
    if (showProfileDialog) {
        var newPass by remember { mutableStateOf("") }
        var passSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User ID: ${user.id}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Role: ${user.role.name}", fontSize = 12.sp)
                    Text("Department: ${user.departmentName}", fontSize = 12.sp)
                    Text("Class/Semester: ${user.semesterOrClass}", fontSize = 12.sp)
                    Text("Institutional Email: ${user.email}", fontSize = 12.sp)
                    Text("Phone: ${user.phone}", fontSize = 12.sp)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passSuccess) {
                        Text("Password updated successfully!", color = RiskLowGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else if (newPass.isNotBlank()) {
                        Button(
                            onClick = { passSuccess = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Password", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        EduRepository.logout()
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskHighRed)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Institutional Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Campus Notifications", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotificationItem(
                        title = "Mid-Term Examination Schedule Announced",
                        time = "2 hours ago",
                        desc = "Written assessments commence October 20th. Download hall tickets."
                    )
                    NotificationItem(
                        title = "Assignment #4 Portal Open",
                        time = "Yesterday",
                        desc = "Dynamic Programming assignment repository due October 18th, 11:59 PM."
                    )
                    NotificationItem(
                        title = "Attendance Deficit Reminders Issued",
                        time = "2 days ago",
                        desc = "Students below 75% threshold have been flagged for faculty review."
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) {
                    Text("Mark All Read")
                }
            }
        )
    }
}

@Composable
private fun NotificationItem(title: String, time: String, desc: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(text = time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
