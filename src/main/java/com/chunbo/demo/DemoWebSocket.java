package com.chunbo.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by Show on 2017/3/29.
 */
@ServerEndpoint(value = "/tugowebsocket/{actId}")
public class DemoWebSocket {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;
	private  int userNumber = 0;

	//concurrent包的线程安全Set，用来存放每个客户端对应的TugWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	// private static CopyOnWriteArraySet<TugWebSocket> webSocketSet = new CopyOnWriteArraySet<TugWebSocket>();

	private static HashMap<String, CopyOnWriteArraySet<DemoWebSocket>> map = new HashMap<String, CopyOnWriteArraySet<DemoWebSocket>>();

	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	/**
	 * 连接建立成功调用的方法
	 *
	 * @param actId   活动id
	 * @param   来源， 0：大屏；1：C 端参与活动用户
	 * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(@PathParam("actId") int actId, Session session) {

		String key = Integer.toString(actId);
		this.setUserNumber(actId);
		if (map.containsKey(key)) {//如果已经有此key
			CopyOnWriteArraySet<DemoWebSocket> webSocketSet = map.get(key);

			webSocketSet.add(this);

			System.out.println("tugowebsocket-open-event  : " + key + "---------------" + webSocketSet.size());

		} else {
			CopyOnWriteArraySet<DemoWebSocket> webSocketSet = new CopyOnWriteArraySet<DemoWebSocket>();
			webSocketSet.add(this);
			map.put(key, webSocketSet);

			System.out.println("tugowebsocket-open-event  : " + key + "---------------" + webSocketSet.size());
		}
		this.session = session;
	}

	/**
	 * 连接关闭调用的方法
	 * @param actId   活动id
	 * @param   来源， 0：大屏；1：C 端参与活动用户
	 */
	@OnClose
	public void onClose(@PathParam("actId") int actId) {
		String key = Integer.toString(actId);
		this.setUserNumber(0);
		if (map.containsKey(key)) {//如果已经有此key
			CopyOnWriteArraySet<DemoWebSocket> webSocketSet = map.get(key);
			webSocketSet.remove(this);  //从set中删除

			System.out.println("tugowebsocket-close-event  : " + key + "---------------" + webSocketSet.size());
		}
	}

	/**
	 * 收到客户端消息后调用的方法
	 * @param actId   活动id
	 * @param   来源， 0：大屏；1：C 端参与活动用户
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(@PathParam("actId") int actId,String message, Session session) {

		String key = Integer.toString(actId);

		if (map.containsKey(key)) {//如果已经有此key
			CopyOnWriteArraySet<DemoWebSocket> webSocketSet = map.get(key);

			for (DemoWebSocket item : webSocketSet) {
				try {
					if(item.getUserNumber() == 1){
						item.sendMessage(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}

			System.out.println("tugowebsocket-sendmsg-event  : " + key + "---------------" + message);
		}

	}

	/**
	 * 发生错误时调用
	 *
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 *
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		// this.session.getBasicRemote().sendText(message);
		this.session.getAsyncRemote().sendText(message);
	}

	public int getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}
}
