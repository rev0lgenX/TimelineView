package com.example.timelinelib.listener

import com.example.timelinelib.core.asset.TimelineAsset

interface OnAssetVisibleListener {
    fun onAssetVisible(y:Double, asset: TimelineAsset)
}