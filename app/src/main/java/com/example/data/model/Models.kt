package com.example.data.model

enum class UserRole {
    STUDENT, TEACHER, ADMIN
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE
}

enum class FeeStatus {
    PAID, PARTIAL, DUE
}

enum class EventType {
    EXAM, HOLIDAY, EVENT, ASSIGNMENT, MILESTONE
}

data class User(
    val id: String, // e.g. STU2026001, TCH101, ADM001
    val name: String,
    val email: String,
    val role: UserRole,
    val departmentId: String,
    val departmentName: String,
    val semesterOrClass: String,
    val phone: String,
    val avatarUrl: String = "",
    val password: String = "password123"
)

data class Department(
    val id: String,
    val code: String,
    val name: String,
    val headOfDepartment: String,
    val studentCount: Int,
    val facultyCount: Int
)

data class Subject(
    val id: String,
    val code: String,
    val name: String,
    val departmentId: String,
    val credits: Int,
    val teacherId: String,
    val teacherName: String,
    val semester: Int
)

data class Student(
    val id: String,
    val name: String,
    val email: String,
    val departmentId: String,
    val departmentName: String,
    val semester: Int,
    val section: String,
    val rollNo: String,
    val phone: String,
    val cgpa: Double,
    val attendancePercentage: Double,
    val feeStatus: FeeStatus,
    val pendingAmount: Double,
    val avatarUrl: String = ""
)

data class Teacher(
    val id: String,
    val name: String,
    val email: String,
    val departmentId: String,
    val departmentName: String,
    val designation: String,
    val subjectsTaught: List<String>,
    val phone: String,
    val avatarUrl: String = ""
)

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subjectId: String,
    val subjectName: String,
    val date: String, // YYYY-MM-DD
    val status: AttendanceStatus
)

data class MarkRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subjectId: String,
    val subjectName: String,
    val semester: Int = 5,
    val examType: String, // "Internal Assessment 1", "Mid-Term Examination", "End-Semester"
    val maxMarks: Int,
    val marksObtained: Double,
    val grade: String
)

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val feeType: String, // "Tuition Fee", "Exam Fee", "Library Fee", "Hostel Fee", "Laboratory Fee"
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String,
    val status: FeeStatus,
    val receiptNo: String? = null,
    val paymentDate: String? = null,
    val paymentMode: String? = null
)

data class TimetableEntry(
    val id: String,
    val dayOfWeek: String, // "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    val periodIndex: Int, // 1, 2, 3, 4, 5, 6
    val timeSlot: String, // e.g. "09:00 - 10:00 AM"
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,
    val roomNumber: String,
    val departmentId: String,
    val semester: Int
)

data class AcademicEvent(
    val id: String,
    val title: String,
    val eventType: EventType,
    val startDate: String, // YYYY-MM-DD
    val endDate: String,
    val description: String,
    val location: String = "Campus Main",
    val departmentFilter: String = "All"
)

data class StudyDayPlan(
    val day: String,
    val focusSubject: String,
    val hours: Double,
    val keyTopics: List<String>
)

data class TrendPoint(
    val label: String, // "Sem 1", "Sem 2", "Sem 3", "Mid-Term", "Predicted Final"
    val actualScore: Double?,
    val predictedScore: Double
)

data class SubjectStrengthScore(
    val subjectName: String,
    val score: Double, // 0..100
    val classAverage: Double,
    val status: String // "Strong", "Average", "Needs Attention"
)

data class AIPrediction(
    val studentId: String,
    val studentName: String,
    val predictedFinalPercentage: Double,
    val predictedGrade: String,
    val passProbability: Double,
    val riskLevel: RiskLevel,
    val topFactors: List<String>,
    val improvementSuggestions: List<String>,
    val weeklyStudyPlan: List<StudyDayPlan>,
    val trendHistory: List<TrendPoint>,
    val subjectStrengths: List<SubjectStrengthScore>,
    val disclaimer: String = "EduPredict AI predictions are probabilistic estimations calculated from current semester telemetry (attendance, internal exams, assignments) and are not academic guarantees."
)

data class StudentRemark(
    val id: String,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val teacherName: String,
    val date: String,
    val category: String, // "Academic", "Attendance", "Discipline", "Commendation"
    val comment: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val timestamp: String,
    val roleContext: UserRole
)
