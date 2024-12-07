package catchmind;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public abstract class CatchmindGUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener, WindowListener, KeyListener {

	private Graphics g;
	protected Graphics2D graphic;

	protected JPanel PaintPanel;
	protected JPanel InfoPanel;

	protected JButton BigPencil;
	protected JButton MediumPencil;
	protected JButton SmallPencil;

	protected JButton BigEraser;
	protected JButton MediumEraser;
	protected JButton SmallEraser;

	protected JButton ClearEraser;

	protected JButton RedPen;
	protected JButton OrangePen;
	protected JButton YellowPen;
	protected JButton GreenPen;
	protected JButton BluePen;
	protected JButton BlackPen;

	protected int Thickness = 5;
	protected int startX;
	protected int startY;
	protected int endX;
	protected int endY;

	protected Color CurrentColor = Color.BLACK;
	protected Color CurrentColorMemory;

	JLabel[] NameLabelArr = new JLabel[4];
	JLabel[] ScoreLabelArr = new JLabel[4];
	JTextArea[] MessageTaArr = new JTextArea[4];

	JLabel TurnLabel;
	JLabel TopLabel;
	JLabel TimerLabel;

	JTextField MessageTf = new JTextField();
	
	JTextArea newChatArea = new JTextArea();
    JTextField newMessageTf = new JTextField();
    
    private ImageIcon newSendBtnIcon = new ImageIcon("./image/button.png");
    JButton newSendBtn = new JButton("보내기", newSendBtnIcon);
    
	private ImageIcon PlayerBackgroundIcon = new ImageIcon("./image/playerbackground.png");
	private ImageIcon ChatBackgroundIcon = new ImageIcon("./image/chatbackground.png");
	private Font BigFont = new Font("1훈솜사탕 Regular", Font.PLAIN, 24);
	private Font SmallFont = new Font("1훈솜사탕 Regular", Font.PLAIN, 16);
	
	
	//채팅창 패널
    public JPanel getNewChatPanel() {
        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.drawImage(ChatBackgroundIcon.getImage(), 10, 50, null);
                setOpaque(false);
                super.paintComponent(g);
            }
        };

        panel.setLayout(null);

        newChatArea.setEditable(false);
        newChatArea.setLineWrap(true);
        JScrollPane newScrollPane = new JScrollPane(newChatArea);
        newScrollPane.setBounds(30, 70, 260, 530);
        newScrollPane.setBorder(null);

        ImageIcon textFieldIcon = new ImageIcon("./image/textbackground.png");
        newMessageTf = new JTextField() {
	        @Override
	        protected void paintComponent(Graphics g) {
	            g.drawImage(textFieldIcon.getImage(), -20, 0, 250, getHeight(), null);
	            super.paintComponent(g);
	        }
	    };
	    newMessageTf.setBorder(null);
	    newMessageTf.setOpaque(false); // 배경 투명
	    newMessageTf.addKeyListener(this);
        newMessageTf.setBounds(30, 650, 180, 30);
        
        /*
        ImageIcon newSendButtonIcon = new ImageIcon("./image/button.png");
	    JButton newSendBtn = new JButton(newSendButtonIcon);
	    newSendBtn.setOpaque(false);
	    newSendBtn.setContentAreaFilled(false);
	    newSendBtn.setBorderPainted(false);
	    newSendBtn.addActionListener(this);	    
        newSendBtn.setBounds(210, 635, 80, 50);
        */
        
        newSendBtn.setHorizontalTextPosition(SwingConstants.CENTER); // 텍스트 위치 조정
        newSendBtn.setVerticalTextPosition(SwingConstants.BOTTOM); // 텍스트 위치 조정
        newSendBtn.setOpaque(false); 
        newSendBtn.setContentAreaFilled(false); // 내용 영역을 투명하게 설정
        newSendBtn.setBorderPainted(false);
        newSendBtn.setFocusPainted(false);

        newSendBtn.setBounds(210, 645, 80, 60);
        newSendBtn.addActionListener(this);

        panel.add(newScrollPane);
        panel.add(newMessageTf);
        panel.add(newSendBtn);

        return panel;
    }

    //플레이어 닉네임, 점수, 입력한 답 반환
	public JPanel getChatPanel(int idx) {

		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(PlayerBackgroundIcon.getImage(), 10, 10, 280, 160, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};

		JPanel taPanel = new JPanel();

		panel.setLayout(null);

		NameLabelArr[idx] = new JLabel("Player" + (idx + 1));
		NameLabelArr[idx].setFont(SmallFont);
		NameLabelArr[idx].setBounds(45, 80, 100, 30);

		ScoreLabelArr[idx] = new JLabel("점수 : 0");
		ScoreLabelArr[idx].setFont(SmallFont);
		ScoreLabelArr[idx].setBounds(45, 110, 100, 20);

		taPanel.setOpaque(false);
		taPanel.setBounds(130, 50, 150, 100);

		MessageTaArr[idx] = new JTextArea(5, 10);
		MessageTaArr[idx].setEditable(false);
		MessageTaArr[idx].setLineWrap(true);
		MessageTaArr[idx].setFont(SmallFont);
		MessageTaArr[idx].setOpaque(false);

		panel.add(NameLabelArr[idx]);
		panel.add(ScoreLabelArr[idx]);
		taPanel.add(MessageTaArr[idx]);
		panel.add(taPanel);

		return panel;
	}

	/*
	//정답패널 전송 버튼 클릭시 내용 전송
	public JPanel getChatInsertPanel() {
	    JPanel panel = new JPanel();

	    // 정답 입력란에 배경 이미지 적용
	    ImageIcon textFieldIcon = new ImageIcon("./image/textbackground.png");
	    MessageTf = new JTextField() {
	        @Override
	        protected void paintComponent(Graphics g) {
	            g.drawImage(textFieldIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
	            super.paintComponent(g);
	        }
	    };
	    MessageTf.setOpaque(false); // 배경 투명
	    MessageTf.addKeyListener(this);

	    // 전송 버튼에 배경 이미지 적용
	    ImageIcon sendButtonIcon = new ImageIcon("./image/button.png");
	    JButton sendBtn = new JButton(sendButtonIcon);
	    sendBtn.setOpaque(false);
	    sendBtn.setContentAreaFilled(false);
	    sendBtn.setBorderPainted(false);
	    sendBtn.addActionListener(this);

	    // 레이아웃 설정
	    panel.setLayout(new BorderLayout());
	    panel.add(MessageTf, BorderLayout.CENTER);
	    panel.add(sendBtn, BorderLayout.EAST);

	    return panel;
	}
	*/

	//턴, 제한시간 상태 표시 패널
	public JPanel getStatusBarPanel() {
	    JPanel panel = new JPanel();
	    panel.setOpaque(false);
	    panel.setLayout(null);

	    // TurnLabel에 배경 이미지 추가
	    ImageIcon turnBackgroundIcon = new ImageIcon("./image/orangeimg.png");
	    TurnLabel = new JLabel("-/10 턴") {
	        @Override
	        protected void paintComponent(Graphics g) {
	            g.drawImage(turnBackgroundIcon.getImage(), 10, 0, 80, 60, null);
	            super.paintComponent(g);
	        }
	    };
	    TurnLabel.setFont(SmallFont);
	    TurnLabel.setHorizontalAlignment(JLabel.CENTER); // 텍스트 가운데 정렬
	    TurnLabel.setBounds(30, 10, 100, 60); // 크기를 배경 이미지에 맞게 조정
	    TurnLabel.setOpaque(false);

	    // TimerLabel에 배경 이미지 추가
	    ImageIcon timerBackgroundIcon = new ImageIcon("./image/orangeimg.png");
	    TimerLabel = new JLabel("30 초") {
	        @Override
	        protected void paintComponent(Graphics g) {
	            g.drawImage(timerBackgroundIcon.getImage(), 10, 0, 80, 60, null);
	            super.paintComponent(g);
	        }
	    };
	    TimerLabel.setFont(SmallFont);
	    TimerLabel.setHorizontalAlignment(JLabel.CENTER); // 텍스트 가운데 정렬
	    TimerLabel.setBounds(450, 10, 100, 60); // 크기를 배경 이미지에 맞게 조정
	    TimerLabel.setOpaque(false);

	    // 중앙의 TopLabel
	    ImageIcon titleBackgroundIcon = new ImageIcon("./image/title.png");
	    TopLabel = new JLabel() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(titleBackgroundIcon.getImage(), 50, 0, 180, 50, null);
	    		super.paintComponent(g);
	    	}
	    };
	    TopLabel.setBounds(150, 20, 300, 50);

	    panel.add(TurnLabel);
	    panel.add(TopLabel);
	    panel.add(TimerLabel);

	    return panel;
	}


	//도구 패널
	//펜의 굵기 선택 가능(대,중,소)
	//지우개의 굵기 선택 가능(대,중,소)
	//그림판 전체 지우기 가능
	//펜의 색상 선택 가능(빨,주,노,초,파,검)
	public JPanel getInfoPanel() {

		JPanel leftPanel1 = new JPanel();
		leftPanel1.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "연필", TitledBorder.CENTER, TitledBorder.TOP));
		leftPanel1.setLayout(new GridLayout(1, 3));
		leftPanel1.setOpaque(false);
		
		ImageIcon bigpenIcon = new ImageIcon("./image/lsize.png");
		ImageIcon mediumpenIcon = new ImageIcon("./image/msize.png");
		ImageIcon smallpenIcon = new ImageIcon("./image/ssize.png");
		
		BigPencil = new JButton(bigpenIcon);
		BigPencil.setOpaque(false);  
		BigPencil.setContentAreaFilled(false); 
		BigPencil.setBorderPainted(false);
		BigPencil.setFocusPainted(false);
		
		MediumPencil = new JButton(mediumpenIcon);
		MediumPencil.setOpaque(false);  
		MediumPencil.setContentAreaFilled(false); 
		MediumPencil.setBorderPainted(false);
		MediumPencil.setFocusPainted(false);
		
		SmallPencil = new JButton(smallpenIcon);
		SmallPencil.setOpaque(false);  
		SmallPencil.setContentAreaFilled(false);  
		SmallPencil.setBorderPainted(false);
		SmallPencil.setFocusPainted(false);

		BigPencil.addActionListener(this);
		MediumPencil.addActionListener(this);
		SmallPencil.addActionListener(this);

		leftPanel1.add(BigPencil);
		leftPanel1.add(MediumPencil);
		leftPanel1.add(SmallPencil);

		JPanel leftPanel2 = new JPanel();
		leftPanel2.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "지우개", TitledBorder.CENTER, TitledBorder.TOP));
		leftPanel2.setLayout(new GridLayout(1, 3));
		leftPanel2.setOpaque(false);
		
		ImageIcon bigeraserIcon = new ImageIcon("./image/lsize.png");
		ImageIcon mediumeaserIcon = new ImageIcon("./image/msize.png");
		ImageIcon smalleraserIcon = new ImageIcon("./image/ssize.png");


		BigEraser = new JButton(bigeraserIcon);
		BigEraser.setOpaque(false);  
		BigEraser.setContentAreaFilled(false);  
		BigEraser.setBorderPainted(false);
		BigEraser.setFocusPainted(false);
		
		MediumEraser = new JButton(mediumeaserIcon);
		MediumEraser.setOpaque(false);  
		MediumEraser.setContentAreaFilled(false);  
		MediumEraser.setBorderPainted(false);
		MediumEraser.setFocusPainted(false);
		
		SmallEraser = new JButton(smalleraserIcon);
		SmallEraser.setOpaque(false);  
		SmallEraser.setContentAreaFilled(false);  
		SmallEraser.setBorderPainted(false);
		SmallEraser.setFocusPainted(false);

		BigEraser.addActionListener(this);
		MediumEraser.addActionListener(this);
		SmallEraser.addActionListener(this);

		leftPanel2.add(BigEraser);
		leftPanel2.add(MediumEraser);
		leftPanel2.add(SmallEraser);

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "전체삭제", TitledBorder.CENTER, TitledBorder.TOP));
		centerPanel.setOpaque(false);
		ImageIcon clearIcon = new ImageIcon("./image/allclear.png");
		ClearEraser = new JButton(clearIcon);
		ClearEraser.setOpaque(false); 
		ClearEraser.setContentAreaFilled(false);  
		ClearEraser.setBorderPainted(false);
		ClearEraser.setFocusPainted(false);
		ClearEraser.addActionListener(this);
		centerPanel.add(ClearEraser);

		ImageIcon ColorBackgroundIcon = new ImageIcon("./image/colorboard.png");
		JPanel rightPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(ColorBackgroundIcon.getImage(), 0, 10, 185, 140, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		rightPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "색상선택", TitledBorder.CENTER, TitledBorder.TOP));
		rightPanel.setLayout(new GridLayout(2, 3));
		rightPanel.setOpaque(false);
		
		ImageIcon redIcon = new ImageIcon("./image/redcolor.png");
		ImageIcon orangeIcon = new ImageIcon("./image/orangecolor.png");
		ImageIcon yellowIcon = new ImageIcon("./image/yellowcolor.png");
		ImageIcon greenIcon = new ImageIcon("./image/greencolor.png");
		ImageIcon blueIcon = new ImageIcon("./image/bluecolor.png");
		ImageIcon blackIcon = new ImageIcon("./image/blackcolor.png");
		
	    RedPen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(redIcon.getImage(), 15, 20, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    RedPen.setBounds(150, 20, 40, 40);		
		RedPen.setOpaque(false);  
		RedPen.setContentAreaFilled(false); 
		RedPen.setBorderPainted(false);

		OrangePen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(orangeIcon.getImage(), 10, 10, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    OrangePen.setBounds(150, 20, 40, 40);
		OrangePen.setOpaque(false);  
		OrangePen.setContentAreaFilled(false);  
		OrangePen.setBorderPainted(false); 
		
		YellowPen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(yellowIcon.getImage(), 0, 25, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    YellowPen.setBounds(150, 20, 40, 40);
		YellowPen.setOpaque(false);  
		YellowPen.setContentAreaFilled(false);  
		YellowPen.setBorderPainted(false);  
		
		GreenPen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(greenIcon.getImage(), 20, 0, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    GreenPen.setBounds(150, 20, 40, 40);
		GreenPen.setOpaque(false); 
		GreenPen.setContentAreaFilled(false);  
		GreenPen.setBorderPainted(false);  
		
		BluePen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(blueIcon.getImage(), 10, 12, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    BluePen.setBounds(150, 20, 40, 40);
		BluePen.setOpaque(false);  
		BluePen.setContentAreaFilled(false);  
		BluePen.setBorderPainted(false); 
		
		BlackPen = new JButton() {
	    	@Override
	    	protected void paintComponent(Graphics g) {
	    		g.drawImage(blackIcon.getImage(), 0, 5, 40, 40, null);
	    		super.paintComponent(g);
	    	}
	    };
	    BlackPen.setBounds(150, 20, 40, 40);
		BlackPen.setOpaque(false);  
		BlackPen.setContentAreaFilled(false);  
		BlackPen.setBorderPainted(false);  

		RedPen.addActionListener(this);
		OrangePen.addActionListener(this);
		YellowPen.addActionListener(this);
		GreenPen.addActionListener(this);
		BluePen.addActionListener(this);
		BlackPen.addActionListener(this);

		rightPanel.add(RedPen);
		rightPanel.add(OrangePen);
		rightPanel.add(YellowPen);
		rightPanel.add(GreenPen);
		rightPanel.add(BluePen);
		rightPanel.add(BlackPen);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(2, 1));
		leftPanel.setOpaque(false);
		leftPanel.add(leftPanel1);
		leftPanel.add(leftPanel2);

		InfoPanel = new JPanel();
		InfoPanel.setLayout(new GridLayout(1, 3));
		InfoPanel.setOpaque(false);
		InfoPanel.add(leftPanel);
		InfoPanel.add(centerPanel);
		InfoPanel.add(rightPanel);

		return InfoPanel;
	}

	//그림판 패널
	public JPanel getPaintPanel() {
	    PaintPanel = new JPanel() {
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            ImageIcon backgroundIcon = new ImageIcon("./image/drawingboardimg.png");
	            g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
	        }
	    };

	    PaintPanel.setBackground(Color.WHITE); // 기본 배경 색 설정 (배경 이미지가 없다면 기본 배경 색 사용)
	    PaintPanel.setPreferredSize(new Dimension(400, 600));
	    PaintPanel.addMouseListener(this);
	    PaintPanel.addMouseMotionListener(this);

	    return PaintPanel;
	}


	//위치 설정
	public CatchmindGUI() {

		JPanel Player1 = getChatPanel(0);
		Player1.setBounds(0, 0, 300, 167);

		JPanel Player2 = getChatPanel(1);
		Player2.setBounds(0, 167, 300, 167);

		JPanel Player3 = getChatPanel(2);
		Player3.setBounds(0, 334, 300, 167);

		JPanel Player4 = getChatPanel(3);
		Player4.setBounds(0, 501, 300, 167);

		//JPanel Answer = getChatInsertPanel();
		//Answer.setBounds(0, 668, 300, 32);

		JPanel TopBar = getStatusBarPanel();
		TopBar.setBounds(300, 0, 600, 70);

		PaintPanel = getPaintPanel();
		JPanel infoPanel = getInfoPanel();
		PaintPanel.setBounds(320, 80, 560, 440);
		infoPanel.setBounds(320, 530, 560, 150);
		
		JPanel newChatPanel = getNewChatPanel();
        newChatPanel.setBounds(880, 0, 300, 700);

		setTitle("캐치마인드");
		setSize(new Dimension(1200, 735));
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		getContentPane().setBackground(new Color(255, 230, 170));

		setLayout(null);
		add(Player1);
		add(Player2);
		add(Player3);
		add(Player4);
		//add(Answer);
		add(TopBar);
		add(PaintPanel);
		add(infoPanel);
		add(newChatPanel);

		g = PaintPanel.getGraphics();
		graphic = (Graphics2D) g;

	}
}