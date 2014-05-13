package pt.utl.ist.cmov.bomberman.handlers.managers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.utl.ist.cmov.bomberman.game.BombermanPlayer;
import pt.utl.ist.cmov.bomberman.game.GameClient;
import pt.utl.ist.cmov.bomberman.game.IGameServer;
import pt.utl.ist.cmov.bomberman.game.dto.ModelDTO;
import pt.utl.ist.cmov.bomberman.handlers.CommunicationObject;
import pt.utl.ist.cmov.bomberman.handlers.channels.ICommunicationChannel;
import pt.utl.ist.cmov.bomberman.util.Direction;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClientCommunicationManager implements ICommunicationManager,
		IGameServer {

	private ICommunicationChannel commChannel;
	private GameClient gameClient;

	public ClientCommunicationManager(GameClient gameClient) {
		this.gameClient = gameClient;
	}

	public void setCommChannel(ICommunicationChannel commChannel) {
		close();
		this.commChannel = commChannel;
		gameClient.putBomberman();
	}

	@Override
	public void receive(Object object) {
		Gson gson = new Gson();

		CommunicationObject obj = (CommunicationObject) object;

		if (obj.getType().equals(CommunicationObject.UPDATE_SCREEN)) {
			Type collectionType = new TypeToken<Collection<ModelDTO>>() {
			}.getType();

			ArrayList<ModelDTO> models = (ArrayList<ModelDTO>) gson.fromJson(
					obj.getMessage(), collectionType);
			this.gameClient.updateScreen(models);
		} else if (obj.getType().equals(CommunicationObject.UPDATE_PLAYERS)) {
			Type collectionType = new TypeToken<HashMap<String, BombermanPlayer>>() {
			}.getType();

			HashMap<String, BombermanPlayer> players = (HashMap<String, BombermanPlayer>) gson
					.fromJson(obj.getMessage(), collectionType);
			this.gameClient.updatePlayers(players);
		} else if (obj.getType().equals(CommunicationObject.INIT)) {
			Type hashType = new TypeToken<HashMap<String, Integer>>() {
			}.getType();

			Type collectionType = new TypeToken<ArrayList<ModelDTO>>() {
			}.getType();

			ArrayList<ModelDTO> models = (ArrayList<ModelDTO>) gson.fromJson(
					obj.getMessage(), collectionType);

			HashMap<String, Integer> mesurements = (HashMap<String, Integer>) gson
					.fromJson(obj.getExtraMessage(), hashType);

			this.gameClient.init(mesurements.get("lines"),
					mesurements.get("cols"), models);
		} else if (obj.getType().equals(CommunicationObject.START_SERVER)) {
			Type hashType = new TypeToken<HashMap<String, Integer>>() {
			}.getType();

			Type collectionType = new TypeToken<ArrayList<ModelDTO>>() {
			}.getType();

			ArrayList<ModelDTO> models = (ArrayList<ModelDTO>) gson.fromJson(
					obj.getMessage(), collectionType);

			HashMap<String, Integer> mesurements = (HashMap<String, Integer>) gson
					.fromJson(obj.getExtraMessage(), hashType);

			this.gameClient.startServer(mesurements.get("width"),
					mesurements.get("height"), models);
		} else if (obj.getType().equals(CommunicationObject.DEBUG)) {
			Log.i("CommunicationManager", obj.getMessage());
		}
	}

	@Override
	public void addCommChannel(ICommunicationChannel commChannel) {
		setCommChannel(commChannel);
	}

	@Override
	public void putBomberman(String username) {
		CommunicationObject object = new CommunicationObject(
				CommunicationObject.PUT_BOMBERMAN, username);

		commChannel.send(object);
	}

	@Override
	public void putBomb(String username) {
		CommunicationObject object = new CommunicationObject(
				CommunicationObject.PUT_BOMB, username);

		commChannel.send(object);
	}

	@Override
	public void move(String username, Direction direction) {
		Gson gson = new Gson();
		String extraMessageJson = gson.toJson(direction);

		CommunicationObject object = new CommunicationObject(
				CommunicationObject.MOVE, username, extraMessageJson);

		commChannel.send(object);
	}

	@Override
	public void pause(String username) {
		CommunicationObject object = new CommunicationObject(
				CommunicationObject.PAUSE, username);

		commChannel.send(object);
	}

	@Override
	public void quit(String username) {
		CommunicationObject object = new CommunicationObject(
				CommunicationObject.QUIT, username);

		commChannel.send(object);
	}

	@Override
	public void close() {
		if (this.commChannel != null) {
			this.commChannel.close();
			this.commChannel = null;
		}
	}

	@Override
	public ArrayList<String> getChannelEndpoints() {
		ArrayList<String> channelAddresses = new ArrayList<String>();
		channelAddresses.add(commChannel.getChannelEndpoint());
		return channelAddresses;
	}
}