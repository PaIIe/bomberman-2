package pt.utl.ist.cmov.bomberman.game;

import java.util.HashMap;
import java.util.List;

import pt.utl.ist.cmov.bomberman.activities.views.MainGamePanel;
import pt.utl.ist.cmov.bomberman.game.drawings.Drawing;
import pt.utl.ist.cmov.bomberman.game.elements.Element;
import pt.utl.ist.cmov.bomberman.util.Direction;
import pt.utl.ist.cmov.bomberman.util.MapMeasurements;
import android.graphics.Canvas;
import android.os.Handler;

public class GameClient implements IGameClient {

	private BombermanPlayer player;

	private MainGamePanel gamePanel;
	protected IGameServer gameServerProxy;
	private HashMap<Integer, Drawing> drawings;

	private Handler handler;

	public GameClient(String username, MainGamePanel gamePanel) {
		super();
		this.player = new BombermanPlayer(username);
		this.gamePanel = gamePanel;
		this.drawings = new HashMap<Integer, Drawing>();
	}

	public Handler getHandler() {
		return handler;
	}

	public void setGameServer(IGameServer gameServer) {
		this.gameServerProxy = gameServer;
	}

	public void init(List<List<Element>> elements) {
		MapMeasurements.updateMapMeasurements(gamePanel.getWidth(),
				gamePanel.getHeight(), elements.get(0).size(), elements.size());
	}

	public void putBomberman() {
		gameServerProxy.putBomberman(player.getUsername());
	}

	public void putBomb() {
		// TODO: put bomb
	}

	public void move(Direction dir) {
		// TODO: move bomberman
	}

	@Override
	public void updateScreen(List<String> drawings) {
		// TODO Auto-generated method stub

	}

	public void draw(Canvas canvas) {
		for (Drawing d : drawings.values()) {
			d.draw(canvas);
		}
	}

}
