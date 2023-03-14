package tpcreative.co.qrscanner.ui.create
import android.content.*
import android.os.Bundle
import android.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.databinding.FragmentGenerateBinding
import tpcreative.co.qrscanner.model.QRCodeType
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel
import java.util.*

class GenerateFragment : BaseFragment(), GenerateCell.ItemSelectedListener {
    lateinit var viewModel : GenerateViewModel
    lateinit var binding : FragmentGenerateBinding
    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View {
        binding = FragmentGenerateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun work() {
        super.work()
        initUI()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
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
        binding.recyclerView.removeAllCells()
        binding.recyclerView.addCells(cells)
    }

    override fun onClickItem(position: Int, isChecked: Boolean) {
        val mPosition = position
        when (mPosition) {
            0 -> {
                Navigator.onGenerateView(activity, null, BarcodeActivity::class.java)
            }
            1 -> {
                Navigator.onGenerateView(activity, null, EmailActivity::class.java)
            }
            2 -> {
                Navigator.onGenerateView(activity, null, MessageActivity::class.java)
            }
            3 -> {
                Navigator.onGenerateView(activity, null, LocationActivity::class.java)
            }
            4 -> {
                Navigator.onGenerateView(activity, null, EventActivity::class.java)
            }
            5 -> {
                Navigator.onGenerateView(activity, null, ContactActivity::class.java)
            }
            6 -> {
                Navigator.onGenerateView(activity, null, TelephoneActivity::class.java)
            }
            7 -> {
                Navigator.onGenerateView(activity, null, TextActivity::class.java)
            }
            8 -> {
                Navigator.onGenerateView(activity, null, WifiActivity::class.java)
            }
            9 -> {
                Navigator.onGenerateView(activity, null, UrlActivity::class.java)
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