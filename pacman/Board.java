package pacman;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import main.Field;
import neat.Client;

public class Board {
	
	public static final int WIDTH = 28;
	public static final int HEIGHT = 31;
	
	public static double w;
	public static double h;
	/*
	 * 0: 1mpty
	 * 1: wall
	 * 2: cookie
	 * 3: power cookie
	 */
	int[] board = new int[WIDTH*HEIGHT];
	
	private static final int scale = 2;
	
	private static BufferedImage board_img;
	
	public PacmanClient player;
	private Client client;
		
	private static final GhostState[] modes = {GhostState.SCATTER, GhostState.CHASE,
											   GhostState.SCATTER, GhostState.CHASE,
											   GhostState.SCATTER, GhostState.CHASE,
											   GhostState.SCATTER, GhostState.CHASE};
	private static final int fps = 80;
	private static final int[] time_intervals = {7*fps, 20*fps, 7*fps, 20*fps, 5*fps, 20*fps, 5*fps, Integer.MAX_VALUE};
	private static final int frightened_length = 6*80;
	private int frightened_runtime = 0;
	private int interval_index = 0; //the current index of the interval
	public int interval_runtime = time_intervals[0]; //how many frames the current interval has left
	
	public GhostState curr_state = modes[0];
	
	private Ghost blinky;
	private Ghost pinky;
	private Ghost inky;
	private Ghost clyde;
		
	private static ArrayList<String> text = new ArrayList<>();
	
	public int cookies = 0;
	public int frames_since_cookie = 0;
		
