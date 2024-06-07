package com.kemarport.kdmsmahindra.view

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
import com.github.mikephil.charting.data.BarEntry
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.adapter.DealerDashboardTableAdapter
import com.kemarport.kdmsmahindra.adapter.MyDialogAdapter
import com.kemarport.kdmsmahindra.databinding.FragmentDashboardNewBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationCountResponseItem
import com.kemarport.kdmsmahindra.model.newapi.DashboardGetDeliveredDetailsResponse
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.DashboardViewModel
import com.kemarport.kdmsmahindra.viewmodel.DashboardViewModelFactory
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.HashMap
import java.util.Locale


class DashboardNewFragment : Fragment() {

    private val TAG = "DealerDashboardActivity"
    private var vehicleRecyclerAdapter: DealerDashboardTableAdapter? = null
    lateinit var binding: FragmentDashboardNewBinding
    private lateinit var viewModel: DashboardViewModel
    lateinit var includedView: View
    private var dealerCode: String? = ""
    private var token: String? = ""
    private lateinit var dealerDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    private var baseUrl: String =""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard_new, container, false)
        session = SessionManager(requireContext())
        dealerDetails = session.getUserDetails()
        dealerCode = dealerDetails["dealerCode"]
        token = dealerDetails["jwtToken"]
        serverIpSharedPrefText = dealerDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = dealerDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"

        val kdmsRepository = KDMSRepository()
        val application = requireActivity().application
        val viewModelProviderFactory = DashboardViewModelFactory(application, kdmsRepository)

        viewModel = ViewModelProvider(this, viewModelProviderFactory)[DashboardViewModel::class.java]
        callApi()

        viewModel.getDeliveredCountMutable.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing=false
                    response.data?.let {

                        binding.tvTotalVehicleScanValue.setText(it.vehicleTodaysCount.toString())
                    }
                }
                is Resource.Error -> {
                    Log.d(TAG, "onCreate: rfid ${response.message}")
                    binding.homeSwipeRefresh.isRefreshing=false
                    if (response.message == "Session Expired ! Please relogin" || response.message == "Authentication token expired" ||
                        response.message == Constants.CONFIG_ERROR) {
                        (requireActivity() as HomeActivity).showCustomDialog(
                            "Session Expired",
                            "Please re-login to continue"
                        )
                    }
                }

                is Resource.Loading -> {

                }
            }
        })

        viewModel.getDeliveredDetailsMutable.observe(viewLifecycleOwner,
            Observer  { response ->
                when (response) {

                    is Resource.Success -> {
                        binding.homeSwipeRefresh.isRefreshing=false
                        hideTableProgressBar()
                        response.data?.let {
                            setDashboardTableList(it)
                        }
                    }
                    is Resource.Error -> {
                        binding.homeSwipeRefresh.isRefreshing=false
                        hideTableProgressBar()
                        response.message?.let { errorMessage ->
                            if (response.message == "Session Expired ! Please relogin" || response.message == "Authentication token expired" ||
                                response.message == Constants.CONFIG_ERROR) {
                                (requireActivity() as HomeActivity).showCustomDialog(
                                    "Session Expired",
                                    "Please re-login to continue"
                                )
                            }
                            Toasty.error(
                                requireContext(),
                                "Error Message: $errorMessage"
                            ).show()
                        }
                    }
                    is Resource.Loading -> {

                        showTableProgressBar()

                    }
                }
            })

        binding.homeSwipeRefresh.setOnRefreshListener {
            callApi()
        }
        return binding.root
    }

    private fun callApi(){
        try {
            viewModel.getDeliveredCount (token!!,baseUrl)
            viewModel.getDeliveredDetails (token!!,baseUrl )

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
        includedView = requireView().findViewById<View>(R.id.table_first_item)
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




    private fun setDashboardTableList(resultResponse: ArrayList<DashboardGetDeliveredDetailsResponse>) {
        binding.tableFirstItem.tvColumnThree.text = "Date"

        val list = ArrayList<DashboardGetDeliveredDetailsResponse>()
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
    }

}