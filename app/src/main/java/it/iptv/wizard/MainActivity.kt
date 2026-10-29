package it.iptv.wizard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTailscale = findViewById<Button>(R.id.btnTailscale)
        val btnAccessibilita = findViewById<Button>(R.id.btnAccessibilita)

        btnTailscale.setOnClickListener {
            try {
                // Su Android 8+ serve il permesso "installa app sconosciute" per questa app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    !packageManager.canRequestPackageInstalls()
                ) {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(settingsIntent)
                    Toast.makeText(
                        this,
                        "Consenti l'installazione da questa app, poi premi di nuovo il pulsante",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val inputStream = assets.open("tailscale.apk")
                val tempFile = File(cacheDir, "tailscale.apk")
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                val apkUri = FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    tempFile
                )

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
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
