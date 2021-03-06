package pt.utl.ist.cmov.bomberman.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.utl.ist.cmov.bomberman.game.dto.ModelDTO;
import pt.utl.ist.cmov.bomberman.game.dto.ModelDTOFactory;
import pt.utl.ist.cmov.bomberman.game.model.BombModel;
import pt.utl.ist.cmov.bomberman.game.model.BombermanModel;
import pt.utl.ist.cmov.bomberman.game.model.EmptyModel;
import pt.utl.ist.cmov.bomberman.game.model.ExplosionModel;
import pt.utl.ist.cmov.bomberman.game.model.Model;
import pt.utl.ist.cmov.bomberman.game.model.ObstacleModel;
import pt.utl.ist.cmov.bomberman.game.model.RobotModel;
import pt.utl.ist.cmov.bomberman.game.model.WallModel;
import pt.utl.ist.cmov.bomberman.util.Direction;
import pt.utl.ist.cmov.bomberman.util.Position;
import android.os.Handler;

public class Level {

	private String levelName;

	private Integer gameDuration;
	private Integer explosionTimeout;
	private Integer explosionDuration;
	private Integer explosionRange;
	private Integer robotSpeed;
	private Integer pointsRobot;
	private Integer pointsOpponent;
	private Map<Integer, Position> bombermansInitialPos;
	private Integer maxBombermans;
	private Integer remainingRobots;
	private Integer remainingBombermans;

	public static final Character WALL = 'W';
	public static final Character OBSTACLE = 'O';
	public static final Character ROBOT = 'R';
	public static final Character BOMB = 'B';
	public static final Character EMPTY = '-';
	public static final Character EXPLODING = 'E';
	public static final Character BOMBERMAN = 'P';

	private Integer height;
	private Integer width;

	private ArrayList<ArrayList<Model>> modelMap;

	private Integer modelIds;

	private Boolean isPaused;

	private ArrayList<ModelDTO> updatesBuffer;

	private Handler handler;

	public Level(String levelName, Integer gameDuration,
			Integer explosionTimeout, Integer explosionDuration,
			Integer explosionRange, Integer robotSpeed, Integer pointsRobot,
			Integer pointsOpponent, Integer numberOfRobots,
			Map<Integer, Position> bombermansInitialPos) {
		super();
		this.levelName = levelName;
		this.gameDuration = gameDuration;
		this.explosionTimeout = explosionTimeout;
		this.explosionDuration = explosionDuration;
		this.explosionRange = explosionRange;
		this.robotSpeed = robotSpeed;
		this.pointsRobot = pointsRobot;
		this.pointsOpponent = pointsOpponent;
		this.remainingRobots = numberOfRobots;
		this.remainingBombermans = 0;
		this.modelMap = new ArrayList<ArrayList<Model>>();
		this.bombermansInitialPos = bombermansInitialPos;
		this.isPaused = false;
		this.updatesBuffer = new ArrayList<ModelDTO>();
		this.handler = new Handler();
	}

	public Handler getHandler() {
		return this.handler;
	}

	public HashMap<Integer, BombermanModel> getBombermanModels() {
		HashMap<Integer, BombermanModel> bombermanModels = new HashMap<Integer, BombermanModel>();
		for (ArrayList<Model> line : modelMap) {
			for (Model model : line) {
				if (model.getType() == BOMBERMAN) {
					BombermanModel bomberman = (BombermanModel) model;
					bombermanModels.put(bomberman.getBombermanId(), bomberman);
				}
			}
		}

		return bombermanModels;
	}

	public String getLevelName() {
		return this.levelName;
	}

	public Integer getGameDuration() {
		return gameDuration;
	}

	public void setGameDuration(Integer gameDuration) {
		this.gameDuration = gameDuration;
	}

	public Integer getExplosionTimeout() {
		return explosionTimeout;
	}

	public void setExplosionTimeout(Integer explosionTimeout) {
		this.explosionTimeout = explosionTimeout;
	}

	public Integer getExplosionDuration() {
		return explosionDuration;
	}

	public void setExplosionDuration(Integer explosionDuration) {
		this.explosionDuration = explosionDuration;
	}

	public Integer getExplosionRange() {
		return explosionRange;
	}

	public void setExplosionRange(Integer explosionRange) {
		this.explosionRange = explosionRange;
	}

	public Integer getRobotSpeed() {
		return robotSpeed;
	}

	public void setRobotSpeed(Integer robotSpeed) {
		this.robotSpeed = robotSpeed;
	}

	public Integer getPointsRobot() {
		return pointsRobot;
	}

