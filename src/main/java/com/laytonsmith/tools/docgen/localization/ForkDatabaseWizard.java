package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.Annotations.CheckOverrides;
import com.laytonsmith.PureUtilities.Common.UIUtils;
import com.laytonsmith.PureUtilities.GithubUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;

/**
 *
 * @author Cailin
 */
@CheckOverrides.SuppressCheckOverrides
public class ForkDatabaseWizard extends javax.swing.JDialog {

	private static interface Callable<T> {
		T call();
	}
	private static interface IntCallable extends Callable<Integer> {}
	private static interface BooleanCallable extends Callable<Boolean> {}

	private static final String REPO = "git@github.com:LadyCailin/MethodScriptTranslationDB.git";
	private static final String MASTER_REPO_OWNER = "LadyCailin";
	private static final String MASTER_REPO_NAME = "MethodScriptTranslationDB";

	private static class StateOptions {
		boolean doFork;
		String githubToken;
		File saveTo;
	}

	private final LocalizationUI parent;
	private final StateOptions stateOptions = new StateOptions();
	private final LogViewer logViewer;
	/**
	 * Creates new form ForkDatabaseWizard
	 * @param parent
	 */
	@SuppressWarnings("LeakingThisInConstructor")
	public ForkDatabaseWizard(LocalizationUI parent, LogViewer logViewer) {
		super(parent, true);
		this.parent = parent;
		initComponents();
		this.logViewer = logViewer;
		step3ErrorLabel.setText("");
		UIUtils.centerWindowOnWindow(this, parent);
		for(int i = 0; i < tabbedPanel.getTabCount(); i++) {
			// Don't allow users to navigate tabs
			tabbedPanel.setEnabledAt(i, false);
		}
		writeStatus("---- Log ----");
	}

	private void validateStep() {
		int index = tabbedPanel.getSelectedIndex();
		boolean setEnabled = new BooleanCallable[] {
			() -> {
				// Step 1
				return (createForkRadioButton.isSelected() || copyRepoRadioButton.isSelected());
			},
			() -> {
				return stateOptions.githubToken != null;
			},
			() -> {
				String fl = fileLocation.getText();
				boolean subpathExists = new File(new File(fileLocation.getText()), MASTER_REPO_NAME).exists();
				if(subpathExists) {
					step3ErrorLabel.setText("The directory already contains a folder named " + MASTER_REPO_NAME);
				} else {
					step3ErrorLabel.setText("");
				}
				return new File(fl).exists() && !subpathExists;
			}
		}[index].call();
		nextButton.setEnabled(setEnabled);
	}

	private void stepTransition(boolean forward) {
		int current = tabbedPanel.getSelectedIndex();
		int next;
		if(forward) {
			if(current == tabbedPanel.getTabCount() - 1) {
				doFinish();
				return;
			}
			next = new IntCallable[] {
				() -> {
					// Step 1
					stateOptions.doFork = createForkRadioButton.isSelected();
					if(createForkRadioButton.isSelected()) {
						return 1;
					} else {
						return 2;
					}
				},
				() -> {
					// Step 2
					return 2;
				}
			}[current].call();
		} else {
			next = new IntCallable[] {
				() -> {
					// Step 1
					return 0; // Can't go back from here
				},
				() -> {
					// Step 2
					return 0;
				},
				() -> {
					// Step 3
					return 0;
				}
			}[current].call();
		}
		tabbedPanel.setSelectedIndex(next);
		if(next == 0) {
			backButton.setEnabled(false);
		} else {
			backButton.setEnabled(true);
		}
		if(next == tabbedPanel.getTabCount() - 1) {
			nextButton.setText("Finish");
		} else {
			nextButton.setText("Next");
		}
		validateStep();
	}

	private void writeStatus(String status) {
		outputWindow.append(status + "\n");
		logViewer.pushLog("ForkDatabase: " + status);
		JScrollBar vertical = outputWindowScrollPanel.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}

