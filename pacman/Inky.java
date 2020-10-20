package pacman;

import java.awt.Color;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public class Inky extends Ghost {

	private Blinky blinky;
	public Inky(Blinky blinky, BufferedImage[] anim_states, BufferedImage[] death_anim)
	{
		super("Inky", Color.CYAN, anim_states, death_anim, new Vec2d(13, 14));
		this.blinky = blinky;
	}
	protected int[] getChaseTarget(PacmanClient client)
	{
		//intermediate direction in front of pac-man
		int int_x = 0;
		int int_y = 0;
		switch(client.curr_state)
		{
		case 0:
			int_x = client.x + 4;
			int_y = client.y;
			break;
		case 1:
			int_x = client.x - 4;
			int_y = client.y - 4;
			break;
		case 2:
			int_x = client.x - 4;
			int_y = client.y;
		case 3:
			int_x = client.x;
			int_y = client.y + 4;
		}
		
		//direction from intermediate target to blinky
		int dir_x = blinky.x - int_x;
		int dir_y = blinky.y - int_y;
		
		//opposite direction
		return new int[] {int_x - dir_x, int_y - dir_y};
	}
	protected int[] getScatterTarget()
	{
		return new int[] {27, 32};
	}

}
