package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterOptimisationClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Gui.class, remap = false)
public abstract class ForgeGuiMixin {
	@Redirect(
			method = "extractRenderState(Lnet/minecraft/client/DeltaTracker;ZZ)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
					remap = false
			),
			remap = false
	)
	private void wateroptimisation$renderDiagnosticsAfterHud(
			Hud hud,
			GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker
	) {
		hud.extractRenderState(graphics, deltaTracker);
		WaterOptimisationClient.renderDiagnostics(graphics, deltaTracker);
	}
}
