package com.example.timelinelib.listener

import com.example.timelinelib.core.asset.TimelineAssetLocation

interface OnAssetVisibleListener {
    fun onAssetVisible(assetLocation: MutableMap<Int,TimelineAssetLocation>)
}