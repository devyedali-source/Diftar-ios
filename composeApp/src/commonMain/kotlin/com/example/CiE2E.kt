package com.example

import com.example.compat.AppApplication
import com.example.compat.PlatformApi
import com.example.data.models.sortedByOfficialOrder
import com.example.ui.HtmlReportHelper
import com.example.ui.TeacherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * اختبار آلي شامل يعمل على المحاكي فقط (عند تشغيله من GitHub Actions بمفتاح DAFTAR_CI_E2E):
 * ينشئ قسمًا تجريبيًا، يملؤه بطلاب ودرجات، ثم يولّد الكشوف PDF لمراجعتها.
 * لا يعمل أبدًا على هاتف حقيقي.
 */
object CiE2E {
    private var started = false

    fun maybeRun(vm: TeacherViewModel) {
        if (started || !PlatformApi.ciFlag("DAFTAR_CI_E2E")) return
        started = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                delay(4000)
                PlatformApi.log("CI", "creating class")
                vm.addClassSection("مدرسة النجاح الابتدائية", 5, "أ", "نواكشوط الغربية", "تفرغ زينة", "2026-2027")
                delay(4000)
                val cls = vm.classSections.value.firstOrNull() ?: run { PlatformApi.log("CI", "no class"); return@launch }
                vm.selectClass(cls.id)
                delay(2000)
                vm.fillActiveClassWithDemoStudents({ PlatformApi.log("CI", "demo ok") }, { PlatformApi.log("CI", "demo fail $it") })
                delay(12000)
                vm.selectTerm(1)
                delay(4000)
                val subjects = vm.buildClassSubjects(cls.id, cls.level, vm.subjects.value, vm.customizations.value).sortedByOfficialOrder()
                val perf = vm.currentClassPerformance.value
                PlatformApi.log("CI", "students=${vm.students.value.size} subjects=${subjects.size} perf=${perf.size}")
                val dir = PlatformApi.filesDir()
                val cards = HtmlReportHelper.generateReportCardsHtml(AppApplication, cls.name, perf, subjects, 1, cls, vm)
                PlatformApi.writeFile("$dir/ci_report_cards.html", cards.encodeToByteArray())
                PlatformApi.renderHtmlToPdfFile(cards, true, "$dir/ci_report_cards.pdf") { ok, err -> PlatformApi.log("CI", "cards pdf $ok $err") }
                delay(8000)
                val ledger = HtmlReportHelper.generateDetailedTermLedgerHtml(AppApplication, cls.name, perf, subjects, 1, cls, vm)
                PlatformApi.renderHtmlToPdfFile(ledger, true, "$dir/ci_ledger.pdf") { ok, err -> PlatformApi.log("CI", "ledger pdf $ok $err") }
                delay(8000)
                PlatformApi.writeFile("$dir/ci_done.txt", "done".encodeToByteArray())
            } catch (e: Throwable) {
                PlatformApi.log("CI", "E2E error: ${e.stackTraceToString()}")
                PlatformApi.writeFile(PlatformApi.filesDir() + "/ci_error.txt", e.stackTraceToString().encodeToByteArray())
            }
        }
    }
}
