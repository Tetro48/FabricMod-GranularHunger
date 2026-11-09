package tetro48.system.configs;

import tetro48.system.HungerSystemBehaviorMode;

// The funny thing about MoulConfig, is that it's built for Hypixel SkyBlock mods, which is pretty much client-side.
public class HungerSystemConfig {
	public HungerSystemBehaviorMode behaviorMode = HungerSystemBehaviorMode.BTW_MODE;
	public float hungerRatioRegen_BTWMode = 0.4f;
	public float hungerRatioRegen_VanillaMode = 0.85f;
	public float exhaustionRate = 0f;
}
