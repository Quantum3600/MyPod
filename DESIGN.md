DESIGN.md — iPod Classic Skeuomorphic Design System

Companion to AGENTS.md and docs/spec.md. This is the single source of truth for how every pixel should look. Any Composable rendering chassis, screen chrome, or controls should pull values from here — no ad-hoc colors/gradients invented per-screen.

1. Design Philosophy

This design should match the reference as closely as possible: identical iPod Classic silhouette, soft clay-morphed rounded geometry, grainy cast-metal chassis, and a more square, balanced screen. The body should feel like a dense, tactile metal shell with rounded edges, subtle sculpting, and deeply layered shadowing. Nothing should read as flat or modern; everything must feel physically machined, soft-edged, and premium.

Light source convention: single key light, upper-left, ~45°, consistent across every element. This is what makes the depth cues feel cohesive instead of a collage of unrelated surfaces.

2. Core Form & Layout

- Device silhouette: broad, rounded rectangular body with thick chamfered edges and subtle clay-like curvature. The overall shape should feel like a sculpted object with soft, beveled transitions instead of hard planar edges.
- Chassis texture: fine grainy, brushed-metal feel with visible depth and material variation. The metal should look cast and slightly aged, not polished or cold industrial.
- Screen proportion: more square than portrait rectangle; the display should feel wider and more balanced, closer to a near-square or slightly landscape rectangle than a narrow tall panel.
- Wheel geometry: much larger than the current concept, occupying the dominant lower-body control area. It should feel like the core mechanical interaction surface of the device.
- Center button: same material language as the chassis, concave, recessed, textured, and integrated into the body. It should be visually carved into the same cast shell, not a separate plastic part.
- Wheel finish: rubberized, soft-touch texture with micrograin and a matte, slightly compressible read.

3. Color Tokens

Each theme defines the same token set; values below are the Space Gray reference theme.

// Chassis
chassis.base            #4A4C4E
chassis.baseHighlight    #6E7072   // upper-left clay specular
chassis.baseShadow       #2E2F30   // lower-right falloff
chassis.edgeHighlight    #8A8C8E   // 1px rim light on device edge
chassis.edgeShadow       #1C1D1E   // 1px rim shadow opposite edge
chassis.grain            rgba(255,255,255,0.10)   // fine metal grain

// Screen bezel
bezel.outer              #0A0A0A
bezel.innerLip           #000000
bezel.innerLipHighlight  #2A2A2A

// Click wheel
wheel.base               #1C1C1E
wheel.baseHighlight      #333335
wheel.baseShadow         #0A0A0B
wheel.rubberTexture      rgba(255,255,255,0.04)
wheel.centerButton        #4A4C4E
wheel.centerButtonShadow  #232527
wheel.glyph              #B8B8BA
wheel.glyphPressed       #6E6E70

// Screen UI (rendered "inside the glass")
screen.statusBarTop      #F5F5F5
screen.statusBarBottom   #D8D8DA
screen.listBg            #FFFFFF
screen.listRowText       #1A1A1A
screen.listChevron       #A8A8AA
screen.highlightTop      #5AC8FA
screen.highlightBottom   #0A84D6
screen.highlightText     #FFFFFF
screen.nowPlayingBgTop   #A8ACC0
screen.nowPlayingBgBottom #4C5068
screen.glassReflection   rgba(255,255,255,0.08)

4. Chassis Texture Recipe

Build as a layered stack, back to front:

- Base gradient — linear 45° from chassis.baseHighlight → chassis.base → chassis.baseShadow, shifted so the highlight sits upper-left.
- Clay-morph body form — broad soft curvature, rounded edges, and a slightly swollen cast-shell feel rather than a crisp planar body.
- Brushed metal grain — overlay a fine, subtle horizontal grain at 8–12% opacity to simulate a cast, textured alloy. Keep the effect faint but visible.
- Specular highlight — large soft white/gray radial gradient, 15–20% opacity, upper-left, to sell the metal under light.
- Edge rim light/shadow — 1px contour stroke around the full silhouette, slightly brighter on the upper-left and darker on the lower-right.
- Ambient occlusion — add thin inset shadowing around seams and junctions so the shell reads as physically layered instead of a flat illustration.

The center button should follow the exact same material recipe as the chassis, but be concave and slightly darker in the recessed center so it reads as a carved part of the same shell.

5. Wheel Texture Recipe (rubber)

The wheel is not metal; it is a large rubberized control surface with a tactile, soft-touch finish.

- Base material: matte rubber, low sheen, slightly compressible, and intentionally more tactile than metallic.
- Microtexture: very fine stipple or low-contrast grain across the surface, subtle enough to remain smooth but distinct enough to feel like rubber.
- Light response: almost no polished reflection; any highlights should be minimal, soft, and diffuse.
- Press response: the pressed section should subtly darken and depress while retaining the same rubber language and texture.

