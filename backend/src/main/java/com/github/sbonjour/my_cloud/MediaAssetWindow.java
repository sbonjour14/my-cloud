package com.github.sbonjour.my_cloud;

import java.util.List;

import com.github.sbonjour.my_cloud.entity.MediaAsset;

public record MediaAssetWindow(List<MediaAsset> items, String nextCursor) {
}