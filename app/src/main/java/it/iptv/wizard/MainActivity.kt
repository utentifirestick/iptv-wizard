package it.iptv.wizard

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val tailscaleUrl = "https://pkgs.tailscale.com/stable/tailscale-android-universal-1.104.2.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTailscale = findViewById<Button>(R.id.btnTailscale)
        val btnAccessibilita = findViewById<Button>(R.id.btnAccessibilita)

        // --- PULSANTE 1: INSTALLA TAILSCALE ---
        btnTailscale.setOnClickListener {
            try {
                Toast.makeText(this, "Avvio download di Tailscale...", Toast.LENGTH_SHORT).show()
                
                val request = DownloadManager.Request(Uri.parse(tailscaleUrl))
                request.setTitle("Tailscale")
                request.setDescription("Download in corso...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "tailscale.apk")

                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)

                val onComplete = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (uri != null) {
                                val installIntent = Intent(Intent.ACTION_VIEW)
                                installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
                                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(installIntent)
                            }
                            try { unregisterReceiver(this) } catch (e: Exception) {}
                        }
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Errore download: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- PULSANTE 2: ATTIVA AVVIO AUTOMATICO (Compatibile Fire OS) ---
        btnAccessibilita.setOnClickListener {
            var aperto = false
            
            // Tentativo 1: Schermata Accessibilità standard di Android
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                aperto = true
            } catch (e: Exception) {}

            // Tentativo 2: Impostazioni generali (Fire OS a volte nasconde l'accessibilità qui)
            if (!aperto) {
                try {
                    val intentGenerale = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intentGenerale)
                    Toast.makeText(this, "Cerca 'Accessibilità' nelle impostazioni.", Toast.LENGTH_LONG).show()
                    aperto = true
                } catch (e: Exception) {}
            }

            // Tentativo 3: Opzioni sviluppatore / Sicurezza di Fire OS
            if (!aperto) {
                try {
                    val intentFire = Intent("com.amazon.tv.settings.action.DEVICE_OPTIONS")
                    startActivity(intentFire)
                    Toast.makeText(this, "Cerca 'Accessibilità' nelle opzioni del dispositivo.", Toast.LENGTH_LONG).show()
                    aperto = true
                } catch (e: Exception) {}
            }

            // Tentativo 4: Se proprio non trova nulla, mostra un messaggio
            if (!aperto) {
                Toast.makeText(this, "Impossibile aprire le impostazioni. Vai in Impostazioni > Assistenza dispositivo.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