	public Board(BufferedImage board_img, PacmanClient player) throws IOException
	{
		if(Board.board_img == null)
			Board.board_img = board_img;
		
		w = ((double) board_img.getWidth())/WIDTH;
		h = ((double) board_img.getHeight())/HEIGHT;
		
		if(text.size() == 0)
		{
			System.out.println("loading map");
			
			InputStream is = this.getClass().getResourceAsStream("pac_data.txt");
			InputStreamReader isr= new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			
			String line = reader.readLine();
			while(line != null)
			{
				text.add(line);
				line = reader.readLine();
			}
			reader.close();
		}
		for(int y = 0; y < text.size(); y++)
		{
			for(int x = 0; x < text.get(y).length(); x++)
			{
				int val = Integer.parseInt(""+text.get(y).charAt(x));
				board[y*WIDTH + x] = val;
				if(val == 2 || val == 3)
					cookies++;
			}
		}
		
		this.player = player;
		player.board = this;
	}
	public void setClient(Client client)
	{
		this.client = client;
	}
	public void setBlinky(Blinky blinky)
	{
		blinky.board = this;
		this.blinky = blinky;
	}
	public void setPinky(Pinky pinky)
	{
		pinky.board = this;
		this.pinky = pinky;
	}
	public void setInky(Inky inky)
	{
		inky.board = this;
		this.inky = inky;
	}
	public void setClyde(Clyde clyde)
	{
		clyde.board = this;
		this.clyde = clyde;
	}
	public void reset(boolean set_client_score)
	{
		for(int y = 0; y < text.size(); y++)
		{
			for(int x = 0; x < text.get(y).length(); x++)
			{
				int val = Integer.parseInt(""+text.get(y).charAt(x));
				board[y*WIDTH + x] = val;
				if(val == 2 || val == 3)
					cookies++;
			}
		}
		player.reset();
		if(set_client_score)
			client.setScore(0);
		interval_index = 0;
		interval_runtime = time_intervals[0];
		frightened_runtime = 0;
		curr_state = modes[0];
		blinky.reset();
		pinky.reset();
		inky.reset();
		clyde.reset();
	}
	public void step(boolean do_collision)
	{
		if(!player.alive)
		{
//			reset(false);
			return;
		}
		if(cookies == 0)
			reset(false);
		if(curr_state == GhostState.FRIGHTENED)
		{
			if(frightened_runtime > 0)
				frightened_runtime --;
			else
				setGhostState(modes[interval_index]);
		}
		else
		{
			if(interval_runtime > 0)
				interval_runtime--;
			else //current state session over
			{
				if(curr_state != GhostState.FRIGHTENED)
				{
					if(interval_index < modes.length-1)
						interval_index++;
					interval_runtime = time_intervals[interval_index];
				}
				setGhostState(modes[interval_index]);
			}
		}
		player.step();
		
		if(do_collision)
		{
			blinky.step(player);
			pinky.step(player);
			inky.step(player);
			clyde.step(player);
			
			if(blinky.eaten)
				blinky.step(player);
			if(pinky.eaten)
				pinky.step(player);
			if(inky.eaten)
				inky.step(player);
			if(clyde.eaten)
				clyde.step(player);
		}
		
		int pos = player.getPos(WIDTH);
		frames_since_cookie++;
		if(board[pos] == 2)
		{
			board[pos] = 0;
			cookies--;
			frames_since_cookie = 0;
			client.addScore(10);
		}
		else if(board[pos] == 3)
		{
			board[pos] = 0;
			cookies--;
			frames_since_cookie = 0;
			client.addScore(50);
			setGhostState(GhostState.FRIGHTENED);
		}
		
		if(do_collision)
		{
			if(player.x == blinky.x && player.y == blinky.y)
				checkIntersection(blinky);
			if(player.x == pinky.x && player.y == pinky.y)
				checkIntersection(pinky);
			if(player.x == inky.x && player.y == inky.y)
				checkIntersection(inky);
			if(player.x == clyde.x && player.y == clyde.y)
				checkIntersection(clyde);
		}
	}
	private void checkIntersection(Ghost ghost)
	{
		if(curr_state == GhostState.FRIGHTENED)
		{
			if(!ghost.eaten)
				client.addScore(200);
			ghost.eaten = true;
		}
		else
			player.alive = false;
	}
	private void setGhostState(GhostState state)
	{
		if(state == GhostState.FRIGHTENED)
			frightened_runtime = frightened_length;
		blinky.vel.mult(-1);
		pinky.vel.mult(-1);
		inky.vel.mult(-1);
		clyde.vel.mult(-1);
		curr_state = state;
	}
	private double[] input_buffer = new double[Field.INPUT_SIZE];
	private BufferedImage input_img = new BufferedImage(Field.INPUT_WIDTH, Field.INPUT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	public double[] getInputData(boolean ghost_data)
	{
		int px = player.x;
		int py = player.y;
		
		for(int i = 0; i < input_buffer.length; i++)
			input_buffer[i] = 0;
		
		if(!checkOpen(px+1, py)) // right wall
			input_buffer[0] = 1;
		if(!checkOpen(px, py-1)) // up wall
			input_buffer[1] = 1;
		if(!checkOpen(px-1, py)) // left wall
			input_buffer[2] = 1;
		if(!checkOpen(px, py+1)) // down wall
			input_buffer[3] = 1;
		
		int right = board[py*WIDTH + px+1];
		int up = board[(py-1)*WIDTH + px];
		int left = board[py*WIDTH + px-1];
		int down = board[(py+1)*WIDTH + px];
		
		if(right == 2 || right == 3)
			input_buffer[4] = 1;
		if(up == 2 || up == 3)
			input_buffer[5] = 1;
		if(left == 2 || left == 3)
			input_buffer[6] = 1;
		if(down == 2 || down == 3)
			input_buffer[7] = 1;
		
		double[] gx = {blinky.x, pinky.x, inky.x, clyde.x};
		double[] gy = {blinky.y, pinky.y, inky.y, clyde.y};
		
		if(ghost_data)
		{
			for(int i = 0; i < 4; i++) //each ghost;
			{
				if(gy[i] == py && gx[i] == px)
					continue;
				if(gy[i] == py)
				{
					double dist = (double) gx[i] - px;
					double strength = 1 / (dist);
					if(gx[i] > px) // right
						if(strength > input_buffer[8])
							input_buffer[8] = strength;
					else if(gy[i] < py) // left
						if(-strength > input_buffer[10])
							input_buffer[10] = -strength;
				}
				else if(gx[i] == px)
				{
					double dist = (double) gy[i] - py;
					double strength = 1 / (dist);
					if(gy[i] < py) // up
						if(-strength > input_buffer[9])
							input_buffer[9] = -strength;
					else if(gy[i] > py) // down
						if(strength > input_buffer[11])
							input_buffer[11] = strength;
				}
			}
		}
		
//		System.out.println(px == gx[0]);
		
		if(curr_state == GhostState.FRIGHTENED)
			input_buffer[12] = 1;
		
//		for(int bx = player.x - Field.INPUT_WIDTH/2, ix = 0; ix < Field.INPUT_WIDTH; bx++, ix++)
//		{
//			if(bx < 0 || bx > WIDTH-1)
//				continue;
//			for(int by = player.y - Field.INPUT_HEIGHT/2, iy = 0; iy < Field.INPUT_HEIGHT; by++, iy++)
//			{
//				if(by < 0 || by > HEIGHT-1)
//					continue;
//				input_buffer[iy*Field.INPUT_WIDTH + ix] = board[by*WIDTH + bx];
//			}
//		}
//		input_buffer[input_buffer.length/2] = 5;
//		int x = player.x;
//		int y = player.y;
//		input_buffer[0] = 0;
		return input_buffer;
	}
	public boolean checkOpen(int x, int y)
	{
		int index = y*WIDTH + x;
		if(index < 0 || index > board.length)
			return false;
		int val = board[index];
		return val != 1 && val != 4;
	}
	public boolean checkGhostOpen(int x, int y, Ghost ghost)
	{
		int index = ghost.y*WIDTH + ghost.x;
		if(index < 0 || index > board.length)
			return false;
		if(board[index] == 4 || ghost.eaten) // in ghost house
		{
			if(y == 12 && (x == 13 || x == 14)) // gate coordinates
				return true; // gates are open
			return board[y*WIDTH + x] != 1;
		}
		return checkOpen(x, y);
	}
	private static final int black = Color.BLACK.getRGB();
	private static final int blue = Color.BLUE.getRGB();
	private static final int white = Color.WHITE.getRGB();
	private static final int yellow = Color.YELLOW.getRGB();
	public BufferedImage getInputImage()
	{
		for(int x = 0; x < Field.INPUT_WIDTH; x++)
		{
			for(int y = 0; y < Field.INPUT_HEIGHT; y++)
			{
				int n = (int) input_buffer[y*Field.INPUT_WIDTH + x];
				if(n == 0)
					input_img.setRGB(x, y, black);
				else if(n == 1)
					input_img.setRGB(x, y, blue);
				else if(n == 2)
					input_img.setRGB(x, y, white);
				else if(n == 5)
					input_img.setRGB(x, y, yellow);
			}
		}
		return input_img;
	}
	public void draw(Graphics2D g2, boolean show_grid, boolean show_targets, boolean show_paths, boolean show_ghosts)
	{
		g2.drawImage(board_img, 0, 0, board_img.getWidth()*scale, board_img.getHeight()*scale, null);
		g2.setColor(Color.YELLOW);
		
		for(double x = 0; x < WIDTH; x++)
		{
			for(double y = 0; y < HEIGHT; y++)
			{
				double x1 = x/WIDTH; //value b/w 0-1
				x1 *= board_img.getWidth()*scale; //distance across the board on screen
				double y1 = y/HEIGHT;
				y1 *= board_img.getHeight()*scale;
				if(board[(int) (y*WIDTH + x)] == 2)
				{
					g2.setColor(Color.YELLOW);
					g2.fillOval((int) (x1 + w - 2), (int) (y1 + h - 2), 4, 4);
				}
				else if(board[(int) (y*WIDTH + x)] == 3)
				{
					g2.setColor(Color.YELLOW);
					g2.fillOval((int) (x1 + w - 4), (int) (y1 + h - 4), 8, 8);
				}
				if(show_grid)
				{
					g2.setColor(Color.DARK_GRAY);
					g2.drawRect((int) x1, (int) y1, (int) (w*2), (int) (h*2));
//					if(board[(int) (y*WIDTH + x)] == 4)
//					{
//						g2.setColor(Color.ORANGE);
//						g2.fillRect((int) x1, (int) y1, (int) (w*2), (int) (h*2));
//					}
				}
			}
		}
		player.draw(g2);
		
		if(show_ghosts)
		{
			blinky.draw(g2, show_targets, show_paths);
			pinky.draw(g2, show_targets, show_paths);
			inky.draw(g2, show_targets, show_paths);
			clyde.draw(g2, show_targets, show_paths);
		}
	}
	public Client getClient()
	{
		return client;
	}

}
