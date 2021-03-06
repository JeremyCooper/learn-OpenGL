package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 13/05/17.
 */

import glm.f
import glm.glm
import glm.mat4x4.Mat4
import glm.rad
import glm.set
import glm.vec2.Vec2
import glm.vec2.Vec2i
import glm.vec3.Vec3
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import learnOpenGL.common.GlfwWindow
import learnOpenGL.common.GlfwWindow.Cursor.Disabled
import learnOpenGL.common.glfw
import learnOpenGL.common.loadTexture
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*
import uno.glsl.Program


fun main(args: Array<String>) {

    with(BlendingDiscard()) {

        run()
        end()
    }
}

private class BlendingDiscard {

    val window: GlfwWindow

    val program: ProgramA

    // settings
    val size = Vec2i(1280, 720)

    // camera
    val camera = Camera(position = Vec3(0.0f, 0.0f, 3.0f))
    var last = Vec2(size / 2)

    var firstMouse = true

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    object Object {
        val cube = 0
        val plane = 1
        val transparent = 2
        val MAX = 3
    }

    val vao = intBufferBig(Object.MAX)
    val vbo = intBufferBig(Object.MAX)
    val tex = intBufferBig(Object.MAX)

    // set up vertex data (and buffer(s)) and configure vertex attributes
    // ------------------------------------------------------------------
    val vertices = arrayOf(

            floatBufferOf(
                    // positions         // texture Coords
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
                    +0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

                    -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
                    +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
                    -0.5f, +0.5f, +0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,

                    -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
                    -0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
                    -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

                    +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
                    +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    +0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                    +0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                    +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
                    +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
                    -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
                    +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
                    +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
                    -0.5f, +0.5f, +0.5f, 0.0f, 0.0f,
                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f),

            floatBufferOf(
                    // positions         // texture Coords (note we set these higher than 1 (together with GL_REPEAT as
                    //                      texture wrapping mode). this will cause the floor texture to repeat)
                    +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
                    -5.0f, -0.5f, +5.0f, 0.0f, 0.0f,
                    -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

                    +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
                    -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
                    +5.0f, -0.5f, -5.0f, 2.0f, 2.0f),

            floatBufferOf(
                    // positions       // texture Coords
                    0.0f, +0.5f, 0.0f, 0.0f, 1.0f,
                    0.0f, -0.5f, 0.0f, 0.0f, 0.0f,
                    1.0f, -0.5f, 0.0f, 1.0f, 0.0f,

                    0.0f, +0.5f, 0.0f, 0.0f, 1.0f,
                    1.0f, -0.5f, 0.0f, 1.0f, 0.0f,
                    1.0f, +0.5f, 0.0f, 1.0f, 1.0f))

    // transparent vegetation locations
    val vegetation = arrayOf(
            Vec3(-1.5f, 0.0f, -0.48f),
            Vec3(+1.5f, 0.0f, +0.51f),
            Vec3(+0.0f, 0.0f, +0.7f),
            Vec3(-0.3f, 0.0f, -2.3f),
            Vec3(+0.5f, 0.0f, -0.6f))

    init {

        with(glfw) {

            /*  Initialize GLFW. Most GLFW functions will not work before doing this.
                It also setups an error callback. The default implementation will print the error message in System.err.    */
            init()

            //  Configure GLFW
            windowHint {
                context.version = "3.3"
                profile = "core"
            }
        }

        //  glfw window creation
        window = GlfwWindow(size, "Blending Discard")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@BlendingDiscard::framebuffer_size_callback
            cursorPosCallback = this@BlendingDiscard::mouse_callback
            scrollCallback = this@BlendingDiscard::scroll_callback

            // tell GLFW to capture our mouse
            cursor = Disabled
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // configure global opengl state
        glEnable(GL_DEPTH_TEST)

        // build and compile our shader program
        program = ProgramA("shaders/d/_04", "blending")

        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in 0..Object.transparent) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW)
            glEnableVertexAttribArray(glf.pos3_tc2)
            glVertexAttribPointer(glf.pos3_tc2)
            glEnableVertexAttribArray(glf.pos3_tc2[1])
            glVertexAttribPointer(glf.pos3_tc2[1])
            glBindVertexArray()
        }

        // load textures
        tex[Object.cube] = loadTexture("textures/marble.jpg")
        tex[Object.plane] = loadTexture("textures/metal.png")
        tex[Object.transparent] = loadTexture("textures/grass.png")
    }

    inner open class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(this) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    fun run() {

        //  render loop
        while (window.open) {

            // per-frame time logic
            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            //  input
            processInput(window)

            // render
            glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // draw objects
            glUseProgram(program)
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            val view = camera.viewMatrix
            var model = Mat4()
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // cubes
            glBindVertexArray(vao[Object.cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Object.cube])
            model.translate_(-1.0f, 0.0f, -1.0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4()
                    .translate_(2.0f, 0.0f, 0.0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)

            // floor
            glBindVertexArray(vao[Object.plane])
            glBindTexture(GL_TEXTURE_2D, tex[Object.plane])
            model = Mat4()
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 6)

            // vegetation
            glBindVertexArray(vao[Object.transparent])
            glBindTexture(GL_TEXTURE_2D, tex[Object.transparent])
            vegetation.forEach {
                model = Mat4().translate_(it)
                glUniform(program.model, model)
                glDrawArrays(GL_TRIANGLES, 6)
            }

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuffers(vao, vbo, tex, *vertices)

        window.destroy()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.close = true

        if (window.pressed(GLFW_KEY_W)) camera.processKeyboard(Forward, deltaTime)
        if (window.pressed(GLFW_KEY_S)) camera.processKeyboard(Backward, deltaTime)
        if (window.pressed(GLFW_KEY_A)) camera.processKeyboard(Left, deltaTime)
        if (window.pressed(GLFW_KEY_D)) camera.processKeyboard(Right, deltaTime)

        // TODO up/down?
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }

    /** glfw: whenever the mouse moves, this callback is called */
    fun mouse_callback(xpos: Double, ypos: Double) {

        if (firstMouse) {
            last.x = xpos.f
            last.y = ypos.f
            firstMouse = false
        }

        var xoffset = xpos - last.x
        var yoffset = last.y - ypos // reversed since y-coordinates go from bottom to top
        last.x = xpos.f
        last.y = ypos.f

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        camera.processMouseMovement(xoffset.f, yoffset.f)
    }

    /** glfw: whenever the mouse scroll wheel scrolls, this callback is called  */
    fun scroll_callback(xOffset: Double, yOffset: Double) = camera.processMouseScroll(yOffset.f)
}