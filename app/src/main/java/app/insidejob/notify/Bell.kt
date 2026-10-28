package app.insidejob.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.insidejob.MainActivity
import app.insidejob.R
import app.insidejob.container
import app.insidejob.domain.day
import app.insidejob.localized
import app.insidejob.ru
import app.insidejob.ui.Words
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** The Shadow's evening round, «Когда сегодня звучала охрана?», only on days with nothing logged. */
object Bell {
    private const val CHANNEL = "round"
    private const val WORK = "round"

    fun channel(c: Context) {
        val ch = NotificationChannel(CHANNEL, c.localized().let { if (it.ru) "Вечерний обход" else "Evening round" }, NotificationManager.IMPORTANCE_DEFAULT)
        c.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    /** The next time the clock reaches [hour] after [now]. */
    fun next(hour: Int, now: Long, zone: ZoneId): Long {
        val today = LocalDate.ofEpochDay(now.day(zone))
        val t = today.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
        return if (t > now) t else today.plusDays(1).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    }

    suspend fun plan(c: Context) {
        val app = c.applicationContext
        val st = app.container.settings.now()
        val wm = WorkManager.getInstance(app)
        if (!st.onboarded || st.bellHour < 0) { wm.cancelUniqueWork(WORK); return }
        val now = System.currentTimeMillis()
        val req = OneTimeWorkRequestBuilder<BellWorker>().setInitialDelay(next(st.bellHour, now, ZoneId.systemDefault()) - now, TimeUnit.MILLISECONDS).build()
        wm.enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, req)
    }

    fun post(c: Context, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val pi = PendingIntent.getActivity(c, 1, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_dial)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(c).notify(1, n)
    }
}

class BellWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val c = ctx.container
        val st = c.settings.now()
        val s = c.repo.refresh()
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val today = now.day(zone)
        val job = s.job
        if (st.onboarded && job != null && s.moves.none { it.at.day(zone) == today } && s.ops.none { it.doneAt > 0 && it.doneAt.day(zone) == today }) {
            val w = Words(ctx.localized().ru, st.fem, s.cat)
            val pending = s.pending.firstOrNull()
            val text = if (pending != null) w.t("Операция «${pending.task}» ждёт отчёта.", "The job “${pending.task}” is waiting for its report.")
            else w.t("Охрана сегодня говорила «${job.voice}»? Тень запишет, где и когда.", "Did the guard say “${job.voice}” today? The Shadow will note where and when.")
            Bell.post(ctx, w.t("Вечерний обход", "Evening round"), text)
        }
        Bell.plan(ctx)
        return Result.success()
    }
}
