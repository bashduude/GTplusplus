package gtPlusPlus.xmod.gregtech.loaders;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.GT_Values;
import gregtech.api.util.GT_ModHandler;

import gtPlusPlus.api.interfaces.RunnableWithInfo;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.material.MaterialGenerator;
import gtPlusPlus.core.material.MaterialStack;
import gtPlusPlus.core.material.state.MaterialState;
import gtPlusPlus.core.recipe.common.CI;
import gtPlusPlus.core.util.minecraft.ItemUtils;
import gtPlusPlus.core.util.minecraft.RecipeUtils;
import net.minecraftforge.fluids.FluidStack;

public class RecipeGen_DustGeneration extends RecipeGen_Base {

	public final static Set<RunnableWithInfo<Material>> mRecipeGenMap = new HashSet<RunnableWithInfo<Material>>();
	static {
		MaterialGenerator.mRecipeMapsToGenerate.put(mRecipeGenMap);
	}

	public RecipeGen_DustGeneration(final Material M){
		this(M, false);
	}
	
	public RecipeGen_DustGeneration(final Material M, final boolean O){
		this.toGenerate = M;		
		this.disableOptional = O;
		mRecipeGenMap.add(this);
	}

	@Override
	public void run() {
		generateRecipes(this.toGenerate, this.disableOptional);
	}

