package fr.f4diw.rotatorapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Masquer la barre de statut pour un vrai plein écran
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

        Handler(Looper.getMainLooper()).postDelayed({
            // Le SplashActivity renvoie maintenant vers la MainActivity qui affichera le HomeFragment par défaut
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 4000)
    }
}
