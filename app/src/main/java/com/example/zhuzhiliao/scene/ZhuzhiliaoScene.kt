package com.example.zhuzhiliao.scene

import com.example.zhuzhiliao.domain.PlayState
import com.example.zhuzhiliao.domain.ZhuzhiliaoState
import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.DirectionalLightComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicallyBasedMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The original Android renderer used a custom hollow-tube and wing mesh. The
 * Spatial port keeps the same visual hierarchy but expresses it with SDK
 * primitives so the scene remains self-contained and resize-safe.
 */
class ZhuzhiliaoScene {
    companion object {
        private const val HANDLE_Y = 0.18f
        // Keep the toy close to the volume center. The emulator's volumetric
        // depth crop is narrower than the nominal container depth.
        private const val HANDLE_Z = 0.02f
        private const val ROPE_ANCHOR_Y = 0.115f
        private const val TOY_CENTER_Y = -0.075f
        private const val ORBIT_RADIUS = 0.055f
        private const val ORBIT_DEPTH = 0.012f
        private const val BODY_HEIGHT = 0.13f
        private const val ROPE_SEGMENTS = 3
        private const val ROPE_SEGMENT_HEIGHT = 0.075f
    }

    private var root: Entity? = null
    private var ready = false
    private var pendingState = ZhuzhiliaoState()

    private lateinit var toyRoot: Entity
    private lateinit var body: Entity
    private lateinit var leftWing: Entity
    private lateinit var rightWing: Entity
    private lateinit var ropeSegments: List<Entity>

    fun initialize(content: SpatialViewContent) {
        if (ready) return
        val sceneRoot = Entity()
        root = sceneRoot
        // The template window is intentionally roomy; enlarge the complete toy
        // as one unit so it reads like a hand-held object at headset distance.
        sceneRoot.components.set(
            TransformComponent()
                .setPosition(Vector3(-0.16f, 0f, 0f))
                .setScaleVector(Vector3(1.45f))
        )

        val wood = createMaterial("#A96838", roughness = 0.46f, glow = 0.13f)
        val darkWood = createMaterial("#3F2418", roughness = 0.38f, glow = 0.05f)
        val bamboo = createMaterial("#D6A044", roughness = 0.44f, glow = 0.10f)
        val paleBamboo = createMaterial("#F1D27C", roughness = 0.34f, glow = 0.14f)
        val red = createMaterial("#C44332", roughness = 0.34f, glow = 0.12f)
        val eye = createMaterial("#120F0C", roughness = 0.18f, metallic = 0.04f, glow = 0.06f)
        val wing = createMaterial("#F6D777", roughness = 0.32f, glow = 0.15f)

        // Grip, collar rings, and the short shaft reproduce the reference's
        // fixed upper assembly while staying inside the volumetric depth slice.
        createEntity(
            MeshResource.createBox(Vector3(0.060f, 0.095f, 0.060f), cornerRadius = 0.012f), wood,
            Vector3(0f, HANDLE_Y, HANDLE_Z), sceneRoot,
        )
        createEntity(
            MeshResource.createBox(Vector3(0.073f, 0.010f, 0.073f), cornerRadius = 0.004f), darkWood,
            Vector3(0f, HANDLE_Y - 0.035f, HANDLE_Z), sceneRoot,
        )
        createEntity(
            MeshResource.createBox(Vector3(0.073f, 0.010f, 0.073f), cornerRadius = 0.004f), darkWood,
            Vector3(0f, HANDLE_Y + 0.035f, HANDLE_Z), sceneRoot,
        )
        createEntity(MeshResource.createSphere(0.020f), red, Vector3(0f, HANDLE_Y + 0.060f, HANDLE_Z), sceneRoot)
        createEntity(
            MeshResource.createBox(Vector3(0.014f, 0.075f, 0.014f), cornerRadius = 0.003f), darkWood,
            Vector3(0f, (ROPE_ANCHOR_Y + HANDLE_Y - 0.050f) * 0.5f, HANDLE_Z), sceneRoot,
        )

        toyRoot = Entity().apply {
            components.set(TransformComponent().setPosition(Vector3(0f, TOY_CENTER_Y, HANDLE_Z)))
            sceneRoot.addChild(this)
        }

        // The bamboo tube hangs vertically from the cord. Torus bands keep the
        // silhouette cylindrical instead of turning the toy into a flat box.
        body = createEntity(
            MeshResource.createCylinder(BODY_HEIGHT, 0.040f), bamboo,
            Vector3(0f, 0f, 0f), toyRoot,
        )
        createEntity(
            MeshResource.createCylinder(0.008f, 0.041f), paleBamboo,
            Vector3(0f, 0.060f, 0f), toyRoot,
        )
        createEntity(
            MeshResource.createCylinder(0.008f, 0.041f), paleBamboo,
            Vector3(0f, -0.060f, 0f), toyRoot,
        )
        createEntity(
            MeshResource.createTorus(0.045f, 0.037f), red,
            Vector3(0f, -0.047f, 0f), toyRoot,
        )
        createEntity(
            MeshResource.createTorus(0.045f, 0.037f), red,
            Vector3(0f, 0.047f, 0f), toyRoot,
        )
        // +Z is the forward-facing side in the Spatial right-handed space.
        createEntity(MeshResource.createSphere(0.021f), eye, Vector3(-0.026f, 0.018f, 0.052f), toyRoot)
        createEntity(MeshResource.createSphere(0.021f), eye, Vector3(0.026f, 0.018f, 0.052f), toyRoot)
        leftWing = createEntity(
            MeshResource.createSphere(0.055f), wing,
            Vector3(-0.090f, 0.002f, 0.004f), toyRoot, EulerAngles(0f, 0f, -14f),
            Vector3(2.20f, 0.62f, 0.18f),
        )
        rightWing = createEntity(
            MeshResource.createSphere(0.055f), wing,
            Vector3(0.090f, 0.002f, 0.004f), toyRoot, EulerAngles(0f, 0f, 14f),
            Vector3(2.20f, 0.62f, 0.18f),
        )

        ropeSegments = List(ROPE_SEGMENTS) {
            createEntity(
                MeshResource.createBox(Vector3(0.0064f, ROPE_SEGMENT_HEIGHT, 0.0064f), cornerRadius = 0.002f), darkWood,
                Vector3(0f, 0f, HANDLE_Z), sceneRoot,
            )
        }

        sceneRoot.components.set(DirectionalLightComponent(Color4.fromSRGBHex("#FFF1D5"), 0.95f))
        content.addEntity(sceneRoot)
        ready = true
        render(pendingState)
    }