	private void doFinish() {
		cancelButton.setEnabled(false);
		backButton.setEnabled(false);
		nextButton.setEnabled(false);
		progressBar.setVisible(true);
		stateOptions.saveTo = new File(fileLocation.getText());
		new Thread(() -> {
			if(stateOptions.doFork) {
				writeStatus("Will first fork database on github");
			}
			writeStatus("Will save repo to " + new File(stateOptions.saveTo, REPO.replaceAll("(?:.*)/(.*?)", "$1")));
			// Start
			try {
				File saveTo = stateOptions.saveTo;
				String cloneUrl = REPO;
				if(stateOptions.doFork) {
					GithubUtil.Repository fork = getExistingFork();
					if(fork == null) {
						writeStatus("Fork not found, now creating fork");
						fork = createFork();
						writeStatus("Fork successfully created. Waiting 30 seconds for repo initialization...");
						Thread.sleep(30000);
					} else {
						writeStatus("Fork already exists! Will reuse existing fork at " + fork.cloneUrl);
					}
					cloneUrl = fork.sshUrl;
				}
				writeStatus("Cloning...");
				writeStatus(CommandExecutor.Execute(saveTo, "git", "clone", cloneUrl));
				parent.initializeTranslationDb(new File(saveTo, MASTER_REPO_NAME));
			} catch (InterruptedException | IOException ex) {
				writeStatus("Caught exception while trying to operate. Some steps may have completed successfully, and"
						+ "were not rolled back!");
				writeStatus(ex.getMessage());
				return;
			}
			// End
			writeStatus("Repo successfully cloned! Feel free to close this dialog now.");
			cancelButton.setEnabled(true);
			cancelButton.setText("Close");
			progressBar.setVisible(false);
		}, "ForkDatabase").start();
	}

	private GithubUtil.Repository getExistingFork() throws GithubUtil.GithubException {
		GithubUtil util = new GithubUtil(stateOptions.githubToken);
		List<GithubUtil.Repository> r = util.listRepos(null, "owner", null, null, null);
		for(GithubUtil.Repository rr : r) {
			if(rr.fork) {
				try {
					GithubUtil.Repository repo = util.getRepo(rr.owner.login, rr.name);
					if((MASTER_REPO_OWNER + "/" + MASTER_REPO_NAME).equals(repo.parent.fullName)) {
						return rr;
					}
				} catch	(GithubUtil.GithubException ex) {
					if(ex.getResponseCode() == 451) {
						continue;
					}
					throw ex;
				}
			}
		}
		return null;
	}

