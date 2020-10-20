package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public abstract class Ghost {
	
	public String name;
	public Color color;
	
	boolean eaten = false;
	
	private BufferedImage[] anim_states;
	private static BufferedImage[] death_anim;
	public static BufferedImage[] frightened_anim;
	
	private static final byte[] right_frames = {0, 1};
	private static final byte[] left_frames = {2, 3};
	private static final byte[] up_frames = {4, 5};
	private static final byte[] down_frames = {6, 7};
	private static final byte[] eaten_ind = {2, 4, 3, 5};
	private int curr_frame = 0;
	
	protected Vec2d pos;
	protected Vec2d vel;
	public int x, y;
	
	private Vec2d restart_pos;
	
	protected Board board;
	
	public Ghost(String name, Color color, BufferedImage[] anim_states, BufferedImage[] death_anim, Vec2d restart_pos)
	{
		this.name = name;
		this.color = color;
		this.anim_states = anim_states;
		if(Ghost.death_anim == null)
			Ghost.death_anim = death_anim;
		vel = new Vec2d(0, 0);
		this.pos = new Vec2d(restart_pos);
		this.restart_pos = restart_pos;
	}
	public void reset()
	{
		curr_frame = 0;
		vel.set(0, 0);
		pos.set(restart_pos);
		x = (int) restart_pos.x;
		y = (int) restart_pos.y;
	}
	private static final BasicStroke stroke1 = new BasicStroke(1);
	private static final BasicStroke stroke3 = new BasicStroke(3);
	public void draw(Graphics2D g2, boolean draw_target, boolean draw_path)
	{
		curr_frame++;
		int ind = curr_frame%2;
		
		int px = (int) (pos.x*Board.w*2) - anim_states[0].getWidth()/3;
		int py = (int) (pos.y*Board.h*2) - anim_states[0].getHeight()/3;
		int w = (int) (anim_states[0].getWidth()*1.5);
		int h = (int) (anim_states[0].getHeight()*1.5);
		
		if(board.curr_state == GhostState.FRIGHTENED)
		{
			if(eaten)
			{
				if(vel.x > 0)
					g2.drawImage(frightened_anim[eaten_ind[0]], px, py, w, h, null);
				else if(vel.y < 0)
					g2.drawImage(frightened_anim[eaten_ind[1]], px, py, w, h, null);
				else if(vel.x < 0)
					g2.drawImage(frightened_anim[eaten_ind[2]], px, py, w, h, null);
				else
					g2.drawImage(frightened_anim[eaten_ind[3]], px, py, w, h, null);
			}
			else
				g2.drawImage(frightened_anim[ind], px, py, w, h, null);
		}
		else if(vel.x > 0)
			g2.drawImage(anim_states[right_frames[ind]], px, py, w, h, null);
		else if(vel.y < 0)
			g2.drawImage(anim_states[up_frames[ind]], px, py, w, h, null);
		else if(vel.x < 0)
			g2.drawImage(anim_states[left_frames[ind]], px, py, w, h, null);
		else
			g2.drawImage(anim_states[down_frames[ind]], px, py, w, h, null);
		g2.setColor(color);
		if(draw_target)
		{
			g2.fillOval((int) (target[0]*Board.w*2), (int) (target[1]*Board.h*2), (int) (Board.w*2), (int) (Board.h*2));
		}
		g2.setStroke(stroke3);
		if(draw_path)
		{
			Vec2d temp = new Vec2d(vel);
			Vec2d temp2 = new Vec2d(pos);
			int tempx = this.x;
			int tempy = this.y;
			
//			iter_strength = 1f;
			
			Vec2d[] path = new Vec2d[200];
			
			for(int i = 0; i < path.length; i++)
			{
				path[i] = new Vec2d(pos);
				path[i].mult((float) (Board.w*2), (float) (Board.h*2));
				path[i].add((float) (Board.w), (float) (Board.h));
				step(board.player);
			}
			
			g2.drawPolyline(Vec2d.xPoints(path), Vec2d.yPoints(path), path.length);
			vel = temp;
			pos = temp2;
			x = tempx;
			y = tempy;
//			iter_strength = 0.25f;
		}
		g2.setStroke(stroke1);
	}
	private float iter_strength = 0.25f;
	public void step(PacmanClient client)
	{
//		int check_x = (int) (pos.x+0.5f+vel.x);
//		int check_y = (int) (pos.y+0.5f+vel.y);
		
		this.x = (int) (pos.x+0.5f);
		this.y = (int) (pos.y+0.5f);
		
		if(pos.x%1 == 0 && pos.y%1 == 0) // no offset on tile
		{
			boolean[] spaces = new boolean[4];
			spaces[0] = board.checkGhostOpen(this.x, this.y-1, this);
			spaces[1] = board.checkGhostOpen(this.x-1, this.y, this);
			spaces[2] = board.checkGhostOpen(this.x, this.y+1, this);
			spaces[3] = board.checkGhostOpen(this.x+1, this.y, this);
			
			vel.set(setDirection(client, spaces)); // path find to the next target position
		}
//		if(board.checkGhostOpen(check_x, check_y, this))
			pos.add(vel.x*iter_strength, vel.y*iter_strength);
//		else
//		{
//			
		if(pos.x < 0.1f)
			pos.x = 27;
		else if(pos.x > 26)
			pos.x = 0;
//			else
//			{
//				vel.set(0, 0);
//				pos.set((int) (pos.x+0.5f), (int) (pos.y+0.5f));
//			}
//		}
	}
	int[] target = null;
	/**
	 * Sets the direction of the ghost based on its AI
	 * @param client - the target client in the game
	 */
	private Vec2d setDirection(PacmanClient client, boolean[] spaces)
	{
		int[] target = null;
		if(board.board[y*Board.WIDTH + x] == 4) // in ghost house and door
		{
			this.target = new int[] {13, 11};
			eaten = false;
			return targetDirection(13, 11, spaces);
		}
		if(eaten)
		{
			this.target = new int[] {14, 14};
			return targetDirection(14, 14, spaces);
		}
		switch(board.curr_state)
		{
		case CHASE:
			target = getChaseTarget(client);
			break;
		case EATEN:
			target = getEatenTarget();
		case FRIGHTENED:
			return setFrightenedDirection(spaces);
		case SCATTER:
			target = getScatterTarget();
			break;
		}
		this.target = target;
		if(target != null)
			return targetDirection(target[0], target[1], spaces);
		return vel;
	}
	/**
	 * directions stored in an array for convenience
	 */
	private static final Vec2d[] DIRECTIONS = {new Vec2d(0, -0.5f),
											   new Vec2d(-0.5f, 0),
											   new Vec2d(0, 0.5f),
											   new Vec2d(0.5f, 0)};
	protected Vec2d targetDirection(int x, int y, boolean[] spaces)
	{
		//checking to not go backwards
		if(vel.y > 0)
			spaces[0] = false;
		else if(vel.y < 0)
			spaces[2] = false;
		else if(vel.x > 0)
			spaces[1] = false;
		else if(vel.x < 0)
			spaces[3] = false;
		
		byte closest_index = 0;
		double closest_length = Double.POSITIVE_INFINITY;
		
		for(byte i = 0; i < spaces.length; i++)
		{
			if(!spaces[i]) //not a valid direction
				continue;
			double length = Math.hypot((this.x+(DIRECTIONS[i].x*2)-x), (this.y+(DIRECTIONS[i].y*2)-y));
			if(length < closest_length)
			{
				closest_length = length;
				closest_index = i;
			}
		}
		return DIRECTIONS[closest_index];
	}
	public Vec2d setFrightenedDirection(boolean[] spaces)
	{
		//checking to not go backwards
			if(vel.y > 0)
				spaces[0] = false;
			else if(vel.y < 0)
				spaces[2] = false;
			else if(vel.x > 0)
				spaces[1] = false;
			else if(vel.x < 0)
				spaces[3] = false;
			
			//find how many open spaces there are
			byte open = 0;
			for(byte i = 0; i < spaces.length; i++)
				if(spaces[i])
					open++;
			
			//choose an open space at random
			byte rand = (byte) (Math.random()*open); //index of direction w/o invalid directions
			for(byte i = 0; i < spaces.length; i++)
			{
				if(!spaces[i]) //invalid direction
					rand++;
				else
					if(rand == i)
						return DIRECTIONS[i];
			}
			return vel;
	}
	protected abstract int[] getChaseTarget(PacmanClient client);
	protected abstract int[] getScatterTarget();
	private int[] getEatenTarget()
	{
		return new int[] {13, 14};
	}

}
