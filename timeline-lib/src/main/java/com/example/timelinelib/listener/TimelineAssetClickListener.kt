package com.example.timelinelib.listener

import com.example.timelinelib.core.asset.TimelineAsset

interface TimelineAssetClickListener {
    fun onAssetClick(asset:TimelineAsset)
}