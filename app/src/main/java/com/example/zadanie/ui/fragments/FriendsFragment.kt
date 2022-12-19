package com.example.zadanie.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentFriendsBinding
import com.example.zadanie.databinding.FragmentLocateBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.setupAppBar
import com.example.zadanie.ui.viewmodels.FriendsViewModel

class FriendsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var binding: FragmentFriendsBinding
    private lateinit var viewmodel: FriendsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(FriendsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)
        setupAppBar(
            title = "Friends",
            fragmentActivity = requireActivity(),
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext(),
            view = binding.root
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }.also { bnd ->

            bnd.addFriend.setOnClickListener{
                it.findNavController().navigate(R.id.action_to_add_friend)
            }

            bnd.swiperefresh.setOnRefreshListener {
                viewmodel.refreshData()
            }
        }

    }

}