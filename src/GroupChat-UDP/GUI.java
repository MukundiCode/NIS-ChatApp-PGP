import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * GUI class
 */
public class GUI {

    static JPanel top;
    static String clientName;

    static JPanel upper;
    static JTextArea incomingMessages;
    static JScrollPane scrollPane;

    static JPanel lower;
    static JTextField clientID_Input;
    static JTextField textInput;
    static JButton button;

    static String hostnameGUI;
    static int portGUI;

    public GUI(String clientID, String host, int port) {

        // JPanel Container
        top = new JPanel(new GridLayout(2, 1));
        clientName = clientID;

        // Reciever - Upper Half
        upper = new JPanel();
        upper.setBackground(new java.awt.Color(28, 29, 31));
        upper.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        upper.setLayout(new GridLayout());

        incomingMessages = new JTextArea(5, 20);
        incomingMessages.setBackground(new java.awt.Color(39, 40, 43));
        incomingMessages.setForeground(Color.WHITE);
        incomingMessages.setLineWrap(true);
        incomingMessages.setEditable(false);
        scrollPane = new JScrollPane(incomingMessages);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        upper.add(scrollPane);

        // Input - Lower Half
        lower = new JPanel();
        lower.setBackground(new java.awt.Color(28, 29, 31));
        lower.setLayout(new GridLayout(3, 1));
        lower.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Java CLIENT ID text field
        clientID_Input = new JTextField("broadcast");
        clientID_Input.setEditable(false);
        clientID_Input.setBorder(BorderFactory.createCompoundBorder(clientID_Input.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        clientID_Input.setBackground(new java.awt.Color(39, 40, 43));
        clientID_Input.setForeground(Color.WHITE);
        clientID_Input.setCaretColor(Color.WHITE);

        // Add Focus Listener for effect
        clientID_Input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (clientID_Input.getText().equals("broadcast")) {
                    clientID_Input.setText("broadcast");
                    clientID_Input.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (clientID_Input.getText().isEmpty()) {
                    clientID_Input.setForeground(Color.WHITE);
                    clientID_Input.setText("broadcast");
                }
            }
        });

        // Java MESSAGE field
        textInput = new JTextField("Send a group chat message here.");
        textInput.setBorder(
                BorderFactory.createCompoundBorder(textInput.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textInput.setBackground(new java.awt.Color(39, 40, 43));
        textInput.setForeground(Color.WHITE);
        textInput.setCaretColor(Color.WHITE);

        // Add Focus Listener for effect
        textInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textInput.getText().equals("Send a group chat message here.")) {
                    textInput.setText("");
                    textInput.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textInput.getText().isEmpty()) {
                    textInput.setForeground(Color.WHITE);
                    textInput.setText("Send a group chat message here.");
                }
            }
        });

        // Java Button in bottom half
        JButton button = new JButton("SEND");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Is there a ClientID supplied?
                if (clientID_Input.getText() == "") {
                    // No client ID given
                    clientID_Input.setText("This is required to be set as 'broadcast'");
                }

                // ClientID is supplied
                else {
                    if (incomingMessages.getText().equals("")) {
                        incomingMessages.append(
                                "You messaged " + (clientID_Input.getText().compareTo("broadcast") == 0 ? "everyone"
                                        : clientID_Input.getText()) + ": " + textInput.getText());
                        Client.sendMessage(clientID_Input.getText(), textInput.getText());
                        textInput.setText("Send a group chat message here.");
                    }

                    else {
                        incomingMessages.append(
                                "\nYou messaged " + (clientID_Input.getText().compareTo("broadcast") == 0 ? "everyone"
                                        : clientID_Input.getText()) + ": " + textInput.getText());
                        Client.sendMessage(clientID_Input.getText(), textInput.getText());
                        textInput.setText("Send a group chat message here.");
                    }
                }
            }
        });

        // Add both fields and button to bottom half
        lower.add(clientID_Input);
        lower.add(textInput);
        lower.add(button);

        // Add to container
        top.add(upper);
        top.add(lower);

        // Java Swing Frame
        JFrame frame = new JFrame();
        frame.add(top);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to stop the application?", "Close App?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    Client.sendMessage("quit", "quit");
                    System.exit(0);
                }
            }
        });

        frame.pack();
        frame.setSize(500, 400);
        frame.setVisible(true);
        frame.setTitle(clientName);

    }

}