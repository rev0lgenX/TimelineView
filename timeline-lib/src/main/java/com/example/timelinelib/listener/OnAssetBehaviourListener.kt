package com.example.timelinelib.listener

import com.example.timelinelib.core.asset.TimelineAssetLocation

interface OnAssetBehaviourListener {
    fun onAssetVisible(assetLocation: MutableMap<Int,TimelineAssetLocation>)
    fun showAssetAssistant()
    fun hideAssetAssistant()
}