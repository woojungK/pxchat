package pxchat.whiteboard;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import pxchat.net.protocol.frames.Frame;

public class RectObject extends PrimitiveObject {

	private Point topLeft;
	private int width;
	private int height;

	public RectObject(Point point1, Point point2, Color color, float width) {
		super(color, width);
		this.id = Frame.ID_RECT;
		this.topLeft = new Point(Math.min(point1.x, point2.x), Math.min(
				point1.y, point2.y));
		this.width = Math.abs(point1.x - point2.x);
		this.height = Math.abs(point1.y - point2.y);
	}

	public RectObject(Point topLeft, int width, int height, Color color,
						float strokeWidth) {
		super(color, strokeWidth);
		this.id = Frame.ID_RECT;
		this.topLeft = topLeft;
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(Graphics2D g) {
		beginDraw(g);
		g.drawRect(topLeft.x, topLeft.y, width, height);
		endDraw(g);
	}

}