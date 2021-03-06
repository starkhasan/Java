package com.usl.usl.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.usl.usl.R
import com.usl.usl.apps.RegisterAbstractActivity
import com.usl.usl.network.response.usersheets.Sheet
import com.usl.usl.utils.AppUser
import com.usl.usl.utils.Helper
import com.usl.usl.utils.MyProgressDialog
import org.greenrobot.eventbus.Subscribe
import java.util.*

class SheetsActivity : RegisterAbstractActivity(), View.OnClickListener {


    @BindView(R.id.toolbar)
    var toolbar: View? = null
    @BindView(R.id.tvNoSheet)
    var tvNoSheet: TextView? = null
    @BindView(R.id.rvSheetData)
    var rvSheetData: RecyclerView? = null
    lateinit var ivBack: ImageView
    lateinit var tvHeading: TextView
    var progressDialog: MyProgressDialog? = null
    var appUser = AppUser()
    var sheetDataAdapter: SheetDataAdapter? = null
    var saveGameList: List<Sheet> = ArrayList()
    var linearLayoutManager: LinearLayoutManager? = null
    var submitPos = 0
    var DeletePos:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        settoolbar()
    }


    fun settoolbar() {
        ivBack = toolbar!!.findViewById(R.id.ivBack)
        ivBack.setImageResource(R.drawable.ic_arrow_back)
        ivBack.setOnClickListener(this)
        tvHeading = toolbar!!.findViewById(R.id.tvHeading)
        tvHeading.text = "Sheets"
    }


    override fun layoutId(): Int {
        return R.layout.activity_sheetdata
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ivBack -> finish()
            else -> {
            }
        }
    }

    @Subscribe
    fun timeout(msg: String?) {
        //progressDialog!!.dismiss()
        Helper().alert(this, msg, "USL")
    }

}                                                                                                                                                       