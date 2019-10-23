package com.egrand.web.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.egrand.web.R
import kotlinx.android.synthetic.main.dialog_qrcode.*
import kotlinx.android.synthetic.main.dialog_scan_result.btnPositive


/**
 *  ico on 2019/09/21.
 */
class QRCodeDialog(private var data: Bitmap?) : BaseFragmentDialog() {

    override val layoutRes: Int = R.layout.dialog_qrcode

    override fun windowAnimations() = R.style.FragmentDialogFade

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.ivQRCode.setImageBitmap(data)
        this.btnPositive.setOnClickListener {
            this.dismiss()
        }
    }
}
