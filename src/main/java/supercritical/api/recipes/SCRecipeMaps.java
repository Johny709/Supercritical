package supercritical.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;
import supercritical.api.recipes.builders.NoEnergyRecipeBuilder;
import supercritical.common.SCConfigHolder;

public class SCRecipeMaps {

	public static final RecipeMap<NoEnergyRecipeBuilder> HEAT_EXCHANGER_RECIPES = SCConfigHolder.misc.enableHX
			? new RecipeMapBuilder<>("heat_exchanger", new NoEnergyRecipeBuilder())
			.itemInputs(1)
			.fluidInputs(2)
			.fluidOutputs(2)
			.progressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
			.sound(GTSoundEvents.COOLING)
			.build()
			: null;

	public static final RecipeMap<SimpleRecipeBuilder> SPENT_FUEL_POOL_RECIPES = new RecipeMapBuilder<>("spent_fuel_pool",
			new SimpleRecipeBuilder())
			.itemInputs(1)
			.itemOutputs(1)
			.fluidInputs(1)
			.fluidOutputs(1)
			.progressBar(GuiTextures.PROGRESS_BAR_BATH, ProgressWidget.MoveType.HORIZONTAL)
			.sound(GTSoundEvents.COOLING)
			.build();

	public static final RecipeMap<SimpleRecipeBuilder> GAS_CENTRIFUGE_RECIPES = new RecipeMapBuilder<>("gas_centrifuge",
			new SimpleRecipeBuilder())
			.fluidInputs(1)
			.fluidOutputs(2)
			.progressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
			.sound(GTSoundEvents.CENTRIFUGE)
			.build();
}
