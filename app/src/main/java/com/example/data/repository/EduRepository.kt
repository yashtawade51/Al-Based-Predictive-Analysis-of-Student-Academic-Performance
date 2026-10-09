package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object EduRepository {

    // --- State Holders ---
    private val _departments = MutableStateFlow<List<Department>>(emptyList())
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _marks = MutableStateFlow<List<MarkRecord>>(emptyList())
    val marks: StateFlow<List<MarkRecord>> = _marks.asStateFlow()

    private val _fees = MutableStateFlow<List<FeeRecord>>(emptyList())
    val fees: StateFlow<List<FeeRecord>> = _fees.asStateFlow()

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<AcademicEvent>>(emptyList())
    val calendarEvents: StateFlow<List<AcademicEvent>> = _calendarEvents.asStateFlow()

    private val _remarks = MutableStateFlow<List<StudentRemark>>(emptyList())
    val remarks: StateFlow<List<StudentRemark>> = _remarks.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Current Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        seedInitialData()
    }

    // --- Authentication ---
    fun login(userId: String, password: String):Result<User> {
        val cleanId = userId.trim().uppercase()
        // Check Admin
        if (cleanId == "ADM001" || cleanId == "ADMIN") {
            val adminUser = User(
                id = "ADM001",
                name = "Dr. K. S. Ramanathan",
                email = "admin.principal@edupredict.edu",
                role = UserRole.ADMIN,
                departmentId = "ALL",
                departmentName = "Dean of Academics",
                semesterOrClass = "Administration",
                phone = "+1 (555) 019-2830"
            )
            _currentUser.value = adminUser
            return Result.success(adminUser)
        }

        // Check Teacher
        val teacher = _teachers.value.find { it.id.equals(cleanId, ignoreCase = true) }
        if (teacher != null) {
            val user = User(
                id = teacher.id,
                name = teacher.name,
                email = teacher.email,
                role = UserRole.TEACHER,
                departmentId = teacher.departmentId,
                departmentName = teacher.departmentName,
                semesterOrClass = teacher.designation,
                phone = teacher.phone
            )
            _currentUser.value = user
            return Result.success(user)
        }

        // Check Student
        val student = _students.value.find { it.id.equals(cleanId, ignoreCase = true) }
        if (student != null) {
            val user = User(
                id = student.id,
                name = student.name,
                email = student.email,
                role = UserRole.STUDENT,
                departmentId = student.departmentId,
                departmentName = student.departmentName,
                semesterOrClass = "Semester ${student.semester} (${student.section})",
                phone = student.phone
            )
            _currentUser.value = user
            return Result.success(user)
        }

        // Generic fallback for any user if role is selected
        if (cleanId.startsWith("STU")) {
            val firstStudent = _students.value.first()
            _currentUser.value = User(
                id = firstStudent.id,
                name = firstStudent.name,
                email = firstStudent.email,
                role = UserRole.STUDENT,
                departmentId = firstStudent.departmentId,
                departmentName = firstStudent.departmentName,
                semesterOrClass = "Semester ${firstStudent.semester} (${firstStudent.section})",
                phone = firstStudent.phone
            )
            return Result.success(_currentUser.value!!)
        }

        return Result.failure(IllegalArgumentException("Invalid User ID or Password. Please check credentials or use 1-click Demo buttons."))
    }

    fun switchRoleQuick(role: UserRole) {
        when (role) {
            UserRole.STUDENT -> {
                val s = _students.value.first()
                _currentUser.value = User(
                    id = s.id,
                    name = s.name,
                    email = s.email,
                    role = UserRole.STUDENT,
                    departmentId = s.departmentId,
                    departmentName = s.departmentName,
                    semesterOrClass = "Semester ${s.semester} (${s.section})",
                    phone = s.phone
                )
            }
            UserRole.TEACHER -> {
                val t = _teachers.value.first()
                _currentUser.value = User(
                    id = t.id,
                    name = t.name,
                    email = t.email,
                    role = UserRole.TEACHER,
                    departmentId = t.departmentId,
                    departmentName = t.departmentName,
                    semesterOrClass = t.designation,
                    phone = t.phone
                )
            }
            UserRole.ADMIN -> {
                _currentUser.value = User(
                    id = "ADM001",
                    name = "Dr. K. S. Ramanathan",
                    email = "admin.principal@edupredict.edu",
                    role = UserRole.ADMIN,
                    departmentId = "ALL",
                    departmentName = "Dean of Academics",
                    semesterOrClass = "Administration",
                    phone = "+1 (555) 019-2830"
                )
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- Attendance Operations ---
    fun markAttendance(
        studentId: String,
        subjectId: String,
        date: String,
        status: AttendanceStatus
    ) {
        val currentList = _attendance.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId && it.subjectId == subjectId && it.date == date }
        val student = _students.value.find { it.id == studentId } ?: return
        val subject = _subjects.value.find { it.id == subjectId } ?: return

        val newRecord = AttendanceRecord(
            id = if (index >= 0) currentList[index].id else UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = student.name,
            subjectId = subjectId,
            subjectName = subject.name,
            date = date,
            status = status
        )

        if (index >= 0) {
            currentList[index] = newRecord
        } else {
            currentList.add(newRecord)
        }
        _attendance.value = currentList
        recalculateStudentAttendance(studentId)
    }

    fun bulkMarkAttendance(
        studentIds: List<String>,
        subjectId: String,
        date: String,
        status: AttendanceStatus
    ) {
        studentIds.forEach { sId ->
            markAttendance(sId, subjectId, date, status)
        }
    }

    private fun recalculateStudentAttendance(studentId: String) {
        val studentRecords = _attendance.value.filter { it.studentId == studentId }
        if (studentRecords.isNotEmpty()) {
            val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT }
            val lateCount = studentRecords.count { it.status == AttendanceStatus.LATE }
            val total = studentRecords.size.toDouble()
            val effectivePresent = presentCount + (lateCount * 0.5)
            val percentage = ((effectivePresent / total) * 100.0).coerceIn(0.0, 100.0)

            _students.value = _students.value.map {
                if (it.id == studentId) it.copy(attendancePercentage = Math.round(percentage * 10.0) / 10.0) else it
            }
        }
    }

    // --- Marks Operations ---
    fun updateMarks(
        studentId: String,
        subjectId: String,
        examType: String,
        marksObtained: Double,
        maxMarks: Int = 100,
        semester: Int = 5
    ) {
        val currentList = _marks.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId && it.subjectId == subjectId && it.examType == examType && it.semester == semester }
        val student = _students.value.find { it.id == studentId } ?: return
        val subject = _subjects.value.find { it.id == subjectId } ?: return

        val pct = (marksObtained / maxMarks) * 100.0
        val grade = when {
            pct >= 90 -> "A+"
            pct >= 80 -> "A"
            pct >= 70 -> "B+"
            pct >= 60 -> "B"
            pct >= 50 -> "C"
            pct >= 40 -> "D"
            else -> "F"
        }

        val record = MarkRecord(
            id = if (index >= 0) currentList[index].id else UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = student.name,
            subjectId = subjectId,
            subjectName = subject.name,
            semester = semester,
            examType = examType,
            maxMarks = maxMarks,
            marksObtained = marksObtained,
            grade = grade
        )

        if (index >= 0) {
            currentList[index] = record
        } else {
            currentList.add(record)
        }
        _marks.value = currentList
        recalculateStudentCGPA(studentId)
    }

    private fun recalculateStudentCGPA(studentId: String) {
        val studentMarks = _marks.value.filter { it.studentId == studentId }
        if (studentMarks.isNotEmpty()) {
            val avgPercentage = studentMarks.map { (it.marksObtained / it.maxMarks) * 100.0 }.average()
            val cgpa = (avgPercentage / 9.5).coerceIn(0.0, 10.0)
            _students.value = _students.value.map {
                if (it.id == studentId) it.copy(cgpa = Math.round(cgpa * 100.0) / 100.0) else it
            }
        }
    }

    // --- Fee Operations ---
    fun payFee(recordId: String, amount: Double) {
        _fees.value = _fees.value.map { fee ->
            if (fee.id == recordId) {
                val newPaid = (fee.paidAmount + amount).coerceAtMost(fee.totalAmount)
                val newStatus = if (newPaid >= fee.totalAmount) FeeStatus.PAID else FeeStatus.PARTIAL
                fee.copy(
                    paidAmount = newPaid,
                    status = newStatus,
                    receiptNo = "RCP-2026-" + (10000..99999).random(),
                    paymentDate = "2026-10-09",
                    paymentMode = "Card / Online"
                )
            } else fee
        }

        // Update student pending
        val fee = _fees.value.find { it.id == recordId } ?: return
        val studentFees = _fees.value.filter { it.studentId == fee.studentId }
        val totalDue = studentFees.sumOf { it.totalAmount - it.paidAmount }
        _students.value = _students.value.map {
            if (it.id == fee.studentId) {
                it.copy(
                    pendingAmount = totalDue,
                    feeStatus = if (totalDue <= 0.0) FeeStatus.PAID else FeeStatus.PARTIAL
                )
            } else it
        }
    }

    // --- Remarks & Feedback ---
    fun addRemark(studentId: String, teacherId: String, category: String, comment: String) {
        val student = _students.value.find { it.id == studentId } ?: return
        val teacher = _teachers.value.find { it.id == teacherId } ?: return
        val newRemark = StudentRemark(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = student.name,
            teacherId = teacherId,
            teacherName = teacher.name,
            date = "2026-10-09",
            category = category,
            comment = comment
        )
        _remarks.value = listOf(newRemark) + _remarks.value
    }

    // --- Timetable & Clash Detection ---
    data class ClashResult(
        val hasClash: Boolean,
        val reason: String = ""
    )

    fun checkTimetableClash(
        entry: TimetableEntry,
        excludeEntryId: String? = null
    ): ClashResult {
        val existing = _timetable.value.filter { it.id != excludeEntryId }

        // Check Teacher Clash
        val teacherClash = existing.find {
            it.dayOfWeek == entry.dayOfWeek &&
            it.periodIndex == entry.periodIndex &&
            it.teacherId == entry.teacherId
        }
        if (teacherClash != null) {
            return ClashResult(
                hasClash = true,
                reason = "Teacher ${teacherClash.teacherName} is already assigned to ${teacherClash.subjectName} in Room ${teacherClash.roomNumber} on ${entry.dayOfWeek} Period ${entry.periodIndex}!"
            )
        }

        // Check Room Clash
        val roomClash = existing.find {
            it.dayOfWeek == entry.dayOfWeek &&
            it.periodIndex == entry.periodIndex &&
            it.roomNumber.equals(entry.roomNumber, ignoreCase = true)
        }
        if (roomClash != null) {
            return ClashResult(
                hasClash = true,
                reason = "Room ${entry.roomNumber} is already occupied by ${roomClash.subjectName} (${roomClash.teacherName}) on ${entry.dayOfWeek} Period ${entry.periodIndex}!"
            )
        }

        // Check Class/Semester Clash
        val classClash = existing.find {
            it.dayOfWeek == entry.dayOfWeek &&
            it.periodIndex == entry.periodIndex &&
            it.departmentId == entry.departmentId &&
            it.semester == entry.semester
        }
        if (classClash != null) {
            return ClashResult(
                hasClash = true,
                reason = "Semester ${entry.semester} already has ${classClash.subjectName} scheduled at this time!"
            )
        }

        return ClashResult(hasClash = false)
    }

    fun saveTimetableEntry(entry: TimetableEntry): Result<Unit> {
        val clash = checkTimetableClash(entry, entry.id)
        if (clash.hasClash) {
            return Result.failure(IllegalArgumentException(clash.reason))
        }

        val list = _timetable.value.toMutableList()
        val index = list.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            list[index] = entry
        } else {
            list.add(entry)
        }
        _timetable.value = list
        return Result.success(Unit)
    }

    fun deleteTimetableEntry(id: String) {
        _timetable.value = _timetable.value.filter { it.id != id }
    }

    // --- Academic Calendar Events ---
    fun addCalendarEvent(event: AcademicEvent) {
        _calendarEvents.value = listOf(event) + _calendarEvents.value
    }

    fun deleteCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.filter { it.id != id }
    }

    // --- Admin Management ---
    fun addStudent(student: Student) {
        _students.value = _students.value + student
    }

    fun updateStudent(student: Student) {
        _students.value = _students.value.map { if (it.id == student.id) student else it }
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }
    }

    fun addTeacher(teacher: Teacher) {
        _teachers.value = _teachers.value + teacher
    }

    fun updateTeacher(teacher: Teacher) {
        _teachers.value = _teachers.value.map { if (it.id == teacher.id) teacher else it }
    }

    fun deleteTeacher(teacherId: String) {
        _teachers.value = _teachers.value.filter { it.id != teacherId }
    }

    fun addSubject(subject: Subject) {
        _subjects.value = _subjects.value + subject
    }

    // --- Chat Messages ---
    fun addChatMessage(msg: ChatMessage) {
        _chatMessages.value = _chatMessages.value + msg
    }

    // --- AI Prediction Algorithm Engine ---
    fun getAIPredictionForStudent(studentId: String): AIPrediction {
        val student = _students.value.find { it.id == studentId }
            ?: _students.value.first()
        val studentMarks = _marks.value.filter { it.studentId == student.id }
        val studentAtt = student.attendancePercentage

        // Academic performance base
        val avgInternalScore = if (studentMarks.isNotEmpty()) {
            studentMarks.map { (it.marksObtained / it.maxMarks) * 100.0 }.average()
        } else {
            student.cgpa * 9.5
        }

        // Predictive weighted formula
        // Final prediction = 0.40 * Internal Marks + 0.35 * Attendance% + 0.15 * CGPA scale + 0.10 * Assignment Regularity
        val feePenalty = if (student.feeStatus == FeeStatus.DUE) 2.5 else 0.0
        val predictedPct = ((0.42 * avgInternalScore) + (0.38 * studentAtt) + (0.20 * (student.cgpa * 9.5)) - feePenalty)
            .coerceIn(25.0, 99.4)

        val riskLevel = when {
            studentAtt < 75.0 || predictedPct < 55.0 || avgInternalScore < 45.0 -> RiskLevel.HIGH
            studentAtt < 82.0 || predictedPct < 70.0 || avgInternalScore < 65.0 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val passProbability = when (riskLevel) {
            RiskLevel.LOW -> (94.0 + (predictedPct * 0.05)).coerceAtMost(99.0)
            RiskLevel.MEDIUM -> (75.0 + (predictedPct * 0.15)).coerceIn(60.0, 89.0)
            RiskLevel.HIGH -> (32.0 + (predictedPct * 0.25)).coerceIn(20.0, 58.0)
        }

        val predictedGrade = when {
            predictedPct >= 85 -> "A+"
            predictedPct >= 75 -> "A"
            predictedPct >= 65 -> "B+"
            predictedPct >= 55 -> "B"
            predictedPct >= 45 -> "C"
            else -> "F"
        }

        val topFactors = mutableListOf<String>()
        if (studentAtt < 75.0) {
            topFactors.add("CRITICAL: Attendance (${studentAtt}%) is below institutional 75% threshold (-14% impact)")
        } else if (studentAtt >= 85.0) {
            topFactors.add("Consistent attendance (${studentAtt}%) provides strong positive correlation (+8% boost)")
        } else {
            topFactors.add("Attendance (${studentAtt}%) is borderline; 3 missed lectures could push to high risk")
        }

        if (avgInternalScore < 50.0) {
            topFactors.add("Mid-term exam scores (${Math.round(avgInternalScore)}%) reflect difficulty in core algorithms")
        } else if (avgInternalScore >= 80.0) {
            topFactors.add("High internal test average (${Math.round(avgInternalScore)}%) demonstrates strong conceptual grasp")
        } else {
            topFactors.add("Internal assessment marks are steady at ${Math.round(avgInternalScore)}%")
        }

        if (student.pendingAmount > 0) {
            topFactors.add("Unsettled examination fee ($${student.pendingAmount.toInt()}) poses registration hold risk")
        } else {
            topFactors.add("Academic registration and fee clearance confirmed")
        }

        val suggestions = mutableListOf<String>()
        when (riskLevel) {
            RiskLevel.HIGH -> {
                suggestions.add("Mandatory faculty tutorial for algorithms and calculus modules")
                suggestions.add("Attend all upcoming review labs to push attendance above 75%")
                suggestions.add("Utilize peer study pods and review solved question banks")
                suggestions.add("Schedule meeting with academic counselor regarding study workload")
            }
            RiskLevel.MEDIUM -> {
                suggestions.add("Target 4 focused study hours/week in Database Management Systems")
                suggestions.add("Maintain current lecture streak to reach 85% attendance tier")
                suggestions.add("Complete weekly self-assessment tests before mid-term revision")
                suggestions.add("Clarify doubts early in lab sessions")
            }
            RiskLevel.LOW -> {
                suggestions.add("Explore advanced research topics & honors projects in CSE")
                suggestions.add("Target A+ grade by perfecting theoretical proofs in Networks")
                suggestions.add("Participate in inter-college competitive coding & hackathons")
            }
        }

        val studyPlan = listOf(
            StudyDayPlan("Monday", "Data Structures", 2.0, listOf("Trees & Graphs", "Dynamic Programming Practice")),
            StudyDayPlan("Tuesday", "Operating Systems", 1.5, listOf("Process Scheduling", "Memory Management")),
            StudyDayPlan("Wednesday", "Database Systems", 2.0, listOf("SQL Queries", "Normalization 3NF/BCNF")),
            StudyDayPlan("Thursday", "Computer Networks", 1.5, listOf("TCP/IP Protocols", "Routing Algorithms")),
            StudyDayPlan("Friday", "Linear Algebra", 2.0, listOf("Eigenvalues & Vectors", "Matrix Transformations")),
            StudyDayPlan("Saturday", "Software Engineering & Mock Exam", 2.5, listOf("Agile Methodologies", "Full-length Timed Quiz")),
            StudyDayPlan("Sunday", "Weekly Review & Rest", 1.0, listOf("Consolidate Notes", "Flashcard Review"))
        )

        val trendHistory = listOf(
            TrendPoint("Sem 1", 78.5, 76.0),
            TrendPoint("Sem 2", 82.0, 80.5),
            TrendPoint("Sem 3", student.cgpa * 9.5 - 2.0, student.cgpa * 9.5 - 3.0),
            TrendPoint("Mid-Term", avgInternalScore, (student.cgpa * 9.5 + avgInternalScore) / 2.0),
            TrendPoint("Predicted Final", null, Math.round(predictedPct * 10.0) / 10.0)
        )

        val subjectStrengths = _subjects.value.map { subj ->
            val subMark = studentMarks.find { it.subjectId == subj.id }
            val score = subMark?.let { (it.marksObtained / it.maxMarks) * 100.0 } ?: (student.cgpa * 9.5 + (-5..5).random())
            val classAvg = 72.0
            val status = when {
                score >= 80 -> "Strong"
                score >= 60 -> "Average"
                else -> "Needs Attention"
            }
            SubjectStrengthScore(
                subjectName = subj.name,
                score = Math.round(score.coerceIn(30.0, 98.0) * 10.0) / 10.0,
                classAverage = classAvg,
                status = status
            )
        }

        return AIPrediction(
            studentId = student.id,
            studentName = student.name,
            predictedFinalPercentage = Math.round(predictedPct * 10.0) / 10.0,
            predictedGrade = predictedGrade,
            passProbability = Math.round(passProbability * 10.0) / 10.0,
            riskLevel = riskLevel,
            topFactors = topFactors,
            improvementSuggestions = suggestions,
            weeklyStudyPlan = studyPlan,
            trendHistory = trendHistory,
            subjectStrengths = subjectStrengths
        )
    }

    // --- Seed Data Generator ---
    private fun seedInitialData() {
        // 1. Departments
        val deptList = listOf(
            Department("DEP_CSE", "CSE", "Computer Science & Engineering", "Dr. Rajesh Sharma", 120, 18),
            Department("DEP_ECE", "ECE", "Electronics & Communication", "Prof. Ananya Iyer", 95, 14),
            Department("DEP_IT", "IT", "Information Technology", "Dr. Vikram Malhotra", 80, 12)
        )
        _departments.value = deptList

        // 2. Teachers
        val teacherList = listOf(
            Teacher(
                id = "TCH101",
                name = "Dr. Rajesh Sharma",
                email = "rajesh.sharma@edupredict.edu",
                departmentId = "DEP_CSE",
                departmentName = "Computer Science & Engineering",
                designation = "Professor & Head of Dept",
                subjectsTaught = listOf("Data Structures & Algorithms", "Advanced Algorithms"),
                phone = "+1 (555) 432-1001"
            ),
            Teacher(
                id = "TCH102",
                name = "Prof. Ananya Iyer",
                email = "ananya.iyer@edupredict.edu",
                departmentId = "DEP_ECE",
                departmentName = "Electronics & Communication",
                designation = "Associate Professor",
                subjectsTaught = listOf("Computer Networks", "Digital Electronics"),
                phone = "+1 (555) 432-1002"
            ),
            Teacher(
                id = "TCH103",
                name = "Dr. Vikram Malhotra",
                email = "vikram.malhotra@edupredict.edu",
                departmentId = "DEP_CSE",
                departmentName = "Computer Science & Engineering",
                designation = "Assistant Professor",
                subjectsTaught = listOf("Database Management Systems", "Big Data Analytics"),
                phone = "+1 (555) 432-1003"
            ),
            Teacher(
                id = "TCH104",
                name = "Prof. Neha Gupta",
                email = "neha.gupta@edupredict.edu",
                departmentId = "DEP_IT",
                departmentName = "Information Technology",
                designation = "Assistant Professor",
                subjectsTaught = listOf("Software Engineering", "Cloud Computing"),
                phone = "+1 (555) 432-1004"
            ),
            Teacher(
                id = "TCH105",
                name = "Dr. Amit Patel",
                email = "amit.patel@edupredict.edu",
                departmentId = "DEP_CSE",
                departmentName = "Computer Science & Engineering",
                designation = "Senior Lecturer",
                subjectsTaught = listOf("Operating Systems", "Linear Algebra & Calculus"),
                phone = "+1 (555) 432-1005"
            )
        )
        _teachers.value = teacherList

        // 3. Subjects
        val subjectList = listOf(
            Subject("SUB101", "CS301", "Data Structures & Algorithms", "DEP_CSE", 4, "TCH101", "Dr. Rajesh Sharma", 5),
            Subject("SUB102", "CS302", "Operating Systems", "DEP_CSE", 4, "TCH105", "Dr. Amit Patel", 5),
            Subject("SUB103", "CS303", "Database Management Systems", "DEP_CSE", 3, "TCH103", "Dr. Vikram Malhotra", 5),
            Subject("SUB104", "EC304", "Computer Networks", "DEP_ECE", 3, "TCH102", "Prof. Ananya Iyer", 5),
            Subject("SUB105", "IT305", "Software Engineering", "DEP_IT", 3, "TCH104", "Prof. Neha Gupta", 5),
            Subject("SUB106", "MA306", "Linear Algebra & Calculus", "DEP_CSE", 4, "TCH105", "Dr. Amit Patel", 5)
        )
        _subjects.value = subjectList

        // 4. 30 Realistic Students
        val studentNames = listOf(
            Triple("Aarav Patel", 8.85, 92.4),      // STU2026001 - Low Risk
            Triple("Priya Sharma", 7.20, 78.5),     // STU2026002 - Medium Risk
            Triple("Rohan Verma", 5.40, 64.2),      // STU2026003 - High Risk
            Triple("Ananya Reddy", 9.15, 95.0),     // STU2026004 - Low Risk
            Triple("Kabir Mehta", 6.10, 68.0),      // STU2026005 - High Risk
            Triple("Ishita Sen", 8.40, 89.2),       // STU2026006 - Low Risk
            Triple("Devansh Nair", 7.65, 82.0),     // STU2026007 - Low Risk
            Triple("Sneha Kulkarni", 5.80, 71.5),   // STU2026008 - High Risk
            Triple("Aditya Deshmukh", 8.05, 86.4),  // STU2026009 - Low Risk
            Triple("Tanvi Joshi", 6.90, 74.0),      // STU2026010 - High/Medium
            Triple("Karan Singhania", 9.30, 96.5),  // STU2026011 - Low Risk
            Triple("Meera Pillai", 7.45, 80.2),     // STU2026012 - Low Risk
            Triple("Aryan Kapoor", 5.25, 62.0),     // STU2026013 - High Risk
            Triple("Dia Mukherjee", 8.70, 91.0),    // STU2026014 - Low Risk
            Triple("Yash Chawla", 6.40, 72.8),      // STU2026015 - Medium Risk
            Triple("Rhea Bhatia", 8.15, 87.5),      // STU2026016 - Low Risk
            Triple("Varun Saxena", 7.30, 79.0),     // STU2026017 - Medium Risk
            Triple("Pooja Hegde", 5.60, 66.4),      // STU2026018 - High Risk
            Triple("Siddharth Roy", 8.95, 93.8),    // STU2026019 - Low Risk
            Triple("Nandini Bose", 7.80, 84.5),     // STU2026020 - Low Risk
            Triple("Harshvardhan Rao", 6.20, 69.5), // STU2026021 - High Risk
            Triple("Tara Nambiar", 8.50, 88.0),     // STU2026022 - Low Risk
            Triple("Arjun Bansal", 7.10, 76.2),     // STU2026023 - Medium Risk
            Triple("Bhavna Mishra", 9.05, 94.2),    // STU2026024 - Low Risk
            Triple("Sameer Merchant", 5.10, 61.5),  // STU2026025 - High Risk
            Triple("Divya Raghavan", 8.25, 86.0),   // STU2026026 - Low Risk
            Triple("Nikhil Varma", 7.50, 81.4),     // STU2026027 - Low Risk
            Triple("Kritika Aggarwal", 6.75, 73.5), // STU2026028 - Medium Risk
            Triple("Manish Tiwari", 8.60, 90.5),    // STU2026029 - Low Risk
            Triple("Simran Kaur", 5.75, 67.8)       // STU2026030 - High Risk
        )

        val studentList = studentNames.mapIndexed { idx, (name, cgpa, att) ->
            val numStr = String.format("%03d", idx + 1)
            val stuId = "STU2026$numStr"
            val dept = when (idx % 3) {
                0 -> "DEP_CSE" to "Computer Science & Engineering"
                1 -> "DEP_ECE" to "Electronics & Communication"
                else -> "DEP_IT" to "Information Technology"
            }
            val feeDue = when {
                att < 72.0 -> 1500.0
                idx % 4 == 0 -> 0.0
                idx % 5 == 0 -> 450.0
                else -> 0.0
            }
            val feeStatus = when {
                feeDue > 1000.0 -> FeeStatus.DUE
                feeDue > 0.0 -> FeeStatus.PARTIAL
                else -> FeeStatus.PAID
            }

            Student(
                id = stuId,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@edupredict.edu",
                departmentId = dept.first,
                departmentName = dept.second,
                semester = 5,
                section = if (idx % 2 == 0) "A" else "B",
                rollNo = "2026-CSE-$numStr",
                phone = "+1 (555) 789-${1000 + idx}",
                cgpa = cgpa,
                attendancePercentage = att,
                feeStatus = feeStatus,
                pendingAmount = feeDue
            )
        }
        _students.value = studentList

        // 5. Attendance Records (1 semester seed for students)
        val sampleDates = listOf("2026-09-08", "2026-09-15", "2026-09-22", "2026-09-29", "2026-10-06")
        val attRecords = mutableListOf<AttendanceRecord>()
        studentList.take(10).forEach { st ->
            sampleDates.forEach { d ->
                subjectList.forEach { sb ->
                    val status = when {
                        st.attendancePercentage > 85 -> AttendanceStatus.PRESENT
                        st.attendancePercentage > 75 -> if ((0..10).random() > 2) AttendanceStatus.PRESENT else AttendanceStatus.LATE
                        else -> if ((0..10).random() > 5) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
                    }
                    attRecords.add(
                        AttendanceRecord(
                            id = UUID.randomUUID().toString(),
                            studentId = st.id,
                            studentName = st.name,
                            subjectId = sb.id,
                            subjectName = sb.name,
                            date = d,
                            status = status
                        )
                    )
                }
            }
        }
        _attendance.value = attRecords

        // 6. Exam Results (Multi-semester: Sem 1, Sem 2, Sem 3, Sem 4, Sem 5)
        val markRecords = mutableListOf<MarkRecord>()
        val pastSemSubjects = mapOf(
            1 to listOf("Mathematics I", "Physics for Computing", "Basic Electrical Eng.", "Programming in C", "English Communication"),
            2 to listOf("Mathematics II", "Digital Logic Design", "Data Structures Fundamentals", "Environmental Science", "Object Oriented Prog."),
            3 to listOf("Discrete Mathematics", "Computer Organization", "Data Structures & Algorithms", "Object Oriented Design", "Principles of Programming"),
            4 to listOf("Design & Analysis of Algorithms", "Database Management Systems", "Theory of Computation", "Microprocessors", "Probability & Statistics")
        )

        studentList.forEach { st ->
            // Past Semesters 1 to 4
            pastSemSubjects.forEach { (sem, subjList) ->
                subjList.forEachIndexed { sIdx, subjName ->
                    val semBase = (st.cgpa * 9.5 + (-4..4).random()).coerceIn(38.0, 96.0)
                    val grade = when {
                        semBase >= 90 -> "A+"
                        semBase >= 80 -> "A"
                        semBase >= 70 -> "B+"
                        semBase >= 60 -> "B"
                        semBase >= 50 -> "C"
                        semBase >= 40 -> "D"
                        else -> "F"
                    }
                    markRecords.add(
                        MarkRecord(
                            id = "MRK_S${sem}_${st.id}_$sIdx",
                            studentId = st.id,
                            studentName = st.name,
                            subjectId = "SUB_S${sem}_$sIdx",
                            subjectName = subjName,
                            semester = sem,
                            examType = "End-Semester Examination",
                            maxMarks = 100,
                            marksObtained = Math.round(semBase * 10.0) / 10.0,
                            grade = grade
                        )
                    )
                }
            }

            // Current Semester 5
            subjectList.forEach { sb ->
                val baseScore = (st.cgpa * 9.5).coerceIn(40.0, 96.0)
                // Internal 1 (Max 50)
                val internalMarks = Math.round(((baseScore / 100.0 * 50.0) + (-3..3).random()).coerceIn(18.0, 49.0) * 10.0) / 10.0
                markRecords.add(
                    MarkRecord(
                        id = UUID.randomUUID().toString(),
                        studentId = st.id,
                        studentName = st.name,
                        subjectId = sb.id,
                        subjectName = sb.name,
                        semester = 5,
                        examType = "Internal Assessment 1",
                        maxMarks = 50,
                        marksObtained = internalMarks,
                        grade = if (internalMarks >= 42) "A" else if (internalMarks >= 30) "B" else "C"
                    )
                )

                // Mid-Term (Max 100)
                val midtermMarks = Math.round((baseScore + (-5..5).random()).coerceIn(32.0, 98.0) * 10.0) / 10.0
                markRecords.add(
                    MarkRecord(
                        id = UUID.randomUUID().toString(),
                        studentId = st.id,
                        studentName = st.name,
                        subjectId = sb.id,
                        subjectName = sb.name,
                        semester = 5,
                        examType = "Mid-Term Examination",
                        maxMarks = 100,
                        marksObtained = midtermMarks,
                        grade = when {
                            midtermMarks >= 90 -> "A+"
                            midtermMarks >= 80 -> "A"
                            midtermMarks >= 70 -> "B+"
                            midtermMarks >= 60 -> "B"
                            midtermMarks >= 50 -> "C"
                            else -> "F"
                        }
                    )
                )
            }
        }
        _marks.value = markRecords

        // 7. Fee Records
        val feeRecords = mutableListOf<FeeRecord>()
        studentList.forEachIndexed { idx, st ->
            val tuitionAmount = 4500.0
            val libraryAmount = 350.0
            val labAmount = 600.0
            val examAmount = 450.0

            val tuitionPaid = if (st.pendingAmount > 1000.0) 3000.0 else tuitionAmount
            feeRecords.add(
                FeeRecord(
                    id = "FEE-TUI-${st.id}",
                    studentId = st.id,
                    studentName = st.name,
                    feeType = "Tuition Fee",
                    totalAmount = tuitionAmount,
                    paidAmount = tuitionPaid,
                    dueDate = "2026-10-15",
                    status = if (tuitionPaid >= tuitionAmount) FeeStatus.PAID else FeeStatus.PARTIAL,
                    receiptNo = if (tuitionPaid > 0) "RCP-2026-T-${1000 + idx}" else null,
                    paymentDate = if (tuitionPaid > 0) "2026-08-20" else null,
                    paymentMode = "Bank Transfer"
                )
            )

            feeRecords.add(
                FeeRecord(
                    id = "FEE-EXM-${st.id}",
                    studentId = st.id,
                    studentName = st.name,
                    feeType = "Exam Fee",
                    totalAmount = examAmount,
                    paidAmount = if (st.pendingAmount > 0) 0.0 else examAmount,
                    dueDate = "2026-11-01",
                    status = if (st.pendingAmount > 0) FeeStatus.DUE else FeeStatus.PAID,
                    receiptNo = if (st.pendingAmount == 0.0) "RCP-2026-E-${1000 + idx}" else null,
                    paymentDate = if (st.pendingAmount == 0.0) "2026-09-02" else null,
                    paymentMode = "Online UPI"
                )
            )

            feeRecords.add(
                FeeRecord(
                    id = "FEE-LIB-${st.id}",
                    studentId = st.id,
                    studentName = st.name,
                    feeType = "Library & Digital Resources",
                    totalAmount = libraryAmount,
                    paidAmount = libraryAmount,
                    dueDate = "2026-09-30",
                    status = FeeStatus.PAID,
                    receiptNo = "RCP-2026-L-${1000 + idx}",
                    paymentDate = "2026-08-15",
                    paymentMode = "Campus Card"
                )
            )
        }
        _fees.value = feeRecords

        // 8. Timetable
        val timeSlots = listOf(
            "09:00 - 10:00 AM",
            "10:00 - 11:00 AM",
            "11:15 - 12:15 PM",
            "01:00 - 02:00 PM",
            "02:00 - 03:00 PM",
            "03:15 - 04:15 PM"
        )
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        val rooms = listOf("LH-101", "LH-102", "CS-Lab 1", "LH-103", "Networks Lab", "Seminar Hall")

        val ttList = mutableListOf<TimetableEntry>()
        var entryCounter = 1
        days.forEach { day ->
            timeSlots.forEachIndexed { pIdx, slot ->
                val subj = subjectList[(day.length + pIdx) % subjectList.size]
                val room = rooms[pIdx % rooms.size]
                ttList.add(
                    TimetableEntry(
                        id = "TT_${entryCounter++}",
                        dayOfWeek = day,
                        periodIndex = pIdx + 1,
                        timeSlot = slot,
                        subjectId = subj.id,
                        subjectName = subj.name,
                        teacherId = subj.teacherId,
                        teacherName = subj.teacherName,
                        roomNumber = room,
                        departmentId = subj.departmentId,
                        semester = subj.semester
                    )
                )
            }
        }
        _timetable.value = ttList

        // 9. Academic Calendar Events
        val events = listOf(
            AcademicEvent("EVT1", "Mid-Term Examination Week", EventType.EXAM, "2026-10-20", "2026-10-27", "Comprehensive mid-semester written assessments for all registered subjects."),
            AcademicEvent("EVT2", "Diwali & Autumn Break", EventType.HOLIDAY, "2026-11-01", "2026-11-06", "Official campus closure for festival recess."),
            AcademicEvent("EVT3", "EduPredict AI Hackathon 2026", EventType.EVENT, "2026-11-14", "2026-11-15", "48-hour student innovation sprint in the Main Auditorium.", "Auditorium"),
            AcademicEvent("EVT4", "Assignment #4 Submission Deadline", EventType.ASSIGNMENT, "2026-10-18", "2026-10-18", "Data Structures Dynamic Programming project repository submission."),
            AcademicEvent("EVT5", "End-Semester Practical Labs", EventType.EXAM, "2026-12-05", "2026-12-12", "Practical viva-voce and laboratory examination."),
            AcademicEvent("EVT6", "Winter Semester Graduation & Commencement", EventType.MILESTONE, "2026-12-28", "2026-12-28", "Annual institutional convocation ceremony.")
        )
        _calendarEvents.value = events

        // 10. Remarks Seed
        _remarks.value = listOf(
            StudentRemark(
                id = "RMK1",
                studentId = "STU2026001",
                studentName = "Aarav Patel",
                teacherId = "TCH101",
                teacherName = "Dr. Rajesh Sharma",
                date = "2026-10-05",
                category = "Commendation",
                comment = "Outstanding performance in Data Structures AVL Tree implementation. Recommended for teaching assistantship."
            ),
            StudentRemark(
                id = "RMK2",
                studentId = "STU2026003",
                studentName = "Rohan Verma",
                teacherId = "TCH105",
                teacherName = "Dr. Amit Patel",
                date = "2026-10-04",
                category = "Attendance",
                comment = "Frequent unexcused absences noted in morning Calculus lecture. Risk of falling below semester eligibility."
            )
        )

        // 11. Initial AI Chat message
        _chatMessages.value = listOf(
            ChatMessage(
                id = "MSG_WELCOME",
                isUser = false,
                message = "Hello! I am **EduPredict AI**, your predictive academic advisor. I analyze attendance telemetry, internal test scores, syllabus coverage, and performance trends to help you excel. How can I assist you today?",
                timestamp = "Just now",
                roleContext = UserRole.STUDENT
            )
        )

        // Initial session is logged out so user starts at the Login page
        _currentUser.value = null
    }
}
