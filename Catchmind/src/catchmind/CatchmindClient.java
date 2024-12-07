package catchmind;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket; 
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import catchmind.CatchmindClient.TimerThread;

public class CatchmindClient extends CatchmindGUI implements Runnable, Constants {
	   
	   private Socket socket;
	   private BufferedReader br;
	   private PrintWriter writer;
	   
	   private String SendDraw = null;
	   private String SendColor = null;
	   private String SendThickness = null;
	   private String SendScore = null;
	   private String SendMessage = null;
	   
	   private String MyNickName;
	   private int MyId;
	   private String[] Nicknames = new String[PLAYER_COUNT];
	   private int[] Scores = new int[PLAYER_COUNT];
	   private int CurrentPlayerCount;
	   private boolean CanDraw = true;
	   private int TurnCount = 0;
	   private SimpleDateFormat sdf = new SimpleDateFormat("(YYYY-MM-dd HH:mm:ss)");
	   
	   public CatchmindClient() {
	      
	      addWindowListener(this);
	      connectSocket();
	   }
	   
	   TimerThread timer;
	   
	   //제한 시간 표시
	   class TimerThread extends Thread {
	      
	      CatchmindClient client;
	      boolean run = true;
	      
	      TimerThread(CatchmindClient client) {
	         this.client = client;
	      }
	      
	      public void stopTimer() {
	         run = false;
	      }
	      
	   @Override
	   public void run() {
	      try {
	         int i = SEC;
	         while (true) {
	         
	            if (run == true) {
	               TimerLabel.setText(i + " 초");
	               Thread.sleep(2000);
	            
	               //제한시간 1초씩 감소
	               i--;
	               if (i < 0) {
	            	   //제한시간 끝나면 시간초과 안내창
	                  int timeover = JOptionPane.showConfirmDialog(client, "타임오버!", "시간초과 안내", JOptionPane.DEFAULT_OPTION);
	                  
	                  if (timeover == JOptionPane.OK_OPTION) {
	                     writer.println("TIMEOVER&");
	                     TurnCount++;
	                     
	                     	if (TurnCount == TURN_COUNT) {
	                     		writer.println("RESULT&");
	                     	}
	                  }     
	                  break;
	               	}
	            }
	         }
	      } catch (InterruptedException e) {
	         e.printStackTrace();
	      	}
	   	}
	  }

	   public void connectSocket() {
	      try {
	        socket = new Socket("127.0.0.1", 5000); 

	        //서버에 정보를 보내는 OutputStream, 정보를 받는 InputStream 연결
	        writer = new PrintWriter(socket.getOutputStream(), true); 
	        br = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

	        Thread thread = new Thread(this); 
	        thread.start();

	        System.out.println(
	        	//서버 연결 성공시 IP와 현재 시간 표시
	            "서버와 연결되었습니다.\n" + "IP : " + socket.getInetAddress() + sdf.format(System.currentTimeMillis()));

	      } catch (IOException e) {
	    	  //서버와 연결 실패시 연결 실패 문장 출력
	    	  System.out.println("서버와 연결을 실패했습니다.");
	    	  e.printStackTrace();
	      }
	   }

