package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.UiTextScrollTuning;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActiveTextCollector.class)
public abstract class ActiveTextCollectorMixin {
	@Inject(method = "defaultScrollingHelper", at = @At("HEAD"))
	private void wateroptimisation$beginModLabelScroll(
			Component message,
			int centerX,
			int left,
			int right,
			int top,
			int bottom,
			int lineWidth,
			int lineHeight,
			ActiveTextCollector.Parameters parameters,
			CallbackInfo callback
	) {
		UiTextScrollTuning.begin(message);
	}

	@ModifyConstant(method = "defaultScrollingHelper", constant = @Constant(doubleValue = 0.5D))
	private static double wateroptimisation$tuneModLabelScrollPeriod(double vanillaPeriod) {
		return UiTextScrollTuning.isModLabelActive()
				? UiTextScrollTuning.MOD_LABEL_PERIOD_PER_PIXEL
				: vanillaPeriod;
	}

	@Inject(method = "defaultScrollingHelper", at = @At("RETURN"))
	private void wateroptimisation$endModLabelScroll(
			Component message,
			int centerX,
			int left,
			int right,
			int top,
			int bottom,
			int lineWidth,
			int lineHeight,
			ActiveTextCollector.Parameters parameters,
			CallbackInfo callback
	) {
		UiTextScrollTuning.end(message);
	}
}
