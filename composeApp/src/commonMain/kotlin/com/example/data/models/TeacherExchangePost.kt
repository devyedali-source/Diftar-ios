package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teacher_exchange_posts")
data class TeacherExchangePost(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val offerType: String = "تبادل مقاعد ودي دون دفع", // "بيع مقعد مكان عمله", "بحث عن شراء مقعد", "تبادل مقاعد ودي دون دفع"
    val priceAmount: String = "", // e.g. "50000" or empty
    val isNegotiable: Boolean = false, // true if "للنقاش في الخاص"
    val specialty: String = "معلم عربية", // "معلم عربية", "معلم فرنسية", "معلم مزدوج", "مقدم خدمة عربي", "مقدم خدمة فرنسي", "مقدم خدمة مزدوج"
    val currentWilaya: String = "",
    val currentMoughataa: String = "",
    val currentCommune: String = "",
    val currentWorkPlace: String = "",
    val schoolName: String = "",
    val whatsappPhone: String = "",
    val targetWilayas: String = "", // Comma-separated list of target Wilayas
    val targetMoughataas: String = "", // Formatted as "Wilaya1:Moughataa1,Moughataa2|Wilaya2:الكل"
    val isMoughataaTeacher: Boolean = false, // معلم مقاطعي
    val isSchoolPrincipal: Boolean = false, // مدير مدرسة
    val isApproved: Boolean = false, // هل تم تفعيل المنشور للعامة بواسطة مدير الأقسام
    val timestamp: Long = com.example.platform.currentTimeMillis(),
    val isSynced: Boolean = true
)
