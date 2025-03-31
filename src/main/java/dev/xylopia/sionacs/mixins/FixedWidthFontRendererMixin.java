package dev.xylopia.sionacs.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;

import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer.QuadEmitter;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dev.xylopia.sionacs.core.apis.CRTModeTracker;
import dev.xylopia.sionacs.utils.Constants;

/**
 * Mixin to add CRT mode to ComputerCraft terminals
 */
@Mixin(value = FixedWidthFontRenderer.class, remap = false)
public class FixedWidthFontRendererMixin {

    // Thread-local storage to track the current terminal being rendered
    private static final ThreadLocal<Terminal> CURRENT_TERMINAL = new ThreadLocal<>();
    
    // Custom CRT mode font texture
    @SuppressWarnings("removal")
    private static final ResourceLocation CRT_FONT = new ResourceLocation(Constants.MOD_ID, "textures/gui/crt_term_font.png");
    
    // Define our own constant for full brightness to avoid RenderTypes dependency
    private static final int FULL_BRIGHTNESS = 15728880; // Same value as RenderTypes.FULL_BRIGHT_LIGHTMAP

    @Shadow(remap = false)
    @Final
    private static float Z_OFFSET;

    @Shadow(remap = false)
    @Final
    static float BACKGROUND_START;

    @Shadow(remap = false)
    @Final
    static float BACKGROUND_END;
    
    @Shadow(remap = false)
    private static void quad(QuadEmitter c, float x1, float y1, float x2, float y2, float z, int colour, float u1, float v1, float u2, float v2, int light) {
        // Shadow method, implementation provided by ComputerCraft
    }
    
    /**
     * Sets the current terminal before rendering begins
     */
    @Inject(method = "drawTerminal", at = @At("HEAD"), remap = false)
    private static void beforeDrawTerminal(
            QuadEmitter emitter, float x, float y,
            Terminal terminal, 
            float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize, 
            CallbackInfo ci) {
        
        // Store the terminal in thread-local storage
        CURRENT_TERMINAL.set(terminal);
    }
    
    /**
     * Injects before drawing strings to bind our custom CRT font texture
     */
    @Inject(
        method = "drawString",
        at = @At(
            value = "HEAD"
        ),
        remap = false
    )
    private static void beforeDrawString(
            QuadEmitter emitter, float x, float y, 
            TextBuffer text, TextBuffer textColour,
            dan200.computercraft.core.terminal.Palette palette, int light,
            CallbackInfo ci) {
        
        Terminal terminal = CURRENT_TERMINAL.get();
        if (terminal != null && CRTModeTracker.isCRTModeEnabled(terminal)) {
            // Bind our custom font texture
            Minecraft.getInstance().getTextureManager().bindForSetup(CRT_FONT);
        } else {
            // Bind the default font texture
            Minecraft.getInstance().getTextureManager().bindForSetup(FixedWidthFontRenderer.FONT);
        }
    }
    
    /**
     * Adds scanline effects after terminal rendering is complete
     */
    @Inject(method = "drawTerminal", at = @At("RETURN"), remap = false)
    private static void afterDrawTerminal(
            QuadEmitter emitter, float x, float y,
            Terminal terminal,
            float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize, 
            CallbackInfo ci) {
        
        // Only render scanlines if CRT mode is enabled for this terminal
        if (terminal != null && CRTModeTracker.isCRTModeEnabled(terminal)) {
            // Save the current transformation matrix
            Matrix4f transformBackup = new Matrix4f(emitter.poseMatrix());
            
            // Move slightly forward to render on top
            emitter.poseMatrix().translate(0, 0, Z_OFFSET);
            
            // Calculate terminal dimensions including margins
            float height = terminal.getHeight() * FixedWidthFontRenderer.FONT_HEIGHT + topMarginSize + bottomMarginSize;
            float width = terminal.getWidth() * FixedWidthFontRenderer.FONT_WIDTH + leftMarginSize + rightMarginSize;
            
            // Define scanline color (semi-transparent black) as ARGB int
            // Alpha=60, R=0, G=0, B=0
            int scanLineColor = ARGB32.color(60, 0, 0, 0);
            
            // Render scanlines with a spacing of 2 pixels
            for (int i = 0; i < height; i += 2) {
                float lineY = y + i - topMarginSize;
                quad(
                    emitter, 
                    x - leftMarginSize, lineY, 
                    x + width - 1.8f, lineY + 0.2f, 
                    0.003f, 
                    scanLineColor, 
                    BACKGROUND_START, BACKGROUND_START, 
                    BACKGROUND_END, BACKGROUND_END, 
                    FULL_BRIGHTNESS // Use our constant instead of importing RenderTypes
                );
            }
            
            // Restore the original transformation matrix
            emitter.poseMatrix().set(transformBackup);
        }
        
        // Clean up thread-local storage
        CURRENT_TERMINAL.remove();
    }
}
