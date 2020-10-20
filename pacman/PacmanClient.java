package pacman;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import utility.math.Vec2d;

public class PacmanClient {
		
	private BufferedImage[] anim_states;
	private BufferedImage[] death_anim;
	
	private static final byte[] right_frames = {-1, 0, 1, 0};
	private static final byte[] up_frames = {-1, 2, 3, 2};
	private static final byte[] left_frames = {-1, 4, 5, 4};
	private static final byte[] down_frames = {-1, 6, 7, 6};
	private byte curr_frame = 0;
	
	public int curr_state = 0;
	boolean alive = true;
	
	Vec2d pos;
	private Vec2d vel;
	public int x, y;
	
	public Board board;
	
	public PacmanClient(BufferedImage[] anim_states, BufferedImage[] death_anim)
	{
		this.anim_states = anim_states;
		this.death_anim = death_anim;
		pos = new Vec2d(14, 23);
		x = (int) pos.x;
		y = (int) pos.y;
		vel = new Vec2d(0, 0);
	}
	public void reset()
	{
		curr_frame = 0;
		curr_state = 0;
		alive = true;
		pos.set(14, 23);
		x = (int) pos.x;
		y = (int) pos.y;
		vel.set(0, 0);
	}
	public void draw(Graphics2D g2)
	{
		if(curr_state > -1)
		{
			if(curr_frame < right_frames.length-1)
				curr_frame++;
			else
				curr_frame = 0;
		}
		
		int px = (int) (pos.x*Board.w*2) - anim_states[0].getWidth()/3;
		int py = (int) (pos.y*Board.h*2) - anim_states[0].getHeight()/3;
		int w = (int) (anim_states[0].getWidth()*1.5);
		int h = (int) (anim_states[0].getHeight()*1.5);
		if(curr_frame == 0)
			g2.drawImage(death_anim[0], px, py, w, h, null);
		else if(vel.x != 0 || vel.y != 0)
		{
			if(curr_state == 0)
				g2.drawImage(anim_states[right_frames[curr_frame]], px, py, w, h, null);
			else if(curr_state == 1)
				g2.drawImage(anim_states[up_frames[curr_frame]], px, py, w, h, null);
			else if(curr_state == 2)
				g2.drawImage(anim_states[left_frames[curr_frame]], px, py, w, h, null);
			else if(curr_state == 3)
				g2.drawImage(anim_states[down_frames[curr_frame]], px, py, w, h, null);
		}
		else
			g2.drawImage(death_anim[0], px, py, w, h, null);
//		for(int i = 0; i < anim_states.length; i++)
//			g2.drawImage(anim_states[i], (int) pos.x - WIDTH/2 + i*30, (int) pos.y - HEIGHT/2, null);
//		for(int i = 0; i < death_anim.length; i++)
//			g2.drawImage(death_anim[i], (int) pos.x - WIDTH/2 + i*30, (int) pos.y - HEIGHT/2 + 20, null);
	}
	public void step()
	{
		if(board.checkOpen((int) (pos.x+0.5f+vel.x), (int) (pos.y+0.5f+vel.y)))
			pos.add(vel.x*0.25f, vel.y*0.25f);
		else
		{
			if(pos.x < 0)
				pos.x = 27;
			else if(pos.x > 26.874f)
				pos.x = 0;
			else
			{
				vel.set(0, 0);
				pos.set((int) (pos.x+0.5f), (int) (pos.y+0.5f));
			}
		}
		x = (int) (pos.x+0.5f);
		y = (int) (pos.y+0.5f);
	}
	public void move(char input)
	{
		switch(input)
		{
		case 'w':
			if(pos.x%1 == 0 && pos.y%1 == 0 && board.checkOpen(x, y-1))
			{
				curr_state = 1;
				vel.set(0, -0.5f);
			}
			break;
		case 'a':
			if(pos.x%1 == 0 && pos.y%1 == 0 && board.checkOpen(x-1, y))
			{
				curr_state = 2;
				vel.set(-0.5f, 0);
			}
			break;
		case 's':
			if(pos.x%1 == 0 && pos.y%1 == 0 && board.checkOpen(x, y+1))
			{
				curr_state = 3;
				vel.set(0, 0.5f);
			}
			break;
		case 'd':
			if(pos.x%1 == 0 && pos.y%1 == 0 && board.checkOpen(x+1, y))
			{
				curr_state = 0;
				vel.set(0.5f, 0);
			}
			break;
		}
	}
	public int getPos(int width)
	{
		return y*width + x;
	}
	public boolean getAlive()
	{
		return alive;
	}

}
