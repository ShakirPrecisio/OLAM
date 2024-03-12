package com.kemar.olam.dashboard.activity

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kemar.olam.R
import com.kemar.olam.bc_print_reprint_edit.fragment.BcTodaysHistoryFragment
import com.kemar.olam.bordereau.fragment.TodaysHistoryFragment
import com.kemar.olam.bordereau.fragment.UnderMaintenaceFragment
import com.kemar.olam.dashboard.adapter.AppMenuAdapter
import com.kemar.olam.dashboard.models.MenusModel
import com.kemar.olam.delivery_management.fragments.DeliveryUserHistoryFragment
import com.kemar.olam.forestry_management.fragment.ForestryManagementDashboardFragment
import com.kemar.olam.forestry_management.fragment.PistageFragment
import com.kemar.olam.forestry_management.fragment.TreeDetailsFragment
import com.kemar.olam.forestry_management.fragment.TreeInventoryFragment
import com.kemar.olam.forestry_management.model.TreeDetailRecordResponse
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsDeclarFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsUserHistoryFragment
import com.kemar.olam.login.activity.LoginActivity
import com.kemar.olam.offlineData.fragment.OfflineModeFragment
import com.kemar.olam.origin_verification.fragment.OriginVeriUserHistoryFragment
import com.kemar.olam.physicalcount.fragment.PhysicalListFragment
import com.kemar.olam.physicalcount.fragment.PhysicalScanFragment
import com.kemar.olam.sales_and_inspection.inspection.fragment.MainContainerFragment
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import com.kemar.olam.utility.WS_URL.ENVIRONMENT
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.content_homes.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {
    var currentFragment: Fragment? = null
    var commingFrom: String = ""
    lateinit var context : Context
    lateinit var mUtils: Utility
    var bcReprint_isFromFilter : Boolean  = false

    // handler for received Intents for the "my-event" event
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Extract data included in the Intent
            val showDialog = intent.getBooleanExtra("showDialog", false)
            val dialogText = intent.getStringExtra("dialogText")

            if(!isFinishing) {
                if (showDialog) {
                    dialog = dialogText?.let { mUtils.setProgressDialog(this@HomeActivity, it) }
                    dialog?.show()
                } else {
                    if (dialog?.isShowing == true) {
                        dialog?.dismiss()
                    }
                    AlertDialog.Builder(this@HomeActivity)
                        .setMessage(dialogText)
                        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
            Log.d("receiver1", "Got message: $showDialog")
        }
    }

    var dialog : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUtils = Utility()
        context = this
        mUtils.setupLocalization(context)
        setContentView(R.layout.activity_home)
        /*   var fragment = UnderMaintenaceFragment()
           supportFragmentManager.beginTransaction()
               .replace(R.id.container, fragment!!) // add to backstack
               .commit()
           currentFragment = supportFragmentManager.findFragmentById(R.id.container)*/

        commingFrom = intent?.getStringExtra(Constants.comming_from).toString()

        setupToggleActionAndIcon()
        setupDrawer()
        setUserProfile()
        setupNaviagtionFromHomeScreen()

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("serviceForestryEvent")
        )
    }

    fun addFragmentWithNoStck(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment) // add to backstack
            .commit()
        currentFragment = supportFragmentManager.findFragmentById(R.id.container)
    }

    fun setupNaviagtionFromHomeScreen() {
        if (commingFrom == resources.getString(R.string.loading_wagons)) {
            val fragment =
                LoadingWagonsUserHistoryFragment()
            //replaceFragment(fragment,false)
            addFragmentWithNoStck(fragment)
        } else if (commingFrom == resources.getString(R.string.bordereau_module)) {
            val fragment = TodaysHistoryFragment()
            // replaceFragment(fragment,false)
            addFragmentWithNoStck(fragment)
        } else if (commingFrom == resources.getString(R.string.origin_verification)) {
            val fragment = OriginVeriUserHistoryFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.Bc_main_screen
            )
            fragment.arguments = bundle
            addFragmentWithNoStck(fragment)
        } else if (commingFrom == resources.getString(R.string.bc_re_print_n_edit)) {
            val fragment = BcTodaysHistoryFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.Bc_main_screen
            )
            fragment.arguments = bundle
            addFragmentWithNoStck(fragment)
        } else if (commingFrom == resources.getString(R.string.inspection_n_sales)) {
            val fragment = MainContainerFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.from_sales
            )
            fragment.arguments = bundle
            addFragmentWithNoStck(fragment)
        } else if (commingFrom == resources.getString(R.string.drawer_delivery_management)) {
            //var fragment  =  DeleiveryManagementFragment()
            val fragment =
                DeliveryUserHistoryFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.Bc_main_screen
            )
            fragment.arguments = bundle
            addFragmentWithNoStck(fragment)
        }
        else if (commingFrom == resources.getString(R.string.offline_mode)) {
            val fragment = OfflineModeFragment()
            // replaceFragment(fragment,false)
            addFragmentWithNoStck(fragment)
        }

        else if (commingFrom == resources.getString(R.string.physical_count)) {
            val fragment = PhysicalListFragment()
            // replaceFragment(fragment,false)
            addFragmentWithNoStck(fragment)
        }
        else if (commingFrom == resources.getString(R.string.forestry_management)) {
            val fragment = ForestryManagementDashboardFragment()
            // replaceFragment(fragment,false)
            addFragmentWithNoStck(fragment)
        }
    }

    fun hideActionBar() {
        supportActionBar?.hide()
    }

    fun showActionBar() {
        supportActionBar?.show()
    }

    fun invisibleFilter() {
        ivHomeFilter.visibility = View.INVISIBLE
    }

    fun visibleFilter() {
        ivHomeFilter.visibility = View.VISIBLE
    }

    fun setupToggleActionAndIcon() {
        setSupportActionBar(toolbars)
        toolbars?.bringToFront()
        supportActionBar?.let { actionBar->
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ham_menu)
        }

        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbars,
            0,
            0
        ) {
            //when Drawer closed then nested scrollview set to top
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                nestedScrollNav.smoothScrollTo(0, 0)

            }

        }
        drawer_layout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = false
        toolbars?.setNavigationIcon(R.drawable.ham_menu)
        toolbars?.setNavigationOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }
        toggle.syncState()
    }


    @SuppressLint("SetTextI18n")
    fun setupDrawer() {

        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            txtVersion.text = "Ver. ${pInfo.versionName}_${ENVIRONMENT}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        rvMenuList.layoutManager = LinearLayoutManager(
            this@HomeActivity,
            LinearLayoutManager.VERTICAL, false
        )

        val adapter =
            AppMenuAdapter(this, getAppMenuList(true))
        rvMenuList.adapter = adapter
        adapter.onItemClick = { appMenuData ->
            drawer_layout?.closeDrawer(GravityCompat.START)
            val name = appMenuData.menuName
            when (name) {
                resources.getString(R.string.logout) -> {
                    alertDialogForLogoutFuntion()
                }
                resources.getString(R.string.bordereau_module) -> {
                    val fragment = TodaysHistoryFragment()
                    replaceFragment(fragment, false)
                }
                resources.getString(R.string.dashboard) -> {
                    //getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    val fragment = UnderMaintenaceFragment()
                    replaceFragment(fragment, false)
                    // txtHomeTitle.text = getString(R.string.dashboard)
                }
                resources.getString(R.string.bc_re_print_n_edit) -> {

                    when (SharedPref.read(Constants.user_role).toString()) {
                        "SuperUserApp", "Super User", "admin" -> {
                            val fragment =
                                BcTodaysHistoryFragment()
                            val bundle = Bundle()
                            bundle.putString(
                                Constants.comming_from,
                                Constants.Bc_main_screen
                            )
                            fragment.arguments = bundle
                            replaceFragment(fragment, false)
                        }
                        else -> {
                            mUtils.showAlert(
                                this@HomeActivity,
                                resources.getString(R.string.you_are_not_a_super_user)
                            )
                        }
                    }

                }
                resources.getString(R.string.loading_wagons) -> {
                    val fragment =
                        LoadingWagonsUserHistoryFragment()
                    replaceFragment(fragment, false)
                }
                resources.getString(R.string.origin_verification) -> {
                    val fragment = OriginVeriUserHistoryFragment()
                    val bundle = Bundle()
                    bundle.putString(
                        Constants.comming_from,
                        Constants.Bc_main_screen
                    )
                    fragment.arguments = bundle
                    replaceFragment(fragment, false)
                }
                resources.getString(R.string.inspection_n_sales) -> {
                    val fragment = MainContainerFragment()
                    val bundle = Bundle()
                    bundle.putString(
                        Constants.comming_from,
                        Constants.from_sales
                    )
                    fragment.arguments = bundle
                    replaceFragment(fragment, false)
                    /*    var fragment = InspectionsFragment()
                        replaceFragment(fragment,false)*/
                }
                resources.getString(R.string.drawer_delivery_management) -> {
                    val fragment =
                        DeliveryUserHistoryFragment()
                    val bundle = Bundle()
                    bundle.putString(
                        Constants.comming_from,
                        Constants.Bc_main_screen
                    )
                    fragment.arguments = bundle
                    //  var fragment = DeleiveryManagementFragment()
                    replaceFragment(fragment, false)
                }

                resources.getString(R.string.offline_mode) -> {

                    val fragment = OfflineModeFragment()
                    replaceFragment(fragment, false)

                }
            }

        }
    }

    fun alertDialogForLogoutFuntion(){
        val alert: android.app.AlertDialog =
            android.app.AlertDialog.Builder(this, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(this.resources.getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(
                    "Ok"
                ) { dialog, which ->
                    dialog.dismiss()
                    logoutUser()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE)
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE)

        val positive: Button =
            alert.getButton(DialogInterface.BUTTON_POSITIVE)
        positive.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        val negative: Button =
            alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
    }


    fun logoutUser() {
        SharedPref.clearAllData()
        SharedPref.write(Constants.LOGIN, false)
        val i: Intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }


    fun setUserProfile() {
        txtUserName.text = SharedPref.read(Constants.user_name)
        Glide
            .with(this) //.load(list.get(position).offerImage)
            .load(SharedPref.read(Constants.profile_path))
            .into(ivProfile)
    }

    fun <ArrayList> getAppMenuList(isSuperAdmin: Boolean): ArrayList {
        var appMenuList = ArrayList<MenusModel>()
        appMenuList.add(
            MenusModel(
                R.drawable.dashboard,
                resources.getString(R.string.dashboard)
            )
        )
        appMenuList.add(
            MenusModel(
                R.drawable.barcode,
                resources.getString(R.string.bordereau_module)
            )
        )
        appMenuList.add(
            MenusModel(
                R.drawable.bc_reprint,
                resources.getString(R.string.bc_re_print_n_edit)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.laoding_wagon,
                resources.getString(R.string.loading_wagons)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.origin_verfication,
                resources.getString(R.string.origin_verification)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.inspection_sales,
                resources.getString(R.string.inspection_n_sales)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.delivery,
                resources.getString(R.string.drawer_delivery_management)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.logout,
                resources.getString(R.string.logout)
            )
        )

        return appMenuList as ArrayList
    }

    override fun onBackPressed() {
        /*/super.onBackPressed()*/
        mUtils.setupLocalization(context)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                supportFragmentManager.popBackStackImmediate(
                    supportFragmentManager.findFragmentById(
                        R.id.container
                    )?.javaClass?.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                currentFragment = supportFragmentManager.findFragmentById(R.id.container)
                val afterbackpressClassName = currentFragment?.javaClass?.simpleName
                if (currentFragment is BcTodaysHistoryFragment) {
                    visibleFilter()
                    if(bcReprint_isFromFilter){
                        (currentFragment as BcTodaysHistoryFragment).callingUserHistoryByFilter()
                    }else{
                        (currentFragment as BcTodaysHistoryFragment).callingUserHistory()
                    }

                    toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)

                } else if (afterbackpressClassName == "AddHeaderFragment" || afterbackpressClassName == "BarcodeBordereuDetailsFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.bordereau_module)
                    invisibleFilter()
                } else if (currentFragment is TodaysHistoryFragment) {
                    toolbars.txtHomeTitle.text = getString(R.string.barcode_entries)
                    invisibleFilter()
                    (currentFragment as TodaysHistoryFragment).callingUserHistory()
                } else if (afterbackpressClassName == "LoadingWagonsUserHistoryFragment") {
                    invisibleFilter()
                    (currentFragment as LoadingWagonsUserHistoryFragment).callingLoadingWagonsUserHistory()
                    toolbars.txtHomeTitle.text = getString(R.string.loading_wagons)
                }
                else if (afterbackpressClassName == "LoadingWagonsHeaderFragment" || afterbackpressClassName == "LoadingWagonsLogsListingFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.loading_wagons)
                    invisibleFilter()
                }
                else if (afterbackpressClassName == "BcTodaysHistoryFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                    visibleFilter()
                } else if (afterbackpressClassName == "BcReprintHeaderFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                    invisibleFilter()
                } else if (afterbackpressClassName == "BCReprintLogsDeatilsFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                    invisibleFilter()
                } else if (afterbackpressClassName == "DeleiveryManagementFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
                    invisibleFilter()
                } else if (afterbackpressClassName == "InspectionsFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.inspection_n_sales)
                    invisibleFilter()
                } else if (afterbackpressClassName == "OriginVerifFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.origin_verification)
                    invisibleFilter()
                } else if (afterbackpressClassName == "OriginLogDetairilsFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.origin_verification)
                    invisibleFilter()
                } else if (afterbackpressClassName == "LoadingWagonsDeclarFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.loading_n_declration_)
                    invisibleFilter()
                    (currentFragment as LoadingWagonsDeclarFragment).callingDeclarationList()
                } else if (afterbackpressClassName == "UnderMaintenaceFragment") {
                    toolbars.txtHomeTitle.text = getString(R.string.dashboard)
                    invisibleFilter()
                } else if (currentFragment is OriginVeriUserHistoryFragment) {
                    invisibleFilter()
                    (currentFragment as OriginVeriUserHistoryFragment).callingUserHistory()
                    toolbars.txtHomeTitle.text = getString(R.string.origin_verification)

                }
                else if (currentFragment is PhysicalListFragment) {
                    invisibleFilter()
                    (currentFragment as PhysicalListFragment).getStockList()
                    toolbars.txtHomeTitle.text = getString(R.string.physical_count)
                    invisibleFilter()

                }
                else if (afterbackpressClassName == "InspectionsFragment" || afterbackpressClassName == "ApprovalListFragment"
                    || afterbackpressClassName == "InnspectionHeaderFragment"
                    || afterbackpressClassName == "SalesLogDetailsFragment" || afterbackpressClassName == "SalesUserHistoryFragment"
                ) {
                    toolbars.txtHomeTitle.text = getString(R.string.inspection_n_sales)
                    invisibleFilter()
                }
                else  if(currentFragment is MainContainerFragment){
                    toolbars.txtHomeTitle.text = getString(R.string.inspection_n_sales)
                    invisibleFilter()
                    (currentFragment as MainContainerFragment).getSalesUserHistory(
                        SharedPref.getUserId(
                            Constants.user_id
                        ).toString()
                    )
                    (currentFragment as MainContainerFragment).getGroundUserHistory(
                        SharedPref.getUserId(
                            Constants.user_id
                        ).toString()
                    )
                }
                else if (currentFragment is DeliveryUserHistoryFragment) {
                    visibleFilter()
                    (currentFragment as DeliveryUserHistoryFragment).callingDeliveryUserHistory()
                    (currentFragment as DeliveryUserHistoryFragment).setupClickListner()
                    toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
                } else if (afterbackpressClassName == "DeliveryHeaderFragment" || afterbackpressClassName =="DeliveredLogsDetailsFragment" || afterbackpressClassName =="DeliverdBordereuHistoryFragment" || afterbackpressClassName=="DeliveryFilterFragment") {
                    invisibleFilter()
                    toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
                } else if(afterbackpressClassName == "TreeInventoryFragment") {
                    (currentFragment as TreeInventoryFragment).setToolbar()
                    (currentFragment as TreeInventoryFragment).setupModuleApiCalls()
                } else if(afterbackpressClassName == "PistageFragment") {
                    (currentFragment as PistageFragment).setToolbar()
                } else if(afterbackpressClassName == "ForestryManagementDashboardFragment") {
                    (currentFragment as ForestryManagementDashboardFragment).setToolbar()
                }

                //getSupportFragmentManager().popBackStack()
            } else {
                super.onBackPressed()
            }
            currentFragment = supportFragmentManager.findFragmentById(R.id.container)
            val afterbackpressClassName = currentFragment?.javaClass?.simpleName
            if (afterbackpressClassName == "BcTodaysHistoryFragment") {
                visibleFilter()
                toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
            } else if (afterbackpressClassName == "AddHeaderFragment" || afterbackpressClassName == "BarcodeBordereuDetailsFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.bordereau_module)
                invisibleFilter()
            } else if (afterbackpressClassName == "TodaysHistoryFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.barcode_entries)
                invisibleFilter()
            } else if (afterbackpressClassName == "BcTodaysHistoryFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                visibleFilter()
            } else if (afterbackpressClassName == "OriginVeriUserHistoryFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.origin_verification)
                invisibleFilter()
            } else if (afterbackpressClassName == "BcReprintHeaderFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                invisibleFilter()
            } else if (afterbackpressClassName == "BCReprintLogsDeatilsFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
                invisibleFilter()
            } else if (afterbackpressClassName == "DeleiveryManagementFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
                invisibleFilter()
            } else if (afterbackpressClassName == "InspectionsFragment" || afterbackpressClassName == "ApprovalListFragment"
                || afterbackpressClassName == "InnspectionHeaderFragment" || afterbackpressClassName == "MainContainerFragment"
                || afterbackpressClassName == "SalesLogDetailsFragment" || afterbackpressClassName == "SalesUserHistoryFragment"
            ) {
                toolbars.txtHomeTitle.text = getString(R.string.inspection_n_sales)
                invisibleFilter()
            } else if (afterbackpressClassName == "LoadingWagonsUserHistoryFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.loading_wagons)
                invisibleFilter()
            }
            else if (afterbackpressClassName == "LoadingWagonsHeaderFragment" || afterbackpressClassName == "LoadingWagonsLogsListingFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.loading_wagons)
                invisibleFilter()
            }
            else if (afterbackpressClassName == "LoadingWagonsDeclarFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.loading_n_declration_)
                invisibleFilter()
            } else if (afterbackpressClassName == "OriginVerifFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.origin_verification)
                invisibleFilter()
            } else if (afterbackpressClassName == "OriginLogDetaiilsFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.origin_verification)
                invisibleFilter()
            } else if (afterbackpressClassName == "UnderMaintenaceFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.dashboard)
                invisibleFilter()
            } else if (afterbackpressClassName == "DeliveryUserHistoryFragment") {
                toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
                visibleFilter()
            } else if (afterbackpressClassName == "DeliveryHeaderFragment" || afterbackpressClassName =="DeliveredLogsDetailsFragment" || afterbackpressClassName =="DeliverdBordereuHistoryFragment" || afterbackpressClassName=="DeliveryFilterFragment") {
                invisibleFilter()
                toolbars.txtHomeTitle.text = getString(R.string.drawer_delivery_management)
            } else if (afterbackpressClassName == "TreeDetailsFragment") {
                (currentFragment as TreeDetailsFragment).setToolbar()
            }


        }

    }


    fun replaceFragment(fragment: Fragment, isClearBackStack: Boolean) {
        currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        if (currentFragment != null) {
            if (fragment::class.java.simpleName.equals(currentFragment!!::class.java.simpleName + "")) {
                // getSupportFragmentManager().popBackStack()
                if (isClearBackStack) {
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }
                supportFragmentManager.beginTransaction()
                    //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .add(R.id.container, fragment) // add to backstack
                    .addToBackStack(fragment.javaClass.simpleName)
                    .commit()
            } else {
                // getSupportFragmentManager().popBackStack()
                if (isClearBackStack) {
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }
                supportFragmentManager.beginTransaction()
                    //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .add(R.id.container, fragment) // add to backstack
                    .addToBackStack(fragment.javaClass.simpleName)
                    .commit()
            }
        } else {
            if (isClearBackStack) {
                supportFragmentManager.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
            supportFragmentManager.beginTransaction()
                //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .add(R.id.container, fragment) // add to backstack
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
        }
    }

//    fun replaceFragment(fragment: Fragment) {
//        currentFragment = supportFragmentManager.findFragmentById(R.id.container)
//        if (currentFragment != null) {
//            if (fragment::class.java.simpleName.equals(currentFragment!!::class.java.simpleName + "")) {
//                // getSupportFragmentManager().popBackStack()
//                supportFragmentManager.beginTransaction()
//                    //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//                    .replace(R.id.container, fragment) // add to backstack
//                    .addToBackStack(fragment.javaClass.simpleName)
//                    .commit()
//            } else {
//                // getSupportFragmentManager().popBackStack()
//                supportFragmentManager.beginTransaction()
//                    //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//                    .replace(R.id.container, fragment) // add to backstack
//                    .addToBackStack(fragment.javaClass.simpleName)
//                    .commit()
//            }
//        } else {
//            supportFragmentManager.beginTransaction()
//                //.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//                .replace(R.id.container, fragment) // add to backstack
//                .addToBackStack(fragment.javaClass.simpleName)
//                .commit()
//        }
//    }


}