	   //그림, 닉네임, 채팅 수신
	   @Override
	   public void run() {
	      try {
	       
	         String Message;
	         String[] parsMessage;

	         int startX = 0;
	         int startY = 0;
	         int endX = 0;
	         int endY = 0;
	         Color SendedColor = Color.BLACK;
	         Color SendedColorMemory = Color.BLACK;
	         
	         while ((Message = br.readLine()) != null) {

	            parsMessage = Message.split(DELIMETER);
	            
	            switch (parsMessage[0]) {
	            case ID:
	               MyId = Integer.parseInt(parsMessage[1]);
	               break;
	            
	            case ALL_CONNECTED:
	               MyNickName = JOptionPane.showInputDialog("닉네임을 입력하세요");
	               NameLabelArr[MyId].setText(MyNickName + "(나)");  
	               writer.println("NICKNAME&" + MyNickName);  
	               break;
	            
	            case NICKNAME:
	               receiveNickname(parsMessage);
	               break;
	            
	            case CurP:
	               receiveCurP(parsMessage);
	               break;
	              
	            case WORD:
	               receiveWord(parsMessage);
	               break;
	            
	            case CORRECT:
	               receiveCorrect(parsMessage);
	               break;
	           
	            case RESULT:
	               receiveResult(parsMessage);
	               break;
	               
	            case DRAW:
	               if ("delete".equals(parsMessage[1])) {
	                  PaintPanel.repaint();
	                  break;
	               }
	               
	               String draw[] = parsMessage[1].split(SUB_DELIMETER);
	               graphic.setColor(SendedColor);
	               
	               if ("start".equals(draw[0])) {
	                  startX = Integer.parseInt(draw[1]);
	                  startY = Integer.parseInt(draw[2]);
	               }

	               if ("end".equals(draw[0])) {
	                  endX = Integer.parseInt(draw[1]);
	                  endY = Integer.parseInt(draw[2]);

	                  graphic.drawLine(startX, startY, endX, endY); 

	                  startX = endX;
	                  startY = endY;
	               }
	               break;
	               
	            case COLOR:
	               if ("red".equals(parsMessage[1])) {
	                  graphic.setColor(Color.RED);
	                  SendedColor = Color.RED;
	               } else if ("orange".equals(parsMessage[1])) {
	                  graphic.setColor(Color.ORANGE);
	                  SendedColor = Color.ORANGE;
	               } else if ("yellow".equals(parsMessage[1])) {
	                  graphic.setColor(Color.YELLOW);
	                  SendedColor = Color.YELLOW;
	               } else if ("green".equals(parsMessage[1])) {
	                  graphic.setColor(Color.GREEN);
	                  SendedColor = Color.GREEN;
	               } else if ("blue".equals(parsMessage[1])) {
	                  graphic.setColor(Color.BLUE);
	                  SendedColor = Color.BLUE;
	               } else if ("black".equals(parsMessage[1])) {
	                  graphic.setColor(Color.BLACK);
	                  SendedColor = Color.BLACK;
	               } else if ("white".equals(parsMessage[1])) {
	                  graphic.setColor(Color.WHITE);
	                  SendedColorMemory = SendedColor;
	                  SendedColor = Color.WHITE;
	               }
	               break;
	               
	            case THICKNESS:
	               graphic.setStroke(new BasicStroke(Integer.parseInt(parsMessage[1]), BasicStroke.CAP_ROUND, 0)); 
	               break;
	            	
	            case CHAT:
	               String chat[] = parsMessage[1].split(SUB_DELIMETER);
	               
	               if ("0".equals(chat[0])) {
	                  MessageTaArr[0].setText(chat[1]);
	               } else if ("1".equals(chat[0])) {
	                  MessageTaArr[1].setText(chat[1]);
	               } else if ("2".equals(chat[0])) {
	                  MessageTaArr[2].setText(chat[1]);
	               } else if ("3".equals(chat[0])) {
	                  MessageTaArr[3].setText(chat[1]);
	               }
	               break;
	               
	            case  CH:
	                String chatText = parsMessage[1];
	                newChatArea.append(chatText + "\n");
	                break;
	                
	            default:
	            }
	         }
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	   }
	   
	   //접속한 플레이어가 닉네임을 모두 입력하면 게임 시작
	   public void receiveNickname(String[] parsMessage) {
	      Nicknames = parsMessage[1].split(SUB_DELIMETER);

	       for (int i=0; i<PLAYER_COUNT; ++i) {
	          NameLabelArr[i].setText(Nicknames[i]);
	       }
	       NameLabelArr[MyId].setText(MyNickName + "(나)"); 
	       
	       int startReady = JOptionPane.showConfirmDialog(this, "게임을 시작합니다", "게임 시작 안내",
	             JOptionPane.DEFAULT_OPTION);

	       if (startReady == JOptionPane.OK_OPTION) {
	          writer.println("START_READY&");
	       }     
	   }

	   //출제자 표시, 몇 턴인지 표시, 제한시간 표시, 제시어 받기
	   public void receiveCurP(String[] parsMessage) {
	      TurnLabel.setText((TurnCount+1) + "/10 턴");  
	       TopLabel.setText("-");  
	       PaintPanel.repaint();  
	       TimerLabel.setText(SEC + " 초");  
	       
	       CurrentPlayerCount = Integer.parseInt(parsMessage[1]);
	       
	       int turnReady = JOptionPane.showConfirmDialog(this, Nicknames[CurrentPlayerCount] + "님 차례입니다", "출제자 안내",
	             JOptionPane.DEFAULT_OPTION);
	       
	       if (turnReady == JOptionPane.OK_OPTION) {
	          writer.println("TURN_READY&");
	       }
	      
	   }

	   //제시어 받기, 출제자가 아니라면 제시어가 뜨지 않음, 제시어 받으면 제한시간 줄어듦
	   public void receiveWord(String[] parsMessage) {
	      
	       if (MyId != CurrentPlayerCount) {
	          
	          MessageTf.setEditable(true);
	          MessageTaArr[MyId].setText("");

	          MessageTaArr[CurrentPlayerCount].setText(Nicknames[CurrentPlayerCount] + "님 차례입니다.");

	          CanDraw = false;
	       }

	       if (MyId == CurrentPlayerCount) {
	          
	          MessageTf.setEditable(false);
	          for (int i=0; i<PLAYER_COUNT; ++i) MessageTaArr[i].setText("");  
	          
	          CanDraw = true;

	          MessageTaArr[CurrentPlayerCount].setText("제시어 : " + parsMessage[1]);   
	       }
	       timer = new TimerThread(this);
	       timer.start();
	   }  	

	   //정답이 나오면 타이머 멈춤, 다음 출제자 알려주고 모두 확인하면 다음 턴 시작
	   //정답을 맞춘 플레이어는 2점, 출제자는 1점
	   public void receiveCorrect(String[] parsMessage) {
	      TurnCount++;  
	       timer.stopTimer();  
	       timer = null;
	       System.gc();
	       String correct[] = parsMessage[1].split(SUB_DELIMETER);
	       String correctNickname = Nicknames[Integer.parseInt(correct[0])];
	             int correctJop = JOptionPane.showConfirmDialog(this, "정답은 " + correct[1] + "입니다!\n" + correctNickname + "님 정답!", "정답 안내",
	             JOptionPane.DEFAULT_OPTION);

	       if (correctJop == JOptionPane.OK_OPTION) {
	          if (TurnCount != TURN_COUNT)
	             writer.println("TURN_END&");  
	       }
	       
	       Scores[Integer.parseInt(correct[0])] += 2;
	       Scores[CurrentPlayerCount] += 1;
	       
	       ScoreLabelArr[Integer.parseInt(correct[0])].setText("점수 : " + Scores[Integer.parseInt(correct[0])]);
	       ScoreLabelArr[CurrentPlayerCount].setText("점수 : " + Scores[CurrentPlayerCount]);

	       if (TurnCount == TURN_COUNT) {
	          writer.println("RESULT&");
	       }
	   }

	   //모든 턴이 끝나면 순위와 점수 안내
	   public void receiveResult(String[] parsMessage) {
	          String resultMessage = "==결과 발표==\n";

	          int[] rank = idxOfSorted(Scores);
	          Set<String> printedNicknames = new HashSet<>();
	          Set<Integer> usedRanks = new HashSet<>();

	          for (int i = 0; i < rank.length; ++i) {
	              int currentRank = i + 1;
	              int currentScore = Scores[rank[i]];
	              String currentNickname = Nicknames[rank[i]];

	              // 이미 출력한 닉네임이면 다음 등수로 건너뛰기
	              if (!printedNicknames.add(currentNickname)) {
	                  continue;
	              }

	              // 같은 점수인 경우 등수를 증가시키지 않고 그대로 유지
	              if (i > 0 && Scores[rank[i - 1]] == currentScore) {
	                  currentRank = i;
	              }

	              resultMessage += currentRank + "위 : " + currentNickname + " ---- " + currentScore + "점\n";
	          }

	          int result = JOptionPane.showConfirmDialog(this, resultMessage, "결과 안내", JOptionPane.DEFAULT_OPTION);

	          if (result == JOptionPane.OK_OPTION) {
	              setVisible(false);
	              writer.println("EXIT&");
	              System.exit(0);
	          }
	      }



	   //결과 창 순위 나타내는 메소드
	   public int[] idxOfSorted(int[] Scores) {
	          Integer[] indexes = new Integer[Scores.length];
	          for (int i = 0; i < indexes.length; i++) {
	              indexes[i] = i;
	          }

	          Arrays.sort(indexes, (i, j) -> Integer.compare(Scores[j], Scores[i]));

	          int[] idxOfSorted = new int[Scores.length];
	          for (int i = 0; i < idxOfSorted.length; i++) {
	              idxOfSorted[i] = indexes[i];
	          }

	          return idxOfSorted;
	      }
	   
	   @Override
	   public void keyPressed(KeyEvent e) {
	      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	        
	         MessageTaArr[MyId].setText(MessageTf.getText());

	         SendMessage = "CHAT&" + MyId + "," + MessageTf.getText();
	         writer.println(SendMessage);

	         MessageTf.setText(null);
	      }
	   }

