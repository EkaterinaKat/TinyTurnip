package com.example.tinyturnip.presentation.acts

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.tinyturnip.R
import com.example.tinyturnip.TinyTurnipApp
import com.example.tinyturnip.data.local.entity.ActEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ActsFragment : Fragment(R.layout.fragment_acts) {

    private val app get() = requireActivity().application as TinyTurnipApp

    private val adapter = ActsAdapter(::onActClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(ActEditDialogFragment.REQUEST_KEY, this) { _, result ->
            saveAct(
                id = result.getLong(ActEditDialogFragment.KEY_ID),
                name = result.getString(ActEditDialogFragment.KEY_NAME).orEmpty(),
                description = result.getString(ActEditDialogFragment.KEY_DESCRIPTION).orEmpty(),
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyTextView = view.findViewById<TextView>(R.id.emptyTextView)
        view.findViewById<RecyclerView>(R.id.actsRecyclerView).adapter = adapter
        view.findViewById<View>(R.id.addActButton).setOnClickListener {
            ActEditDialogFragment.newInstance().show(childFragmentManager, ActEditDialogFragment.TAG)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.actRepository.getAll().collect { acts ->
                    adapter.submitList(acts)
                    emptyTextView.visibility = if (acts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun onActClicked(act: ActEntity) {
        val actions = arrayOf(getString(R.string.act_edit), getString(R.string.act_delete))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(act.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> ActEditDialogFragment.newInstance(act)
                        .show(childFragmentManager, ActEditDialogFragment.TAG)
                    1 -> confirmDelete(act)
                }
            }
            .show()
    }

    private fun confirmDelete(act: ActEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.act_delete_confirm, act.name))
            .setPositiveButton(R.string.act_delete) { _, _ -> deleteAct(act) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveAct(id: Long, name: String, description: String) {
        // Fragment scope, not view scope: the result may arrive before the view is recreated.
        lifecycleScope.launch {
            if (id == ActEditDialogFragment.NEW_ACT_ID) {
                app.actRepository.create(name, description)
            } else {
                app.actRepository.update(ActEntity(id = id, name = name, description = description))
            }
        }
    }

    private fun deleteAct(act: ActEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!app.actRepository.delete(act.id)) {
                Snackbar.make(requireView(), R.string.act_delete_has_logs, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
