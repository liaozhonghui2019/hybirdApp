package com.egrand.web.view

import android.os.Bundle
import android.view.View
import com.egrand.web.R
import kotlinx.android.synthetic.main.dialog_scan_result.*


/**
 *  ico on 2019/09/21.
 */
class ScanResultDialog(private var data: String?, private var onConfirm: View.OnClickListener?, private var onCancel: View.OnClickListener?) : BaseFragmentDialog() {

    override val layoutRes: Int = R.layout.dialog_scan_result

    override fun windowAnimations() = R.style.FragmentDialogFade

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.tvMessage.text = data
        this.btnPositive.setOnClickListener {
            this.dismiss()
            if (this.onConfirm != null) {
                this.onConfirm!!.onClick(it)
            }
        }
        this.btnCancel.setOnClickListener {
            this.dismiss()
            if (this.onCancel != null) {
                this.onCancel!!.onClick(it)
            }
        }
    }
}
