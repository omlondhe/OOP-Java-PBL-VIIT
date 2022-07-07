//package p1;

import java.io.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class FileOperation {
	Notepad npd;

	boolean saved;
	boolean newFileFlag;
	String fileName;
	String applicationTitle = "JavaPad";

	File fileRef;
	JFileChooser chooser;

	// Check if file is saved
	boolean isSave() {
		return saved;
	}

	// Change saved status
	void setSave(boolean saved) {
		this.saved = saved;
	}

	// Get the file name
	String getFileName() {
		return new String(fileName);
	}

	// Setting the file name
	void setFileName(String fileName) {
		this.fileName = new String(fileName);
	}

	// Constructor of the class
	FileOperation(Notepad npd) {
		this.npd = npd;

		saved = true;
		newFileFlag = true;
		fileName = new String("Untitled");
		fileRef = new File(fileName);
		this.npd.f.setTitle(fileName + " - " + applicationTitle);

		chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new MyFileFilter(".java", "Java Source Files(*.java)"));
		chooser.addChoosableFileFilter(new MyFileFilter(".txt", "Text Files(*.txt)"));
		chooser.setCurrentDirectory(new File("."));

	}

	// helper function for saving the file
	boolean saveFile(File temp) {
		FileWriter fout = null;
		try {
			fout = new FileWriter(temp);
			fout.write(npd.ta.getText());
		} catch (IOException ioe) {
			updateStatus(temp, false);
			return false;
		} finally {
			try {
				fout.close();
			} catch (IOException excp) {
			}
		}
		updateStatus(temp, true);
		return true;
	}

	// function for saving the file
	boolean saveThisFile() {
		if (!newFileFlag) {
			return saveFile(fileRef);
		}

		return saveAsFile();
	}

	// Saving the file as a new file when we have a file saved or not
	boolean saveAsFile() {
		File temp = null;
		chooser.setDialogTitle("Save As");
		chooser.setApproveButtonText("Save Now");
		chooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		chooser.setApproveButtonToolTipText("Click to save!");

		do {
			if (chooser.showSaveDialog(this.npd.f) != JFileChooser.APPROVE_OPTION)
				return false;
			temp = chooser.getSelectedFile();
			if (!temp.exists())
				break;
			if (JOptionPane.showConfirmDialog(
					this.npd.f, "<html>" + temp.getPath() + " already exists.<br>Do you want to replace it?<html>",
					"Save As", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				break;
		} while (true);

		return saveFile(temp);
	}

	// Function to open a file
	boolean openFile(File temp) {
		FileInputStream fin = null;
		BufferedReader din = null;

		try {
			fin = new FileInputStream(temp);
			din = new BufferedReader(new InputStreamReader(fin));
			String str = " ";
			while (str != null) {
				str = din.readLine();
				if (str == null)
					break;
				this.npd.ta.append(str + "\n");
			}

		} catch (IOException ioe) {
			updateStatus(temp, false);
			return false;
		} finally {
			try {
				din.close();
				fin.close();
			} catch (IOException excp) {
			}
		}
		updateStatus(temp, true);
		this.npd.ta.setCaretPosition(0);
		return true;
	}

	// Function to open a file
	void openFile() {
		if (!confirmSave())
			return;
		chooser.setDialogTitle("Open File...");
		chooser.setApproveButtonText("Open this");
		chooser.setApproveButtonMnemonic(KeyEvent.VK_O);
		chooser.setApproveButtonToolTipText("Click me to open the selected file.!");

		File temp = null;
		do {
			if (chooser.showOpenDialog(this.npd.f) != JFileChooser.APPROVE_OPTION)
				return;
			temp = chooser.getSelectedFile();
			if (temp.exists())
				break;
			JOptionPane.showMessageDialog(this.npd.f,
					"<html>" + temp.getName() + "<br>file not found.<br>" +
							"Please verify the correct file name was given.<html>",
					"Open", JOptionPane.INFORMATION_MESSAGE);

		} while (true);

		this.npd.ta.setText("");

		if (!openFile(temp)) {
			fileName = "Untitled";
			saved = true;
			this.npd.f.setTitle(fileName + " - " + applicationTitle);
		}
		if (!temp.canWrite())
			newFileFlag = true;
	}

	// Updating the status
	void updateStatus(File temp, boolean saved) {
		if (saved) {
			this.saved = true;
			fileName = new String(temp.getName());
			if (!temp.canWrite()) {
				fileName += "(Read only)";
				newFileFlag = true;
			}
			fileRef = temp;
			npd.f.setTitle(fileName + " - " + applicationTitle);
			npd.statusBar.setText("File : " + temp.getPath() + " saved/opened successfully.");
			newFileFlag = false;
		} else {
			npd.statusBar.setText("Failed to save/open : " + temp.getPath());
		}
	}

	// the save dialog for confirmation
	boolean confirmSave() {
		String strMsg = "<html>The text in the " + fileName + " file has been changed.<br>"
				+ "Do you want to save the changes?<html>";
		if (!saved) {
			int x = JOptionPane.showConfirmDialog(this.npd.f, strMsg, applicationTitle,
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (x == JOptionPane.CANCEL_OPTION)
				return false;
			if (x == JOptionPane.YES_OPTION && !saveAsFile())
				return false;
		}
		return true;
	}

	// creating a new file
	void newFile() {
		if (!confirmSave())
			return;

		this.npd.ta.setText("");
		fileName = new String("Untitled");
		fileRef = new File(fileName);
		saved = true;
		newFileFlag = true;
		this.npd.f.setTitle(fileName + " - " + applicationTitle);
	}
}

// A notepad class which is a main class to start the application
public class Notepad implements ActionListener, MenuConstants {
	JFrame f;
	JTextArea ta;
	JLabel statusBar;

	private String fileName = "Unsaved";
	String applicationName = "JavaPad";

	String searchString, replaceString;
	int lastSearchIndex;

	FileOperation fileHandler;
	FontChooser fontDialog = null;
	FindDialog findReplaceDialog = null;
	JColorChooser backgroundColorChooser = null;
	JColorChooser fColorChooser = null;
	JDialog backgroundDialog = null;
	JDialog foregroundDialog = null;
	JMenuItem cutItem, copyItem, deleteItem, findItem, findNextItem, replaceItem, gotoItem, selectAllItem;

	// Creating a constructor and initializing all the variables
	Notepad() {
		f = new JFrame(fileName + " - " + applicationName);
		f.setIconImage(new ImageIcon("icon.jpeg").getImage());
		ta = new JTextArea(30, 60);
		statusBar = new JLabel("Line no. 1 at Index 1", JLabel.CENTER);
		f.add(new JScrollPane(ta), BorderLayout.CENTER);
		f.add(statusBar, BorderLayout.SOUTH);
		f.add(new JLabel("  "), BorderLayout.EAST);
		f.add(new JLabel("  "), BorderLayout.WEST);
		// Creating Menu bar by calling the function to do so
		createMenuBar(f);
		// f.setSize(350,350);
		f.pack();
		f.setLocation(100, 50);
		f.setVisible(true);
		f.setLocation(150, 50);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// handling file operations
		fileHandler = new FileOperation(this);

		// adding a caret listener for the text area
		ta.addCaretListener(
				new CaretListener() {
					public void caretUpdate(CaretEvent e) {
						int lineNumber = 0, column = 0, pos = 0;

						try {
							pos = ta.getCaretPosition();
							lineNumber = ta.getLineOfOffset(pos);
							column = pos - ta.getLineStartOffset(lineNumber);
						} catch (Exception excp) {
						}
						if (ta.getText().length() == 0) {
							lineNumber = 0;
							column = 0;
						}
						statusBar.setText("Line no. " + (lineNumber + 1) + " at Index " + (column + 1));
					}
				});

		// creating a document listener for listening to changes in the document
		DocumentListener myListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void removeUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void insertUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}
		};
		// adding document listener to the text area
		ta.getDocument().addDocumentListener(myListener);

		// creating a window adapter
		WindowListener frameClose = new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (fileHandler.confirmSave())
					System.exit(0);
			}
		};
		// adding a window adapter to the frame
		f.addWindowListener(frameClose);
	}

	// Going to the specified line number
	void goTo() {
		int lineNumber = 0;
		try {
			lineNumber = ta.getLineOfOffset(ta.getCaretPosition()) + 1;
			String tempStr = JOptionPane.showInputDialog(f, "Enter Line Number:", "" + lineNumber);
			if (tempStr == null) {
				return;
			}
			lineNumber = Integer.parseInt(tempStr);
			ta.setCaretPosition(ta.getLineStartOffset(lineNumber - 1));
		} catch (Exception e) {
		}
	}

	// Handling actions performed
	public void actionPerformed(ActionEvent ev) {
		String cmdText = ev.getActionCommand();

		// New file handler
		if (cmdText.equals(fileNew))
			fileHandler.newFile();
		// Open file handler
		else if (cmdText.equals(fileOpen))
			fileHandler.openFile();
		// File save handler
		else if (cmdText.equals(fileSave))
			fileHandler.saveThisFile();
		// File Save as handler
		else if (cmdText.equals(fileSaveAs))
			fileHandler.saveAsFile();
		// Exit handler
		else if (cmdText.equals(fileExit)) {
			if (fileHandler.confirmSave())
				System.exit(0);
		}
		// File print dialog
		else if (cmdText.equals(filePrint))
			JOptionPane.showMessageDialog(
					Notepad.this.f,
					"Error occurred while printing!",
					"Printer error",
					JOptionPane.INFORMATION_MESSAGE);
		// Cut handler
		else if (cmdText.equals(editCut))
			ta.cut();
		// Copy handler
		else if (cmdText.equals(editCopy))
			ta.copy();
		// Paste handler
		else if (cmdText.equals(editPaste))
			ta.paste();
		// Delete handler
		else if (cmdText.equals(editDelete))
			ta.replaceSelection("");
		// Find text handler
		else if (cmdText.equals(editFind)) {
			if (Notepad.this.ta.getText().length() == 0)
				return;
			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, true);
		}
		// Find next text handler
		else if (cmdText.equals(editFindNext)) {
			if (Notepad.this.ta.getText().length() == 0)
				return;
			if (findReplaceDialog == null)
				statusBar.setText("Nothing to search for, use Find option of Edit Menu to proceed!");
			else
				findReplaceDialog.findNextWithSelection();
		}
		// Replace text handler
		else if (cmdText.equals(editReplace)) {
			if (Notepad.this.ta.getText().length() == 0)
				return;
			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, false);
		}
		// Goto text handler
		else if (cmdText.equals(editGoTo)) {
			if (Notepad.this.ta.getText().length() == 0)
				return;
			goTo();
		}
		// Select all text handler
		else if (cmdText.equals(editSelectAll))
			ta.selectAll();
		// Add date and time as text handler
		else if (cmdText.equals(editTimeDate))
			ta.insert(new Date().toString(), ta.getSelectionStart());
		// Word wrap handler
		else if (cmdText.equals(formatWordWrap)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			ta.setLineWrap(temp.isSelected());
		}
		// Font format handler
		else if (cmdText.equals(formatFont)) {
			if (fontDialog == null)
				fontDialog = new FontChooser(ta.getFont());
			if (fontDialog.showDialog(Notepad.this.f, "Choose a font"))
				Notepad.this.ta.setFont(fontDialog.createFont());
		}
		// Foreground edit handler
		else if (cmdText.equals(formatForeground))
			showForegroundColorDialog();
		// Background edit handler
		else if (cmdText.equals(formatBackground))
			showBackgroundColorDialog();
		// Shoe statusbar handler
		else if (cmdText.equals(viewStatusBar)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			statusBar.setVisible(temp.isSelected());
		}
		// Help click handler
		else if (cmdText.equals(helpAboutNotepad))
			JOptionPane.showMessageDialog(Notepad.this.f, aboutText, "About this JavaPad!",
					JOptionPane.INFORMATION_MESSAGE);
		// Handling else condition
		else
			statusBar.setText("This " + cmdText + " command is yet to be implemented");
	}

	// Background color picker dialog
	void showBackgroundColorDialog() {
		if (backgroundColorChooser == null)
			backgroundColorChooser = new JColorChooser();
		if (backgroundDialog == null)
			backgroundDialog = JColorChooser.createDialog(Notepad.this.f,
					formatBackground,
					false,
					backgroundColorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							Notepad.this.ta.setBackground(backgroundColorChooser.getColor());
						}
					},
					null);
		backgroundDialog.setVisible(true);
	}

	// Foreground color picker dialog
	void showForegroundColorDialog() {
		if (fColorChooser == null)
			fColorChooser = new JColorChooser();
		if (foregroundDialog == null)
			foregroundDialog = JColorChooser.createDialog(
					Notepad.this.f,
					formatForeground,
					false,
					fColorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							Notepad.this.ta.setForeground(fColorChooser.getColor());
						}
					},
					null);
		foregroundDialog.setVisible(true);
	}

	// Creating a Menu item without a key listener shortcut
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JMenuItem temp = new JMenuItem(s, key);
		temp.addActionListener(al);
		toMenu.add(temp);
		return temp;
	}

	// Creating a Menu item with a key listener shortcut to add it to Menu bar
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, int aclKey, ActionListener al) {
		JMenuItem menuItem = new JMenuItem(s, key);
		menuItem.addActionListener(al);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(aclKey, ActionEvent.CTRL_MASK));
		toMenu.add(menuItem);
		return menuItem;
	}

	// Creating a checkbox Menu item
	JCheckBoxMenuItem createCheckBoxMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(s);
		checkBoxMenuItem.setMnemonic(key);
		checkBoxMenuItem.addActionListener(al);
		checkBoxMenuItem.setSelected(false);
		toMenu.add(checkBoxMenuItem);

		return checkBoxMenuItem;
	}

	// Creating a Menu to add it to Menu bar
	JMenu createMenu(String s, int key, JMenuBar toMenuBar) {
		JMenu menu = new JMenu(s);
		menu.setMnemonic(key);
		toMenuBar.add(menu);
		return menu;
	}

	// Creating a Menu bar
	void createMenuBar(JFrame f) {
		JMenuBar mb = new JMenuBar();

		JMenu fileMenu = createMenu(fileText, KeyEvent.VK_F, mb);
		JMenu editMenu = createMenu(editText, KeyEvent.VK_E, mb);
		JMenu formatMenu = createMenu(formatText, KeyEvent.VK_O, mb);
		JMenu viewMenu = createMenu(viewText, KeyEvent.VK_V, mb);
		JMenu helpMenu = createMenu(helpText, KeyEvent.VK_H, mb);

		createMenuItem(fileNew, KeyEvent.VK_N, fileMenu, KeyEvent.VK_N, this);
		createMenuItem(fileOpen, KeyEvent.VK_O, fileMenu, KeyEvent.VK_O, this);
		createMenuItem(fileSave, KeyEvent.VK_S, fileMenu, KeyEvent.VK_S, this);
		createMenuItem(fileSaveAs, KeyEvent.VK_A, fileMenu, this);
		fileMenu.addSeparator();
		createMenuItem(filePrint, KeyEvent.VK_P, fileMenu, KeyEvent.VK_P, this);
		fileMenu.addSeparator();
		createMenuItem(fileExit, KeyEvent.VK_X, fileMenu, this);

		cutItem = createMenuItem(editCut, KeyEvent.VK_T, editMenu, KeyEvent.VK_X, this);
		copyItem = createMenuItem(editCopy, KeyEvent.VK_C, editMenu, KeyEvent.VK_C, this);
		createMenuItem(editPaste, KeyEvent.VK_P, editMenu, KeyEvent.VK_V, this);
		deleteItem = createMenuItem(editDelete, KeyEvent.VK_L, editMenu, this);
		deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu.addSeparator();
		findItem = createMenuItem(editFind, KeyEvent.VK_F, editMenu, KeyEvent.VK_F, this);
		findNextItem = createMenuItem(editFindNext, KeyEvent.VK_N, editMenu, this);
		findNextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		replaceItem = createMenuItem(editReplace, KeyEvent.VK_R, editMenu, KeyEvent.VK_H, this);
		gotoItem = createMenuItem(editGoTo, KeyEvent.VK_G, editMenu, KeyEvent.VK_G, this);
		editMenu.addSeparator();
		selectAllItem = createMenuItem(editSelectAll, KeyEvent.VK_A, editMenu, KeyEvent.VK_A, this);
		createMenuItem(editTimeDate, KeyEvent.VK_D, editMenu, this)
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		createCheckBoxMenuItem(formatWordWrap, KeyEvent.VK_W, formatMenu, this);

		createMenuItem(formatFont, KeyEvent.VK_F, formatMenu, this);
		formatMenu.addSeparator();
		createMenuItem(formatForeground, KeyEvent.VK_T, formatMenu, this);
		createMenuItem(formatBackground, KeyEvent.VK_P, formatMenu, this);

		createCheckBoxMenuItem(viewStatusBar, KeyEvent.VK_S, viewMenu, this).setSelected(true);

		LookAndFeelMenu.createLookAndFeelMenuItem(viewMenu, this.f);

		createMenuItem(helpAboutNotepad, KeyEvent.VK_A, helpMenu, this);

		MenuListener editMenuListener = new MenuListener() {
			public void menuSelected(MenuEvent event) {
				boolean isTextPresent = Notepad.this.ta.getText().length() != 0;
				findItem.setEnabled(isTextPresent);
				findNextItem.setEnabled(isTextPresent);
				replaceItem.setEnabled(isTextPresent);
				selectAllItem.setEnabled(isTextPresent);
				gotoItem.setEnabled(isTextPresent);
				boolean isTextSelected = Notepad.this.ta.getSelectionStart() != ta.getSelectionEnd();
				cutItem.setEnabled(isTextSelected);
				copyItem.setEnabled(isTextSelected);
				deleteItem.setEnabled(isTextSelected);
			}

			public void menuDeselected(MenuEvent event) {
			}

			public void menuCanceled(MenuEvent event) {
			}
		};
		editMenu.addMenuListener(editMenuListener);
		f.setJMenuBar(mb);
	}

	// The main route of the application
	public static void main(String[] s) {
		new Notepad();
	}
}

