package com.clovertech.autolib.views.ui.home


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.clovertech.autolib.R
import com.clovertech.autolib.model.AgentToken
import com.clovertech.autolib.model.Task
import com.clovertech.autolib.adapters.ListTasksAdapter
import com.clovertech.autolib.adapters.TaskStepsAdapter
import com.clovertech.autolib.utils.PrefUtils
import com.clovertech.autolib.viewmodel.NotificationViewModel
import com.clovertech.autolib.viewmodel.TaskViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    val TAG = "LOG TAG"

    lateinit var taskViewModel: TaskViewModel
    lateinit var notificationViewModel: NotificationViewModel
    lateinit var adapterSteps: TaskStepsAdapter
    lateinit var taskPrem: Task


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView4.text = "Salut! "+PrefUtils.with(requireContext()).getString(PrefUtils.Keys.AGENT_NAME, "Hamid Reda")

        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        notificationViewModel =
            ViewModelProvider(requireActivity()).get(NotificationViewModel::class.java)
        var adapter = ListTasksAdapter(requireActivity(), taskViewModel, this)
        pagerTasksHome.adapter = adapter
        pagerTasksHome.clipToPadding = false
        pagerTasksHome.clipChildren = false
        pagerTasksHome.offscreenPageLimit = 4
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(32))
        //transformer.addTransformer { page, position ->
        //    val r = 1 - Math.abs(position)
        //    page.scaleY = 0.85f + r * 0.15f
        //}
        pagerTasksHome.setPageTransformer(transformer)
        pagerTasksHome.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                taskViewModel.task = adapter.data[position]
                adapterSteps.setListSteps(adapter.data[position].steps!!)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
        adapterSteps = TaskStepsAdapter(requireActivity(), taskViewModel)
        tasksRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        tasksRecyclerView.isNestedScrollingEnabled = false
        tasksRecyclerView.adapter = adapterSteps

        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        var id = PrefUtils.with(requireContext()).getInt(PrefUtils.Keys.ID, 0)
        id = 3

        if (id != 0) {
            /* Toast.makeText(requireContext(), "Test is working", Toast.LENGTH_SHORT)
                 .show()*/
            taskViewModel.getTasksByIdAgent(requireContext(), 100)
            // tacheViewModel.getTacheAllModel(requireContext())
            taskViewModel.getAllTasks(requireContext())?.observe(viewLifecycleOwner, Observer {
                var listFiltered =
                    it.filter { tache -> ((tache.idTaskState == 1) || (tache.idTaskState == 2)) }
                adapter.setListTache(listFiltered)
                nbTaches2.text = listFiltered.size.toString()
                pagerTasksHome.requestTransform()
            })

        }

        details.setOnClickListener() {
            //var viewModel = ViewModelProvider(this).get(TacheViewModel::class.java)

            it.findNavController()?.navigate(R.id.action_navigation_home_to_detailTache)
        }

        taskViewModel.getTasksByIdAgent(requireContext(), 100)

    }

    override fun onResume() {
        super.onResume()
        sendFCMToken()
    }

    fun update(task: Task) {
        var viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        task.steps?.let { adapterSteps.setListSteps(it) }
        viewModel.task = task
    }

    private fun sendFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            var token = task.result
            if (token == null) {
                token = ""
            }

            // Post token
            notificationViewModel.postFCMToken(requireContext(), AgentToken(3, token))

            // Log
            Log.d(TAG, "FCM token : " + token)

        })
    }

    fun goToTaskDetails(task: Task) {
        view?.findNavController()?.navigate(R.id.action_navigation_home_to_detailTache)
    }
}







