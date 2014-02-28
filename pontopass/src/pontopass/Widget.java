package pontopass;

import pontopass.Auth;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Widget extends JFrame {
	JPanel panel;
	JLabel msg;
	
	public Widget()
	{
		super("Pontopass");
	}
	
	public void createFrame(String apiId, String apiKey, String username, String IP, String userAgent)
	{		
		panel = new JPanel();
		String[] methodDesc = {"Receber ligação em ", "Receber código de confirmação por SMS em ",
								"Fornecer código gerado pelo Token Mobile em ", "Receber confirmação pelo aplicativo mobile em "};  
		final Auth pontopassAuth = new Auth(apiId,apiKey);
		boolean state = false;
		String msg = "";
		final int click = 0;
		state = pontopassAuth.init(username, false, false, IP, userAgent);
		final JSONArray deviceList = pontopassAuth.listMethods();
		if (!state)
			msg = "Erro ao inicializar";
		JLabel headerList = new JLabel("Selecione o método de autenticação desejado");
		final JButton[] methodButtons = new JButton[deviceList.size()];
		JSONObject actualMethod = new JSONObject();
		int i = 0;
		panel.add(headerList);
		panel.add(Box.createRigidArea(new Dimension(5,0)));
		while (i < deviceList.size()){
			actualMethod = (JSONObject) deviceList.get(i);
			final int id = Integer.parseInt(String.valueOf(actualMethod.get("id")));
			int type = Integer.parseInt(String.valueOf(actualMethod.get("token_type"))) - 1;
			String btnText =  methodDesc[type] + actualMethod.get("description");
			if ((type == 0) || (type == 1)){
				btnText = btnText + " (" + actualMethod.get("number") + ")";
			}
			methodButtons[i] = new JButton();
			methodButtons[i].setText(btnText);
			methodButtons[i].setPreferredSize(new Dimension(500,30));
			methodButtons[i].addActionListener(new ActionListener() {     	 
	            public void actionPerformed(ActionEvent e)
	            {	
	            	panel.setEnabled(false);
	            	boolean loginAttempt = pontopassAuth.ask(id);
	            	if (!loginAttempt){
	            		JOptionPane.showMessageDialog(null, "Erro na inicialização do método");
	            	}
	            	//click = 1;
	            }
	        });    
			panel.add(methodButtons[i]);
			panel.add(Box.createRigidArea(new Dimension(5,0)));
			i++;
		}
		panel.setPreferredSize(new Dimension(550,170));
		getContentPane().add(panel, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		pack();
		setVisible(true);
	}
	
	public static void main(String args[])
	{
		Widget s = new Widget();
		String api_id = "b4d544083922ee27a6ab92386fe543fe9df1c2ae521e43b1b718ad05b39a7440";
		String api_key = "967bfa05d2d98f0c519521c3433f0ff4bd2802e778970015e83af4ddea7b1bb6";
		String user = "joao";
		String IP = "123.123.123.123";
		String agent = "firefox";
		s.createFrame(api_id,api_key,user,IP,agent);

	}
}