	public void setPointsRobot(Integer pointsRobot) {
		this.pointsRobot = pointsRobot;
	}

	public Integer getPointsOpponent() {
		return pointsOpponent;
	}

	public void setPointsOpponent(Integer pointsOpponent) {
		this.pointsOpponent = pointsOpponent;
	}

	public Integer getRemainingRobots() {
		return remainingRobots;
	}
	
	public void decrRemainingRobots() {
		this.remainingRobots--;
	}

	public Integer getRemainingBombermans() {
		return remainingBombermans;
	}
	
	public void decrRemainingBomberman() {
		this.remainingBombermans--;
	}
	
	public void incrRemainingBomberman() {
		this.remainingBombermans++;
	}

	public Map.Entry<Integer, Position> getBombermanInitialPos() {
		Map.Entry<Integer, Position> bombermanEntry = this.bombermansInitialPos
				.entrySet().iterator().next();
		this.bombermansInitialPos.remove(bombermanEntry.getKey());
		return bombermanEntry;
	}

	public Integer getMaxBombermans() {
		return maxBombermans;
	}

	public Character getContent(Integer x, Integer y) {
		return getOnMap(new Position(x, y)).getType();
	}

	public Character getContent(Position pos) {
		return getOnMap(pos).getType();
	}

	public Model getOnMap(Position pos) {
		return this.modelMap.get(pos.y).get(pos.x);
	}

	public Integer getHeight() {
		return this.height;
	}

	public Integer getWidth() {
		return this.width;
	}

	public boolean move(Model model, Direction direction) {
		if (!this.isPaused) {
			Position newPos = Position.calculateNext(direction, model.getPos());

			Model destination = this.getOnMap(newPos);
			if (destination.canMoveOver(model)) {
				this.move(model.getPos(), newPos);
			} else {
				model.moveAction(destination);
				destination.moveAction(model);

				return false;
			}

			return true;
		} else {
			return false;
		}
	}

	private void move(Position orig, Position dest) {
		synchronized (this.modelMap) {
			Model model = getOnMap(dest);
			Model otherModel = getOnMap(orig);

			if (otherModel.getType() == ROBOT) {
				removeKillingZone(otherModel.getPos());
			}

			model.setPos(orig);
			otherModel.setPos(dest);

			setOnMap(dest, otherModel);
			setOnMap(orig, model);

			if (otherModel.getType() == ROBOT) {
				putKillingZone(otherModel.getPos());
			}
		}
	}

	public void putKillingZone(Position pos) {
		getOnMap(pos).putKillingZone();
		getOnMap(Position.calculateUpPosition(pos)).putKillingZone();
		getOnMap(Position.calculateDownPosition(pos)).putKillingZone();
		getOnMap(Position.calculateLeftPosition(pos)).putKillingZone();
		getOnMap(Position.calculateRightPosition(pos)).putKillingZone();
	}

	public void removeKillingZone(Position pos) {
		getOnMap(pos).removeKillingZone();
		getOnMap(Position.calculateUpPosition(pos)).removeKillingZone();
		getOnMap(Position.calculateDownPosition(pos)).removeKillingZone();
		getOnMap(Position.calculateLeftPosition(pos)).removeKillingZone();
		getOnMap(Position.calculateRightPosition(pos)).removeKillingZone();
	}

	public void putEmpty(Position pos) {
		Model current = getOnMap(pos);

		EmptyModel empty = new EmptyModel(this, current.getId(), pos);
		setOnMap(pos, empty);
	}

	public void putExploding(BombModel model, Position pos) {
		Model current = getOnMap(pos);

		ExplosionModel explosion = new ExplosionModel(this, current.getId(),
				pos, model);
		current.moveAction(explosion);
		explosion.moveAction(current);
		setOnMap(pos, explosion);
	}

	public BombModel createBomb(BombermanModel bomberman) {
		Position pos = bomberman.getPos();

		return new BombModel(this, bomberman.getId(), pos, bomberman);
	}

	public void putBomb(BombModel bomb) {
		Model previous = getOnMap(bomb.getPos());

		bomb.setId(previous.getId());

		setOnMap(bomb.getPos(), bomb);
	}

	public void removeBomberman(BombermanModel bomberman) {
		this.bombermansInitialPos.put(bomberman.getBombermanId(),
				bomberman.getPos());
		this.putEmpty(bomberman.getPos());
	}

