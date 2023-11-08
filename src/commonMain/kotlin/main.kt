import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.io.lang.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.time.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*
import korlibs.time.*

class Tank private constructor(image: Image) : Container() {
    companion object {

        private const val UPDATE_RATE = 1000.0 / 120.0

        private const val MOVING_SPEED_PER_UPDATE = 3

        suspend fun build(): Tank {
            return Tank(Image(resourcesVfs["tank.png"].readBitmap()))
        }
    }

    private var direction: Point3? = null
    private var _timer: Closeable? = null

    init {
        addChild(image)
        image.anchor = Anchor.CENTER
    }

    fun move(newDirection: Point3) {
        if (_timer == null) {
            _timer = intervalAndNow(TimeSpan.fromMilliseconds(UPDATE_RATE)) {
                x += (direction?.x ?: 0.0) * MOVING_SPEED_PER_UPDATE
                y += (direction?.y ?: 0.0) * MOVING_SPEED_PER_UPDATE
            }
        }
        direction = newDirection
        rotation = Angle.fromRadians(direction?.z ?: 0.0)
    }

    fun stop() {
        _timer?.close()
        _timer = null
        direction = null
    }
}

suspend fun main() = Korge(windowSize = Size(600, 800)) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo({ MyScene() })
}

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val tank = Tank.build()
        addChild(tank.centerOn(this))

        val controller = CrossController.build().apply {
            listener = CrossController.OnPressedListener {
                if (it == null) {
                    tank.stop()
                } else {
                    tank.move(it)
                }
            }
        }
        addChild(controller.centerXOn(this).positionY(600))
    }
}