    fun render(state: ZhuzhiliaoState) {
        pendingState = state
        if (!ready) return

        val angle = state.angle
        val running = state.playState == PlayState.AUTO || state.angularVelocity > 0.01f
        val toyX = sin(angle) * ORBIT_RADIUS
        val toyZ = HANDLE_Z + cos(angle) * ORBIT_DEPTH
        val toyY = TOY_CENTER_Y + if (running) sin(state.elapsedSeconds * 5.4f) * 0.004f else 0f
        toyRoot.components[TransformComponent::class.java]?.apply {
            setPosition(Vector3(toyX, toyY, toyZ))
            setEulerAngles(EulerAngles(0f, Math.toDegrees(angle.toDouble()).toFloat(), 0f))
        }

        // WebRopeShape from the reference is represented by short primitive
        // cylinders. This keeps the endpoint and gentle sag without OpenGL.
        val anchor = Vector3(0f, ROPE_ANCHOR_Y, HANDLE_Z)
        val bob = Vector3(toyX, toyY + BODY_HEIGHT * 0.47f, toyZ)
        val directLength = distance(anchor, bob)
        val ropeLength = (directLength + 0.008f).coerceAtLeast(0.02f)
        val slack = (ropeLength - directLength).coerceAtLeast(0f)
        val sag = slack * 0.55f
        ropeSegments.forEachIndexed { index, entity ->
            val start = ropePoint(anchor, bob, sag, index.toFloat() / ROPE_SEGMENTS)
            val end = ropePoint(anchor, bob, sag, (index + 1).toFloat() / ROPE_SEGMENTS)
            val dx = end.x - start.x
            val dy = end.y - start.y
            val dz = end.z - start.z
            val length = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.001f)
            val transform = entity.components[TransformComponent::class.java] ?: return@forEachIndexed
            transform.setPosition(
                Vector3(
                    (start.x + end.x) * 0.5f,
                    (start.y + end.y) * 0.5f,
                    (start.z + end.z) * 0.5f,
                ),
            )
            transform.setScaleVector(Vector3(1f, length / ROPE_SEGMENT_HEIGHT, 1f))
            transform.setEulerAngles(EulerAngles(0f, 0f, -Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())).toFloat()))
        }

        val flap = if (state.playState == PlayState.AUTO) sin(state.elapsedSeconds * 18f) * 7f else 0f
        leftWing.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(flap, 0f, -14f))
        rightWing.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(-flap, 0f, 14f))
        val selfSpin = Math.toDegrees(state.elapsedSeconds.toDouble() * 0.7).toFloat()
        body.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, selfSpin, 0f))
    }

    fun dispose() {
        root?.destroy(true)
        root = null
        ready = false
    }

    private fun ropePoint(anchor: Vector3, bob: Vector3, sag: Float, fraction: Float): Vector3 {
        val point = Vector3(
            anchor.x + (bob.x - anchor.x) * fraction,
            anchor.y + (bob.y - anchor.y) * fraction,
            anchor.z + (bob.z - anchor.z) * fraction,
        )
        return Vector3(point.x, point.y - sag * sin(Math.PI.toFloat() * fraction), point.z)
    }

    private fun distance(first: Vector3, second: Vector3): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        val dz = first.z - second.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun createEntity(
        mesh: MeshResource,
        material: PhysicallyBasedMaterial,
        position: Vector3,
        parent: Entity,
        rotation: EulerAngles = EulerAngles(0f, 0f, 0f),
        scale: Vector3 = Vector3.ONE,
    ): Entity = Entity().apply {
        components.set(ModelComponent(mesh, material))
        components.set(TransformComponent().apply {
            setPosition(position)
            setEulerAngles(rotation)
            setScaleVector(scale)
        })
        parent.addChild(this)
    }

    private fun createMaterial(
        color: String,
        opacity: Float = 1f,
        roughness: Float,
        metallic: Float = 0.02f,
        glow: Float = 0f,
    ): PhysicallyBasedMaterial = PhysicallyBasedMaterial.create(
        if (opacity < 1f) BlendingMode.TRANSPARENT else BlendingMode.OPAQUE,
    ).apply {
        setBaseColor(Color4.fromSRGBHex(color))
        setRoughness(roughness)
        setMetallic(metallic)
        setOpacity(opacity)
        if (glow > 0f) setEmissiveColor(Color4.fromSRGBHex(color).times(glow))
    }
}
