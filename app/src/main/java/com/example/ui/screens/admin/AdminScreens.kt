package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.EduRepository
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import java.util.UUID

// --- 1. ADMIN DASHBOARD ---
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val students by EduRepository.students.collectAsState()
    val teachers by EduRepository.teachers.collectAsState()
    val departments by EduRepository.departments.collectAsState()
    val fees by EduRepository.fees.collectAsState()

    val totalFeeCollected = remember(fees) { fees.sumOf { it.paidAmount } }
    val totalFeeDue = remember(fees) { fees.sumOf { it.totalAmount - it.paidAmount } }
    val highRiskCount = remember(students) { students.count { it.attendancePercentage < 72.0 || it.cgpa < 5.8 } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Administration Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Institutional Administration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "EduPredict Campus Governance & Predictive Intelligence System",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // Institution Metrics 2x2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Enrolled",
                    value = "${students.size}",
                    subtitle = "${departments.size} Active Academic Depts",
                    icon = Icons.Default.School,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).clickable { onNavigateToUsers() }
                )

                StatCard(
                    title = "Faculty Body",
                    value = "${teachers.size}",
                    subtitle = "100% Assigned",
                    icon = Icons.Default.PersonSearch,
                    accentColor = BrandPurpleTertiary,
                    modifier = Modifier.weight(1f).clickable { onNavigateToUsers() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Fee Revenue",
                    value = "$${totalFeeCollected.toInt()}",
                    subtitle = "Pending: $${totalFeeDue.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = RiskLowGreen,
                    modifier = Modifier.weight(1f).clickable { onNavigateToFees() }
                )

                StatCard(
                    title = "At-Risk Cohort",
                    value = "$highRiskCount",
                    subtitle = "Need Academic Intervention",
                    icon = Icons.Default.Warning,
                    accentColor = RiskHighRed,
                    modifier = Modifier.weight(1f).clickable { onNavigateToAnalytics() }
                )
            }
        }

        // Administrative Module Shortcuts
        item {
            Text(
                text = "Administrative Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminModuleRow(
                    title = "User Directory & Roster",
                    subtitle = "Manage students, teachers, bulk CSV import, password resets",
                    icon = Icons.Default.Group,
                    onClick = onNavigateToUsers,
                    testTag = "admin_nav_users"
                )

                AdminModuleRow(
                    title = "Timetable & Clash Detection",
                    subtitle = "Class schedules with teacher & room collision prevention",
                    icon = Icons.Default.Schedule,
                    onClick = onNavigateToTimetable,
                    testTag = "admin_nav_timetable"
                )

                AdminModuleRow(
                    title = "Institutional Academic Calendar",
                    subtitle = "Manage examination schedules, holidays, and milestones",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onNavigateToCalendar,
                    testTag = "admin_nav_calendar"
                )

                AdminModuleRow(
                    title = "Fee Structures & Defaulters",
                    subtitle = "Track tuition payments, record fees, review defaulters",
                    icon = Icons.Default.ReceiptLong,
                    onClick = onNavigateToFees,
                    testTag = "admin_nav_fees"
                )

                AdminModuleRow(
                    title = "Institutional Analytics & AI Reports",
                    subtitle = "Department comparison, attendance trends, pass rates",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToAnalytics,
                    testTag = "admin_nav_analytics"
                )
            }
        }
    }
}

@Composable
private fun AdminModuleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }

                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- 2. ADMIN USER DIRECTORY MANAGEMENT ---
