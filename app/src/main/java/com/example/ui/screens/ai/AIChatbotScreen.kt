package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.api.GeminiApi
import com.example.data.model.ChatMessage
import com.example.data.model.UserRole
import com.example.data.repository.EduRepository
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AIChatbotScreen() {
    val currentUser by EduRepository.currentUser.collectAsState()
    val chatMessages by EduRepository.chatMessages.collectAsState()
    val user = currentUser ?: return
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val quickQuestions = remember(user.role) {
        when (user.role) {
            UserRole.STUDENT -> listOf(
                "How can I improve my maths & calculus score?",
                "Generate a 5-day study timetable for me",
                "What are my weakest subjects according to AI?",
                "How to increase my attendance to 85%?"
            )
            UserRole.TEACHER -> listOf(
                "Which students need attention this week?",
                "Generate guidance notes for students with <75% attendance",
                "Tips to improve class engagement in Data Structures",
                "Draft an early-warning notice for parents"
            )
            UserRole.ADMIN -> listOf(
                "Summarize institution-wide at-risk trends",
                "Which department has highest fee default rate?",
                "Recommend strategies to improve semester pass percentage",
                "How can we balance teacher classroom workload?"
            )
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isGenerating) return

        inputPrompt = ""
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            message = trimmed,
            timestamp = "Just now",
            roleContext = user.role
        )
        EduRepository.addChatMessage(userMsg)

        isGenerating = true
        coroutineScope.launch {
            listState.animateScrollToItem((chatMessages.size + 1).coerceAtLeast(0))

            val contextInfo = when (user.role) {
                UserRole.STUDENT -> {
                    val pred = EduRepository.getAIPredictionForStudent(user.id)
                    "Student: ${user.name}, Department: ${user.departmentName}, Expected Grade: ${pred.predictedGrade}, Expected Final: ${pred.predictedFinalPercentage}%, Risk: ${pred.riskLevel}"
                }
                UserRole.TEACHER -> {
                    "Teacher: ${user.name}, Designation: ${user.semesterOrClass}, Subjects: ${user.departmentName}"
                }
                UserRole.ADMIN -> {
                    "Administrator: ${user.name}, Institutional Governance Head"
                }
            }

            val response = GeminiApi.askChatbot(
                userMessage = trimmed,
                userRole = user.role,
                userName = user.name,
                contextInfo = contextInfo
            )

            isGenerating = false
            EduRepository.addChatMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    message = response,
                    timestamp = "Just now",
                    roleContext = user.role
                )
            )
            listState.animateScrollToItem((chatMessages.size + 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_chatbot_screen")
    ) {
        // Chat Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EduPredict AI Academic Advisor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Powered by Gemini Intelligence • Instant Contextual Feedback",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                ChatMessageItem(msg = msg)
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "EduPredict AI is generating response...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { q ->
                SuggestionChip(
                    onClick = { sendMessage(q) },
                    label = { Text(q, fontSize = 11.sp) }
                )
            }
        }

        // Input Field & Send Action
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = {
                        Text(
                            when (user.role) {
                                UserRole.STUDENT -> "Ask AI: 'How can I improve my maths score?'"
                                UserRole.TEACHER -> "Ask AI: 'Which students need attention?'"
                                UserRole.ADMIN -> "Ask AI: 'Summarize cohort predictions...'"
                            },
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = { sendMessage(inputPrompt) },
                    enabled = inputPrompt.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputPrompt.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputPrompt.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(msg: ChatMessage) {
    val isUser = msg.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = msg.message,
                    color = textColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Text(
            text = msg.timestamp,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp).padding(top = 2.dp)
        )
    }
}
