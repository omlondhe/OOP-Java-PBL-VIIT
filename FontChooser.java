//package p1;

// import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class FontDemo extends JFrame {
	FontChooser dialog = null;
	JTextArea ta;
	JButton fontButton;

	FontDemo() {
		super("Font");
		// creating a preview text area
		ta = new JTextArea(7, 20);
		fontButton = new JButton("Set Font");

		// creating the action listener
		ActionListener ac = new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (dialog == null)
					dialog = new FontChooser(ta.getFont());
				if (dialog.showDialog(FontDemo.this, "Choose a font")) {
					FontDemo.this.ta.setFont(dialog.createFont());
				}
			}
		};
		fontButton.addActionListener(ac);

		add(ta, BorderLayout.CENTER);
		add(fontButton, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 400, 400);
		ta.append("This is how your font will look like.");
		ta.append("\n\n A quick brown fox jumps over the lazy dog.");
		ta.append("\n\n0123456789");
		ta.append("\n~!@#$%^&*()_+|?><");
		setVisible(true);
	}

	// A main function
	public static void main(String[] args) {
		new FontDemo();
	}
}

// creating a font chooser
public class FontChooser extends JPanel // implements ActionListener
{
	private Font thisFont;
	private JList<String> jFace, jStyle, jSize;
	private JDialog dialog;
	private JButton okButton;
	JTextArea tf;
	private boolean ok;

	public FontChooser(Font withFont) {
		thisFont = withFont;

		// getting the font names
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		jFace = new JList<>(fontNames);
		jFace.setSelectedIndex(0);

		// action listener for selection font names
		jFace.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				tf.setFont(createFont());
			}
		});

		// defining the font sizes
		String[] fontStyles = { "Regular", "Italic", "Bold", "Bold Italic" };
		jStyle = new JList<>(fontStyles);
		jStyle.setSelectedIndex(0);

		// action listener for selection font styles
		jStyle.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				tf.setFont(createFont());
			}
		});

		// defining the font sizes
		String[] fontSizes = new String[30];
		for (int j = 0; j < 30; j++)
			fontSizes[j] = new String(10 + j * 2 + "");
		jSize = new JList<>(fontSizes);
		jSize.setSelectedIndex(0);

		// action listener for selection font sizes
		jSize.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				tf.setFont(createFont());
			}
		});

		// creating a jpanel
		JPanel jpLabel = new JPanel();
		// adding the grid layout to the jpanel
		jpLabel.setLayout(new GridLayout(1, 3));
		// adding labels for the select boxes
		jpLabel.add(new JLabel("Font", JLabel.CENTER));
		jpLabel.add(new JLabel("Font Style", JLabel.CENTER));
		jpLabel.add(new JLabel("Size", JLabel.CENTER));

		// creating a jpanel
		JPanel jpList = new JPanel();
		// adding the grid layout to the jpanel
		jpList.setLayout(new GridLayout(1, 3));
		// adding labels for the select boxes
		jpList.add(new JScrollPane(jFace));
		jpList.add(new JScrollPane(jStyle));
		jpList.add(new JScrollPane(jSize));

		// adding the button
		okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		// listener for the ok button
		okButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						ok = true;
						FontChooser.this.thisFont = FontChooser.this.createFont();
						dialog.setVisible(false);
					}
				});
		// listener for the calcel button
		cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						dialog.setVisible(false);
					}
				});

		// a panel for the label
		JPanel jpButton = new JPanel();
		jpButton.setLayout(new FlowLayout());
		jpButton.add(okButton);
		jpButton.add(new JLabel("          "));
		jpButton.add(cancelButton);
		// adding the text area
		tf = new JTextArea(5, 30);
		JPanel jpTextField = new JPanel();
		jpTextField.add(new JScrollPane(tf));
		// creating a center panel for the list and text field
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(2, 1));
		centerPanel.add(jpList);
		centerPanel.add(jpTextField);
		// adding all the panels to the window
		setLayout(new BorderLayout());
		add(jpLabel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(jpButton, BorderLayout.SOUTH);
		add(new JLabel("  "), BorderLayout.EAST);// dummy label
		add(new JLabel("  "), BorderLayout.WEST);// dummy label
		// adding the font data to the textarea
		tf.setFont(thisFont);
		tf.append("\nA quick brown fox jumps over the lazy dog.");
		tf.append("\n0123456789");
		tf.append("\n~!@#$%^&*()_+|?><\n");
	}

	// creating a font style
	public Font createFont() {
		// getting the font
		Font font = thisFont;
		// getting font style
		int fontStyle = Font.PLAIN;
		int x = jStyle.getSelectedIndex();

		switch (x) {
			case 0:
				fontStyle = Font.PLAIN;
				break;
			case 1:
				fontStyle = Font.ITALIC;
				break;
			case 2:
				fontStyle = Font.BOLD;
				break;
			case 3:
				fontStyle = Font.BOLD + Font.ITALIC;
				break;
		}

		// getting the font size
		int fontSize = Integer.parseInt((String) jSize.getSelectedValue());
		// getting the font name
		String fontName = (String) jFace.getSelectedValue();
		// creating a font object
		font = new Font(fontName, fontStyle, fontSize);
		return font;
	}

	// a function to show dialog
	public boolean showDialog(Component parent, String title) {
		ok = false;

		Frame owner = null;
		if (parent instanceof Frame)
			owner = (Frame) parent;
		else
			owner = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
		if (dialog == null || dialog.getOwner() != owner) {
			// creating a dialog
			dialog = new JDialog(owner, true);
			dialog.add(this);
			dialog.getRootPane().setDefaultButton(okButton);
			dialog.setSize(400, 325);
		}

		dialog.setTitle(title);
		dialog.setVisible(true);
		return ok;
	}
}