An android framework/game engine consisting of a scene graph plus some utility classes. It's mainly aimed to abstract OpenGL ES. It's developed to function with the Android Dev Phone 1 that features an accelerometer, magnetometer, touchscreen and trackball. If you got another device, please test it and report back!

Some of the current features:
  * Spatial key frame animations (no morphing)
  * Picking
  * OBJ importer
  * Binary importer/exporter
  * Lighting
  * Materials
  * Simple sensor filtering
  * VBO support

In the current state, it's tightly integrated with a demo application.

Some missing features:
  * Textures
  * Blending
  * Rendering queues
  * Lines and points. Currently only geometrics based upon triangles are supported.
  * Collision detection

Tested phones:
  * Android Dev Phone 1 / G1
  * HTC Hero (Thanks to letrocquermick)
  * HTC Tattoo (Thanks to letrocquermick)
  * Google Nexus One
  * Motorola DROID

Note, the test application currently needs a trackball to be able to switch to the "first person" 3D View as seen on the Youtube video.

See the [youtube demo](http://www.youtube.com/watch?v=wnr65iId-v0).