package com.example.ui.screens.teacher

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
import com.example.ui.components.RiskDistributionBar
import com.example.ui.components.StatCard
import com.example.ui.theme.*

// --- 1. TEACHER DASHBOARD ---
@Composable
fun TeacherDashboardScreen(
    teacherId: String,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTimetable: () -> Unit
) {
    val teachers by EduRepository.teachers.collectAsState()
    val students by EduRepository.students.collectAsState()
    val timetable by EduRepository.timetable.collectAsState()

    val currentTeacher = teachers.find { it.id == teacherId } ?: teachers.firstOrNull() ?: return
    val teacherSchedule = remember(timetable, currentTeacher.id) {
        timetable.filter { it.teacherId == currentTeacher.id || it.dayOfWeek == "Monday" }.take(3)
    }

    // Identify At-Risk students
    val atRiskStudents = remember(students) {
        students.filter { it.attendancePercentage < 75.0 || it.cgpa < 6.0 }.take(6)
    }

    var selectedStudentForFeedback by remember { mutableStateOf<Student?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Profile Banner
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
                            text = "Faculty Portal: ${currentTeacher.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currentTeacher.designation} • ${currentTeacher.departmentName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Subjects: " + currentTeacher.subjectsTaught.joinToString(", "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CoPresent, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Assigned Cohort",
                    value = "${students.size}",
                    subtitle = "Semester 5 CSE/IT",
                    icon = Icons.Default.People,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    testTag = "teacher_total_students_card"
                )

                StatCard(
                    title = "At-Risk Flagged",
                    value = "${atRiskStudents.size}",
                    subtitle = "Needs Intervention",
                    icon = Icons.Default.Warning,
                    accentColor = RiskHighRed,
                    modifier = Modifier.weight(1f),
                    testTag = "teacher_at_risk_students_card"
                )
            }
        }

        // Quick Actions Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToAttendance,
                    modifier = Modifier.weight(1f).testTag("teacher_mark_attendance_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Attendance", fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToMarks,
                    modifier = Modifier.weight(1f).testTag("teacher_enter_marks_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enter Marks", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f).testTag("teacher_view_analytics_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Insights", fontSize = 12.sp)
                }
            }
        }

        // Today's Lectures
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Assigned Lectures",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTimetable) {
                        Text("My Timetable", fontSize = 12.sp)
                    }
                }

                teacherSchedule.forEach { entry ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "P${entry.periodIndex}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                                Column {
                                    Text(text = entry.subjectName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Room ${entry.roomNumber} • Sem ${entry.semester}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = entry.timeSlot, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // At-Risk Students Early-Warning List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Early-Warning: At-Risk Students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = RiskHighRedContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${atRiskStudents.size} Identified",
                            color = RiskHighRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                atRiskStudents.forEach { st ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "${st.name} (${st.id})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Attendance: ${st.attendancePercentage}% • CGPA: ${st.cgpa}",
                                    fontSize = 11.sp,
                                    color = if (st.attendancePercentage < 75.0) RiskHighRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RiskBadge(riskLevel = if (st.attendancePercentage < 70.0 || st.cgpa < 5.8) RiskLevel.HIGH else RiskLevel.MEDIUM)
                                IconButton(
                                    onClick = { selectedStudentForFeedback = st },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = "Give Feedback",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedStudentForFeedback?.let { st ->
        TeacherFeedbackDialog(
            student = st,
            teacherId = teacherId,
            onDismiss = { selectedStudentForFeedback = null }
        )
    }
}

// --- 2. TEACHER ATTENDANCE MARKING SCREEN ---
@Composable
fun TeacherAttendanceScreen(teacherId: String) {
    val students by EduRepository.students.collectAsState()
    val subjects by EduRepository.subjects.collectAsState()
    val attendanceRecords by EduRepository.attendance.collectAsState()

    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "SUB101") }
    var selectedDate by remember { mutableStateOf("2026-10-09") }
    var saveSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Local attendance map for selected session
    val attendanceMap = remember(selectedSubjectId, selectedDate, attendanceRecords) {
        val map = mutableStateMapOf<String, AttendanceStatus>()
        students.forEach { st ->
            val existing = attendanceRecords.find { it.studentId == st.id && it.subjectId == selectedSubjectId && it.date == selectedDate }
            map[st.id] = existing?.status ?: if (st.attendancePercentage > 75) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
        }
        map
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Class Attendance Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Record daily attendance and synchronize student records in real time",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subject Selector
        item {
            Text(text = "Select Course Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubjectId == subj.id,
                        onClick = { selectedSubjectId = subj.id },
                        label = { Text(subj.code + " - " + subj.name.take(16)) }
                    )
                }
            }
        }

        // Bulk Actions Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date: $selectedDate", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                students.forEach { st -> attendanceMap[st.id] = AttendanceStatus.PRESENT }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("bulk_mark_present_btn")
                        ) {
                            Text("All Present", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                students.forEach { st ->
                                    val status = attendanceMap[st.id] ?: AttendanceStatus.PRESENT
                                    EduRepository.markAttendance(st.id, selectedSubjectId, selectedDate, status)
                                }
                                saveSuccessMsg = "Attendance saved for ${students.size} students on $selectedDate!"
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("save_attendance_btn")
                        ) {
                            Text("Save All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        saveSuccessMsg?.let { msg ->
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = msg,
                        color = RiskLowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Student List with Present/Absent/Late Toggles
        items(students) { student ->
            val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT

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
                        Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${student.rollNo} • Current: ${student.attendancePercentage}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.PRESENT,
                            onClick = { attendanceMap[student.id] = AttendanceStatus.PRESENT },
                            label = { Text("P", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RiskLowGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.LATE,
                            onClick = { attendanceMap[student.id] = AttendanceStatus.LATE },
                            label = { Text("L", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RiskModerateAmber,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.ABSENT,
                            onClick = { attendanceMap[student.id] = AttendanceStatus.ABSENT },
                            label = { Text("A", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RiskHighRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// --- 3. TEACHER MARKS ENTRY SCREEN ---
@Composable
fun TeacherMarksEntryScreen(teacherId: String) {
    val students by EduRepository.students.collectAsState()
    val subjects by EduRepository.subjects.collectAsState()
    val marks by EduRepository.marks.collectAsState()

    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "SUB101") }
    var selectedExamType by remember { mutableStateOf("Mid-Term Examination") }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    val examTypes = listOf("Internal Assessment 1", "Mid-Term Examination", "End-Semester")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_marks_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Course Grade & Marks Entry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter evaluation scores with automatic GPA and grade computation",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subject selector
        item {
            Text("Select Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubjectId == subj.id,
                        onClick = { selectedSubjectId = subj.id },
                        label = { Text(subj.code) }
                    )
                }
            }
        }

        // Exam Type selector
        item {
            Text("Select Assessment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(examTypes) { ex ->
                    FilterChip(
                        selected = selectedExamType == ex,
                        onClick = { selectedExamType = ex },
                        label = { Text(ex) }
                    )
                }
            }
        }

        saveFeedback?.let { msg ->
            item {
                Surface(
                    color = RiskLowGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = msg,
                        color = RiskLowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Student Score Rows
        items(students) { st ->
            val markRecord = marks.find { it.studentId == st.id && it.subjectId == selectedSubjectId && it.examType == selectedExamType }
            var currentScore by remember(markRecord) {
                mutableStateOf(markRecord?.marksObtained?.toInt()?.toString() ?: "75")
            }

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
                        Text(text = "${st.rollNo} • Current CGPA: ${st.cgpa}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentScore,
                            onValueChange = { currentScore = it },
                            modifier = Modifier.width(68.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )

                        Text("/ 100", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Button(
                            onClick = {
                                val scoreVal = currentScore.toDoubleOrNull() ?: 70.0
                                EduRepository.updateMarks(
                                    studentId = st.id,
                                    subjectId = selectedSubjectId,
                                    examType = selectedExamType,
                                    marksObtained = scoreVal,
                                    maxMarks = 100
                                )
                                saveFeedback = "Saved ${st.name}: ${scoreVal.toInt()} pts"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Save", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- 4. TEACHER CLASS ANALYTICS SCREEN ---
@Composable
fun TeacherAnalyticsScreen(teacherId: String) {
    val students by EduRepository.students.collectAsState()

    val lowRiskCount = students.count { it.attendancePercentage >= 80 && it.cgpa >= 7.0 }
    val medRiskCount = students.count { (it.attendancePercentage in 72.0..79.9) || (it.cgpa in 5.8..6.9) }
    val highRiskCount = students.count { it.attendancePercentage < 72.0 || it.cgpa < 5.8 }

    val avgAttendance = if (students.isNotEmpty()) students.map { it.attendancePercentage }.average() else 80.0
    val avgCgpa = if (students.isNotEmpty()) students.map { it.cgpa }.average() else 7.5

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Class-Wise Academic Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Institutional performance telemetry and student risk distribution",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Avg Attendance",
                    value = "${Math.round(avgAttendance)}%",
                    subtitle = "Cohort Baseline",
                    icon = Icons.Default.EventAvailable,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Avg Class CGPA",
                    value = "${Math.round(avgCgpa * 100.0) / 100.0}",
                    subtitle = "Scale of 10.0",
                    icon = Icons.Default.Grade,
                    accentColor = BrandPurpleTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Cohort Risk Distribution Card
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
                        text = "Predicted Risk Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    RiskDistributionBar(
                        lowCount = lowRiskCount,
                        mediumCount = medRiskCount,
                        highCount = highRiskCount
                    )
                }
            }
        }

        // Student Predictions List
        item {
            Text(
                text = "Individual Student Predictions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(students) { st ->
            val pred = EduRepository.getAIPredictionForStudent(st.id)

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
                        Text(text = "${st.name} (${st.id})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "Attendance: ${st.attendancePercentage}% | Expected Final: ${pred.predictedFinalPercentage}% (Grade ${pred.predictedGrade})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RiskBadge(riskLevel = pred.riskLevel)
                }
            }
        }
    }
}

// --- 5. TEACHER FEEDBACK DIALOG ---
@Composable
fun TeacherFeedbackDialog(
    student: Student,
    teacherId: String,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("Academic") }
    var comment by remember { mutableStateOf("") }
    val categories = listOf("Academic", "Attendance", "Discipline", "Commendation")
    var saved by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Faculty Remark for ${student.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Teacher Remark / Guidance Note") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                if (saved) {
                    Text(
                        text = "Remark officially published to student portal!",
                        color = RiskLowGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isNotBlank()) {
                        EduRepository.addRemark(student.id, teacherId, category, comment)
                        saved = true
                    }
                }
            ) {
                Text("Save Remark")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
