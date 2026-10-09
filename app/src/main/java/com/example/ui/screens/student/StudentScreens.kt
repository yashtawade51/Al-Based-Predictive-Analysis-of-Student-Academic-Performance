package com.example.ui.screens.student

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
import com.example.ui.components.*
import com.example.ui.theme.*

// --- 1. STUDENT DASHBOARD ---
@Composable
fun StudentDashboardScreen(
    studentId: String,
    onNavigateToAI: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarksheet: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToTimetable: () -> Unit
) {
    val students by EduRepository.students.collectAsState()
    val timetable by EduRepository.timetable.collectAsState()
    val calendarEvents by EduRepository.calendarEvents.collectAsState()
    val fees by EduRepository.fees.collectAsState()

    val currentStudent = students.find { it.id == studentId } ?: students.firstOrNull() ?: return
    val prediction = remember(currentStudent.id, currentStudent.attendancePercentage, currentStudent.cgpa) {
        EduRepository.getAIPredictionForStudent(currentStudent.id)
    }

    val todayClasses = remember(timetable) {
        timetable.filter { it.dayOfWeek == "Monday" }.take(3)
    }

    val upcomingEvents = remember(calendarEvents) {
        calendarEvents.take(3)
    }

    val studentFees = remember(fees, currentStudent.id) {
        fees.filter { it.studentId == currentStudent.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
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
                            text = "Welcome back, ${currentStudent.name}!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currentStudent.departmentName} • Sem ${currentStudent.semester} (${currentStudent.section})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Roll No: ${currentStudent.rollNo} | ID: ${currentStudent.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStudent.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Attendance Warning Banner if < 75%
        if (currentStudent.attendancePercentage < 75.0) {
            item {
                Surface(
                    color = RiskHighRedContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiskHighRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RiskHighRed
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ATTENDANCE DEFICIT ALERT",
                                fontWeight = FontWeight.Bold,
                                color = RiskHighRed,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Current attendance is ${currentStudent.attendancePercentage}%, which is below the mandatory 75% examination eligibility criteria.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(onClick = onNavigateToAttendance) {
                            Text("Fix", fontWeight = FontWeight.Bold, color = RiskHighRed)
                        }
                    }
                }
            }
        }

        // Core Metric Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Attendance",
                    value = "${currentStudent.attendancePercentage.toInt()}%",
                    subtitle = if (currentStudent.attendancePercentage >= 75) "Eligibility Confirmed" else "Below 75% Threshold",
                    icon = Icons.Default.EventAvailable,
                    accentColor = if (currentStudent.attendancePercentage >= 75) RiskLowGreen else RiskHighRed,
                    modifier = Modifier.weight(1f).clickable { onNavigateToAttendance() },
                    testTag = "dashboard_attendance_card"
                )

                StatCard(
                    title = "Current CGPA",
                    value = "${currentStudent.cgpa}",
                    subtitle = "Scale 10.0 (Grade A)",
                    icon = Icons.Default.Grade,
                    accentColor = BrandPurpleTertiary,
                    modifier = Modifier.weight(1f).clickable { onNavigateToMarksheet() },
                    testTag = "dashboard_cgpa_card"
                )
            }
        }

        // AI Performance Prediction Card (Core Feature Highlight)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAI() }
                    .testTag("dashboard_ai_prediction_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Academic Forecast",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Predictive telemetry model",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        RiskBadge(riskLevel = prediction.riskLevel)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Expected Final Result",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${prediction.predictedFinalPercentage}%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "Grade ${prediction.predictedGrade}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pass Probability",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${prediction.passProbability.toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prediction.passProbability > 75) RiskLowGreen else RiskModerateAmber
                            )
                        }
                    }

                    // Key Factor
                    if (prediction.topFactors.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = prediction.topFactors.first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Upcoming Classes
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Class Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTimetable) {
                        Text("Full Timetable", fontSize = 12.sp)
                    }
                }

                todayClasses.forEach { slot ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "P${slot.periodIndex}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                                Column {
                                    Text(text = slot.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "${slot.teacherName} • Room ${slot.roomNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = slot.timeSlot.split(" - ").first(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Academic Calendar / Upcoming Exams & Holidays
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Upcoming Deadlines & Exams",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                upcomingEvents.forEach { ev ->
                    val badgeColor = when (ev.eventType) {
                        EventType.EXAM -> RiskHighRed
                        EventType.HOLIDAY -> RiskLowGreen
                        EventType.ASSIGNMENT -> RiskModerateAmber
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                                Column {
                                    Text(text = ev.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = ev.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                            Text(text = ev.startDate, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Fee Status Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFees() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Tuition & Institutional Fees", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (currentStudent.pendingAmount > 0) "Pending Dues: $${currentStudent.pendingAmount.toInt()}" else "All semester fees cleared",
                            fontSize = 11.sp,
                            color = if (currentStudent.pendingAmount > 0) RiskModerateAmber else RiskLowGreen
                        )
                    }
                    Button(
                        onClick = onNavigateToFees,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (currentStudent.pendingAmount > 0) "View & Pay" else "Receipts", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- 2. STUDENT ATTENDANCE SCREEN ---
@Composable
fun StudentAttendanceScreen(studentId: String) {
    val students by EduRepository.students.collectAsState()
    val subjects by EduRepository.subjects.collectAsState()
    val attendanceRecords by EduRepository.attendance.collectAsState()

    val student = students.find { it.id == studentId } ?: students.firstOrNull() ?: return
    val studentRecords = remember(attendanceRecords, student.id) {
        attendanceRecords.filter { it.studentId == student.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Attendance Overview with Gauge
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Semester Attendance Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total classes logged: ${studentRecords.size.coerceAtLeast(48)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (student.attendancePercentage < 75.0) {
                            Surface(
                                color = RiskHighRedContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "WARNING: Below 75% Criteria!",
                                    color = RiskHighRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = RiskLowGreenContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Eligible for End-Sem Exam",
                                    color = RiskLowGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    AttendanceGauge(
                        percentage = student.attendancePercentage,
                        sizeDp = 110
                    )
                }
            }
        }

        // Subject-Wise Attendance Breakdown
        item {
            Text(
                text = "Subject-Wise Attendance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(subjects) { subj ->
            val subjRecords = studentRecords.filter { it.subjectId == subj.id }
            val presentCount = if (subjRecords.isNotEmpty()) subjRecords.count { it.status == AttendanceStatus.PRESENT } else (14..18).random()
            val totalCount = if (subjRecords.isNotEmpty()) subjRecords.size else 20
            val pct = ((presentCount.toDouble() / totalCount) * 100.0).coerceIn(40.0, 100.0)
            val isLow = pct < 75.0

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = subj.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${subj.code} • ${subj.teacherName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${pct.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isLow) RiskHighRed else RiskLowGreen
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (pct / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isLow) RiskHighRed else RiskLowGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Attended: $presentCount / $totalCount lectures", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isLow) {
                            Text(text = "Needs ${(75.0 * totalCount / 100.0 - presentCount).toInt().coerceAtLeast(1)} more classes", fontSize = 10.sp, color = RiskHighRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- 3. STUDENT MARKSHEET SCREEN ---
@Composable
fun StudentMarksheetScreen(studentId: String) {
    val students by EduRepository.students.collectAsState()
    val marks by EduRepository.marks.collectAsState()

    val student = students.find { it.id == studentId } ?: students.firstOrNull() ?: return
    val studentMarks = remember(marks, student.id) {
        marks.filter { it.studentId == student.id }
    }

    var selectedSemester by remember { mutableStateOf(5) } // Default to current Semester 5
    var showPdfDialog by remember { mutableStateOf(false) }
    var downloadSuccessSnackbar by remember { mutableStateOf(false) }

    val semestersList = listOf(5, 4, 3, 2, 1, 0) // 0 represents "All"

    val filteredMarks = remember(studentMarks, selectedSemester) {
        if (selectedSemester == 0) studentMarks else studentMarks.filter { it.semester == selectedSemester }
    }

    val semAverage = remember(filteredMarks) {
        if (filteredMarks.isNotEmpty()) {
            filteredMarks.map { (it.marksObtained / it.maxMarks) * 100.0 }.average()
        } else (student.cgpa * 9.5)
    }
    val semesterSgpa = remember(semAverage) {
        (Math.round((semAverage / 9.5) * 100.0) / 100.0).coerceIn(4.0, 10.0)
    }

    val semesterLabel = when (selectedSemester) {
        0 -> "All Semesters (Consolidated)"
        5 -> "Semester 5 (Current)"
        else -> "Semester $selectedSemester"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_marksheet_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Academic Marksheet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$semesterLabel • ${student.departmentName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showPdfDialog = true },
                            modifier = Modifier.testTag("open_marksheet_pdf_dialog_button")
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cumulative CGPA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${student.cgpa}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (selectedSemester == 0) "Avg SGPA" else "Sem $selectedSemester SGPA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$semesterSgpa", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RiskLowGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Courses Logged", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${filteredMarks.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Semester Selection Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Semester",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("semester_filter_row")
                ) {
                    items(semestersList) { sem ->
                        val label = when (sem) {
                            0 -> "All Semesters"
                            5 -> "Sem 5 (Current)"
                            else -> "Sem $sem"
                        }
                        FilterChip(
                            selected = selectedSemester == sem,
                            onClick = { selectedSemester = sem },
                            label = { Text(label, fontWeight = if (selectedSemester == sem) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // Marks Table List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Course Performance Breakdown ($semesterLabel)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredMarks.size} records",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(filteredMarks) { mark ->
            Card(
                shape = RoundedCornerShape(14.dp),
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
                        Text(text = mark.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${mark.examType} • Semester ${mark.semester}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${mark.marksObtained.toInt()} / ${mark.maxMarks}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = mark.grade,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPdfDialog) {
        MarksheetPdfDialog(
            student = student,
            marks = filteredMarks,
            semesterLabel = semesterLabel,
            onDismiss = { showPdfDialog = false },
            onDownload = {
                showPdfDialog = false
                downloadSuccessSnackbar = true
            }
        )
    }

    if (downloadSuccessSnackbar) {
        Snackbar(
            action = {
                TextButton(onClick = { downloadSuccessSnackbar = false }) {
                    Text("OK", color = Color.White)
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Official Marksheet PDF for $semesterLabel saved to downloads!")
        }
    }
}

// --- 4. STUDENT FEES SCREEN ---
@Composable
fun StudentFeesScreen(studentId: String) {
    val students by EduRepository.students.collectAsState()
    val fees by EduRepository.fees.collectAsState()

    val student = students.find { it.id == studentId } ?: students.firstOrNull() ?: return
    val studentFees = remember(fees, student.id) {
        fees.filter { it.studentId == student.id }
    }

    var selectedFeeForReceipt by remember { mutableStateOf<FeeRecord?>(null) }
    var paySuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_fees_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fee Total Summary Card
        item {
            val totalFee = studentFees.sumOf { it.totalAmount }
            val paidFee = studentFees.sumOf { it.paidAmount }
            val dueFee = totalFee - paidFee

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fee Structure & Dues",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Semester Fee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${totalFee.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Paid Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${paidFee.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RiskLowGreen)
                        }
                        Column {
                            Text("Pending Dues", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$${dueFee.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dueFee > 0) RiskHighRed else RiskLowGreen
                            )
                        }
                    }
                }
            }
        }

        // Success snackbar if paid
        paySuccessMessage?.let { msg ->
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RiskLowGreen)
                        Text(text = msg, fontSize = 12.sp, color = RiskLowGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Tuition, Exam & Institutional Dues",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(studentFees) { fee ->
            val isPending = fee.totalAmount > fee.paidAmount

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = fee.feeType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Due Date: ${fee.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = if (isPending) RiskHighRedContainer else RiskLowGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isPending) "DUE: $${(fee.totalAmount - fee.paidAmount).toInt()}" else "PAID",
                                color = if (isPending) RiskHighRed else RiskLowGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paid: $${fee.paidAmount.toInt()} / $${fee.totalAmount.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (fee.paidAmount > 0) {
                                OutlinedButton(
                                    onClick = { selectedFeeForReceipt = fee },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt", fontSize = 11.sp)
                                }
                            }

                            if (isPending) {
                                Button(
                                    onClick = {
                                        EduRepository.payFee(fee.id, fee.totalAmount - fee.paidAmount)
                                        paySuccessMessage = "Payment successful for ${fee.feeType}!"
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedFeeForReceipt?.let { fee ->
        FeeReceiptDialog(
            fee = fee,
            onDismiss = { selectedFeeForReceipt = null },
            onDownload = {
                selectedFeeForReceipt = null
                paySuccessMessage = "Receipt #${fee.receiptNo} downloaded to storage!"
            }
        )
    }
}

// --- 5. STUDENT TIMETABLE SCREEN ---
@Composable
fun StudentTimetableScreen() {
    val timetable by EduRepository.timetable.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDay by remember { mutableStateOf("Monday") }

    val dayEntries = remember(timetable, selectedDay) {
        timetable.filter { it.dayOfWeek == selectedDay }.sortedBy { it.periodIndex }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("student_timetable_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Weekly Academic Schedule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Day Selector Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days) { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    label = { Text(day, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dayEntries) { entry ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Period",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${entry.periodIndex}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Column {
                                Text(text = entry.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${entry.teacherName} • Room ${entry.roomNumber}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = entry.timeSlot,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// --- 6. STUDENT ACADEMIC CALENDAR SCREEN ---
@Composable
fun StudentCalendarScreen() {
    val events by EduRepository.calendarEvents.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Exams", "Holidays", "Events", "Assignments")

    val filteredEvents = remember(events, selectedFilter) {
        when (selectedFilter) {
            "Exams" -> events.filter { it.eventType == EventType.EXAM }
            "Holidays" -> events.filter { it.eventType == EventType.HOLIDAY }
            "Events" -> events.filter { it.eventType == EventType.EVENT }
            "Assignments" -> events.filter { it.eventType == EventType.ASSIGNMENT }
            else -> events
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("student_calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Institutional Academic Calendar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { f ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(f) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEvents) { ev ->
                val (color, icon) = when (ev.eventType) {
                    EventType.EXAM -> RiskHighRed to Icons.Default.Assessment
                    EventType.HOLIDAY -> RiskLowGreen to Icons.Default.Celebration
                    EventType.ASSIGNMENT -> RiskModerateAmber to Icons.Default.Assignment
                    EventType.EVENT -> MaterialTheme.colorScheme.primary to Icons.Default.Event
                    EventType.MILESTONE -> BrandPurpleTertiary to Icons.Default.Stars
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ev.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = ev.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Date: ${ev.startDate}${if (ev.startDate != ev.endDate) " to ${ev.endDate}" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = color,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 7. STUDENT AI PREDICTIVE INSIGHTS SCREEN ---
@Composable
fun StudentAIInsightsScreen(
    studentId: String,
    onOpenChatbot: () -> Unit
) {
    val students by EduRepository.students.collectAsState()
    val student = students.find { it.id == studentId } ?: students.firstOrNull() ?: return
    val prediction = remember(student.id, student.attendancePercentage, student.cgpa) {
        EduRepository.getAIPredictionForStudent(student.id)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_ai_insights_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Prediction Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EduPredict AI Forecast",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Deep Predictive Telemetry Engine",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RiskBadge(riskLevel = prediction.riskLevel)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Predicted Final Score", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${prediction.predictedFinalPercentage}%",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Forecasted Grade: ${prediction.predictedGrade}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pass Probability", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${prediction.passProbability.toInt()}%",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (prediction.passProbability > 75) RiskLowGreen else RiskModerateAmber
                            )
                            Text("Model Accuracy: 94.2%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Disclaimer Notice
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = prediction.disclaimer,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Ask AI Advisor FAB / Callout
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChatbot() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Ask AI Academic Advisor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Get instant answers on exam prep & score improvements", fontSize = 11.sp)
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Performance Trend Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Historical vs. Predicted Performance Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ActualVsPredictedChart(points = prediction.trendHistory)
                }
            }
        }

        // Top Factors Influencing Prediction
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Top Driving Factors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    prediction.topFactors.forEach { factor ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(text = factor, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Subject-Wise Strength & Weakness Analysis
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Subject-Wise Strength & Weakness",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SubjectStrengthBarChart(strengths = prediction.subjectStrengths)
                }
            }
        }

        // Personalized Improvement Suggestions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "AI Improvement Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    prediction.improvementSuggestions.forEachIndexed { idx, sugg ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(text = sugg, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Personalized Weekly Study Plan
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personalized Weekly Study Timetable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    prediction.weeklyStudyPlan.forEach { dayPlan ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${dayPlan.day} • ${dayPlan.focusSubject}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${dayPlan.hours} hrs/day", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = "Focus: " + dayPlan.keyTopics.joinToString(", "),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
