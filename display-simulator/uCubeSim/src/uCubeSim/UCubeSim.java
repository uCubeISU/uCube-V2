/**
 * 
 */
package uCubeSim;

import processing.core.*;
//import processing.data.*;
//import processing.event.*;
//import processing.opengl.*;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;

//import java.util.HashMap;
//import java.util.ArrayList;
//import java.io.File;
//import java.io.BufferedReader;
//import java.io.PrintWriter;
//import java.io.InputStream;
//import java.io.OutputStream;
import java.io.IOException;

/**
 * @author vens
 *
 */
public class UCubeSim extends PApplet {

	private static final long serialVersionUID = 8157175666919036531L;
	
	float xmag, ymag = 0;
	float newXmag, newYmag = 0;

	int[][][] leds = new int[6][6][6];
	int[] colorMap = new int[256];

	
	public void setup() {
		int i, j, k, r, g, b;
		// set the window size
		size(640, 360, P3D);
		noStroke();
		// RGB mode scaled from 0-1
		colorMode(RGB, 1);

		i = 0;
		// TODO: load a static color map from a file
		for (r = 0; r < 8; r++) {
			for (g = 0; g < 4; g++) {
				for (b = 0; b < 8; b++) {
					colorMap[i] = color(r / 7.0f, g / 3.0f, b / 7.0f);
					i++;
				}
			}
		}

		// Start with all LEDs being black
		for (i = 0; i < 6; i++) {
			for (j = 0; j < 6; j++) {
				for (k = 0; k < 6; k++) {
					leds[i][j][k] = 0;
				}
			}
		}
		
		// load the thread to update the cube colors from the ucube driver
		thread("updateColors");
	}

	// This thread runs async to update the led colors from the driver
	public void updateColors() {
		RandomAccessFile file = null;
		try {
			// open the driver file
			file = new RandomAccessFile("/tmp/mapped_data", "r");
			// get the file channel from the file
			FileChannel channel = file.getChannel();
			// open the memmap buffer from the file
			MappedByteBuffer shared_mem = channel.map(
					FileChannel.MapMode.READ_ONLY, 0, 216);

			while (true) {
				int i, j, k, l = 0;
				// make sure that the file is synced
				shared_mem.load();
				for (i = 0; i < 6; i++) {
					for (j = 0; j < 6; j++) {
						for (k = 0; k < 6; k++) {
							// update the LED color from the file
							leds[i][j][k] = shared_mem.get(l) + 128; // convert from signed to unsigned
							l++;
						}
					}
				}
				// TODO: add a small update delay here (probably 50ms)
			}
		} catch (IOException e) {
			e.printStackTrace();
			// exit();
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	// set the led on side @param side in the location @param x and @param y to the color from the map index @param c
	public void setLed(int side, int x, int y, int c) {
		float vx1 = x / 2.5f - 1.2f;
		float vx2 = vx1 + 0.4f;
		float vy1 = y / 2.5f - 1.2f;
		float vy2 = vy1 + 0.4f;
		
		// set the fill color from the color map
		fill(colorMap[c]);

		// choose the correct plane
		switch (side) {
		case 0:
			vertex(vx1, vy1, 1.2f);
			vertex(vx2, vy1, 1.2f);
			vertex(vx2, vy2, 1.2f);
			vertex(vx1, vy2, 1.2f);
			break;
		case 1:
			vertex(vx1, 1.2f, vy1);
			vertex(vx2, 1.2f, vy1);
			vertex(vx2, 1.2f, vy2);
			vertex(vx1, 1.2f, vy2);
			break;
		case 2:
			vertex(1.2f, vx1, vy1);
			vertex(1.2f, vx2, vy1);
			vertex(1.2f, vx2, vy2);
			vertex(1.2f, vx1, vy2);
			break;
		case 3:
			vertex(vx1, vy1, -1.2f);
			vertex(vx2, vy1, -1.2f);
			vertex(vx2, vy2, -1.2f);
			vertex(vx1, vy2, -1.2f);
			break;
		case 4:
			vertex(vx1, -1.2f, vy1);
			vertex(vx2, -1.2f, vy1);
			vertex(vx2, -1.2f, vy2);
			vertex(vx1, -1.2f, vy2);
			break;
		case 5:
			vertex(-1.2f, vx1, vy1);
			vertex(-1.2f, vx2, vy1);
			vertex(-1.2f, vx2, vy2);
			vertex(-1.2f, vx1, vy2);
			break;
		}
	}

	// on the draw update
	public void draw() {
		// set the background color
		background(0.5f);

		pushMatrix();
		translate(width / 2, height / 2, -30);

		newXmag = mouseX / PApplet.parseFloat(width) * TWO_PI;
		newYmag = mouseY / PApplet.parseFloat(height) * TWO_PI;

		float diff = xmag - newXmag;
		if (abs(diff) > 0.01f) {
			xmag -= diff / 4.0f;
		}

		diff = ymag - newYmag;
		if (abs(diff) > 0.01f) {
			ymag -= diff / 4.0f;
		}

		rotateX(-ymag);
		rotateY(-xmag);

		scale(90);
		beginShape(QUADS);

		int i, j, k;
		for (i = 0; i < 6; i++) {
			for (j = 0; j < 6; j++) {
				for (k = 0; k < 6; k++) {
					setLed(i, j, k, leds[i][j][k]);
				}
			}
		}
		endShape();

		popMatrix();
	}
	
	

	public static void main(String args[]) {
		PApplet.main(new String[] { "uCubeSim.UCubeSim" });
	}
}
