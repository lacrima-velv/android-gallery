package com.example.gallery.permissiondialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.gallery.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeniedPermissionDialogExplanation : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getText(R.string.permission_explanation_dialog_title))
            .setMessage(getText(R.string.permission_explanation_dialog_body))
            .setPositiveButton(getText(R.string.permission_explanation_dialog_ok)) {
                    dialog, _ -> dialog.dismiss()
            }
            .show()
    }
}