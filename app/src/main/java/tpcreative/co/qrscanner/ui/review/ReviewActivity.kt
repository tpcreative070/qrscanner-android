package tpcreative.co.qrscanner.ui.review

import android.Manifest
import android.content.Context
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.listener.PermissionRequest
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.adapter.DividerItemDecoration
import tpcreative.co.qrscanner.model.Create
import tpcreative.co.qrscanner.model.Theme
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultAdapter
import java.io.File
import java.util.*

class ReviewActivity : BaseActivitySlide(), ReviewView, UtilsListener, ScannerResultAdapter.ItemSelectedListener {
    @BindView(R.id.imgResult)
    var imgResult: AppCompatImageView? = null
    private var presenter: ReviewPresenter? = null
    private var create: Create? = null
    private var bitmap: Bitmap? = null
    private var code: String? = null
    private var save: SaveModel? = SaveModel()
    private var isComplete = false

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.scrollView)
    var scrollView: NestedScrollView? = null
    private var adapter: ScannerResultAdapter? = null
    var llm: LinearLayoutManager? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        scrollView.smoothScrollTo(0, 0)
        presenter = ReviewPresenter()
        setupRecyclerViewItem()
        presenter.bindView(this)
        presenter.getIntent(this)
    }

    fun setupRecyclerViewItem() {
        llm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(llm)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        adapter = ScannerResultAdapter(getLayoutInflater(), this, this)
        recyclerView.setAdapter(adapter)
        recyclerView.setNestedScrollingEnabled(false)
    }

    override fun onClickItem(position: Int) {
        val itemNavigation: ItemNavigation? = presenter.mListItemNavigation[position]
        if (itemNavigation != null) {
            when (itemNavigation.enumAction) {
                EnumAction.SHARE -> {
                    if (code != null) {
                        Utils.Log(TAG, "Share")
                        onGenerateCode(code, EnumAction.SHARE)
                    }
                }
                EnumAction.SAVE -> {
                    if (code != null) {
                        onAddPermissionSave(EnumAction.SAVE)
                    }
                }
            }
        }
    }

    override fun onCatch() {
        onBackPressed()
    }

    protected override fun onResume() {
        super.onResume()
    }

    protected override fun onPause() {
        super.onPause()
    }

    protected override fun onDestroy() {
        super.onDestroy()
    }

    protected override fun onStop() {
        super.onStop()
        if (isComplete) {
            GenerateSingleton.Companion.getInstance().onCompletedGenerate()
        }
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun setView() {
        create = presenter.create
        when (create.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                code = "MECARD:N:" + create.fullName + ";TEL:" + create.phone + ";EMAIL:" + create.email + ";ADR:" + create.address + ";"
                save = SaveModel()
                save.fullName = create.fullName
                save.phone = create.phone
                save.email = create.email
                save.address = create.address
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                code = "MATMSG:TO:" + create.email + ";SUB:" + create.subject + ";BODY:" + create.message + ";"
                save = SaveModel()
                save.email = create.email
                save.subject = create.subject
                save.message = create.message
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.PRODUCT -> {
                code = create.productId
                save = SaveModel()
                save.text = create.productId
                save.createType = create.createType.name
                save.barcodeFormat = create.barcodeFormat
                onGenerateReview(code)
            }
            ParsedResultType.URI -> {
                code = create.url
                save = SaveModel()
                save.url = create.url
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.WIFI -> {
                code = "WIFI:S:" + create.ssId + ";T:" + create.networkEncryption + ";P:" + create.password + ";H:" + create.hidden + ";"
                save = SaveModel()
                save.ssId = create.ssId
                save.password = create.password
                save.networkEncryption = create.networkEncryption
                save.hidden = create.hidden
                save.createType = create.createType.name
                onGenerateReview(code)
                Utils.Log(TAG, "wifi " + create.networkEncryption)
            }
            ParsedResultType.GEO -> {
                code = "geo:" + create.lat + "," + create.lon + "?q=" + create.query + ""
                save = SaveModel()
                save.lat = create.lat
                save.lon = create.lon
                save.query = create.query
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.TEL -> {
                code = "tel:" + create.phone + ""
                save = SaveModel()
                save.phone = create.phone
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.SMS -> {
                code = "smsto:" + create.phone + ":" + create.message
                save = SaveModel()
                save.phone = create.phone
                save.message = create.message
                save.createType = create.createType.name
                onGenerateReview(code)
            }
            ParsedResultType.CALENDAR -> {
                val builder = StringBuilder()
                builder.append("BEGIN:VEVENT")
                builder.append("\n")
                builder.append("SUMMARY:" + create.title)
                builder.append("\n")
                builder.append("DTSTART:" + create.startEvent)
                builder.append("\n")
                builder.append("DTEND:" + create.endEvent)
                builder.append("\n")
                builder.append("LOCATION:" + create.location)
                builder.append("\n")
                builder.append("DESCRIPTION:" + create.description)
                builder.append("\n")
                builder.append("END:VEVENT")
                save = SaveModel()
                save.title = create.title
                save.startEvent = create.startEvent
                save.endEvent = create.endEvent
                save.startEventMilliseconds = create.startEventMilliseconds
                save.endEventMilliseconds = create.endEventMilliseconds
                save.location = create.location
                save.description = create.description
                save.createType = create.createType.name
                code = builder.toString()
                onGenerateReview(code)
            }
            ParsedResultType.ISBN -> {
            }
            else -> {
                code = create.text
                save = SaveModel()
                save.text = create.text
                save.createType = create.createType.name
                onGenerateReview(code)
            }
        }
        presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
        presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SAVE, R.drawable.baseline_save_alt_white_48, "Save"))
        onReloadData()
    }

    override fun onReloadData() {
        adapter.setDataSource(presenter.mListItemNavigation)
    }

    fun onAddPermissionSave(enumAction: EnumAction?) {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            onGenerateCode(code, enumAction)
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_review, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_print -> {
                if (code != null) {
                    onAddPermissionSave(EnumAction.PRINT)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onGenerateCode(code: String?, enumAction: EnumAction?) {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                val barcodeEncoder = BarcodeEncoder()
                                val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
                                hints[EncodeHintType.MARGIN] = 2
                                val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
                                Utils.Log(TAG, "Starting save items 0")
                                bitmap = if (create.createType == ParsedResultType.PRODUCT) {
                                    barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.valueOf(create.barcodeFormat), 400, 400, hints)
                                } else {
                                    barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 400, 400, hints)
                                }
                                Utils.saveImage(bitmap, enumAction, create.createType.name, code, this@ReviewActivity)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun onSaved(path: String?, enumAction: EnumAction?) {
        Utils.Log(TAG, "Saved successful")
        isComplete = true
        when (enumAction) {
            EnumAction.SAVE -> {

                /*Adding new columns*/if (save.createType !== ParsedResultType.PRODUCT.name) {
                    save.barcodeFormat = BarcodeFormat.QR_CODE.name
                }
                save.favorite = false
                Utils.onAlertNotify(this, "Saved code successful => Path: $path")
                if (create.enumImplement == EnumImplement.CREATE) {
                    val time = Utils.getCurrentDateTimeSort()
                    save.createDatetime = time
                    save.updatedDateTime = time
                    SQLiteHelper.onInsert(save)
                } else if (create.enumImplement == EnumImplement.EDIT) {
                    val time = Utils.getCurrentDateTimeSort()
                    save.updatedDateTime = time
                    save.createDatetime = create.createdDateTime
                    save.id = create.id
                    save.isSynced = create.isSynced
                    save.uuId = create.uuId
                    SQLiteHelper.onUpdate(save, true)
                }
            }
            EnumAction.SHARE -> {
                val file = File(path)
                if (file.isFile) {
                    Utils.Log(TAG, "path : $path")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val uri: Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
                        shareToSocial(uri)
                    } else {
                        val uri = Uri.fromFile(file)
                        shareToSocial(uri)
                    }
                } else {
                    Utils.onAlertNotify(this, getString(R.string.no_items_found))
                }
            }
            EnumAction.PRINT -> {
                onPhotoPrint(path)
            }
            else -> {
                Utils.Log(TAG, "Other case")
            }
        }
    }

    private fun onPhotoPrint(path: String?) {
        try {
            val photoPrinter = PrintHelper(this)
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT)
            val bitmap: Bitmap = BitmapFactory.decodeFile(path)
            photoPrinter.printBitmap(Utils.getCurrentDate(), bitmap)
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    fun shareToSocial(value: Uri?) {
        Utils.Log(TAG, "path call")
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, value)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onGenerateReview(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
            Utils.Log(TAG, "barcode====================> " + code + "--" + create.createType.name)
            bitmap = if (create.createType == ParsedResultType.PRODUCT) {
                barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.valueOf(create.barcodeFormat), 200, 200, hints)
            } else {
                barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 200, 200, hints)
            }
            imgResult.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        protected val TAG = ReviewActivity::class.java.simpleName
    }
}