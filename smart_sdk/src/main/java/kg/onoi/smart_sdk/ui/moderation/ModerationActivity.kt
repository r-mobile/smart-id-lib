package kg.onoi.smart_sdk.ui.moderation

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.face_detection.RegistrationFaceDetectionActivity
import kg.onoi.smart_sdk.ui.moderation.adapters.ModerationItemClick
import kg.onoi.smart_sdk.ui.moderation.adapters.RefusedItemAdapter
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoActivity
import kg.onoi.smart_sdk.ui.photos.PassportBackPhotoActivity
import kg.onoi.smart_sdk.ui.photos.PassportFrontPhotoActivity
import kg.onoi.smart_sdk.ui.photos.SelfieWithPassportPhotoActivity
import kg.onoi.smart_sdk.ui.registration_complete.RegistrationStatusActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_moderation.*
import org.parceler.Parcels

class ModerationActivity :
    BaseActivity<ModerationVM>(R.layout.activity_moderation, ModerationVM::class),
    ModerationItemClick {

    private val adapter = RefusedItemAdapter(this)
    private var moderationItem: ModerationItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        parseIntentData()
        subscribeToLiveData()
        setupViews()
        updateRefusedItem()
        updateNextButtonState()
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is ModerationEvents.CancelRegistration -> {
                        closeActivityStack()
                    }
                    is Event.Success -> {
                        closeActivityStack(); RegistrationStatusActivity.start(
                            this,
                            SdkHelper.sessionId,
                            SdkHelper.checkStatusCallback
                        )
                    }
                }
            }
        })
    }

    private fun parseIntentData() {
        viewModel.moderationResult =
            Parcels.unwrap<Moderation>(
                intent.getParcelableExtra(ModerationResult::class.java.canonicalName)
            )
    }

    private fun setupViews() {
        cv_toolbar.init(this, R.string.refuse_moderator)
        cv_toolbar.hideBackButton()
        rv_rufused_items.adapter = adapter
        rv_rufused_items.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        viewModel.moderationResult?.userData?.run {
            tv_name.text = "$surname $name $patronymic"
            tv_pin.text = "${getString(R.string.inn)}: $inn"

            tv_name.visible()
            tv_pin.visible()
        }
        btn_cancel.setOnClickListener { showCancelQueryMessage() }
        btn_next.setOnClickListener { viewModel.resubmit() }
    }

    private fun showCancelQueryMessage() {
        showConfirmDialog(
            message = getString(R.string.cancel_registration_query),
            okLabel = getString(R.string.yes),
            cancelLabel = getString(R.string.no),
            onOkClick = { viewModel.cancelRegistration() })
    }

    private fun updateRefusedItem() {
        val items = mutableListOf<ModerationItem>()
        viewModel.moderationResult?.moderationResult?.let {
            if (it.failedPassportFront) items.add(
                ModerationItem(
                    R.string.passport_front_photo,
                    PhotoType.PASSPORT_FRONT
                )
            )
            if (it.failedPassportBack) items.add(
                ModerationItem(
                    R.string.passport_back_photo,
                    PhotoType.PASSPORT_BACK
                )
            )
            if (it.failedUserPhoto) items.add(ModerationItem(R.string.selfie, PhotoType.SELFIE))
            if (it.failedUserPhotoWithPassport) items.add(
                ModerationItem(
                    R.string.selfie_with_passport,
                    PhotoType.SELFIE_W_PASSPORT
                )
            )
            if (it.failedPassportInfo) items.add(ModerationItem(R.string.passport_data))
        }
        adapter.items = items
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            moderationItem?.isSuccessUpdated = true
            adapter.notifyDataSetChanged()
            updateNextButtonState()
        }
    }

    private fun updateNextButtonState() {
        btn_next.isEnabled = adapter.items.any { !it.isSuccessUpdated }.not()
    }

    override fun onItemClick(item: ModerationItem) {
        moderationItem = item
        when (item.type) {
            PhotoType.SELFIE_W_PASSPORT -> SelfieWithPassportPhotoActivity.startForResult(this)
            PhotoType.SELFIE -> RegistrationFaceDetectionActivity.startForResult(this)
            PhotoType.PASSPORT_BACK -> PassportBackPhotoActivity.startForResult(this)
            PhotoType.PASSPORT_FRONT -> PassportFrontPhotoActivity.startForResult(this)
            null -> PassportInfoActivity.startForResult(this)
        }
    }

    companion object {
        fun start(context: Context, moderation: Moderation) {
            context.startActivity(Intent(context, ModerationActivity::class.java).apply {
                putExtra(ModerationResult::class.java.canonicalName, Parcels.wrap(moderation))
                putExtra(Constant.Extra.USER, Parcels.wrap(User(
                    SdkHelper.sessionId,
                    moderation.userData!!)))
            })
        }
    }
}