// Menubar constants that are used
interface MenuConstants {
	final String fileText = "File";
	final String editText = "Edit";
	final String formatText = "Format";
	final String viewText = "View";
	final String helpText = "Other";

	final String fileNew = "New";
	final String fileOpen = "Open";
	final String fileSave = "Save";
	final String fileSaveAs = "Save As";
	final String filePrint = "Print";
	final String fileExit = "Exit";

	final String editCut = "Cut";
	final String editCopy = "Copy";
	final String editPaste = "Paste";
	final String editDelete = "Delete";
	final String editFind = "Find";
	final String editFindNext = "Find Next";
	final String editReplace = "Replace";
	final String editGoTo = "Go To";
	final String editSelectAll = "Select All";
	final String editTimeDate = "Time/Date";

	final String formatWordWrap = "Word Wrap";
	final String formatFont = "Font";
	final String formatForeground = "Set Text color";
	final String formatBackground = "Set Pad color";

	final String viewStatusBar = "Status Bar";

	final String helpAboutNotepad = "About Javapad";

	final String aboutText = "<html><big>JavaPad</big><hr>"
			+ "<p align=left>A multi-platform Notepad!"
			+ "<hr><p align=left>Developed by:<br><br>"
			+ "<strong>221092 22120276 Om Prashant Londhe</strong><br>"
			+ "<strong>221089 22120252 Somesh Bhandarkar</strong><br>"
			+ "<strong>221086 22120232 Vishnu Jadhav</strong><br>"
			+ "<strong>221085 22120231 Mayur Bahiram</strong><br>"
			+ "Guided by Prof. Nitin Sakhare Sir<p align=center>"
			+ "<html>";
}

// java -Xdock:icon=icon.jpeg Notepad
