package com.kemarport.kdmsmahindra.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.adapter.DealerDashboardTableAdapter
import com.kemarport.kdmsmahindra.adapter.MyDialogAdapter
import com.kemarport.kdmsmahindra.databinding.FragmentDashBoardBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationCountResponseItem
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationResponseItem
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.DashboardViewModel
import com.kemarport.kdmsmahindra.viewmodel.DashboardViewModelFactory
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DashBoardFragment : Fragment() {
    private val TAG = "DealerDashboardActivity"
    private var vehicleRecyclerAdapter: DealerDashboardTableAdapter? = null
    lateinit var binding: FragmentDashBoardBinding
    private lateinit var viewModel: DashboardViewModel
    lateinit var includedView: View
    private var dealerCode: String? = ""
    private var token: String? = ""
    private lateinit var dealerDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    var vehicleDataList:  ArrayList<VehicleConfirmationCountResponseItem> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dash_board, container, false)
        session = SessionManager(requireContext())
        dealerDetails = session.getUserDetails()
        dealerCode = dealerDetails["dealerCode"]
        token = dealerDetails["jwtToken"]

        val kdmsRepository = KDMSRepository()
        val application = requireActivity().application
        val viewModelProviderFactory = DashboardViewModelFactory(application, kdmsRepository)

        viewModel = ViewModelProvider(this, viewModelProviderFactory)[DashboardViewModel::class.java]
        callApi()

        viewModel.rfidMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing=false
                    response.data?.let {
                        try {
                            binding.tvRfidValue.text = "${it.rfid}"
                            binding.tvBarcodeValue.text = "${it.scans}/${it.maxScans}"
                        }
                        catch (e:Exception)
                        {

                        }

                    }
                }

                is Resource.Error -> {
                    Log.d(TAG, "onCreate: rfid ${response.message}")
                    binding.homeSwipeRefresh.isRefreshing=false
                /*    if (response.message == "Session Expired ! Please relogin" || response.message == "Authentication token expired" ||
                        response.message == Constants.CONFIG_ERROR) {
                        (requireActivity() as MainActivity).showCustomDialog(
                            "Session Expired",
                            "Please re-login to continue"
                        )
                    }*/
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            requireContext(),
                            "Error Message: $errorMessage"
                        ).show()
                    }

                }

                is Resource.Loading -> {

                }
            }
        })
        viewModel.vehicleConfirmationLiveData.observe(viewLifecycleOwner,
            Observer  { response ->
            when (response) {
                is Resource.Error -> {
                    binding.homeSwipeRefresh.isRefreshing=false
                    hideTableProgressBar()
                    response.message?.let { errorMessage ->
                      /*  if (response.message == "Session Expired ! Please relogin" || response.message == "Authentication token expired" ||
                            response.message == Constants.CONFIG_ERROR) {
                            (requireActivity() as MainActivity).showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }*/
                        Toasty.error(
                            requireContext(),
                            "Error Message: $errorMessage"
                        ).show()
                    }
                }
                is Resource.Loading -> {

                    showTableProgressBar()

                }
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing=false
                    hideTableProgressBar()
                    response.data?.let {
                       // setDashboardTableList(it)
                    }
                }
            }
        })
        viewModel.vehicleConfirmationCountLiveData.observe(viewLifecycleOwner,
            Observer  { response ->
            when (response) {
                is Resource.Success -> {
                    hideGraphProgressBar()
                    binding.homeSwipeRefresh.isRefreshing=false
                    vehicleDataList.clear()
                    response.data?.let {
                        try {
                            vehicleDataList.addAll(it)
                            setDashboardGraphList(vehicleDataList)
                        }
                        catch (e:Exception)
                        {

                        }

                    }
                }
                is Resource.Error -> {
                    binding.homeSwipeRefresh.isRefreshing=false
                    hideGraphProgressBar()
                    response.message?.let { errorMessage ->
                   /*     if (response.message == "Session Expired ! Please relogin" || response.message == "Authentication token expired" ||
                            response.message == Constants.CONFIG_ERROR) {
                            (requireActivity() as MainActivity).showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }*/
                        Toasty.error(
                            requireContext(),
                            "Error Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showGraphProgressBar()
                }


            }
        })

        binding.tvSelectPeriod.setOnClickListener {
            showRecyclerDialog(barChart = false)
        }
        binding.tvSelectPeriodBarchart.setOnClickListener {
            showRecyclerDialog(barChart = true)
        }
        binding.homeSwipeRefresh.setOnRefreshListener {
            callApi()
        }
        return binding.root
    }
    private fun callApi(){
        try {
            viewModel.getVehicleConfirmation( requireActivity(),dealerCode!!, 7)
            viewModel.getVehicleConfirmationCount( requireActivity(),dealerCode!!, 7)
            viewModel.getRFIDCount(requireActivity(),dealerCode!!)
        }
        catch (e:Exception)
        {
            Toasty.error(
                requireContext(),
                "Error Message: $e"
            ).show()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        includedView = requireView().findViewById<View>(R.id.table_first_item_a)
    }

    private fun hideGraphProgressBar() {
        binding.selectPeriodLayout.visibility = View.VISIBLE
        binding.barProgressBar.visibility = View.GONE
    }
    private fun showGraphProgressBar() {
        binding.selectPeriodLayout.visibility = View.GONE
        binding.barProgressBar.visibility = View.VISIBLE
    }

    private fun hideTableProgressBar() {


        // Set the visibility of the included view
        includedView.visibility = View.VISIBLE
        //binding.tableFirstItemA.visibility = View.VISIBLE
        binding.dashboardTable.visibility = View.VISIBLE
        binding.tableProgressBar.visibility = View.GONE
    }

    private fun showTableProgressBar() {
        binding.dashboardTable.visibility = View.GONE
        includedView.visibility = View.GONE
       // binding.tableFirstItem.visibility = View.GONE
        binding.tableProgressBar.visibility = View.VISIBLE
    }



    private fun setDashboardGraphList(resultResponse: List<VehicleConfirmationCountResponseItem>) {
        val barEntriesList = ArrayList<BarEntry>()
        val xAxisLabel = ArrayList<String>()
        resultResponse.forEachIndexed { index, dashboardGraphResponseItem ->
            dashboardGraphResponseItem.count?.let {
                BarEntry(
                    index.toFloat(),
                    it.toFloat()
                )
            }?.let {
                barEntriesList.add(
                    it
                )
            }
            dashboardGraphResponseItem.day?.let { convertDateStringToDate(it)?.let { xAxisLabel.add(it) } }
        }
        addLables(barEntriesList, xAxisLabel)
    }



    fun convertDateStringToDate(inputDate: String): String? {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())

        try {
            val date = inputFormat.parse(inputDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }


    private fun addLables(entries: ArrayList<BarEntry>, xAxisLabel: ArrayList<String>) {
        val barChart: BarChart = binding.idBarChart
        val dataSet = BarDataSet(entries, "Values")
        dataSet.color = Color.BLUE

        val data = BarData(dataSet)
        barChart.data = data

        // Customize the X-axis labels
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabel)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // Refresh the chart
        barChart.invalidate()

    }

/*    private fun setDashboardTableList(resultResponse: List<VehicleConfirmationResponseItem>) {
        binding.tableFirstItemA.tvColumnThree.text = "Date"

        val list = mutableListOf<VehicleConfirmationResponseItem>()
        list.addAll(resultResponse)

        // Access the RecyclerView using binding
        binding.dashboardTable.adapter = vehicleRecyclerAdapter
        binding.dashboardTable.layoutManager = LinearLayoutManager(requireContext())

        vehicleRecyclerAdapter?.let {
            vehicleRecyclerAdapter?.updateItems(list)
        } ?: kotlin.run {
            // Initialize the adapter if it's null
            vehicleRecyclerAdapter = DealerDashboardTableAdapter(requireContext(), list)
            binding.dashboardTable.adapter = vehicleRecyclerAdapter
        }
    }*/

    private fun showRecyclerDialog(barChart: Boolean) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.pop_up_menu_layout, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)

        // Create and set up your RecyclerView adapter and layout manager here
        val layoutManager = LinearLayoutManager(requireContext())
        val list = mutableListOf<Int>()
        list.add(7)
        list.add(14)
        list.add(21)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()

        val adapter = MyDialogAdapter(list) { item ->
            if (barChart) {
                binding.tvSelectPeriodBarchart.text = "Period $item"
                viewModel.getVehicleConfirmation(requireActivity(),dealerCode!!, item)
            } else {
                binding.tvSelectPeriod.text = "Period $item"
                viewModel.getVehicleConfirmationCount( requireActivity(),dealerCode!!, item)
            }
            alertDialog.dismiss()
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        alertDialog.show()
    }

}