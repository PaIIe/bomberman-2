package pt.utl.ist.cmov.bomberman.game.drawings;

import pt.utl.ist.cmov.bomberman.util.Position;
import android.content.Context;

public class RobotDrawing extends Drawing {

	private static final long serialVersionUID = -1573822000954051199L;

	public RobotDrawing() {
		super();
	}

	public RobotDrawing(Context context, Integer id, Position pos) {
		super(id, pos, "images/robot.png");
	}

}
