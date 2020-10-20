package pacman;

import java.awt.Color;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public class Clyde extends Ghost {

	public Clyde(BufferedImage[] anim_states, BufferedImage[] death_anim)
	{
		super("Clyde", Color.ORANGE, anim_states, death_anim, new Vec2d(14, 14));
	}
	protected int[] getChaseTarget(PacmanClient client)
	{
		if(Math.hypot(client.x-this.x, client.y-this.y) > 8)
			return new int[] {client.x, client.y};
		return getScatterTarget();
	}
	protected int[] getScatterTarget()
	{
		return new int[] {0, 32};
	}

}
