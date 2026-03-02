package com.example.yocapital.login

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "YoCapitalPrefs"
    private const val IS_LOGGED_IN = "sesionIniciada"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_ROL = "rol"
    private const val KEY_CORREO = "correo"
    private const val KEY_ONBOARDING = "vioOnboarding"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, id: String, nombre: String, correo: String, rol: String) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, id)
        editor.putString(KEY_NOMBRE, nombre)
        editor.putString(KEY_CORREO, correo)
        editor.putString(KEY_ROL, rol)
        editor.apply()
    }

    fun setOnboardingVisto(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isLogueado(context: Context): Boolean = getPrefs(context).getBoolean(IS_LOGGED_IN, false)
    fun getRol(context: Context): String = getPrefs(context).getString(KEY_ROL, "") ?: ""
    fun getNombre(context: Context): String = getPrefs(context).getString(KEY_NOMBRE, "") ?: ""
    fun veteAOnboarding(context: Context): Boolean = !getPrefs(context).getBoolean(KEY_ONBOARDING, false)
    fun getUserId(context: Context): String = getPrefs(context).getString(KEY_USER_ID, "") ?: ""
}