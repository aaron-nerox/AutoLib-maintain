package com.clovertech.autolib.views.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.clovertech.autolib.adapters.NotificationsAdapter
import com.clovertech.autolib.databinding.FragmentNotificationsBinding
import com.clovertech.autolib.utils.PrefUtils
import com.clovertech.autolib.viewmodel.NotificationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationsFragment : Fragment() {

    private val notificationsViewModel: NotificationViewModel by activityViewModels()
    private lateinit var binding: FragmentNotificationsBinding

    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotificationsBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NotificationsAdapter(requireActivity())
        binding.notificationsRecycler.layoutManager = LinearLayoutManager(requireActivity())
        binding.notificationsRecycler.adapter = adapter

        val name = PrefUtils.with(requireContext()).getString(PrefUtils.Keys.AGENT_NAME, "...")
        binding.salutations.text = "Hello $name"
        notificationsViewModel.fetchAllNotifications(requireContext())
        notificationsViewModel.getAllNotifications(requireContext())
        notificationsViewModel.notificationList.observe(viewLifecycleOwner, {
            if(it != null){
                adapter.setNotificationList(it)
                binding.notificationNumber.text = it.size.toString()
            }
        })

    }
}