	public BombermanModel putBomberman() {
		Map.Entry<Integer, Position> bombermanEntry = this
				.getBombermanInitialPos();
		Integer bombermanId = bombermanEntry.getKey();
		Position pos = bombermanEntry.getValue();

		Model current = getOnMap(pos);
		BombermanModel bomberman = new BombermanModel(this, current.getId(),
				pos, bombermanId);
		setOnMap(pos, bomberman);
		
		this.remainingBombermans++;

		return bomberman;
	}

	public void putBomberman(BombermanModel bomberman) {
		setOnMap(bomberman.getPos(), bomberman);
	}

	public void parseMap(List<List<Character>> initialMap) {
		this.isPaused = true;

		this.height = initialMap.size();
		this.width = initialMap.get(0).size();

		this.maxBombermans = this.bombermansInitialPos.size();
		this.modelIds = maxBombermans + 1;

		for (int y = 0; y < this.height; y++) {
			ArrayList<Model> line = new ArrayList<Model>();

			for (int x = 0; x < this.width; x++) {
				Character c = initialMap.get(y).get(x);
				Integer id = this.modelIds++;

				Position pos = new Position(x, y);

				if (c == WALL)
					line.add(new WallModel(this, id, pos));
				else if (c == OBSTACLE)
					line.add(new ObstacleModel(this, id, pos));
				else if (c == ROBOT)
					line.add(new RobotModel(this, id, pos));
				else if (c == EMPTY)
					line.add(new EmptyModel(this, id, pos));
			}

			this.modelMap.add(line);
		}

		this.isPaused = false;
	}

	public void parseMap(Integer height, Integer width,
			ArrayList<ModelDTO> initialMap) {
		this.isPaused = true;

		this.height = height;
		this.width = width;

		this.maxBombermans = this.bombermansInitialPos.size();
		this.modelIds = 0;

		for (int y = 0; y < this.height; y++) {
			ArrayList<Model> line = new ArrayList<Model>();

			for (int x = 0; x < this.width; x++) {
				ModelDTO model = findByPos(initialMap, x, y);

				Position pos = new Position(x, y);

				if (model.getType() == WALL) {
					line.add(new WallModel(this, model.getId(), pos));
				} else if (model.getType() == OBSTACLE) {
					line.add(new ObstacleModel(this, model.getId(), pos));
				} else if (model.getType() == ROBOT) {
					line.add(new RobotModel(this, model.getId(), pos));
				} else if (model.getType() == EMPTY) {
					line.add(new EmptyModel(this, model.getId(), pos));
				} else if (model.getType() == BOMBERMAN) {
					line.add(new BombermanModel(this, model.getId(), pos, model
							.getBombermanId()));
					bombermansInitialPos.remove(model.getBombermanId());
				}

				if (this.modelIds < model.getId()) {
					this.modelIds = model.getId();
				}
			}

			this.modelMap.add(line);
		}

		for (int y = 0; y < this.height; y++) {

			for (int x = 0; x < this.width; x++) {
				Position pos = new Position(x, y);

				if (getOnMap(pos).getType() == ROBOT) {
					putKillingZone(pos);
				}
			}
		}

		this.isPaused = false;
	}

	private ModelDTO findByPos(List<ModelDTO> initialMap, int x, int y) {
		for (ModelDTO model : initialMap) {
			if (model.getPos().x == x && model.getPos().y == y) {
				return model;
			}
		}
		return null;
	}

	public boolean isInDeathZone(Position testPosition) {
		for (Direction direction : Direction.values()) {
			Position nextPosition = Position.calculateNext(direction,
					testPosition);
			if (getOnMap(nextPosition).getType() == Level.ROBOT) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<ModelDTO> getMapDTO() {
		ArrayList<ModelDTO> dtos = new ArrayList<ModelDTO>();
		for (List<Model> line : modelMap) {
			for (Model model : line) {
				ModelDTO dto = ModelDTOFactory.create(model);
				dtos.add(dto);
			}
		}
		return dtos;
	}

	public ArrayList<ModelDTO> getLatestUpdates() {
		ArrayList<ModelDTO> dtos = new ArrayList<ModelDTO>(updatesBuffer);
		this.updatesBuffer = new ArrayList<ModelDTO>();

		return dtos;
	}

	public void stopAll() {
		for (ArrayList<Model> line : modelMap) {
			for (Model model : line) {
				if (model.getType().equals(ROBOT)) {
					RobotModel robot = (RobotModel) model;
					robot.stopAll();
				}
			}
		}
	}

	private void setOnMap(Position position, Model model) {
		this.modelMap.get(position.y).set(position.x, model);
		updatesBuffer.add(ModelDTOFactory.create(model));
	}
}
