//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Vanilla Generator Listener. This class contains listener that process Generator options.
 * This listener uses only vanilla minecraft generator and replaces end block after a tick.
 */
public class VanillaGeneratorListener extends GeneratorListener
{
	/**
	 * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
	 *
	 * @param addon of type StoneGeneratorAddon
	 */
	public VanillaGeneratorListener(StoneGeneratorAddon addon)
	{
		super(addon);
	}


	/**
	 * Handles magic generation when a block is formed
	 * @param event - event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockFormEvent(BlockFormEvent event)
	{
		Block eventSourceBlock = event.getBlock();

		if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld()))
		{
			// If not operating in world, then return as fast as possible
			return;
		}

		if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation()))
		{
			// If island members are not online then do not continue
			return;
		}

		if (!eventSourceBlock.isLiquid())
		{
			// We are interested only in blocks that were created from lava or water
			return;
		}

		// if flag is toggled off, return
		if (this.addon.getIslands().getIslandAt(eventSourceBlock.getLocation()).
			map(island -> !island.isAllowed(this.addon.getMagicFlag())).
			orElse(!this.addon.getMagicFlag().isSetForWorld(eventSourceBlock.getWorld())))
		{
			return;
		}

		if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation()))
		{
			// If island members are not online then do not continue
			return;
		}

		if (!this.isInRangeToGenerate(eventSourceBlock))
		{
			// Check if any island member is at generator range.
			return;
		}

		// Run 1-tick later to catch the type made and only take action on cobblestone, stone and basalt
		Bukkit.getScheduler().runTask(this.addon.getPlugin(), () -> {
			// Lava is generating cobblestone into eventToBlock place

			final boolean playEffect;

			switch (eventSourceBlock.getType())
			{
				case COBBLESTONE:
					playEffect = this.isCobblestoneReplacementGenerated(eventSourceBlock);
					break;
				case STONE:
					playEffect = this.isStoneReplacementGenerated(eventSourceBlock);
					break;
				case BASALT:
					playEffect = this.isBasaltReplacementGenerated(eventSourceBlock);
					break;
				default:
					playEffect = false;
			}

			if (playEffect)
			{
				// sound when lava transforms to cobble
				this.playEffects(eventSourceBlock);
			}
		});
	}
}
