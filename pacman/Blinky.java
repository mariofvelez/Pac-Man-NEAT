package pacman;

import java.awt.Color;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public class Blinky extends Ghost {

	public Blinky(BufferedImage[] anim_states, BufferedImage[] death_anim)
	{
		super("Blinky", Color.RED, anim_states, death_anim, new Vec2d(13, 11));
	}
	protected int[] getChaseTarget(PacmanClient client)
	{
		return new int[] {client.x, client.y};
	}
	protected int[] getScatterTarget()
	{
		return new int[] {25, -3};
	}

}
