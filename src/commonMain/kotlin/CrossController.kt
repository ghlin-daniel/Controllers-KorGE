import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlin.math.*

private sealed class Direction(
    protected val degreesFrom: Double,
    protected val degreesTo: Double,
    val direction: Point3,
) {
    companion object {
        private const val DEGREES_RANGE = 45.0

        fun get(degrees: Double): Direction =
            when {
                degrees >= Right.degreesFrom || degrees <= Right.degreesTo -> Right
                degrees in Down.degreesFrom..Down.degreesTo -> Down
                degrees in Left.degreesFrom..Left.degreesTo -> Left
                degrees in Up.degreesFrom..Up.degreesTo -> Up
                else -> None
            }
    }

    data object None : Direction(0.0, 0.0, Point3(0.0, 0.0, 0.0))
    data object Right : Direction(360 - DEGREES_RANGE, DEGREES_RANGE, Point3(1.0, 0.0, 0.0))
    data object Down : Direction(90 - DEGREES_RANGE, 90 + DEGREES_RANGE, Point3(0.0, 1.0, PI / 2))
    data object Left : Direction(180 - DEGREES_RANGE, 180 + DEGREES_RANGE, Point3(-1.0, 0.0, PI))
    data object Up : Direction(270 - DEGREES_RANGE, 270 + DEGREES_RANGE, Point3(0.0, -1.0, -PI / 2))
}

class CrossController private constructor(image: Image) : Container() {

    fun interface OnPressedListener {
        fun onUpdate(direction: Point3?)
    }

    companion object {
        private const val DEFAULT_IMAGE_NAME = "cross_flat_dark.png"

        suspend fun build(imageName: String? = null): CrossController {
            return CrossController(Image(resourcesVfs[imageName ?: DEFAULT_IMAGE_NAME].readBitmap()))
        }
    }

    private val _center: Point
    private var _direction: Direction = Direction.None

    var listener: OnPressedListener? = null

    init {
        addChild(image)
        _center = Point(width / 2, height / 2)

        onMove {
            calculate(it.currentPosLocal)
        }
        onOut {
            _direction = Direction.None
            listener?.onUpdate(null)
        }
    }

    private fun calculate(position: Point) {
        val offset = position - _center
        val theta = atan2(offset.y, offset.x)
        val degrees = (theta / PI * 180) + if (theta > 0) 0 else 360
        Direction.get(degrees).apply {
            if (this != Direction.None && this != _direction) {
                _direction = this
                listener?.onUpdate(this.direction)
            }
        }
    }
}
