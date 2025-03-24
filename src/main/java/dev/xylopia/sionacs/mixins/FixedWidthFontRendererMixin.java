package dev.xylopia.sionacs.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer.QuadEmitter;
import dan200.computercraft.core.terminal.Terminal;
import dev.xylopia.sionacs.SionaCore;
import dev.xylopia.sionacs.computercraft.apis.TermExtensions;
import static dan200.computercraft.client.render.RenderTypes.FULL_BRIGHT_LIGHTMAP;
import net.minecraft.resources.ResourceLocation;
import dev.xylopia.sionacs.utils.Constants;

/**
 * A refined mixin that properly handles both scanlines and font texture switching
 */
@Mixin(value = FixedWidthFontRenderer.class, remap = false)
public class FixedWidthFontRendererMixin {
    
    @Shadow @Final static float BACKGROUND_START;
    @Shadow @Final static float BACKGROUND_END;
    
    @Shadow @Final public static ResourceLocation FONT;
    
    // Our custom CRT font texture
    @SuppressWarnings("removal")
    private static final ResourceLocation CRT_FONT = new ResourceLocation(Constants.MOD_ID, "textures/gui/crt_term_font.png");
    
    /**
     * Before drawing a terminal, we need to decide which texture to use.
     * This redirects any access to the FONT field to return our custom font when CRT is enabled.
     */
    @Inject(
        method = "drawTerminal(Ldan200/computercraft/client/render/text/FixedWidthFontRenderer$QuadEmitter;FFLdan200/computercraft/core/terminal/Terminal;FFFF)V",
        at = @At("HEAD"),
        remap = false
    )
    private static void beforeDrawTerminal(
            QuadEmitter emitter, float x, float y,
            Terminal terminal,
            float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize,
            CallbackInfo ci) {
        
        // Check very explicitly if CRT effect is enabled
        boolean isCrtEnabled = TermExtensions.isCRTEnabled(terminal);
        
        // Log the state for debugging purposes
        if (terminal.getCursorX() == 0 && terminal.getCursorY() == 0) {
            SionaCore.LOGGER.debug("Terminal CRT state: {}", isCrtEnabled);
        }
        
        // If CRT is enabled, override the texture in the render engine
        if (isCrtEnabled) {
            net.minecraft.client.Minecraft.getInstance().getTextureManager().bindForSetup(CRT_FONT);
        } else {
            // Make sure we're using the default font when CRT is disabled
            net.minecraft.client.Minecraft.getInstance().getTextureManager().bindForSetup(FONT);
        }
    }
    
    /**
     * Adds CRT scanlines after the terminal is rendered
     */
    @Inject(
        method = "drawTerminal(Ldan200/computercraft/client/render/text/FixedWidthFontRenderer$QuadEmitter;FFLdan200/computercraft/core/terminal/Terminal;FFFF)V",
        at = @At("RETURN"),
        remap = false
    )
    private static void addCRTScanlines(
            QuadEmitter emitter, float x, float y,
            Terminal terminal,
            float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize,
            CallbackInfo ci) {
        
        // Explicitly check if CRT is enabled and exit early if not
        boolean isCrtEnabled = TermExtensions.isCRTEnabled(terminal);
        if (!isCrtEnabled) {
            return;
        }
        
        // Calculate terminal dimensions
        float height = terminal.getHeight() * FixedWidthFontRenderer.FONT_HEIGHT + topMarginSize + bottomMarginSize;
        float width = terminal.getWidth() * FixedWidthFontRenderer.FONT_WIDTH + leftMarginSize + rightMarginSize;
        
        // Draw semi-transparent black scanlines
        int scanlineColor = 0x40000000; // Semi-transparent black
        for (int i = 0; i < height; i += 2) {
            float lineY = y + i - topMarginSize;
            
            // Call the quad method directly
            FixedWidthFontRenderer.drawQuad(
                emitter, 
                x - leftMarginSize, lineY, 
                0.003f,
                width,
                0.5f,
                scanlineColor,
                FULL_BRIGHT_LIGHTMAP
            );
        }
    }
}
