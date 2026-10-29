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

        // --- PULSANTE 2: ATTIVA AVVIO AUTOMATICO (Universale) ---
        btnAccessibilita.setOnClickListener {
            // Controlla se siamo su una Fire TV
            val isFireTV = packageManager.hasSystemFeature("amazon.hardware.fire_tv")
            
            if (isFireTV) {
                // Se è Fire TV, mostriamo il messaggio che l'avvio si fa col PC
                Toast.makeText(this, "Su Fire TV l'avvio automatico si attiva SOLO dal Kit col PC. Usa il tuo programma installa.bat.", Toast.LENGTH_LONG).show()
            } else {
                // Se è Xiaomi o altra Android TV, proviamo ad aprire l'Accessibilità
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                    Toast.makeText(this, "Scorri e attiva 'IPTV Wizard'", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    // Se anche Xiaomi dà errore (a volte nasconde l'opzione), mostriamo un messaggio
                    Toast.makeText(this, "Impossibile aprire le impostazioni. Cerca 'Accessibilità' nel menu della TV.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
