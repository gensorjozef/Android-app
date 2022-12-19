package com.example.zadanie.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.example.zadanie.MainActivity
import com.example.zadanie.R
import com.example.zadanie.helpers.PreferenceData

fun setupAppBar(
    title: String,
    fragmentActivity: FragmentActivity,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    view: View
) {

    val menuHost: MenuHost = fragmentActivity

    setAppBarTitle(title, fragmentActivity)

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            menuInflater.inflate(R.menu.logout_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

            return when (menuItem.itemId) {
                R.id.action_logout -> {
                    createLogoutAlertDialog(view, context).show()
                    true
                }
                else -> false
            }
        }
    }, lifecycleOwner, Lifecycle.State.RESUMED)
}
fun setAppBarTitle(title: String, fragmentActivity: FragmentActivity) {
    val main = fragmentActivity as MainActivity
    main.setActionBarTitle(title)
}
fun createLogoutAlertDialog(view: View, context: Context): AlertDialog {
    val alertDialog: AlertDialog =
        AlertDialog.Builder(context).create()

    alertDialog.setTitle("Logout")
    alertDialog.setMessage("You will be logged out!")
    alertDialog.setButton(
        Dialog.BUTTON_POSITIVE, "Continue"
    ) { _, _ ->
        PreferenceData.getInstance().clearData(context)
        view.findNavController().navigate(R.id.action_to_login)
    }
    alertDialog.setButton(
        Dialog.BUTTON_NEGATIVE,
        "Cancel"
    ) { dialog, _ ->
        dialog.cancel()
    }

    return alertDialog
}
