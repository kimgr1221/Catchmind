package catchmind;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import catchmind.CatchmindServer.ClientInfo;

public class CatchmindServer implements Constants {

	private ServerSocket serverSocket;
	private Socket socket;
	private Vector<ClientInfo> vcClient;

	private String[] Nicknames = new String[PLAYER_COUNT];

	//제시어 20개 설정
	private String Words[] = 
		{
				"곰", "토끼", "강아지", "고양이", "호랑이", "거북이", "곰", "펭귄", "선인장", "꽃", 
				"복숭아", "수박", "아보카도", "감", "체리", "블루베리", "참외", "밤", "귤", "포도"
		};

	private int[] WordsIndex = new int[TURN_COUNT];

	private int Count = 0;
	private int TurnCount = 0;
	private int CurrentPlayer;
	private int PlayerAnswer;
	private String Answer;

	public CatchmindServer() {
		connectSocket();
		makeWordsIdx();
	}

	//제시어 20개 중에 랜덤으로 출제
	public void makeWordsIdx() {

		for (int i = 0; i < TURN_COUNT; ++i) {
			WordsIndex[i] = (int) (Math.random() * 20);

			for (int j = 0; j < i; ++j) {
				if (WordsIndex[i] == WordsIndex[j]) {
					i--;
					break;
				}
			}
		}
	}

	//연결 포트 5000으로 설정
	//콘솔창에 연결 상태 출력
	public void connectSocket() {
		try {

			serverSocket = new ServerSocket(5000);
			vcClient = new Vector<>();

			while (vcClient.size() < PLAYER_COUNT) {
				System.out.println("서버 준비 완료! 클라이언트를 기다리는중...");

				socket = serverSocket.accept();

				ClientInfo ci = new ClientInfo(socket);
				ci.start();
				vcClient.add(ci);

				System.out.println("클라이언트" + (vcClient.size()) + "(과)와 연결되었습니다.");
			}

			System.out.println("모든 클라이언트 연결 완료");

		} catch (Exception e) {
			System.out.println("연결안됨");
		}
	}

	//Client로부터 정보 수신
	class ClientInfo extends Thread {
		BufferedReader br;
		PrintWriter writer;
		Socket socket;

		public ClientInfo(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				writer = new PrintWriter(socket.getOutputStream(), true);
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String Message = null;
				String[] parsMessage;

				sendId();
				allConnected();

				while ((Message = br.readLine()) != null) {
					parsMessage = Message.split("&");

					switch (parsMessage[0]) {
					case NICKNAME:
						receivedNickname(parsMessage);
						break;
					case START_READY:
						receivedStartReady(parsMessage);
						break;
					case TURN_READY:
						receivedTurnReady(parsMessage);
						break;
					case TURN_END:
						receivedTurnEnd(parsMessage);
						break;
					case TIMEOVER:
						for (int i = 0; i < vcClient.size(); i++) {
							if (vcClient.get(i) == this) {
								Count++;
							}
						}

						if (Count == PLAYER_COUNT) {
							int randNum = CurrentPlayer;
							while (randNum == CurrentPlayer) {
								randNum = (int) (Math.random() * PLAYER_COUNT);
							}

							CurrentPlayer = randNum;

							sendString("CurP&" + CurrentPlayer);

							Count = 0;
							TurnCount++;
						}

						break;
					case RESULT:
						receivedResult(parsMessage);
						break;
						
					case EXIT:
						serverSocket.close();
						System.exit(0);
						break;

					case DRAW:
						sendStringWithoutSelf(Message);
						break;
						
					case COLOR:
						sendStringWithoutSelf(Message);
						break;
						
					case THICKNESS:
						sendStringWithoutSelf(Message);
						break;
						
					case CHAT:
						sendStringWithoutSelf(Message);
						findAnswer(parsMessage);
						break;
						
					case CH:
	                    processChatMessage(parsMessage);
	                    break;
					default:
					}
				}
			} catch (Exception e) {
				System.out.println("메세지 통신 실패");
				e.printStackTrace();
			}
		}

		private void sendId() {
			for (int i = 0; i < vcClient.size(); i++) {
				vcClient.get(i).writer.println("ID&" + i);
			}
		}

		//모두 접속하면 ALL_CONNECTED발신
		private void allConnected() {
			if (vcClient.size() == PLAYER_COUNT) {
				for (int i = 0; i < vcClient.size(); i++) {
					vcClient.get(i).writer.println("ALL_CONNECTED&");
				}
			}
			System.out.println("ALL_CONNECTED 보내짐");

		}

		//닉네임 수신
		private void receivedNickname(String[] parsMessage) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					Count++;
					Nicknames[i] = parsMessage[1];
				}
			}

			if (Count == PLAYER_COUNT) {
				String nicks = "NICKNAME&";
				for (int i = 0; i < vcClient.size(); i++) {
					nicks += Nicknames[i];
					if (i == vcClient.size() - 1)
						break;
					nicks += ",";
				}
				sendString(nicks);
				Count = 0;
			}
		}

		//모두 준비 완료하면 출제자 전송
		private void receivedStartReady(String[] parsMessage) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					Count++;
				}
			}
			System.out.println("START_READY 도착함");

			if (Count == PLAYER_COUNT) {
				CurrentPlayer = (int) (Math.random() * PLAYER_COUNT);
				sendString("CurP&" + CurrentPlayer);

				System.out.println("출제자 보내짐");
				Count = 0;
			}
		}

		//준비 수신 다 받으면 제시어 전송
		private void receivedTurnReady(String[] parsMessage) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					Count++;
				}
			}
			System.out.println("TURN_READY 도착함");

			if (Count == PLAYER_COUNT) {

				Answer = Words[WordsIndex[TurnCount]];
				sendString("WORD&" + Answer);
				System.out.println("제시어 보내짐");
				System.out.println("TurnCount: " + TurnCount);
				Count = 0;
			}
		}

		//턴 종료 수신
		private void receivedTurnEnd(String[] parsMessage) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					Count++;
				}
			}

			if (Count == PLAYER_COUNT) {
				CurrentPlayer = PlayerAnswer;
				sendString("CurP&" + CurrentPlayer);
				Count = 0;
			}
		}

		//결과 수신
		private void receivedResult(String[] parsMessage) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					Count++;
				}
			}
			System.out.println("RESULT 도착함");

			if (Count == PLAYER_COUNT) {
				sendString("RESULT&");
				Count = 0;

				System.out.println("RESULT 보내짐");
			}
		}

		private void sendString(String Message) {
			for (int i = 0; i < vcClient.size(); i++) {
				vcClient.get(i).writer.println(Message);
			}
		}

		private void sendStringWithoutSelf(String Message) {
			for (int i = 0; i < vcClient.size(); i++) {
		        if (vcClient.get(i) != this) {
		            vcClient.get(i).writer.println(Message);
		        }
		    }
		}
		
		//채팅 메시지
		private void sendNewChatMessage(String message) {
		    sendString("CH&" + message);
		}

		private void processChatMessage(String[] parsMessage) {
		    String chatContent = parsMessage[1];

		    sendNewChatMessage(chatContent);
		}

		//정답 나오면 다음 턴으로 이동
		private void findAnswer(String[] parsMessage) {
			String[] chat = parsMessage[1].split(SUB_DELIMETER);
			PlayerAnswer = Integer.parseInt(chat[0]);

			if (chat[1].equals(Answer)) {

				sendString("CORRECT&" + PlayerAnswer + SUB_DELIMETER + chat[1]);

				TurnCount++;
			}
		}
	}

	public static void main(String[] args) {
		new CatchmindServer();
	}
}