	private void generateRecipes(final Material material, final boolean disableOptional){
		final int tVoltageMultiplier = material.getMeltingPointK() >= 2800 ? 60 : 15;

		Logger.WARNING("Generating Shaped Crafting recipes for "+material.getLocalizedName()); //TODO
		//Ring Recipe

		if (RecipeUtils.addShapedGregtechRecipe(
				"craftingToolWrench", null, null,
				null, material.getRod(1), null,
				null, null, null,
				material.getRing(1))){
			Logger.WARNING("Ring Recipe: "+material.getLocalizedName()+" - Success");
		}
		else {
			Logger.WARNING("Ring Recipe: "+material.getLocalizedName()+" - Failed");
		}


		final ItemStack normalDust = material.getDust(1);
		final ItemStack smallDust = material.getSmallDust(1);
		final ItemStack tinyDust = material.getTinyDust(1);

		final ItemStack[] inputStacks = material.getMaterialComposites();
		final ItemStack outputStacks = material.getDust(material.smallestStackSizeWhenProcessing);

		if (RecipeUtils.recipeBuilder(
				tinyDust,	tinyDust, tinyDust,
				tinyDust, tinyDust, tinyDust,
				tinyDust, tinyDust, tinyDust,
				normalDust)){
			Logger.WARNING("9 Tiny dust to 1 Dust Recipe: "+material.getLocalizedName()+" - Success");
		}
		else {
			Logger.WARNING("9 Tiny dust to 1 Dust Recipe: "+material.getLocalizedName()+" - Failed");
		}

		if (RecipeUtils.recipeBuilder(
				normalDust, null, null,
				null, null, null,
				null, null, null,
				material.getTinyDust(9))){
			Logger.WARNING("9 Tiny dust from 1 Recipe: "+material.getLocalizedName()+" - Success");
		}
		else {
			Logger.WARNING("9 Tiny dust from 1 Recipe: "+material.getLocalizedName()+" - Failed");
		}


		if (RecipeUtils.recipeBuilder(
				smallDust, smallDust, null,
				smallDust, smallDust, null,
				null, null, null,
				normalDust)){
			Logger.WARNING("4 Small dust to 1 Dust Recipe: "+material.getLocalizedName()+" - Success");
		}
		else {
			Logger.WARNING("4 Small dust to 1 Dust Recipe: "+material.getLocalizedName()+" - Failed");
		}


		if (RecipeUtils.recipeBuilder(
				null, normalDust, null,
				null, null, null,
				null, null, null,
				material.getSmallDust(4))){
			Logger.WARNING("4 Small dust from 1 Dust Recipe: "+material.getLocalizedName()+" - Success");
		}
		else {
			Logger.WARNING("4 Small dust from 1 Dust Recipe: "+material.getLocalizedName()+" - Failed");
		}

		//Macerate blocks back to dusts.
		final ItemStack materialBlock = material.getBlock(1);
		final ItemStack materialFrameBox = material.getFrameBox(1);

		if (materialBlock != null) {
			GT_ModHandler.addPulverisationRecipe(materialBlock, material.getDust(9));
		}

		if (materialFrameBox != null) {
			GT_ModHandler.addPulverisationRecipe(materialFrameBox, material.getDust(2));
		}

		//Is this a composite?
		if ((inputStacks != null) && !disableOptional){
			//Is this a composite?
			Logger.WARNING("mixer length: "+inputStacks.length);
			if ((inputStacks.length != 0) && (inputStacks.length <= 4)){
				//Log Input items
				Logger.WARNING(ItemUtils.getArrayStackNames(inputStacks));
				final long[] inputStackSize = material.vSmallestRatio;
				Logger.WARNING("mixer is stacksizeVar null? "+(inputStackSize != null));
				//Is smallest ratio invalid?
				if (inputStackSize != null){
					//set stack sizes on an input ItemStack[]
					for (short x=0;x<inputStacks.length;x++){
						if ((inputStacks[x] != null) && (inputStackSize[x] != 0)){
							inputStacks[x].stackSize = (int) inputStackSize[x];
						}
					}
					//Relog input values, with stack sizes
					Logger.WARNING(ItemUtils.getArrayStackNames(inputStacks));

					//Get us four ItemStacks to input into the mixer
					ItemStack input1, input2, input3, input4;
					input1 = (inputStacks.length >= 1) ? (input1 = (inputStacks[0] == null) ? null : inputStacks[0]) : null;
					input2 = (inputStacks.length >= 2) ? (input2 = (inputStacks[1] == null) ? null : inputStacks[1]) : null;
					input3 = (inputStacks.length >= 3) ? (input3 = (inputStacks[2] == null) ? null : inputStacks[2]) : null;
					input4 = (inputStacks.length >= 4) ? (input4 = (inputStacks[3] == null) ? null : inputStacks[3]) : null;

					//Add mixer Recipe

					FluidStack oxygen = GT_Values.NF;
					if (material.getComposites() != null){
						for (final MaterialStack x : material.getComposites()){
							if (!material.getComposites().isEmpty()){
								if (x != null){
									if (x.getStackMaterial() != null){
										if (x.getStackMaterial().getDust(1) == null){
											if (x.getStackMaterial().getState() == MaterialState.GAS){
												oxygen = x.getStackMaterial().getFluid(1000);
											}
										}
									}
								}
							}
						}

					}

					//Add mixer Recipe
					if (GT_Values.RA.addMixerRecipe(
							input1, input2,
							input3, input4,
							oxygen,
							null,
							outputStacks,
							(int) Math.max(material.getMass() * 2L * 1, 1),
							2 * material.vVoltageMultiplier)) //Was 6, but let's try 2. This makes Potin LV, for example.
					{
						Logger.WARNING("Dust Mixer Recipe: "+material.getLocalizedName()+" - Success");
					}
					else {
						Logger.WARNING("Dust Mixer Recipe: "+material.getLocalizedName()+" - Failed");
					}

					//Add Shapeless recipe for low tier alloys.
					if (tVoltageMultiplier <= 30){
						if (RecipeUtils.addShapedGregtechRecipe(inputStacks, outputStacks)){
							Logger.WARNING("Dust Shapeless Recipe: "+material.getLocalizedName()+" - Success");
						}
						else {
							Logger.WARNING("Dust Shapeless Recipe: "+material.getLocalizedName()+" - Failed");
						}
					}
				}
			}
		}






	}