	private GithubUtil.Repository createFork() throws GithubUtil.GithubException {
		GithubUtil util = new GithubUtil(stateOptions.githubToken);
		return util.forkRepo(MASTER_REPO_OWNER, MASTER_REPO_NAME, null);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        step1ButtonGroup = new javax.swing.ButtonGroup();
        tabbedPanel = new javax.swing.JTabbedPane();
        step1Panel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        createForkRadioButton = new javax.swing.JRadioButton();
        copyRepoRadioButton = new javax.swing.JRadioButton();
        step2Panel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        authorizeGithubButton = new javax.swing.JButton();
        step3Panel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        fileLocation = new javax.swing.JTextField();
        browseFileButton = new javax.swing.JButton();
        step3ErrorLabel = new javax.swing.JLabel();
        nextButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        outputWindowScrollPanel = new javax.swing.JScrollPane();
        outputWindow = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Fork Database Wizard");

        jLabel1.setText("<html><body>Would you like to create a fork first? You must<br>create a fork before you will be able to make contributions.<br>If you create a fork, you must have a github account. You can create one in the next step.<br>If you already have a fork, but just want to check it, select \"Create Fork\" anyways.");

        step1ButtonGroup.add(createForkRadioButton);
        createForkRadioButton.setText("Create Fork");
        createForkRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createForkRadioButtonActionPerformed(evt);
            }
        });

        step1ButtonGroup.add(copyRepoRadioButton);
        copyRepoRadioButton.setText("Just copy master repo");
        copyRepoRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyRepoRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout step1PanelLayout = new javax.swing.GroupLayout(step1Panel);
        step1Panel.setLayout(step1PanelLayout);
        step1PanelLayout.setHorizontalGroup(
            step1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step1PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(step1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(copyRepoRadioButton)
                    .addComponent(createForkRadioButton)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        step1PanelLayout.setVerticalGroup(
            step1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step1PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(createForkRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(copyRepoRadioButton)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        tabbedPanel.addTab("Step 1", step1Panel);

        jLabel2.setText("<html><body>First, we need to authorize the application to access github.<br>If you don't have an account, this will create it.");

        authorizeGithubButton.setText("Authorize");
        authorizeGithubButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authorizeGithubButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout step2PanelLayout = new javax.swing.GroupLayout(step2Panel);
        step2Panel.setLayout(step2PanelLayout);
        step2PanelLayout.setHorizontalGroup(
            step2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(step2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authorizeGithubButton))
                .addContainerGap(168, Short.MAX_VALUE))
        );
        step2PanelLayout.setVerticalGroup(
            step2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authorizeGithubButton)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        tabbedPanel.addTab("Step 2", step2Panel);

        jLabel3.setText("<html><body>Where would you like to put the repo on your local machine?<br>A new folder will automatically be created within the directory you select.");

        fileLocation.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fileLocationKeyReleased(evt);
            }
        });

        browseFileButton.setText("Browse");
        browseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFileButtonActionPerformed(evt);
            }
        });

        step3ErrorLabel.setForeground(new java.awt.Color(255, 51, 0));
        step3ErrorLabel.setText("Error Label");

        javax.swing.GroupLayout step3PanelLayout = new javax.swing.GroupLayout(step3Panel);
        step3Panel.setLayout(step3PanelLayout);
        step3PanelLayout.setHorizontalGroup(
            step3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step3PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(step3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(step3PanelLayout.createSequentialGroup()
                        .addComponent(fileLocation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseFileButton))
                    .addGroup(step3PanelLayout.createSequentialGroup()
                        .addGroup(step3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(step3ErrorLabel))
                        .addGap(0, 90, Short.MAX_VALUE)))
                .addContainerGap())
        );
        step3PanelLayout.setVerticalGroup(
            step3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(step3PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(step3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(step3ErrorLabel)
                .addContainerGap(88, Short.MAX_VALUE))
        );

        tabbedPanel.addTab("Step 3", step3Panel);

        nextButton.setText("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.setEnabled(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        progressBar.setIndeterminate(true);

        outputWindow.setEditable(false);
        outputWindow.setColumns(20);
        outputWindow.setLineWrap(true);
        outputWindow.setRows(5);
        outputWindow.setWrapStyleWord(true);
        outputWindowScrollPanel.setViewportView(outputWindow);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputWindowScrollPanel)
                    .addComponent(tabbedPanel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(nextButton)
                        .addComponent(backButton)
                        .addComponent(cancelButton))
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputWindowScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addContainerGap())
        );

        progressBar.setVisible(false);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
		this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void createForkRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createForkRadioButtonActionPerformed
        validateStep();
    }//GEN-LAST:event_createForkRadioButtonActionPerformed

    private void copyRepoRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyRepoRadioButtonActionPerformed
        validateStep();
    }//GEN-LAST:event_copyRepoRadioButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        stepTransition(true);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        stepTransition(false);
    }//GEN-LAST:event_backButtonActionPerformed

    private void authorizeGithubButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authorizeGithubButtonActionPerformed
        parent.authorizeGithub(false, (token) -> {
			stateOptions.githubToken = token;
			writeStatus("Obtained Github Authorization!");
			validateStep();
		});
    }//GEN-LAST:event_authorizeGithubButtonActionPerformed

    private void fileLocationKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileLocationKeyReleased
        validateStep();
    }//GEN-LAST:event_fileLocationKeyReleased

    private void browseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFileButtonActionPerformed
        JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(JFileChooser.APPROVE_OPTION == fc.showDialog(this, "Select Directory")) {
			try {
				fileLocation.setText(fc.getSelectedFile().getCanonicalPath());
				validateStep();
			} catch (IOException ex) {
				//
			}
		}
    }//GEN-LAST:event_browseFileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton authorizeGithubButton;
    private javax.swing.JButton backButton;
    private javax.swing.JButton browseFileButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton copyRepoRadioButton;
    private javax.swing.JRadioButton createForkRadioButton;
    private javax.swing.JTextField fileLocation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton nextButton;
    private javax.swing.JTextArea outputWindow;
    private javax.swing.JScrollPane outputWindowScrollPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.ButtonGroup step1ButtonGroup;
    private javax.swing.JPanel step1Panel;
    private javax.swing.JPanel step2Panel;
    private javax.swing.JLabel step3ErrorLabel;
    private javax.swing.JPanel step3Panel;
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables
}