6. Depth & Shadow System

Every interactive element uses one of these standardized elevation profiles. Define as a reusable SkeuoElevation enum so nothing free-hands its own shadow values.

Profile	Use for	Drop shadow (down/right)	Inner highlight (top/left)	Notes
RECESSED	screen bezel lip, wheel center indent	inset, 3dp blur, 60% black	none	reads as carved into the surface
FLUSH	chassis back, wheel base	none	subtle 1px rim only	resting surface plane
RAISED_LOW	list row highlight bar, scrubber thumb	2dp offset, 4dp blur, 25% black	1px, 20% white, top edge	gentle lift off the screen
RAISED_HIGH	Now Playing 3D album art, unpressed wheel glyph zones	6dp offset, 10dp blur, 35% black	1px, 30% white, top-left	strongest lift, used sparingly
PRESSED	any control mid-press (MENU/center/prev/next/play)	inset, 2dp blur, 50% black	none, plus 4% darken overlay on the active area	swap in on WheelEvent press, revert on release with a 60ms crossfade

Rule of thumb: nothing sits at the same elevation as its parent. The wheel sits proud relative to the chassis, the center button is concave relative to the wheel, and the screen sits recessed behind the bezel.

7. Screen "Glass" Treatment

The screen contents render behind a recessed glass panel with a wider, more square layout than the current portrait layout.

- Screen geometry: more square than portrait rectangle, with soft rounded corners and balanced proportions.
- Glass panel: 1–2dp inner rounding consistent with the bezel, plus a subtle high-contrast reflection near the upper third.
- Edge vignette: slight darkening at the corners to imply curvature and depth, without a noticeable fisheye effect.
- Bezel: thick and confident, framing the display cleanly and feeling like a machined inset.

8. Click Wheel Component Spec

Geometry:
- Wheel diameter: much larger than the current concept, dominating the device lower half and visually pulling the chassis together.
- Wheel inset: 4–6dp from the outer edge, but large enough to feel substantial and deliberate.
- Center button diameter: 30–34% of wheel diameter, concave, same texture as chassis, and visually carved into the wheel.
- Zone layout: MENU at top (~110° arc), Prev left (~110° arc), Next right (~110° arc), Play/Pause bottom (~110° arc), Select as the recessed center button.

Materials:
- Wheel face: rubberized matte ring with micro-grain and very soft, low-gloss reflection.
- Wheel edge: subtle recessed lip to read as a thick grippy ring rather than a thin disc.
- Center button: same cast-metal chassis texture as the shell, but concave and recessed into the wheel body.

Press behavior:
- Pressed zones darken and depress in place while the wheel retains the same rubber finish.
- The center button should press inward with the chassis material language and a subtle shift in shadow to signal real mechanical depth.
- Scroll gestures should feel tactile and physical, with the wheel dominating as the focal control.

9. Theme Variants

All themes share the token names in §3; only values swap. Persist active theme via DataStore per AGENTS.md §3.

Theme	chassis.base	wheel.base	Accent notes
Space Gray (default)
#4A4C4E
#1C1C1E	soft metal + matte rubber ring
Silver
#D8D9DB
#EAEAEC	all-metal body with lighter tone
Black
#161616
#0C0C0C	darker cast metal with stronger contrast
U2 Special Edition
#141414
#141414	red accent ring, static engraved mark
Pink / Blue / Green (stretch)	soft pastel shells	matte rubber ring with tinted metal body

10. Typography

Screen UI: rounded geometric sans, matching the reference's weight and clarity. Use a slightly heavier weight for selected text and titles, but keep the letters soft and readable rather than aggressively modern.
Wheel glyphs: same family, medium-weight, all-caps for MENU as appropriate.
Sizes should scale off a single screenBaseUnit derived from the rendered screen width so the UI stays proportional across sizes without hardcoded values.

11. Motion

List scroll: each detent moves exactly one item with a crisp 60–90ms ease-out; no bounce, no glossiness, no exaggerated overshoot.
Fast-spin fling: subtle motion blur or alpha-stack effect only at higher velocity, with minimal intensity.
Screen transitions: horizontal slide, 150ms ease-in-out, with deeper navigation sliding in from the right and back sliding from the left.
Button press: 60ms crossfade between resting and pressed states; no dramatic spring or bounce.

12. Implementation Notes

- Represent every token in §3 as a data class Theme(...) with no raw hex values in Composables.
- Create a dedicated Modifier.skeuoElevation(profile: SkeuoElevation) extension so the visual layering is centralized.
- The chassis, wheel, and center button should be generated from procedural textures and cached per theme+size to keep the grainy metal and rubber finish crisp without redrawing every frame.
- The design must stay faithful to the request: clay-morphed rounded chassis, large wheel, square screen, grainy metal shell, concave center button, and rubberized wheel finish.
