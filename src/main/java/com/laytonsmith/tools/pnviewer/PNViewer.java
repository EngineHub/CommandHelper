
package com.laytonsmith.tools.pnviewer;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Common.UIUtils;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 */


public class PNViewer extends javax.swing.JFrame {

	private Map<String, String> data;
	private Map<String, String> dataSources;

	/**
	 * Creates new form PNViewer
	 */
	public PNViewer() {
		initComponents();
		setTitle("Persistence Network Viewer");
		configurationFromLabel.setText("");
		namespaceLabel.setText("");
		keyLabel.setText("");
		valueTypeLabel.setText("");
		sourceLabel.setText("");
		setStatus("Waiting for configuration to be loaded...", false);
		loadFromConfigurationMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigurationLoaderDialog cld = new ConfigurationLoaderDialog(PNViewer.this, true);
				UIUtils.centerWindowOnWindow(cld, PNViewer.this);
				cld.setLoaderDialogFinish(new ConfigurationLoaderDialog.LoaderDialogFinish() {

					@Override
					public void data(boolean isLocal, String localPath, String username, String password, String privateKey, String knownHosts, String remoteFile) {
						if(isLocal){
							loadFromLocal(localPath);
						} else {
							loadFromRemote(username, password, privateKey, knownHosts, remoteFile);
						}
					}
				});
				cld.setVisible(true);
			}
		});
		keyTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				try {
					String[] key = new String[e.getNewLeadSelectionPath().getPathCount() - 1];
					for(int i = 1; i < e.getNewLeadSelectionPath().getPathCount(); i++){
						key[i - 1] = (String)((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getPathComponent(i)).getUserObject();
					}
					showData(join(key), data.get(join(key)));
				} catch(NullPointerException ex){
					// Ignore
				}
			}
		});