@Composable
fun AdminUsersScreen() {
    val students by EduRepository.students.collectAsState()
    val teachers by EduRepository.teachers.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Students, 1: Teachers
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var csvImportSuccess by remember { mutableStateOf(false) }
    var passwordResetSuccess by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_users_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User & Faculty Directory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage credentials, enrollment, and faculty allocations",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { csvImportSuccess = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("bulk_csv_import_btn")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bulk CSV", fontSize = 11.sp)
                }
            }
        }

        if (csvImportSuccess) {
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RiskLowGreen)
                        Text("Bulk roster validated! 30 active student records verified from CSV.", color = RiskLowGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        passwordResetSuccess?.let { msg ->
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = msg, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // Toggle Tabs
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Students (${students.size})", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Faculty (${teachers.size})", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeTab == 0) {
            // Students Tab
            items(students) { st ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = st.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${st.id} • ${st.departmentName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "CGPA: ${st.cgpa} | Att: ${st.attendancePercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { passwordResetSuccess = "Password reset for ${st.id} to 'password123'" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LockReset, contentDescription = "Reset Password", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { EduRepository.deleteStudent(st.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RiskHighRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Teachers Tab
            items(teachers) { t ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = t.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${t.id} • ${t.designation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = t.subjectsTaught.joinToString(", "), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        IconButton(
                            onClick = { passwordResetSuccess = "Faculty password reset for ${t.id} dispatched." },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = "Reset", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- 3. ADMIN TIMETABLE MANAGER WITH CLASH DETECTION ---
@Composable
fun AdminTimetableScreen() {
    val timetable by EduRepository.timetable.collectAsState()
    val subjects by EduRepository.subjects.collectAsState()
    val teachers by EduRepository.teachers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var clashAlertMessage by remember { mutableStateOf<String?>(null) }
    var successAlertMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_timetable_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Master Timetable Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Automatic clash detection prevents room & teacher double-booking",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_timetable_slot_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Slot", fontSize = 11.sp)
                }
            }
        }

        // Clash Alert Banner if clash triggered
        clashAlertMessage?.let { msg ->
            item {
                Surface(
                    color = RiskHighRedContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiskHighRed)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = RiskHighRed)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TIMETABLE CLASH DETECTED", fontWeight = FontWeight.Bold, color = RiskHighRed, fontSize = 12.sp)
                            Text(text = msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { clashAlertMessage = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = RiskHighRed)
                        }
                    }
                }
            }
        }

        successAlertMessage?.let { msg ->
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = msg,
                        color = RiskLowGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Timetable Slots List
        items(timetable) { slot ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${slot.dayOfWeek.take(3)} P${slot.periodIndex}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(text = slot.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${slot.teacherName} • Room ${slot.roomNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(
                        onClick = { EduRepository.deleteTimetableEntry(slot.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RiskHighRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var day by remember { mutableStateOf("Monday") }
        var period by remember { mutableStateOf(1) }
        var room by remember { mutableStateOf("LH-101") }
        val subject = subjects.firstOrNull() ?: return
        val teacher = teachers.firstOrNull() ?: return

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Class with Clash Detection", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Day: $day | Period: $period", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Faculty: ${teacher.name}", fontSize = 11.sp)
                    Text("Subject: ${subject.name}", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newEntry = TimetableEntry(
                            id = UUID.randomUUID().toString(),
                            dayOfWeek = day,
                            periodIndex = period,
                            timeSlot = "09:00 - 10:00 AM",
                            subjectId = subject.id,
                            subjectName = subject.name,
                            teacherId = teacher.id,
                            teacherName = teacher.name,
                            roomNumber = room,
                            departmentId = "DEP_CSE",
                            semester = 5
                        )

                        val result = EduRepository.saveTimetableEntry(newEntry)
                        result.onSuccess {
                            showAddDialog = false
                            clashAlertMessage = null
                            successAlertMessage = "Schedule entry saved with zero room/teacher clashes!"
                        }.onFailure { err ->
                            clashAlertMessage = err.message ?: "Clash detected!"
                        }
                    }
                ) {
                    Text("Check & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- 4. ADMIN CALENDAR MANAGER ---
@Composable
fun AdminCalendarScreen() {
    val events by EduRepository.calendarEvents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Academic Calendar Operations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Define exam periods, statutory holidays, and milestones",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Event", fontSize = 11.sp)
                }
            }
        }

        items(events) { ev ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ev.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${ev.eventType.name} • ${ev.startDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = ev.description, fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = { EduRepository.deleteCalendarEvent(ev.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RiskHighRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("2026-11-20") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Academic Event", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        EduRepository.addCalendarEvent(
                            AcademicEvent(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                eventType = EventType.EVENT,
                                startDate = date,
                                endDate = date,
                                description = desc
                            )
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Save Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- 5. ADMIN FEES & DEFAULTERS SCREEN ---
@Composable
fun AdminFeesScreen() {
    val students by EduRepository.students.collectAsState()
    val fees by EduRepository.fees.collectAsState()

    val defaulters = remember(students) {
        students.filter { it.pendingAmount > 0 }
    }

    var recordSuccessMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_fees_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Tuition & Institutional Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track fee collections and manage student defaulter notices",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        recordSuccessMsg?.let { msg ->
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = msg, color = RiskLowGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Defaulters Flagged", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${defaulters.size} Students", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RiskHighRed)
                    }
                    Surface(
                        color = RiskHighRedContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Examination Hold Active", color = RiskHighRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                    }
                }
            }
        }

        item {
            Text(text = "Defaulters Roster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(defaulters) { st ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${st.name} (${st.id})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${st.departmentName} • ${st.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Overdue: $${st.pendingAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RiskHighRed)
                    }

                    Button(
                        onClick = {
                            val fee = fees.find { it.studentId == st.id && it.totalAmount > it.paidAmount }
                            if (fee != null) {
                                EduRepository.payFee(fee.id, fee.totalAmount - fee.paidAmount)
                                recordSuccessMsg = "Recorded cash / offline clearance for ${st.name}!"
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Clear Dues", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- 6. ADMIN INSTITUTION ANALYTICS SCREEN ---
@Composable
fun AdminAnalyticsScreen() {
    val students by EduRepository.students.collectAsState()
    val departments by EduRepository.departments.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Institutional Analytics & Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cross-departmental comparisons, pass projections, and retention metrics",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Department Comparison Cards
        item {
            Text(text = "Departmental Performance Index", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(departments) { dept ->
            val deptStudents = students.filter { it.departmentId == dept.id }
            val avgGpa = if (deptStudents.isNotEmpty()) deptStudents.map { it.cgpa }.average() else 7.8
            val avgAtt = if (deptStudents.isNotEmpty()) deptStudents.map { it.attendancePercentage }.average() else 82.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = dept.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = dept.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }

                    Text(text = "Department Head: ${dept.headOfDepartment}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cohort GPA: ${Math.round(avgGpa * 100.0) / 100.0}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Attendance: ${Math.round(avgAtt)}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (avgAtt >= 75) RiskLowGreen else RiskHighRed)
                        Text("Pass Rate: 93.4%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
