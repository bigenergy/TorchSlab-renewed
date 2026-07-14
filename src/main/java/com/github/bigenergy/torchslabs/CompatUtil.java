package com.github.bigenergy.torchslabs;

import com.github.bigenergy.torchslabs.config.TorchSlabConfig;

import net.minecraftforge.fml.ModList;

/**
 * Soft-compat checks. Some mods (Amendments, Supplementaries) add their own lantern
 * placement / physics; when one of them is installed Torch Slabs should not intercept
 * lantern right-clicks, or that behaviour breaks. Each is individually toggleable in the config.
 */
public final class CompatUtil
{
	private CompatUtil() {}

	/** True when Torch Slabs should stop intercepting lantern placement and defer to another mod. */
	public static boolean yieldLanterns()
	{
		return (ModList.get().isLoaded("amendments") && TorchSlabConfig.amendmentsLanternCompat.get())
				|| (ModList.get().isLoaded("supplementaries") && TorchSlabConfig.supplementariesLanternCompat.get());
	}
}
