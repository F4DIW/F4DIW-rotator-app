package com.example.f4diwrotatorapp.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.utils.GeoUtils
import kotlinx.coroutines.launch

class ControlFragment : Fragment() {

    private val vm: ControlViewModel by viewModels()

    // ── Views ─────────────────────────────────────────────────────
    private lateinit var tvAz: TextView
    private lateinit var tvEl: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvVersion: TextView
    private lateinit var tvError: TextView
    private lateinit var etAz: EditText
    private lateinit var etEl: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnGo: Button
    private lateinit var btnStop: Button
    private lateinit var btnPark: Button
    private lateinit var btnReboot: Button
    private lateinit var btnVersion: Button
    private lateinit var indicatorBt: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_control, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeState()
        setupButtons()
    }

    private fun bindViews(view: View) {
        tvAz       = view.findViewById(R.id.tvAz)
        tvEl       = view.findViewById(R.id.tvEl)
        tvStatus   = view.findViewById(R.id.tvStatus)
        tvVersion  = view.findViewById(R.id.tvVersion)
        tvError    = view.findViewById(R.id.tvError)
        etAz       = view.findViewById(R.id.etAz)
        etEl       = view.findViewById(R.id.etEl)
        btnConnect = view.findViewById(R.id.btnConnect)
        btnGo      = view.findViewById(R.id.btnGo)
        btnStop    = view.findViewById(R.id.btnStop)
        btnPark    = view.findViewById(R.id.btnPark)
        btnReboot  = view.findViewById(R.id.btnReboot)
        btnVersion = view.findViewById(R.id.btnVersion)
        indicatorBt = view.findViewById(R.id.indica