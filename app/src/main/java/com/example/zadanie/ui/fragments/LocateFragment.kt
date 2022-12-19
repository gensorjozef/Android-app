package com.example.zadanie.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.GeofenceBroadcastReceiver
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentLocateBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.setupAppBar
import com.example.zadanie.ui.viewmodels.LocateViewModel
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import com.example.zadanie.ui.widget.nearbyBars.NearbyBarsEvents
import com.google.android.gms.location.*

class LocateFragment : Fragment() {
    private lateinit var binding: FragmentLocateBinding
    private lateinit var viewmodel: LocateViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                // Precise location access granted.
            }
            else -> {
                viewmodel.show("Background location access denied.")
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(LocateViewModel::class.java)

        if (!checkPermissions()) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocateBinding.inflate(inflater, container, false)
        setupAppBar(
            title = "Location",
            fragmentActivity = requireActivity(),
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext(),
            view = binding.root
        )

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }
        binding.lottieAnim.visibility = View.VISIBLE
        binding.lottieAnim.playAnimation()
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }.also { bnd ->
//            bnd.back.setOnClickListener {
//                it.findNavController().popBackStack()
//            }
            bnd.swiperefresh.setOnRefreshListener {

                loadData()

            }

            bnd.checkme.setOnClickListener {
                bnd.checkme.playAnimation()
                if (checkBackgroundPermissions()) {
                    bnd.checkme.apply {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                viewmodel.checkMe()
                            },2000
                        )
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionDialog()
                    }
                }
            }
            bnd.nearbyBars.events = object : NearbyBarsEvents {
                override fun onBarClick(nearbyBar: NearbyBar) {
                    viewmodel.myBar.postValue(nearbyBar)
                }

            }

        }
        viewmodel.loading.observe(viewLifecycleOwner) {

            binding.swiperefresh.isRefreshing = it
//            binding.lottieAnim.cancelAnimation()

        }
        viewmodel.animation.observe(viewLifecycleOwner) {
            if (it){
                binding.lottieAnim.visibility = View.VISIBLE
                binding.nearbyBars.visibility = View.INVISIBLE
                binding.lottieAnim.loop(true)
                binding.lottieAnim.playAnimation()
            }else{
                binding.lottieAnim.cancelAnimation()
                binding.lottieAnim.visibility = View.INVISIBLE
                binding.nearbyBars.visibility = View.VISIBLE
            }
//            binding.lottieAnim.cancelAnimation()

        }
        viewmodel.checkedIn.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                if (it) {
                    viewmodel.checkedIn
                    viewmodel.show("Successfully checked in.")
                    viewmodel.myLocation.value?.let {
                        createFence(it.lat, it.lon)
                    }
                }
            }
        }

        if (checkPermissions()) {
            loadData()
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
        }

        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }
        binding.lottieAnim.visibility = View.INVISIBLE
        binding.lottieAnim.cancelAnimation()

    }

    @SuppressLint("MissingPermission")
    private fun loadData() {
        if (checkPermissions()) {
            viewmodel.loading.postValue(true)
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().setDurationMillis(30000)
                    .setMaxUpdateAgeMillis(60000).build(), null
            ).addOnSuccessListener {
                it?.let {
                    viewmodel.myLocation.postValue(MyLocation(it.latitude, it.longitude))
                } ?: viewmodel.loading.postValue(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission", "UnspecifiedImmutableFlag")
    private fun createFence(lat: Double, lon: Double) {
        if (!checkPermissions()) {
            viewmodel.show("Geofence failed, permissions not granted.")
        }
        val geofenceIntent = PendingIntent.getBroadcast(
            requireContext(), 0,
            Intent(requireContext(), GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )

        val request = GeofencingRequest.Builder().apply {
            addGeofence(
                Geofence.Builder()
                    .setRequestId("mygeofence")
                    .setCircularRegion(lat, lon, 300F)
                    .setExpirationDuration(1000L * 60 * 60 * 24)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )
        }.build()

        geofencingClient.addGeofences(request, geofenceIntent).run {
            addOnSuccessListener {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
            }
            addOnFailureListener {
                viewmodel.show("Geofence failed to create.") //permission is not granted for All times.
                it.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun permissionDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Background location needed")
                setMessage("Allow background location (All times) for detecting when you leave bar.")
                setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog.show()
    }

    private fun checkBackgroundPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}