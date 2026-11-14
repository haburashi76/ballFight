package io.github.haburashi76.ballfight.ability

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

object SoundManager {
    fun playSound(media: Media, volume: Double) {
        val player = MediaPlayer(media).apply {
            this.volume = volume
            setOnEndOfMedia {
                dispose()
            }
        }
        player.play()
    }
}