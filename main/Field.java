package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import data_structures.RandomHashSet;
import genome.Genome;
import neat.Neat;
import neat.Species;
import pacman.Blinky;
import pacman.Board;
import pacman.Clyde;
import pacman.Ghost;
import pacman.Inky;
import pacman.PacmanClient;
import pacman.Pinky;
import utility.Utils;

/**
 * 
 * @author Mario Velez
 *
 */
public class Field extends Canvas
		implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener,
				   Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -796167392411348854L;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private Graphics bufferGraphics; // graphics for backbuffer
	private BufferStrategy bufferStrategy;
	
	public static int mousex = 0; // mouse values
	public static int mousey = 0;

	public static ArrayList<Integer> keysDown; // holds all the keys being held down
	boolean leftClick;

	private Thread thread;

	private boolean running;
	private int runTime;
	private float seconds;
	public int refreshTime;
	
	public static int[] anchor = new  int[2];
	public static boolean dragging;
	
	Neat neat;
	
	private BufferedImage texture_map;
	
	private ArrayList<Board> boards;
	private ArrayList<PacmanClient> clients;
	
	public static final int INPUT_WIDTH = 7;
	public static final int INPUT_HEIGHT = 7;
	public static final int INPUT_SIZE = 13;
	
	public Field(Dimension size) throws Exception {
		this.setPreferredSize(size);
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		
		this.setBackground(Color.BLACK);

		this.thread = new Thread(this);
		running = true;
		runTime = 0;
		seconds = 0;
		refreshTime = 32;//(int) (1f/50 * 1000);

		keysDown = new ArrayList<Integer>();
		
		neat = new Neat(INPUT_SIZE, 4, 100);
		System.out.println("NEAT created");
		
		boards = new ArrayList<>();
		clients = new ArrayList<>();
		
		URL url = Field.class.getResource("pac_sprites.png");
		texture_map = ImageIO.read(url);
		
		URL url2 = Field.class.getResource("pacmaze.png");
		BufferedImage maze_img = ImageIO.read(url2);
		System.out.println("Images loaded");
										
		BufferedImage[] pac_anim = new BufferedImage[8];
		for(int i = 0; i < pac_anim.length; i++)
			pac_anim[i] = extract(texture_map, i*16 + 337, 0, 15, 15);
		
		BufferedImage[] pac_death = new BufferedImage[11];
		for(int i = 0; i < pac_death.length; i++)
			pac_death[i] = extract(texture_map, i*16 + 337, 16, 15, 15);
		
		BufferedImage[] blinky_anim = new BufferedImage[8];
		for(int i = 0; i < blinky_anim.length; i++)
			blinky_anim[i] = extract(texture_map, i*16 + 336, 80, 15, 15);
		
		BufferedImage[] pinky_anim = new BufferedImage[8];
		for(int i = 0; i < pinky_anim.length; i++)
			pinky_anim[i] = extract(texture_map, i*16 + 336, 96, 15, 15);
		
		BufferedImage[] inky_anim = new BufferedImage[8];
		for(int i = 0; i < inky_anim.length; i++)
			inky_anim[i] = extract(texture_map, i*16 + 336, 112, 15, 15);
		
		BufferedImage[] clyde_anim = new BufferedImage[8];
		for(int i = 0; i < clyde_anim.length; i++)
			clyde_anim[i] = extract(texture_map, i*16 + 336, 128, 15, 15);
		
		BufferedImage[] ghost_death = new BufferedImage[4];
		for(int i = 0; i < ghost_death.length; i++)
			ghost_death[i] = extract(texture_map, i*16 + 370, 144, 15, 15);
		
		BufferedImage[] ghost_frightened = new BufferedImage[6];
		for(int i = 0; i < ghost_frightened.length; i++)
			ghost_frightened[i] = extract(texture_map, i*16 + 336, 144, 15, 15);
		
		Ghost.frightened_anim = ghost_frightened;
		
		Genome genome = neat.loadGenome("network2");
		
		for(int i = 0; i < neat.getMaxClients(); i++)
		{
			PacmanClient client = new PacmanClient(pac_anim, pac_death);
			clients.add(client);
			Board board = new Board(extract(maze_img, 0, 8, maze_img.getWidth(), maze_img.getHeight()-8), client);
			boards.add(board);
			board.setClient(neat.getClient(i));
			Blinky blinky = new Blinky(blinky_anim, ghost_death);
			board.setBlinky(blinky);
			board.setPinky(new Pinky(pinky_anim, ghost_death));
			board.setInky(new Inky(blinky, inky_anim, ghost_death));
			board.setClyde(new Clyde(clyde_anim, ghost_death));
			neat.getClient(i).setGenome(genome.clone());
			neat.getClient(i).generateCalculator();
		}
		System.out.println("Games created");
	}
	
	private BufferedImage extract(BufferedImage source, int x, int y, int width, int height)
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		for(int x1 = x, i = 0; x1 < x+width; x1++, i++)
			for(int y1 = y, j = 0; y1 < y+height; y1++, j++)
				img.setRGB(i, j, source.getRGB(x1, y1));
		
		return img;
	}
	
	public void paint(Graphics g) {


		if (bufferStrategy == null) {
			this.createBufferStrategy(2);
			bufferStrategy = this.getBufferStrategy();
			bufferGraphics = bufferStrategy.getDrawGraphics();

			this.thread.start();
		}
	}
	@Override
	public void run() {
		// what runs when editor is running
		
		while (running) {
			long t1 = System.currentTimeMillis();
			
			DoLogic();
			
			Draw();

			DrawBackbufferToScreen();

			Thread.currentThread();
			try {
				Thread.sleep(refreshTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
			long t2 = System.currentTimeMillis();
			
//			if(t2 - t1 > 200)
//			{
//				if(refreshTime > 0)
//					refreshTime --;
//			}
//			else
//				refreshTime ++;
			
			seconds += refreshTime/1000f;
			//System.out.println(t2 - t1);
			

		}
	}

	public void DrawBackbufferToScreen() {
		bufferStrategy.show();

		Toolkit.getDefaultToolkit().sync();
	}
	private int gens = 0;
	private int highest_index = 0;
	private int all_time_high = 0;
	public void DoLogic() {
		
		for(int i = 0; i < neat.getMaxClients(); i++)
		{
			double[] output = neat.getClient(i).calculate(boards.get(i).getInputData(train_ghost_input));
			int index = (int) Utils.indexOfMax(output);
//			if(index != 0)
//				System.out.println("index: " + index);
			PacmanClient client = clients.get(i);
			if(index == 0)
				client.move('w');
			else if(index == 1)
				client.move('a');
			else if(index == 2)
				client.move('s');
			else if(index == 3)
				client.move('d');
			
			boards.get(i).step(train_ghost_input);
		}
		
//		if(keysDown.contains(KeyEvent.VK_W))
//			clients.get(0).move('w');
//		if(keysDown.contains(KeyEvent.VK_A))
//			clients.get(0).move('a');
//		if(keysDown.contains(KeyEvent.VK_S))
//			clients.get(0).move('s');
//		if(keysDown.contains(KeyEvent.VK_D))
//			clients.get(0).move('d');
//		boards.get(0).step();
		runTime++;
		
		if(runTime%100 == 0 && checkReset())
		{
			gens++;
			double score = 0;
			for(int i = 0; i < neat.getMaxClients(); i++)
			{
				if(neat.getClient(i).getScore() > score)
				{
					score = neat.getClient(i).getScore();
					highest_index = i;
				}
			}
			if(score > all_time_high)
			{
				all_time_high = (int) score;
				try
				{
					neat.getClient(highest_index).getGenome().write("network3");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			neat.evolve();
			boards.forEach(b -> b.reset(true));
		}
	}
	private boolean checkReset()
	{
		boolean reset = true;
		for(int i = 0; i < neat.getMaxClients(); i++)
		{
			if(boards.get(i).player.getAlive())
			{
				reset = false;
				break;
			}
		}
		//at least one is alive
		if(reset)
			return reset;
		for(int i = 0; i < neat.getMaxClients(); i++) //all have been idle
		{
			if(boards.get(i).frames_since_cookie < 2000)
			{
				return false;
			}
		}
		return true;
	}
	private boolean show_grid = false;
	private boolean show_targets = false;
	private boolean show_paths = false;
	private boolean train_ghost_input = true;
	public void Draw() // titleScreen
	{
		// clears the backbuffer
		bufferGraphics = bufferStrategy.getDrawGraphics();
		try {
			bufferGraphics.clearRect(0, 0, this.getSize().width, this.getSize().height);
			// where everything will be drawn to the backbuffer
			Graphics2D g2 = (Graphics2D) bufferGraphics;
			boards.get(highest_index).draw(g2, show_grid, show_targets, show_paths, train_ghost_input);
			neat.getClient(highest_index).draw(g2, true, 0, 500, 450, 100);
			
//			g2.drawImage(texture_map, 0, 0, null);
			int y = 0;
			g2.drawString("Ghost State: " + boards.get(highest_index).curr_state, 450, y += 15);
//			g2.drawString("coord: " + mousex + ", " + mousey, 450, 20);
			g2.drawString("State Change In: " + boards.get(highest_index).interval_runtime, 450, y += 15);
			g2.drawString("Current Score: " + boards.get(highest_index).getClient().getScore(), 450, y += 15);
			g2.drawString("All Time High Score: " + all_time_high, 450, y += 15);
			g2.drawString("Generation: " + gens, 450, y += 15);
			RandomHashSet<Species> species = neat.getSpecies();
			g2.drawString("Species: " + species.size(), 450, y += 15);
			for(int i = 0; i < species.size(); i++)
				g2.drawString("score " + (i+1) + ": " + species.get(i).getScore(), 470, y += 15);
			g2.drawString("Connections: " + neat.getConnectionsSize(), 450, y += 15);
			g2.drawString("Nodes: " + neat.getNodesSize(), 450, y += 15);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bufferGraphics.dispose();
		}
	}
	public void setShowGrid(boolean show_grid)
	{
		this.show_grid = show_grid;
	}
	public boolean getShowGrid()
	{
		return show_grid;
	}
	public void setShowTargets(boolean show_targets)
	{
		this.show_targets = show_targets;
	}
	public boolean getShowTargets()
	{
		return show_targets;
	}
	public void setShowPaths(boolean show_paths)
	{
		this.show_paths = show_paths;
	}
	public boolean getShowPaths()
	{
		return show_paths;
	}
	public void setTrainGhostInput(boolean train_ghost_input)
	{
		this.train_ghost_input = train_ghost_input;
	}
	public boolean getTrainGhostInput()
	{
		return train_ghost_input;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!keysDown.contains(e.getKeyCode()) && e.getKeyCode() != 86)
			keysDown.add(new Integer(e.getKeyCode()));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysDown.remove(new Integer(e.getKeyCode()));
		
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == 1)
		{
			leftClick = true;
		}
		else if(e.getButton() == 2)
		{
			
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == 1)
			leftClick = false;
		if(e.getButton() == 2)
			dragging = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(leftClick)
			leftClick = true;
		mousex = e.getX();
		mousey = e.getY();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
	}

}