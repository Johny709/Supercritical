package supercritical.api.gui;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.api.gui.resources.TextureArea;
import supercritical.supercritical.Tags;

public class SCGuiTextures {

	// Nuclear GuiTextures
	public static final TextureArea BUTTON_CONTROL_ROD_HELPER = TextureArea
			.fullImage("textures/gui/widget/button_control_rod_helper.png");
	public static final TextureArea DARK_SLIDER_BACKGROUND = TextureArea
			.fullImage("textures/gui/widget/dark_slider_background.png");
	public static final TextureArea DARK_SLIDER_BACKGROUND_VERTICAL = TextureArea
			.fullImage("textures/gui/widget/dark_slider_background_vertical.png");
	public static final TextureArea DARK_SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/dark_slider.png");
	public static final TextureArea PROGRESS_BAR_FISSION_HEAT = TextureArea
			.fullImage("textures/gui/progress_bar/progress_bar_fission_heat.png");
	public static final TextureArea PROGRESS_BAR_FISSION_PRESSURE = TextureArea
			.fullImage("textures/gui/progress_bar/progress_bar_fission_pressure.png");
	public static final TextureArea PROGRESS_BAR_FISSION_ENERGY = TextureArea
			.fullImage("textures/gui/progress_bar/progress_bar_fission_energy.png");

	// Nuclear GuiTextures (MUI2)
	public static final UITexture BUTTON_CONTROL_ROD_HELPER_2 = UITexture
			.fullImage(Tags.MOD_ID, "textures/gui/widget/button_control_rod_helper.png");
	public static final UITexture PROGRESS_BAR_FISSION_HEAT_2 = UITexture
			.fullImage(Tags.MOD_ID, "textures/gui/progress_bar/progress_bar_fission_heat.png");
	public static final UITexture PROGRESS_BAR_FISSION_PRESSURE_2 = UITexture
			.fullImage(Tags.MOD_ID, "textures/gui/progress_bar/progress_bar_fission_pressure.png");
	public static final UITexture PROGRESS_BAR_FISSION_ENERGY_2 = UITexture
			.fullImage(Tags.MOD_ID, "textures/gui/progress_bar/progress_bar_fission_energy.png");
}
