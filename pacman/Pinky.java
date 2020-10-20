package pacman;

import java.awt.Color;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public class Pinky extends Ghost {

	public Pinky(BufferedImage[] anim_states, BufferedImage[] death_anim)
	{
		super("Pinky", Color.PINK, anim_states, death_anim, new Vec2d(12, 14));
	}
	protected int[] getChaseTarget(PacmanClient client)
	{
		switch(client.curr_state)
		{
		case 0: return new int[] {client.x + 4, client.y};
		case 1: return new int[] {client.x - 4, client.y - 4};
		case 2: return new int[] {client.x - 4, client.y};
		case 3: return new int[] {client.x, client.y + 4};
		}
		return null;
	}
	protected int[] getScatterTarget()
	{
		return new int[] {2, -3};
	}

}
