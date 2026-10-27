package com.example.tinyturnip.presentation.acts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.example.tinyturnip.R
import com.example.tinyturnip.data.local.entity.ActEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Dialog for creating a new act or editing an existing one.
 * The entered values are delivered via fragment result [REQUEST_KEY].
 */
class ActEditDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val actId = args.getLong(KEY_ID)

        val view = layoutInflater.inflate(R.layout.dialog_act_edit, null)
        val nameInputLayout = view.findViewById<TextInputLayout>(R.id.nameInputLayout)
        val nameEditText = view.findViewById<TextInputEditText>(R.id.nameEditText)
        val descriptionEditText = view.findViewById<TextInputEditText>(R.id.descriptionEditText)

        // On recreation the EditTexts restore their own state.
        if (savedInstanceState == null) {
            nameEditText.setText(args.getString(KEY_NAME))
            descriptionEditText.setText(args.getString(KEY_DESCRIPTION))
        }
        nameEditText.doAfterTextChanged { nameInputLayout.error = null }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (actId == NEW_ACT_ID) R.string.act_new_title else R.string.act_edit_title)
            .setView(view)
            .setPositiveButton(R.string.act_save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        // Overridden after show() so the dialog stays open when validation fails.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameEditText.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    nameInputLayout.error = getString(R.string.act_name_required)
                    return@setOnClickListener
                }
                val description = descriptionEditText.text?.toString()?.trim().orEmpty()
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(KEY_ID to actId, KEY_NAME to name, KEY_DESCRIPTION to description),
                )
                dismiss()
            }
        }
        return dialog
    }

    companion object {
        const val TAG = "ActEditDialog"
        const val REQUEST_KEY = "act_edit_request"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_DESCRIPTION = "description"
        const val NEW_ACT_ID = 0L

        fun newInstance(act: ActEntity? = null) = ActEditDialogFragment().apply {
            arguments = bundleOf(
                KEY_ID to (act?.id ?: NEW_ACT_ID),
                KEY_NAME to act?.name.orEmpty(),
                KEY_DESCRIPTION to act?.description.orEmpty(),
            )
        }
    }
}
