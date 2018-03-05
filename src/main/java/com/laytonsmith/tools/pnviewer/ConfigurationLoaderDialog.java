package com.laytonsmith.tools.pnviewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 *
 */
public class ConfigurationLoaderDialog extends javax.swing.JDialog {

	private LoaderDialogFinish finishAction = null;

	/**
	 * Creates new form ConfigurationLoaderDialog
	 */
	public ConfigurationLoaderDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle("Load configuration...");
		setRemoteEnabled(false);
		ActionListener radioButtonChangedActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isLocal = false;
				if(localOrRemoteGroup.getSelection() == localRadioButton.getModel()) {
					isLocal = true;
				}
				setLocalEnabled(isLocal);
				setRemoteEnabled(!isLocal);
			}
		};
		localRadioButton.addActionListener(radioButtonChangedActionListener);
		remoteRadioButton.addActionListener(radioButtonChangedActionListener);

		loadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(validateFields()) {
					ConfigurationLoaderDialog.this.setVisible(false);
					if(finishAction != null) {
						if(localOrRemoteGroup.isSelected(localRadioButton.getModel())) {
							finishAction.data(true, localFileField.getText(), "", 1, "", "");
						} else {
							finishAction.data(false, "", hostField.getText(),
									Integer.parseInt(portField.getText()),
									new String(passwordField.getPassword()), remoteFileField.getText());
						}
					}
				} else {
					JOptionPane.showMessageDialog(ConfigurationLoaderDialog.this, getValidationError(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigurationLoaderDialog.this.setVisible(false);
			}
		});
		registerShowFileLoader(localFileField, browseLocalFileButton);
		registerEnterHandler(loadButton, localFileField);
		registerEnterHandler(loadButton, hostField, portField, passwordField, remoteFileField);
	}

	/**
	 * Registers and handles a file chooser field/button combo.
	 *
	 * @param toPopulate
	 * @param browseButton
	 */
	private void registerShowFileLoader(final JTextField toPopulate, JButton browseButton) {
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(ConfigurationLoaderDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					toPopulate.setText(file.getAbsolutePath());
				}
			}
		});
	}

	/**
	 * Registers a button to be clicked when the user presses enter in one of the given fields.
	 *
	 * @param button
	 * @param fields
	 */
	private void registerEnterHandler(final JButton button, JTextField... fields) {
		for(JTextField field : fields) {
			field.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					button.doClick();
				}
			});
		}
	}

	private boolean validateFields() {
		return getValidationError() == null;
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	private String getValidationError() {
		if(localOrRemoteGroup.isSelected(localRadioButton.getModel())) {
			String localFile = localFileField.getText();
			if(!new File(localFile).exists()) {
				return "File specified doesn't exist.";
			}
		} else {
			String host = hostField.getText();
			String sport = portField.getText();
			String remote = remoteFileField.getText().trim();
			try {
				new URI(host);
			} catch(URISyntaxException ex) {
				return ex.getMessage();
			}
			int port;
			try {
				port = Integer.parseInt(sport);
			} catch(NumberFormatException ex) {
				return "Port must be a number.";
			}
			if(port < 1 || port > 65535) {
				return "Port must be between 1 and 65535";
			}
			if("".equals(remote)) {
				return "No remote file specified.";
			}
		}
		return null;
	}

	private void setLocalEnabled(boolean state) {
		localFileField.setEnabled(state);
		browseLocalFileButton.setEnabled(state);
	}

	private void setRemoteEnabled(boolean state) {
		hostLabel.setEnabled(state);
		hostField.setEnabled(state);
		passwordLabel.setEnabled(state);
		passwordField.setEnabled(state);
		portLabel.setEnabled(state);
		portField.setEnabled(state);
		remoteFileLabel.setEnabled(state);
		remoteFileField.setEnabled(state);
	}

	public void setLoaderDialogFinish(LoaderDialogFinish finish) {
		this.finishAction = finish;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        localOrRemoteGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        localRadioButton = new javax.swing.JRadioButton();
        localFileField = new javax.swing.JTextField();
        browseLocalFileButton = new javax.swing.JButton();
        remoteRadioButton = new javax.swing.JRadioButton();
        hostLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        loadButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        remoteFileLabel = new javax.swing.JLabel();
        remoteFileField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Load Configuration from:");

        localOrRemoteGroup.add(localRadioButton);
        localRadioButton.setSelected(true);
        localRadioButton.setText("Local");

        browseLocalFileButton.setText("Browse...");

        localOrRemoteGroup.add(remoteRadioButton);
        remoteRadioButton.setText("Remote");

        hostLabel.setText("Host:");

        passwordLabel.setText("Password:");

        loadButton.setText("Load");

        cancelButton.setText("Cancel");

        portLabel.setText("Port:");

        remoteFileLabel.setText("Remote File:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(localFileField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseLocalFileButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hostLabel)
                            .addComponent(portLabel))
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(portField)
                            .addComponent(hostField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passwordLabel)
                                .addGap(30, 30, 30))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(remoteFileLabel)
                                .addGap(18, 18, 18)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remoteFileField)
                            .addComponent(passwordField)))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(localRadioButton)
                            .addComponent(jLabel1)
                            .addComponent(remoteRadioButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(loadButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)))
                        .addGap(0, 236, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(localRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseLocalFileButton))
                .addGap(18, 18, 18)
                .addComponent(remoteRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remoteFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remoteFileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ConfigurationLoaderDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>
		
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ConfigurationLoaderDialog dialog = new ConfigurationLoaderDialog(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	public interface LoaderDialogFinish {

		/**
		 * Called when the data in the dialog is submitted.
		 *
		 * @param isLocal If the selection is local. If true, username, password, privateKey, knownHosts, and remoteFile
		 * can be ignored. If false, localPath can be ignored.
		 * @param localPath The local path to the configuration file, if isLocal is true
		 * @param host The remote host to connect to
		 * @param port The remote port to connect to
		 * @param password The password for the remote connection
		 * @param remoteFile The path to the configuration file on the remote, if isLocal is false
		 */
		void data(boolean isLocal, String localPath, String host, int port, String password, String remoteFile);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLocalFileButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton loadButton;
    private javax.swing.JTextField localFileField;
    private javax.swing.ButtonGroup localOrRemoteGroup;
    private javax.swing.JRadioButton localRadioButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField remoteFileField;
    private javax.swing.JLabel remoteFileLabel;
    private javax.swing.JRadioButton remoteRadioButton;
    // End of variables declaration//GEN-END:variables
}
