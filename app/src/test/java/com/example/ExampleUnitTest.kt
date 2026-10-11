package com.example

import com.example.data.model.StreamItem
import com.example.util.DeviceCodecHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDeviceCodecHelper_detectsCodecAndQuality() {
    val stream4kHevc = StreamItem(
      title = "Movie.2024.2160p.UHD.HEVC.x265.DDP5.1.mkv",
      url = "https://example.com/video1.mkv"
    )
    val stream1080pAvc = StreamItem(
      title = "Movie.2024.1080p.FHD.AVC.x264.AAC.mkv",
      url = "https://example.com/video2.mkv"
    )

    assertTrue(DeviceCodecHelper.is4kStream(stream4kHevc))
    assertTrue(DeviceCodecHelper.isHevcStream(stream4kHevc))

    assertFalse(DeviceCodecHelper.is4kStream(stream1080pAvc))
    assertFalse(DeviceCodecHelper.isHevcStream(stream1080pAvc))
    assertTrue(DeviceCodecHelper.isH264Stream(stream1080pAvc))
  }

  @Test
  fun testDeviceCodecHelper_selectBestCompatibleStream_prefersPlayable() {
    val stream4k = StreamItem(
      title = "Movie.2160p.HEVC.x265.mkv",
      url = "https://example.com/4k.mkv"
    )
    val stream1080p = StreamItem(
      title = "Movie.1080p.H264.x264.mkv",
      url = "https://example.com/1080p.mkv"
    )
    val streams = listOf(stream4k, stream1080p)
    val selected = DeviceCodecHelper.selectBestCompatibleStream(streams)

    assertNotNull(selected)
    assertTrue(selected?.isPlayable == true)
  }
}

