package com.niusounds.arfukidashi

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class MyArFragment : ArFragment() {
    override fun getAdditionalPermissions(): Array<String> {
        return arrayOf(Manifest.permission.RECORD_AUDIO)
    }
}