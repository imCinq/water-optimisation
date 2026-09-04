package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.UiTextScrollTuning;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
	@Inject(
			method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIII)V",
			at = @At("HEAD")
	)
	private static void wateroptimisation$beginModLabelScroll(
			GuiGraphics graphics,
			Font font,
			Component message,
			int minX,
			int minY,
			int maxX,
			int maxY,
			int color,
			CallbackInfo callback
	) {
		UiTextScrollTuning.begin(message);
	}

	@ModifyConstant(
			method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIII)V",
			constant = @Constant(doubleValue = 0.5D)
	)
	private static double wateroptimisation$tuneModLabelScrollPeriod(double vanillaPeriod) {
		return UiTextScrollTuning.isModLabelActive()
				? UiTextScrollTuning.MOD_LABEL_PERIOD_PER_PIXEL
				: vanillaPeriod;
	}

	@Inject(
			method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIII)V",
			at = @At("RETURN")
	)
	private static void wateroptimisation$endModLabelScroll(
			GuiGraphics graphics,
			Font font,
			Component message,
			int minX,
			int minY,
			int maxX,
			int maxY,
			int color,
			CallbackInfo callback
	) {
		UiTextScrollTuning.end(message);
	}
}