	   //그림판 패널에서 마우스 클릭
	   @Override
	   public void mousePressed(MouseEvent e) {
	      
	      if (CanDraw == true) {
	              graphic.setColor(CurrentColor);
	              
	            startX = e.getX(); 
	            startY = e.getY(); 

	            // 서버로 전달
	            SendScore = "DRAW&" + "start," + startX + "," + startY;
	            writer.println(SendScore);
	            if (true) {
	               SendThickness = "THICKNESS&" + Thickness;
	               writer.println(SendThickness);
	            }
	      }
	   }

	   //그림판 패널에서 마우스 드래그
	   @Override
	   public void mouseDragged(MouseEvent e) {
	      
	      
	      if (CanDraw == true) {
	            endX = e.getX();

	            endY = e.getY();
	            
	            SendScore = "DRAW&" + "end," + endX + "," + endY;
	            writer.println(SendScore);

	            graphic.setStroke(new BasicStroke(Thickness, BasicStroke.CAP_ROUND, 0)); 
	            graphic.drawLine(startX, startY, endX, endY); 

	            startX = endX; 
	            startY = endY; 
	      }
	   }

	  
	   @Override
	   public void actionPerformed(ActionEvent e) {
	      //정답 전송 버튼 클릭한 경우
	      JButton jButton = (JButton) e.getSource();
	      if ("전송".equals(jButton.getText())) {
	         
	         MessageTaArr[MyId].setText(MessageTf.getText());

	         SendMessage = "CHAT&" + MyId + "," + MessageTf.getText();
	         writer.println(SendMessage);

	         MessageTf.setText(null);
	      }
	      
	      //채팅방 보내기 버튼 클릭한 경우
	      if ("보내기".equals(jButton.getText())) {

	          String newChatMessage = "CH&" + MyNickName + ": " + newMessageTf.getText();
	          writer.println(newChatMessage);

	          newMessageTf.setText(null);
	      }
	     
	      //연필 굵기, 지우개 굵기, 전체 삭제, 펜의 색깔 선택 하는 경우
	      if (e.getSource() == BigPencil) {
	        CurrentColor = CurrentColorMemory;
	         graphic.setColor(CurrentColor);
	         Thickness = 10; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         writer.println(SendThickness);
	      }
	      
	      if (e.getSource() == MediumPencil) {
	        CurrentColor = CurrentColorMemory;
	         graphic.setColor(CurrentColor);
	         Thickness = 5; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         writer.println(SendThickness);
	      }
	      
	      if (e.getSource() == SmallPencil) {
	        CurrentColor = CurrentColorMemory;
	         graphic.setColor(CurrentColor);
	         Thickness = 1; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         writer.println(SendThickness);
	      }
	     
	      if (e.getSource() == BigEraser) {
	        CurrentColorMemory = CurrentColor;
	         CurrentColor = Color.WHITE;
	         graphic.setColor(CurrentColor);
	         Thickness = 10; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         SendColor = "COLOR&white"; 
	         writer.println(SendThickness);
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == MediumEraser) {
	        CurrentColorMemory = CurrentColor;
	         CurrentColor = Color.WHITE;
	         graphic.setColor(CurrentColor);
	         Thickness = 5; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         SendColor = "COLOR&white"; 
	         writer.println(SendThickness);
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == SmallEraser) {
	        CurrentColorMemory = CurrentColor;
	         CurrentColor = Color.WHITE;
	         graphic.setColor(CurrentColor);
	         Thickness = 1; 
	         SendThickness = "THICKNESS&" + Thickness; 
	         SendColor = "COLOR&white"; 
	         writer.println(SendThickness);
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == ClearEraser) {
	         PaintPanel.repaint(); 
	         SendDraw = "DRAW&delete";
	         writer.println(SendDraw);
	      }
	      
	      if (e.getSource() == RedPen) {
	         CurrentColor = Color.RED;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&red";
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == OrangePen) {
	         CurrentColor = Color.ORANGE;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&orange";
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == YellowPen) {
	         CurrentColor = Color.YELLOW;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&yellow";
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == GreenPen) {
	         CurrentColor = Color.GREEN;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&green";
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == BluePen) {
	         CurrentColor = Color.BLUE;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&blue";
	         writer.println(SendColor);
	      }
	      
	      if (e.getSource() == BlackPen) {
	         CurrentColor = Color.BLACK;
	         graphic.setColor(CurrentColor);
	         SendColor = "COLOR&black";
	         writer.println(SendColor);
	      }
	   }

	   //창 종료 시 소켓 종료
	   @Override
	   public void windowClosed(WindowEvent e) {
	      try {
	         socket.close();
	         System.out.println("소켓 닫힘");
	      } catch (IOException e1) {
	         e1.printStackTrace();
	      }
	   }

	   @Override
	   public void keyTyped(KeyEvent e) {}
	   @Override
	   public void keyReleased(KeyEvent e) {}
	   @Override
	   public void mouseClicked(MouseEvent e) {}
	   @Override
	   public void mouseReleased(MouseEvent e) {}
	   @Override
	   public void mouseEntered(MouseEvent e) {}
	   @Override
	   public void mouseExited(MouseEvent e) {}
	   @Override
	   public void mouseMoved(MouseEvent e) {}
	   @Override
	   public void windowOpened(WindowEvent e) {}
	   @Override
	   public void windowClosing(WindowEvent e) {}
	   @Override
	   public void windowIconified(WindowEvent e) {}
	   @Override
	   public void windowDeiconified(WindowEvent e) {}
	   @Override
	   public void windowActivated(WindowEvent e) {}
	   @Override
	   public void windowDeactivated(WindowEvent e) {}

	   public static void main(String[] args) {
	      new CatchmindClient();
	   }
}