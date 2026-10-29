package it.iptv.wizard

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    // Il link diretto di Tailscale dal tuo Foglio
    private val tailscaleUrl = "https://pkgs.tailscale.com/stable/tailscale-android-universal-1.104.2.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTailscale = findViewById<Button>(R.id.btnTailscale)
        val btnAccessibilita = findViewById<Button>(R.id.btnAccessibilita)

        btnTailscale.setOnClickListener {
            try {
                // Chiediamo ad Android di scaricare il file
                val request = DownloadManager.Request(Uri.parse(tailscaleUrl))
                request.setTitle("Tailscale")
                request.setDescription("Download in corso...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "tailscale.apk")

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)

                Toast.makeText(this, "Download avviato! Attendi...", Toast.LENGTH_LONG).show()

                // Aspettiamo che il download finisca per aprire l'installazione
                val onComplete = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (uri != null) {
                                // Apriamo la schermata di installazione
                                val installIntent = Intent(Intent.ACTION_VIEW)
                                installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
                                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(installIntent)
                            }
                            unregisterReceiver(this)
                        }
                    }
                }
                registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)

            } catch (e: Exception) {
                Toast.makeText(this, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnAccessibilita.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Cerca 'IPTV Wizard' e attivalo", Toast.LENGTH_LONG).show()
        }
    }
}
