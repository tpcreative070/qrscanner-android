package tpcreative.co.qrscanner.ui.create
import android.content.*
import android.os.Bundle
import android.view.*
import kotlinx.android.synthetic.main.fragment_generate.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.QRCodeType
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel
import java.util.*

class GenerateFragment : BaseFragment(), GenerateCell.ItemSelectedListener {
    lateinit var viewModel : GenerateViewModel
    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_generate, viewGroup, false)
    }

    override fun work() {
        super.work()
        initUI()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
        } else {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
            Utils.Log(TAG, "isInVisible")
        }
    }

    override fun getContext(): Context? {
        return super.getContext()
    }

    fun bindData(data : MutableList<QRCodeType>) {
        //CUSTOM SORT ACCORDING TO CATEGORIES
        val cells: MutableList<GenerateCell?> = ArrayList()
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (galaxy in data) {
            val cell = GenerateCell(galaxy)
            cell.setListener(this)
            cells.add(cell)
        }
        recyclerView.removeAllCells()
        recyclerView.addCells(cells)
    }

    override fun onClickItem(position: Int, isChecked: Boolean) {
        var mPosition = position
        when (mPosition) {
            0 -> {
                Navigator.onGenerateView(activity, null, BarcodeFragment::class.java)
            }
            1 -> {
                Navigator.onGenerateView(activity, null, EmailFragment::class.java)
            }
            2 -> {
                Navigator.onGenerateView(activity, null, MessageFragment::class.java)
            }
            3 -> {
                Navigator.onGenerateView(activity, null, LocationFragment::class.java)
            }
            4 -> {
                Navigator.onGenerateView(activity, null, EventFragment::class.java)
            }
            5 -> {
                Navigator.onGenerateView(activity, null, ContactFragment::class.java)
            }
            6 -> {
                Navigator.onGenerateView(activity, null, TelephoneFragment::class.java)
            }
            7 -> {
                Navigator.onGenerateView(activity, null, TextFragment::class.java)
            }
            8 -> {
                Navigator.onGenerateView(activity, null, WifiFragment::class.java)
            }
            9 -> {
                Navigator.onGenerateView(activity, null, UrlFragment::class.java)
            }
        }
    }

    override fun onClickShare(value: String?) {}
    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
    }

    companion object {
        private val TAG = GenerateFragment::class.java.simpleName
        fun newInstance(index: Int): GenerateFragment {
            val fragment = GenerateFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}