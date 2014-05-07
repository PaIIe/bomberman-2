package pt.utl.ist.cmov.bomberman.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.utl.ist.cmov.bomberman.R;
import pt.utl.ist.cmov.bomberman.activities.adapters.GameAdapter;
import pt.utl.ist.cmov.bomberman.activities.interfaces.CommunicationPeer;
import pt.utl.ist.cmov.bomberman.controllers.GameDiscoveryController;
import pt.utl.ist.cmov.bomberman.handlers.CommunicationManager;
import pt.utl.ist.cmov.bomberman.handlers.PlayerSocketHandler;
import pt.utl.ist.cmov.bomberman.handlers.ServerSocketHandler;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class GameDiscoveryActivity extends FullScreenActivity implements
		Handler.Callback, CommunicationPeer, PeerListListener,
		ConnectionInfoListener, AdapterView.OnItemClickListener {

	public static final String PEER_MESSAGE = "pt.utl.ist.cmov.bomberman.activities.PEER";

	private WifiP2pManager manager;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private GameDiscoveryController discoveryController = null;

	private List<CommunicationManager> commManagers = new ArrayList<CommunicationManager>();

	private Handler handler = new Handler(this);

	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	public Handler getHandler() {
		return handler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_peer_choice);

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);

		discoveryController = new GameDiscoveryController(manager, channel,
				this);

		discoveryController.setupWifi();

		discoveryController.discoverPeers();
	}

	public void refreshGames(View view) {
		if (commManagers.isEmpty()) {
			discoveryController.discoverPeers();
			Toast.makeText(getApplicationContext(), "Discovering peers...",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(),
					"Message successfuly sent!", Toast.LENGTH_SHORT).show();
			for (Iterator<CommunicationManager> commManager = commManagers
					.iterator(); commManager.hasNext();) {
				commManager.next().write("Hello world!");
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		String readMessage = (String) msg.obj;

		Log.e("BOMBERMAN-SOCKET", readMessage);
		Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG)
				.show();

		return true;
	}

	@Override
	public void onStop() {
		if (manager != null && channel != null) {
			manager.removeGroup(channel, new ActionListener() {

				@Override
				public void onFailure(int reasonCode) {
					Log.d("BOMBERMAN", "Disconnect failed. Reason :"
							+ reasonCode);
				}

				@Override
				public void onSuccess() {
				}

			});
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		discoveryController = new GameDiscoveryController(manager, channel,
				this);
		registerReceiver(discoveryController, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(discoveryController);
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
		Thread handler = null;

		if (p2pInfo.isGroupOwner) {
			Log.d("BOMBERMAN", "Connected as group owner");
			try {
				handler = new ServerSocketHandler((CommunicationPeer) this);
				handler.start();
			} catch (IOException e) {
				Log.d("BOMBERMAN",
						"Failed to create a server thread - " + e.getMessage());
				return;
			}
		} else {
			Log.d("BOMBERMAN", "Connected as peer");
			handler = new PlayerSocketHandler((CommunicationPeer) this,
					p2pInfo.groupOwnerAddress);
			handler.start();
		}
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {

		peers.clear();
		peers.addAll(peerList.getDeviceList());

		loadPeers(peers);
	}

	public void loadPeers(List<WifiP2pDevice> peers) {
		GameAdapter adapter = new GameAdapter(this, peers);
		ListView listView = (ListView) findViewById(R.id.list_levels);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ListView list = (ListView) parent;

		WifiP2pDevice device = (WifiP2pDevice) list.getItemAtPosition(position);

		discoveryController.connect(device);

		// Intent intent = new Intent(this, PlayerActivity.class);
		// intent.putExtra(PEER_MESSAGE, device);
		// startActivity(intent);
	}

	@Override
	public void addCommunicationManager(CommunicationManager cManager) {
		this.commManagers.add(cManager);
	}

	@Override
	public List<CommunicationManager> getCommunicationManagers() {
		return commManagers;
	}
}
