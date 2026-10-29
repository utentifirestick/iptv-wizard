package it.iptv.wizard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class WizardAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Nessuna azione: il servizio serve solo a risultare attivo nel sistema.
    }

    override fun onInterrupt() {
    }
}