//		Map<String[], String> data = new HashMap<>();
//		Map<String, String> dataSources = new HashMap<>();
//		data.put(new String[]{"first"}, "\"1\"");
//		data.put(new String[]{"first", "first"}, "1");
//		data.put(new String[]{"first", "second"}, "1");
//		data.put(new String[]{"second", "first"}, "1");
//		for(String[] key : data.keySet()){
//			dataSources.put(join(key), "Test Source");
//		}
//		displayData(data, dataSources);
	}

	private String join(String[] key){
		return StringUtils.Join(key, ".");
	}

	private void showData(String key, String value){
		String[] split = key.split("\\.");
		String[] namespace = new String[split.length - 1];
		String keyPart = null;
		for(int i = 0; i < split.length; i++){
			if(i == split.length - 1){
				keyPart = split[i];
			} else {
				namespace[i] = split[i];
			}
		}
		namespaceLabel.setText(join(namespace));
		keyLabel.setText(keyPart);
		if(value == null){
			sourceLabel.setText("");
			valueTypeLabel.setText("(empty key)");
			valueTextArea.setText("");
		} else {
			sourceLabel.setText(dataSources.get(key));
			Construct c = CNull.NULL;
			try {
				c = Construct.json_decode(value, Target.UNKNOWN);
			} catch (MarshalException ex) {
				Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
			}
			valueTypeLabel.setText(new DataHandling.typeof().exec(Target.UNKNOWN, null, c).val());
			valueTextArea.setText(c.val());
		}
	}

	private void loadFromLocal(final String path){
		new Thread(new Runnable() {

			@Override
			public void run() {
				setStatus("Loading from local file system", true);
				setProgress(null);
				File config = new File(path);
				ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
				options.setWorkingDirectory(config.getParentFile().getParentFile());
				try {
					PersistenceNetwork pn = new PersistenceNetwork(config, new URI("sqlite://" + new File(config.getParentFile().getParentFile(), "persistence.db")), options);
					Map<String[], String> data = pn.getNamespace(new String[0]);
					Map<String, String> dataSources = new HashMap<>();
					for(String[] key : data.keySet()){
						dataSources.put(join(key), pn.getKeySource(key).toString());
					}
					displayData(data, dataSources);
				} catch (URISyntaxException | IOException | DataSourceException ex) {
					Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
					showError(ex.getMessage());
				}
			}
		}).start();
	}

	private void loadFromRemote(String username, String password, String privateKey, String knownHosts, String remoteFile){
		JOptionPane.showMessageDialog(this, "Loading from remote is not yet implemented.", "Not yet implemented", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Once the data is loaded, however that may take place, the data should be sent here, which will
	 * load the data into the viewer.
	 * @param data
	 */
	private void displayData(final Map<String[], String> data, final Map<String, String> dataSources){
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				PNViewer.this.data = new HashMap<>();
				PNViewer.this.dataSources = dataSources;
				DefaultTreeModel model = (DefaultTreeModel)keyTree.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
				for(String[] key : data.keySet()){
					PNViewer.this.data.put(join(key), data.get(key));
					DefaultMutableTreeNode node = root;
					outer: for(String n : key){
						for(int i = 0; i < node.getChildCount(); i++){
							DefaultMutableTreeNode at = (DefaultMutableTreeNode)node.getChildAt(i);
							if(n.equals(at.getUserObject())){
								// This is our node, so recurse down this
								node = at;
								continue outer;
							}
						}
						// If we got here, then there is not an appropriate node at this level, so we needd
						// to create it.
						DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(n);
						node.add(newChild);
						node = newChild;
					}
				}
				model.reload(root);

				setStatus("Data loaded.", false);
			}
		});
	}

	/**
	 * Sets the status label. If the current operation is happening because the system is actively doing something, thinking should
	 * be set to true, and the progress bar will be set to visible (but will keep its existing state) otherwise, the progress bar
	 * is hidden.
	 * @param statusString
	 * @param thinking
	 */
	private synchronized void setStatus(String statusString, boolean thinking){
		statusLabel.setText(statusString);
		statusProgressBar.setVisible(thinking);
	}

	/**
	 * Sets the progress bar percentage.
	 * @param i A percentage point, from 0 to 100. If null, it is set to be indeterminate.
	 */
	private void setProgress(Integer i){
		statusProgressBar.setValue(i == null ? 100 : i);
		statusProgressBar.setIndeterminate(i == null);
	}

	private void showError(String message){
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        keyTree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        namespaceLabel = new javax.swing.JLabel();
        keyLabel = new javax.swing.JLabel();
        valueTypeLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        valueTextArea = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        configurationFromLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadFromConfigurationMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(200);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        keyTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        keyTree.setToolTipText("");
        keyTree.setRootVisible(false);
        jScrollPane1.setViewportView(keyTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jLabel1.setText("Namespace:");

        jLabel2.setText("Key:");

        jLabel3.setText("Value type:");

        jLabel4.setText("Value:");

        namespaceLabel.setText(".............");

        keyLabel.setText(".............");

        valueTypeLabel.setText("............");

        valueTextArea.setEditable(false);
        valueTextArea.setColumns(20);
        valueTextArea.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        valueTextArea.setLineWrap(true);
        valueTextArea.setRows(5);
        jScrollPane2.setViewportView(valueTextArea);

        jLabel6.setText("Source:");

        sourceLabel.setText("...........");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(namespaceLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keyLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(valueTypeLabel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(namespaceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(keyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(valueTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jLabel5.setText("Configuration from:");

        configurationFromLabel.setText("...............");

        statusLabel.setText(".........");

        fileMenu.setText("File");

        loadFromConfigurationMenu.setText("Load from Configuration...");
        fileMenu.add(loadFromConfigurationMenu);

        jMenuBar1.add(fileMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configurationFromLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(configurationFromLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
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
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(PNViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				PNViewer pnViewer = new PNViewer();
				pnViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				pnViewer.setLocationByPlatform(true);
				UIUtils.centerWindow(pnViewer);
				pnViewer.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel configurationFromLabel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTree keyTree;
    private javax.swing.JMenuItem loadFromConfigurationMenu;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JTextArea valueTextArea;
    private javax.swing.JLabel valueTypeLabel;
    // End of variables declaration//GEN-END:variables
}
