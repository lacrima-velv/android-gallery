package com.example.gallery.permissiondialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.gallery.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeniedPermissionDialogShowRationaleFragment : DialogFragment() {

    private lateinit var listener: DialogClickListener
    /*
    Parent fragment must implement this interface to set click listener on the button to call
    permission request
     */
    interface DialogClickListener {
        fun onYesClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement DialogClickListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getText(R.string.permission_rationale_dialog_title))
            .setMessage(getText(R.string.permission_rationale_dialog_body))
            .setPositiveButton(getText(R.string.permission_rationale_dialog_yes)) { _, _, ->
                listener.onYesClick()
            }
            .setNegativeButton(getText(R.string.permission_rationale_dialog_no)) {
                    dialog, _ -> dialog.dismiss()
            }
            .show()
    }

}