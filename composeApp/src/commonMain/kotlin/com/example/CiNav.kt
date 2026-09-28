package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.planning.AnnualLessonItem
import com.example.ui.components.OfficialStampVisual
import com.example.ui.screens.ConcoursGuideScreen
import com.example.ui.screens.ExamProposalsScreen
import com.example.ui.screens.LessonProposalScreen

/**
 * تنقّل آلي بين الشاشات أثناء الاختبار على المحاكي فقط (قيمه فارغة دائمًا في الاستعمال العادي).
 */
object CiNav {
    var insideTab by mutableStateOf<Int?>(null)
    var outerTab by mutableStateOf<Int?>(null)
    var overlay by mutableStateOf<String?>(null)
    var classId: Long = 0L

    @Composable
    fun Overlay() {
        val o = overlay ?: return
        Box(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
            when (o) {
                "lesson" -> LessonProposalScreen(
                    lesson = AnnualLessonItem(
                        id = "5AP|TIS-CORAN|K1|U01", level = 5, week = 2,
                        subject = "التربية الإسلامية", domain = "القرآن الكريم", title = "سورة الملك"
                    ),
                    currentWeek = 2, classId = classId, onBack = {}
                )
                "stamp" -> Column(
                    Modifier.fillMaxSize().padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    OfficialStampVisual(roleTitle = "المدير", personName = "محمد ولد أحمد", schoolName = "النجاح", shape = "circle")
                    OfficialStampVisual(roleTitle = "المعلم", personName = "سيدي ولد محمد", schoolName = "النجاح", shape = "rectangle", financialId = "123456")
                }
                "concours" -> ConcoursGuideScreen(onBack = {})
                "exams" -> ExamProposalsScreen(levelCode = "5AP", semaine = 12, subjectCode = "AR", onBack = {})
            }
        }
    }
}