	public static boolean addMixerRecipe_Standalone(final Material material){
		final ItemStack[] inputStacks = material.getMaterialComposites();
		final ItemStack outputStacks = material.getDust(material.smallestStackSizeWhenProcessing);
		//Is this a composite?
		if ((inputStacks != null)){
			//Is this a composite?
			Logger.WARNING("mixer length: "+inputStacks.length);
			if ((inputStacks.length >= 1) && (inputStacks.length <= 4)){
				//Log Input items
				Logger.WARNING(ItemUtils.getArrayStackNames(inputStacks));
				final long[] inputStackSize = material.vSmallestRatio;
				Logger.WARNING("mixer is stacksizeVar not null? "+(inputStackSize != null));
				//Is smallest ratio invalid?
				if (inputStackSize != null){
					//set stack sizes on an input ItemStack[]
					for (short x=0;x<inputStacks.length;x++){
						if ((inputStacks[x] != null) && (inputStackSize[x] != 0)){
							inputStacks[x].stackSize = (int) inputStackSize[x];
						}
					}
					//Relog input values, with stack sizes
					Logger.WARNING(ItemUtils.getArrayStackNames(inputStacks));

					//Get us four ItemStacks to input into the mixer
					ItemStack input1, input2, input3, input4;
					input1 = (inputStacks.length >= 1) ? (input1 = (inputStacks[0] == null) ? null : inputStacks[0]) : null;
					input2 = (inputStacks.length >= 2) ? (input2 = (inputStacks[1] == null) ? null : inputStacks[1]) : null;
					input3 = (inputStacks.length >= 3) ? (input3 = (inputStacks[2] == null) ? null : inputStacks[2]) : null;
					input4 = (inputStacks.length >= 4) ? (input4 = (inputStacks[3] == null) ? null : inputStacks[3]) : null;

					if (inputStacks.length == 1) {
						input2 = CI.getNumberedCircuit(20);
					}
					else if (inputStacks.length == 2) {
						input3 = CI.getNumberedCircuit(20);

					}
					else if (inputStacks.length == 3) {
						input4 = CI.getNumberedCircuit(20);

					}


					//Add mixer Recipe

					FluidStack oxygen = GT_Values.NF;
					if (material.getComposites() != null){
						int compSlot = 0;
						for (final MaterialStack x : material.getComposites()){
							if (!material.getComposites().isEmpty()){
								if (x != null){
									if (x.getStackMaterial() != null){
										if (x.getStackMaterial().getDust(1) == null){
											MaterialState f = x.getStackMaterial().getState();
											if (f == MaterialState.GAS || f == MaterialState.LIQUID || f == MaterialState.PURE_LIQUID){
												oxygen = x.getStackMaterial().getFluid((int) (material.vSmallestRatio[compSlot] * 1000));
											}
										}
									}
								}
							}
							compSlot++;
						}

					}

					//Add mixer Recipe
					try {
						if (GT_Values.RA.addMixerRecipe(
								input1, input2,
								input3, input4,
								oxygen,
								null,
								outputStacks,
								(int) Math.max(material.getMass() * 2L * 1, 1),
								2 * material.vVoltageMultiplier)) //Was 6, but let's try 2. This makes Potin LV, for example.
						{
							Logger.WARNING("Dust Mixer Recipe: "+material.getLocalizedName()+" - Success");
							return true;
						}
						else {
							Logger.WARNING("Dust Mixer Recipe: "+material.getLocalizedName()+" - Failed");
							return false;
						}
					}
					catch (Throwable t) {
						t.printStackTrace();
					}
				}
				else {
					Logger.WARNING("inputStackSize == NUll - "+material.getLocalizedName());
				}
			}
			else {
				Logger.WARNING("InputStacks is out range 1-4 - "+material.getLocalizedName());
			}
		}
		else {
			Logger.WARNING("InputStacks == NUll - "+material.getLocalizedName());
		}
		return false;
	}
	
}

