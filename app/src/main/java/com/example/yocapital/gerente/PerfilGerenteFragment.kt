package com.example.yocapital.gerente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity

class PerfilGerenteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_gerente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombre = view.findViewById<TextView>(R.id.txt_nombre_usuario)
        val txtCorreo = view.findViewById<TextView>(R.id.txt_correo_usuario)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btn_cerrar_sesion)

        txtNombre.text = SessionManager.getNombre(requireContext())
        txtCorreo.text = SessionManager.getCorreo(requireContext())

        btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Sí") { _, _ ->
                    SessionManager.logout(requireContext())
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}