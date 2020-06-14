package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.Annotations.CheckOverrides;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.TemplateBuilder;
import com.laytonsmith.PureUtilities.Common.UIUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.UI.TextDialog;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.ProfilesImpl;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.functions.OAuth;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManagerImpl;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
import com.laytonsmith.persistence.ReadOnlyException;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * UI supporting localization efforts.
 */
@CheckOverrides.SuppressCheckOverrides
public final class LocalizationUI extends javax.swing.JFrame {

	private static final String FINISHED = "Finished.";

	private boolean unsavedChanges = false;
	private TranslationMaster translations;
	private PersistenceNetwork pn;
	private String azureKey = null;
	private String storedLocation = null;
	private final DaemonManager dm = new DaemonManager();
	private GlobalEnv gEnv;
	private StaticRuntimeEnv staticRuntimeEnv;

	private List<TranslationMemory> currentSegments;
	private TranslationMemory currentMemory;
	private TranslationSummary.TranslationSummaryEntry currentSummary;

	private LogViewer logViewer = new LogViewer(this);

	/**
	 * Creates new form LocalizationUI
	 */
	private LocalizationUI() {
		Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
			String msg = "Exception in thread " + t.getName() + ":\n" + ex.getMessage() + "\n"
				 + StackTraceUtils.GetStacktrace(ex);
			System.err.println(msg);
			if(logViewer != null) {
				logViewer.pushLog(msg);
			}
		});
		initComponents();
		setUnsavedChanges(false);
		setStatus("Welcome to the " + getBranding() + " Localization (L10N) UI!"
				+ " To get started, use File->Load... and select your local database, or Tools->Fork Database... to"
				+ " create or checkout an existing one.");
		try {
			setIconImage(ImageIO.read(LocalizationUI.class.getResourceAsStream("GearIcon.png")));
		} catch(IOException ex) {
			Logger.getLogger(LocalizationUI.class.getName()).log(Level.SEVERE, null, ex);
		}
		setSummarySettingsEnabled(false);
		setLocaleSettingsEnabled(false);

		localeList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String selectedSegment = segmentsList.getSelectedValue();
				setupSegmentsList();
				if(selectedSegment != null) {
					segmentsList.setSelectedValue(selectedSegment, true);
				}
			}

		});

		segmentsList.addListSelectionListener((ListSelectionEvent lse) -> {
			if(segmentsList.getSelectedIndex() != -1) {
				populateSegment(currentSegments.get(segmentsList.getSelectedIndex()));
			}
		});

		pagesList.addListSelectionListener((ListSelectionEvent lse) -> {
			setupSegmentsList();
			viewPageInBrowserButton.setEnabled(pagesList.getSelectedIndex() > 0);
		});
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        summaryEligibleForMachineTranslationButtonGroup = new javax.swing.ButtonGroup();
        filtersButtonGroup = new javax.swing.ButtonGroup();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new java.awt.Label();
        progressBar = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        localeList = new javax.swing.JList<>();
        localeLabel = new javax.swing.JLabel();
        pagesLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pagesList = new javax.swing.JList<>();
        segmentDetailsPanel = new javax.swing.JPanel();
        summaryDataLabel = new javax.swing.JLabel();
        summaryPanel = new javax.swing.JPanel();
        idLabel = new javax.swing.JLabel();
        summaryIdField = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        summaryEnglishKeyField = new javax.swing.JTextArea();
        summaryEnglishKeyLabel = new javax.swing.JLabel();
        summaryGlobalCommentLabel = new javax.swing.JLabel();
        summaryGlobalCommentField = new javax.swing.JTextField();
        summaryIsUntranslatable = new javax.swing.JCheckBox();
        summaryIsSuspectSegment = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        summaryMachineTranslatableUncategorizedRadioButton = new javax.swing.JRadioButton();
        summaryMachineTranslatableYes = new javax.swing.JRadioButton();
        summaryMachineTranslatableNo = new javax.swing.JRadioButton();
        summaryAppearsOnPagesLabel = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        summaryAppearsOnPagesList = new javax.swing.JList<>();
        summaryWordWrapCheckbox = new javax.swing.JCheckBox();
        localeSettingsLabel = new javax.swing.JLabel();
        localeSettingsPanel = new javax.swing.JPanel();
        localeSettingsLocaleIdLabel = new javax.swing.JLabel();
        localeSettingsLocaleIdField = new javax.swing.JTextField();
        localeSettingsLocaleCommentLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        localeSettingsLocaleCommentField = new javax.swing.JTextField();
        localeSettingsMachineTranslationLabel = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        localeSettingsMachineTranslationField = new javax.swing.JTextArea();
        localeSettingsManualTranslationLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        localeSettingsManualTranslationField = new javax.swing.JTextArea();
        localeSettingsMachineTranslationWordWrapCheckbox = new javax.swing.JCheckBox();
        localeSettingsManualTranslationWordWrapCheckbox = new javax.swing.JCheckBox();
        localeSettingsMachineTranslationGenerateButton = new javax.swing.JButton();
        localeSettingsMachineTranslationClearButton = new javax.swing.JButton();
        segmentDetailsLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        segmentsList = new javax.swing.JList<>();
        segmentsLabel = new javax.swing.JLabel();
        filtersLabel = new javax.swing.JLabel();
        viewPageInBrowserButton = new javax.swing.JButton();
        filterShowAllRadioButton = new javax.swing.JRadioButton();
        filterShowUntranslatedRadioButton = new javax.swing.JRadioButton();
        filterShowUncategorizedRadioButton = new javax.swing.JRadioButton();
        filterShowSuspectRadioButton = new javax.swing.JRadioButton();
        filterShowTranslatableRadioButton = new javax.swing.JRadioButton();
        segmentCountLabel = new javax.swing.JLabel();
        topMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuLoad = new javax.swing.JMenuItem();
        menuUpdateRepo = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenuItem();
        menuSaveAndCommit = new javax.swing.JMenuItem();
        menuSaveCommitAndPush = new javax.swing.JMenuItem();
        menuPullRequest = new javax.swing.JMenuItem();
        menuExit = new javax.swing.JMenuItem();
        navigationMenu = new javax.swing.JMenu();
        menuFindSegment = new javax.swing.JMenuItem();
        menuJumpToPage = new javax.swing.JMenuItem();
        menuMoveDownSegment = new javax.swing.JMenuItem();
        menuMoveUpSegment = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        menuAzureKey = new javax.swing.JMenuItem();
        menuAuthorizeOnGithub = new javax.swing.JMenuItem();
        menuForkDatabase = new javax.swing.JMenuItem();
        menuCountUntranslatedChars = new javax.swing.JMenuItem();
        menuShowLogs = new javax.swing.JMenuItem();
        helpMenuTop = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        statusLabel.setText("label1");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jScrollPane1.setViewportView(localeList);

        localeLabel.setText("Locale");

        pagesLabel.setText("Pages");

        jScrollPane2.setViewportView(pagesList);

        segmentDetailsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        summaryDataLabel.setText("Summary Data");

        summaryPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        idLabel.setText("ID");

        summaryIdField.setEditable(false);
        summaryIdField.setToolTipText("ID that uniquely distinguishes this string across time and locales");

        summaryEnglishKeyField.setEditable(false);
        summaryEnglishKeyField.setColumns(20);
        summaryEnglishKeyField.setLineWrap(true);
        summaryEnglishKeyField.setRows(5);
        summaryEnglishKeyField.setToolTipText("Original English word/phrase");
        summaryEnglishKeyField.setWrapStyleWord(true);
        jScrollPane4.setViewportView(summaryEnglishKeyField);

        summaryEnglishKeyLabel.setText("English Key");

        summaryGlobalCommentLabel.setText("Global Comment (English)");

        summaryGlobalCommentField.setForeground(new java.awt.Color(255, 0, 0));
        summaryGlobalCommentField.setToolTipText("Note about this segment to editors of all locales (English only please)");
        summaryGlobalCommentField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                summaryGlobalCommentFieldKeyReleased(evt);
            }
        });

        summaryIsUntranslatable.setText("Is Untranslatable");
        summaryIsUntranslatable.setToolTipText("If selected, this entire segment is not eligible for translation to any locale, for instance due to it being a technical string");
        summaryIsUntranslatable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryIsUntranslatableActionPerformed(evt);
            }
        });

        summaryIsSuspectSegment.setText("Is Suspect Segment");
        summaryIsSuspectSegment.setToolTipText("If there is an error in the original English segment or you don't think it should be in the database at all.");
        summaryIsSuspectSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryIsSuspectSegmentActionPerformed(evt);
            }
        });

        jLabel1.setText("Eligible For Machine Translation");

        summaryEligibleForMachineTranslationButtonGroup.add(summaryMachineTranslatableUncategorizedRadioButton);
        summaryMachineTranslatableUncategorizedRadioButton.setText("Uncategorized");
        summaryMachineTranslatableUncategorizedRadioButton.setToolTipText("The default state for all segments");
        summaryMachineTranslatableUncategorizedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryMachineTranslatableUncategorizedRadioButtonActionPerformed(evt);
            }
        });

        summaryEligibleForMachineTranslationButtonGroup.add(summaryMachineTranslatableYes);
        summaryMachineTranslatableYes.setText("Yes");
        summaryMachineTranslatableYes.setToolTipText("In general, a machine translation will probably be correct");
        summaryMachineTranslatableYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryMachineTranslatableYesActionPerformed(evt);
            }
        });

        summaryEligibleForMachineTranslationButtonGroup.add(summaryMachineTranslatableNo);
        summaryMachineTranslatableNo.setText("No");
        summaryMachineTranslatableNo.setToolTipText("There is some text in this segment that shouldn't be translated, and cannot be automatically translated");
        summaryMachineTranslatableNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryMachineTranslatableNoActionPerformed(evt);
            }
        });

        summaryAppearsOnPagesLabel.setText("Appears on Pages");

        summaryAppearsOnPagesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        summaryAppearsOnPagesList.setToolTipText("Pages this segment appears on. Double click to select the page in the pages list to see it in context.");
        summaryAppearsOnPagesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                summaryAppearsOnPagesListMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(summaryAppearsOnPagesList);

        summaryWordWrapCheckbox.setSelected(true);
        summaryWordWrapCheckbox.setText("Word Wrap");
        summaryWordWrapCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryWordWrapCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout summaryPanelLayout = new javax.swing.GroupLayout(summaryPanel);
        summaryPanel.setLayout(summaryPanelLayout);
        summaryPanelLayout.setHorizontalGroup(
            summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(summaryPanelLayout.createSequentialGroup()
                        .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(idLabel)
                            .addComponent(summaryEnglishKeyLabel)
                            .addComponent(summaryGlobalCommentLabel)
                            .addGroup(summaryPanelLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(summaryWordWrapCheckbox)))
                        .addGap(57, 57, 57)
                        .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
                            .addComponent(summaryIdField)
                            .addComponent(summaryGlobalCommentField)))
                    .addGroup(summaryPanelLayout.createSequentialGroup()
                        .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(summaryPanelLayout.createSequentialGroup()
                                .addComponent(summaryIsUntranslatable)
                                .addGap(18, 18, 18)
                                .addComponent(summaryIsSuspectSegment))
                            .addComponent(jLabel1)
                            .addGroup(summaryPanelLayout.createSequentialGroup()
                                .addComponent(summaryMachineTranslatableUncategorizedRadioButton)
                                .addGap(18, 18, 18)
                                .addComponent(summaryMachineTranslatableYes)
                                .addGap(18, 18, 18)
                                .addComponent(summaryMachineTranslatableNo)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(summaryPanelLayout.createSequentialGroup()
                        .addComponent(summaryAppearsOnPagesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane5)))
                .addContainerGap())
        );
        summaryPanelLayout.setVerticalGroup(
            summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idLabel)
                    .addComponent(summaryIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(summaryPanelLayout.createSequentialGroup()
                        .addComponent(summaryEnglishKeyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(summaryWordWrapCheckbox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(summaryGlobalCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryGlobalCommentLabel))
                .addGap(18, 18, 18)
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryIsUntranslatable)
                    .addComponent(summaryIsSuspectSegment))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryMachineTranslatableUncategorizedRadioButton)
                    .addComponent(summaryMachineTranslatableYes)
                    .addComponent(summaryMachineTranslatableNo))
                .addGap(18, 18, 18)
                .addGroup(summaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryAppearsOnPagesLabel)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        localeSettingsLabel.setText("Locale Settings");

        localeSettingsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        localeSettingsLocaleIdLabel.setText("Locale ID");

        localeSettingsLocaleIdField.setEditable(false);
        localeSettingsLocaleIdField.setToolTipText("The locale id that uniquely identifies this translation within this locale");

        localeSettingsLocaleCommentLabel.setText("Locale Comment");

        jLabel4.setText("(English Preferred)");

        localeSettingsLocaleCommentField.setForeground(new java.awt.Color(255, 0, 0));
        localeSettingsLocaleCommentField.setToolTipText("Note about this segment to editors of this specific locale (English is preferred, but can be the locale language can be used as well)");
        localeSettingsLocaleCommentField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                localeSettingsLocaleCommentFieldKeyReleased(evt);
            }
        });

        localeSettingsMachineTranslationLabel.setText("Machine Translation");

        localeSettingsMachineTranslationField.setEditable(false);
        localeSettingsMachineTranslationField.setColumns(20);
        localeSettingsMachineTranslationField.setLineWrap(true);
        localeSettingsMachineTranslationField.setRows(5);
        localeSettingsMachineTranslationField.setToolTipText("If generated (and allowed due to the settings), the machine generated translation.");
        localeSettingsMachineTranslationField.setWrapStyleWord(true);
        jScrollPane6.setViewportView(localeSettingsMachineTranslationField);

        localeSettingsManualTranslationLabel.setText("Manual Translation");

        localeSettingsManualTranslationField.setColumns(20);
        localeSettingsManualTranslationField.setLineWrap(true);
        localeSettingsManualTranslationField.setRows(5);
        localeSettingsManualTranslationField.setToolTipText("The manual translation. This overrides the machine translation, if available.");
        localeSettingsManualTranslationField.setWrapStyleWord(true);
        localeSettingsManualTranslationField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                localeSettingsManualTranslationFieldKeyReleased(evt);
            }
        });
        jScrollPane7.setViewportView(localeSettingsManualTranslationField);

        localeSettingsMachineTranslationWordWrapCheckbox.setSelected(true);
        localeSettingsMachineTranslationWordWrapCheckbox.setText("Word Wrap");
        localeSettingsMachineTranslationWordWrapCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localeSettingsMachineTranslationWordWrapCheckboxActionPerformed(evt);
            }
        });

        localeSettingsManualTranslationWordWrapCheckbox.setSelected(true);
        localeSettingsManualTranslationWordWrapCheckbox.setText("Word Wrap");
        localeSettingsManualTranslationWordWrapCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localeSettingsManualTranslationWordWrapCheckboxActionPerformed(evt);
            }
        });

        localeSettingsMachineTranslationGenerateButton.setText("Generate");
        localeSettingsMachineTranslationGenerateButton.setToolTipText("Generates a machine translation. Only available for the art locale, or if an Azure Key is stored.");
        localeSettingsMachineTranslationGenerateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localeSettingsMachineTranslationGenerateButtonActionPerformed(evt);
            }
        });

        localeSettingsMachineTranslationClearButton.setText("Clear");
        localeSettingsMachineTranslationClearButton.setToolTipText("Clears the automatic translation. Only do this if the translation is really so bad that English would be better. Instead, create a better manual translation, and leave this as is.");
        localeSettingsMachineTranslationClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localeSettingsMachineTranslationClearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout localeSettingsPanelLayout = new javax.swing.GroupLayout(localeSettingsPanel);
        localeSettingsPanel.setLayout(localeSettingsPanelLayout);
        localeSettingsPanelLayout.setHorizontalGroup(
            localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(localeSettingsLocaleIdLabel)
                            .addComponent(localeSettingsLocaleCommentLabel)))
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jLabel4))
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(localeSettingsManualTranslationLabel))
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(localeSettingsMachineTranslationWordWrapCheckbox)
                            .addComponent(localeSettingsMachineTranslationLabel)
                            .addComponent(localeSettingsManualTranslationWordWrapCheckbox)))
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(localeSettingsMachineTranslationGenerateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(localeSettingsMachineTranslationClearButton)))
                .addGap(7, 7, 7)
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(localeSettingsLocaleIdField)
                    .addComponent(localeSettingsLocaleCommentField)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1073, Short.MAX_VALUE)
                    .addComponent(jScrollPane7))
                .addContainerGap())
        );
        localeSettingsPanelLayout.setVerticalGroup(
            localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localeSettingsLocaleIdLabel)
                    .addComponent(localeSettingsLocaleIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localeSettingsLocaleCommentLabel)
                    .addComponent(localeSettingsLocaleCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addComponent(localeSettingsMachineTranslationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(localeSettingsMachineTranslationWordWrapCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(localeSettingsMachineTranslationGenerateButton)
                            .addComponent(localeSettingsMachineTranslationClearButton)))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(localeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(localeSettingsPanelLayout.createSequentialGroup()
                        .addComponent(localeSettingsManualTranslationLabel)
                        .addGap(18, 18, 18)
                        .addComponent(localeSettingsManualTranslationWordWrapCheckbox))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout segmentDetailsPanelLayout = new javax.swing.GroupLayout(segmentDetailsPanel);
        segmentDetailsPanel.setLayout(segmentDetailsPanelLayout);
        segmentDetailsPanelLayout.setHorizontalGroup(
            segmentDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(summaryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(segmentDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(segmentDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryDataLabel)
                    .addComponent(localeSettingsLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(localeSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        segmentDetailsPanelLayout.setVerticalGroup(
            segmentDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(segmentDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(summaryDataLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(summaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(localeSettingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localeSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        segmentDetailsLabel.setText("Segment Details");

        jScrollPane3.setViewportView(segmentsList);

        segmentsLabel.setText("Segments");

        filtersLabel.setText("Filters");

        viewPageInBrowserButton.setText("View Page In Browser");
        viewPageInBrowserButton.setEnabled(false);
        viewPageInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewPageInBrowserButtonActionPerformed(evt);
            }
        });

        filtersButtonGroup.add(filterShowAllRadioButton);
        filterShowAllRadioButton.setSelected(true);
        filterShowAllRadioButton.setText("Show All");
        filterShowAllRadioButton.setToolTipText("Shows all segments");
        filterShowAllRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterShowAllRadioButtonActionPerformed(evt);
            }
        });

        filtersButtonGroup.add(filterShowUntranslatedRadioButton);
        filterShowUntranslatedRadioButton.setText("Show Untranslated");
        filterShowUntranslatedRadioButton.setToolTipText("Shows only segments that are missing any translation at all");
        filterShowUntranslatedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterShowUntranslatedRadioButtonActionPerformed(evt);
            }
        });

        filtersButtonGroup.add(filterShowUncategorizedRadioButton);
        filterShowUncategorizedRadioButton.setText("Show Uncategorized");
        filterShowUncategorizedRadioButton.setToolTipText("Shows only segments that are not categorized for machine translation yet");
        filterShowUncategorizedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterShowUncategorizedRadioButtonActionPerformed(evt);
            }
        });

        filtersButtonGroup.add(filterShowSuspectRadioButton);
        filterShowSuspectRadioButton.setText("Show Suspect");
        filterShowSuspectRadioButton.setToolTipText("Shows only segments that have been marked as suspect");
        filterShowSuspectRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterShowSuspectRadioButtonActionPerformed(evt);
            }
        });

        filtersButtonGroup.add(filterShowTranslatableRadioButton);
        filterShowTranslatableRadioButton.setText("Show Translatable");
        filterShowTranslatableRadioButton.setToolTipText("Shows only segments that are marked as translatable");
        filterShowTranslatableRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterShowTranslatableRadioButtonActionPerformed(evt);
            }
        });

        segmentCountLabel.setText("                      ");

        fileMenu.setText("File");

        menuLoad.setText("Load...");
        menuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadActionPerformed(evt);
            }
        });
        fileMenu.add(menuLoad);

        menuUpdateRepo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/git_icon16x16.png"))); // NOI18N
        menuUpdateRepo.setText("Update Repo");
        menuUpdateRepo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuUpdateRepoActionPerformed(evt);
            }
        });
        fileMenu.add(menuUpdateRepo);

        menuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        menuSave.setText("Save");
        menuSave.setToolTipText("Saves locally, but does not make a commit");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        fileMenu.add(menuSave);

        menuSaveAndCommit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        menuSaveAndCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/git_icon16x16.png"))); // NOI18N
        menuSaveAndCommit.setText("Save and Commit");
        menuSaveAndCommit.setToolTipText("Saves and makes a commit to the local repo");
        menuSaveAndCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveAndCommitActionPerformed(evt);
            }
        });
        fileMenu.add(menuSaveAndCommit);

        menuSaveCommitAndPush.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        menuSaveCommitAndPush.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/git_icon16x16.png"))); // NOI18N
        menuSaveCommitAndPush.setText("Save, Commit, and Push");
        menuSaveCommitAndPush.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveCommitAndPushActionPerformed(evt);
            }
        });
        fileMenu.add(menuSaveCommitAndPush);

        menuPullRequest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/github_icon16x16.png"))); // NOI18N
        menuPullRequest.setText("Pull Request");
        fileMenu.add(menuPullRequest);

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        fileMenu.add(menuExit);

        topMenu.add(fileMenu);

        navigationMenu.setText("Navigation");

        menuFindSegment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        menuFindSegment.setText("Find Segment...");
        menuFindSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFindSegmentActionPerformed(evt);
            }
        });
        navigationMenu.add(menuFindSegment);

        menuJumpToPage.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        menuJumpToPage.setText("Jump to Page");
        menuJumpToPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuJumpToPageActionPerformed(evt);
            }
        });
        navigationMenu.add(menuJumpToPage);

        menuMoveDownSegment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_MASK));
        menuMoveDownSegment.setText("Move Down Segment");
        menuMoveDownSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMoveDownSegmentActionPerformed(evt);
            }
        });
        navigationMenu.add(menuMoveDownSegment);

        menuMoveUpSegment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_MASK));
        menuMoveUpSegment.setText("Move Up Segment");
        menuMoveUpSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMoveUpSegmentActionPerformed(evt);
            }
        });
        navigationMenu.add(menuMoveUpSegment);

        topMenu.add(navigationMenu);

        toolsMenu.setText("Tools");

        menuAzureKey.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/cognitive_services_logo16x16.png"))); // NOI18N
        menuAzureKey.setText("Add Azure Key...");
        menuAzureKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAzureKeyActionPerformed(evt);
            }
        });
        toolsMenu.add(menuAzureKey);

        menuAuthorizeOnGithub.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/github_icon16x16.png"))); // NOI18N
        menuAuthorizeOnGithub.setText("Authorize on Github");
        menuAuthorizeOnGithub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAuthorizeOnGithubActionPerformed(evt);
            }
        });
        toolsMenu.add(menuAuthorizeOnGithub);

        menuForkDatabase.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/github_icon16x16.png"))); // NOI18N
        menuForkDatabase.setText("Fork Database...");
        menuForkDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuForkDatabaseActionPerformed(evt);
            }
        });
        toolsMenu.add(menuForkDatabase);

        menuCountUntranslatedChars.setText("Count Untranslated Chars");
        menuCountUntranslatedChars.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCountUntranslatedCharsActionPerformed(evt);
            }
        });
        toolsMenu.add(menuCountUntranslatedChars);

        menuShowLogs.setText("Show Logs");
        menuShowLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuShowLogsActionPerformed(evt);
            }
        });
        toolsMenu.add(menuShowLogs);

        topMenu.add(toolsMenu);

        helpMenuTop.setText("Help");

        helpMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/laytonsmith/tools/docgen/localization/help_icon16x16.png"))); // NOI18N
        helpMenu.setText("Help...");
        helpMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuActionPerformed(evt);
            }
        });
        helpMenuTop.add(helpMenu);

        aboutMenu.setText("About...");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        helpMenuTop.add(aboutMenu);

        topMenu.add(helpMenuTop);

        setJMenuBar(topMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pagesLabel)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(viewPageInBrowserButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(segmentCountLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(segmentsLabel)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(filtersLabel)
                            .addComponent(filterShowAllRadioButton)
                            .addComponent(filterShowUntranslatedRadioButton)
                            .addComponent(filterShowUncategorizedRadioButton)
                            .addComponent(filterShowSuspectRadioButton)
                            .addComponent(filterShowTranslatableRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(segmentDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(segmentDetailsLabel)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localeLabel)
                    .addComponent(pagesLabel)
                    .addComponent(segmentDetailsLabel)
                    .addComponent(segmentsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(segmentDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewPageInBrowserButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filtersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterShowAllRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterShowUntranslatedRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterShowUncategorizedRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterShowSuspectRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterShowTranslatableRadioButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(segmentCountLabel)
                .addGap(13, 13, 13)
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadActionPerformed
		if(!unsavedChanges || UIUtils.confirm(this, "Unsaved Changes",
				"You have unsaved changes, loading will lose them, would you like to continue?")) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(LocalizationUI.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				initializeTranslationDb(file);
			}
		}

    }//GEN-LAST:event_menuLoadActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        doSave(() -> {});
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        if(!unsavedChanges) {
			System.exit(0);
		} else {
			if(UIUtils.confirm(this, "Unsaved Changes", "You have unsaved changes, are you sure you want to quit?")) {
				System.exit(0);
			}
		}
    }//GEN-LAST:event_menuExitActionPerformed

    private void menuAzureKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAzureKeyActionPerformed
        AzureKeyInputDialog d = new AzureKeyInputDialog(LocalizationUI.this,
				(key, save) -> {
			LocalizationUI.this.azureKey = key;
			if(save) {
				if(pn != null) {
					try {
						pn.set(dm, new String[]{"l10n", "azureKey"}, key);
					} catch(DataSourceException | ReadOnlyException | IOException | IllegalArgumentException ex) {
						showError("Could not save Azure Key! " + ex.getMessage());
					}
				}
			}
		});
		UIUtils.centerWindowOnWindow(d, LocalizationUI.this);
		d.setVisible(true);
    }//GEN-LAST:event_menuAzureKeyActionPerformed

    private void menuForkDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuForkDatabaseActionPerformed
        if(unsavedChanges) {
			showError("You have unsaved changes, cannot create a new fork now!");
			return;
		}
		new ForkDatabaseWizard(this, logViewer).setVisible(true);
    }//GEN-LAST:event_menuForkDatabaseActionPerformed

    private void menuFindSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFindSegmentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menuFindSegmentActionPerformed

    private void menuJumpToPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuJumpToPageActionPerformed
        List<String> pages = translations.getPages();
		new FindDialog(this, new FindDialog.SearchModel() {
			@Override
			public void selectedEntry(int index) {
				pagesList.setSelectedIndex(index + 1);
				pagesList.ensureIndexIsVisible(index + 1);
			}

			@Override
			public List<String> getEntrySet() {
				return pages;
			}

			@Override
			public String getDialogTitle() {
				return "Find Page";
			}
		}).setVisible(true);
    }//GEN-LAST:event_menuJumpToPageActionPerformed

    private void helpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuActionPerformed
        TemplateBuilder builder = new TemplateBuilder();
		builder.addTemplate("wiki", new TemplateBuilder.Generator() {

			@Override
			public String generate(String... args) {
				return "https://methodscript.com/docs/" + MSVersion.LATEST + "/" + args[0] + ".html";
			}
		});
		String text = builder.build(StreamUtils.GetString(LocalizationUI.class
				.getResourceAsStream("HelpDialog.html")));
		TextDialog td = new TextDialog(LocalizationUI.this, false, text);
		UIUtils.centerWindowOnWindow(td, LocalizationUI.this);
		td.setVisible(true);
    }//GEN-LAST:event_helpMenuActionPerformed

    private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuActionPerformed
        TemplateBuilder builder = new TemplateBuilder();
		builder.addTemplate("version", new TemplateBuilder.Generator() {

			@Override
			public String generate(String... args) {
				return MSVersion.LATEST.toString();
			}
		});
		builder.addTemplate("implementation", new TemplateBuilder.Generator() {

			@Override
			public String generate(String... args) {
				return getBranding();
			}
		});

		String text = builder.build(StreamUtils.GetString(LocalizationUI.class
				.getResourceAsStream("AboutDialog.html")));
		TextDialog td = new TextDialog(LocalizationUI.this, true, text);
		UIUtils.centerWindowOnWindow(td, LocalizationUI.this);
		td.setVisible(true);
    }//GEN-LAST:event_aboutMenuActionPerformed

    private void summaryGlobalCommentFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_summaryGlobalCommentFieldKeyReleased
        updateCurrent();
    }//GEN-LAST:event_summaryGlobalCommentFieldKeyReleased

    private void summaryIsUntranslatableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryIsUntranslatableActionPerformed
        updateCurrent();
    }//GEN-LAST:event_summaryIsUntranslatableActionPerformed

    private void summaryIsSuspectSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryIsSuspectSegmentActionPerformed
        updateCurrent();
    }//GEN-LAST:event_summaryIsSuspectSegmentActionPerformed

    private void summaryMachineTranslatableUncategorizedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryMachineTranslatableUncategorizedRadioButtonActionPerformed
        updateCurrent();
    }//GEN-LAST:event_summaryMachineTranslatableUncategorizedRadioButtonActionPerformed

    private void summaryMachineTranslatableYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryMachineTranslatableYesActionPerformed
        updateCurrent();
    }//GEN-LAST:event_summaryMachineTranslatableYesActionPerformed

    private void summaryMachineTranslatableNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryMachineTranslatableNoActionPerformed
        updateCurrent();
    }//GEN-LAST:event_summaryMachineTranslatableNoActionPerformed

    private void summaryWordWrapCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryWordWrapCheckboxActionPerformed
		summaryEnglishKeyField.setLineWrap(summaryWordWrapCheckbox.isSelected());
    }//GEN-LAST:event_summaryWordWrapCheckboxActionPerformed

    private void summaryAppearsOnPagesListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_summaryAppearsOnPagesListMouseClicked
		if(evt.getClickCount() == 2) {
			String currentSegment = currentMemory.getEnglishKey();
			String page = summaryAppearsOnPagesList.getSelectedValue();
			pagesList.setSelectedValue(page, true);
			segmentsList.setSelectedValue(currentSegment, true);
		}
    }//GEN-LAST:event_summaryAppearsOnPagesListMouseClicked

    private void localeSettingsMachineTranslationWordWrapCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localeSettingsMachineTranslationWordWrapCheckboxActionPerformed
        localeSettingsMachineTranslationField.setLineWrap(localeSettingsMachineTranslationWordWrapCheckbox.isSelected());
    }//GEN-LAST:event_localeSettingsMachineTranslationWordWrapCheckboxActionPerformed

    private void localeSettingsManualTranslationWordWrapCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localeSettingsManualTranslationWordWrapCheckboxActionPerformed
        localeSettingsManualTranslationField.setLineWrap(localeSettingsManualTranslationWordWrapCheckbox.isSelected());
    }//GEN-LAST:event_localeSettingsManualTranslationWordWrapCheckboxActionPerformed

    private void localeSettingsLocaleCommentFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_localeSettingsLocaleCommentFieldKeyReleased
        updateCurrent();
    }//GEN-LAST:event_localeSettingsLocaleCommentFieldKeyReleased

    private void localeSettingsManualTranslationFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_localeSettingsManualTranslationFieldKeyReleased
        updateCurrent();
    }//GEN-LAST:event_localeSettingsManualTranslationFieldKeyReleased

    private void localeSettingsMachineTranslationGenerateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localeSettingsMachineTranslationGenerateButtonActionPerformed
        if(localeList.getSelectedIndex() == 0) {
			return;
		}
		final String locale = localeList.getSelectedValue();
		if(!locale.equals("art") && azureKey == null) {
			return;
		}
		final TranslationMemory tm = currentMemory;
		setStatus("Looking up translation...");

		new Thread(() -> {
			MachineTranslation mt = new AzureMachineTranslation(azureKey);
			String t;
			try {
				t = mt.translate(locale, currentMemory.getEnglishKey());
			} catch(MachineTranslation.TranslationException ex) {
				UIUtils.alert(this, "Error", "<html><body>" + ex.getMessage().replace("\n", "<br>") + "</body></html>");
				setStatus(FINISHED);
				return;
			}
			tm.setAutomaticTranslation(t);
			EventQueue.invokeLater(() -> {
				setStatus(FINISHED);
				populateSegment(currentMemory);
				updateCurrent();
			});
		}, "GenerateTranslationThread").start();
    }//GEN-LAST:event_localeSettingsMachineTranslationGenerateButtonActionPerformed

    private void viewPageInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewPageInBrowserButtonActionPerformed
		try {
			UIUtils.openWebpage(new URL("https://methodscript.com"
					+ pagesList.getSelectedValue().replace(".tmem.xml", ".html").replace("\\", "/")));
		} catch(IOException | URISyntaxException ex) {
			showError("Cannot open browser: " + ex.getMessage());
		}
    }//GEN-LAST:event_viewPageInBrowserButtonActionPerformed

    private void menuMoveDownSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMoveDownSegmentActionPerformed
		int current = segmentsList.getSelectedIndex();
		if(current == segmentsList.getModel().getSize()) {
			return;
		}
		current++;
		segmentsList.setSelectedIndex(current);
		segmentsList.ensureIndexIsVisible(current);
    }//GEN-LAST:event_menuMoveDownSegmentActionPerformed

    private void menuMoveUpSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMoveUpSegmentActionPerformed
		int current = segmentsList.getSelectedIndex();
		if(current == 0) {
			return;
		}
		current--;
		segmentsList.setSelectedIndex(current);
		segmentsList.ensureIndexIsVisible(current);
    }//GEN-LAST:event_menuMoveUpSegmentActionPerformed

    private void menuCountUntranslatedCharsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCountUntranslatedCharsActionPerformed
        new Thread(() -> {
			Map<Locale, Integer> counts = new HashMap<>();
			int total = 0;
			for(Locale locale : translations.getLocales()) {
				int count = 0;
				List<TranslationMemory> memories = translations.getMemoriesForLocale(locale);
				for(TranslationMemory tm : memories) {
					if(tm.getMachineTranslation().isEmpty()) {
						count += tm.getEnglishKey().length();
					}
				}
				if(!Locale.getDummyLocale().equals(locale)) {
					total += count;
				}
				counts.put(locale, count);
			}
			setStatus(FINISHED);
			StringBuilder b = new StringBuilder();
			b.append("<html><body>");
			for(Map.Entry<Locale, Integer> e : counts.entrySet()) {
				b.append("<p>Locale: ").append(e.getKey().getEnglishName())
						.append("; Count: ").append(String.format("%,d", e.getValue())).append("</p>");
			}
			b.append("<p>Total (minus art): ").append(String.format("%,d", total)).append("</p>");
			b.append("</body></html>");
			UIUtils.alert(this, "Character Count", b.toString());
		}, "count-chars").start();
		setStatus("Counting characters...");
    }//GEN-LAST:event_menuCountUntranslatedCharsActionPerformed

    private void filterShowAllRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterShowAllRadioButtonActionPerformed
        setupSegmentsList();
    }//GEN-LAST:event_filterShowAllRadioButtonActionPerformed

    private void filterShowUntranslatedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterShowUntranslatedRadioButtonActionPerformed
        setupSegmentsList();
    }//GEN-LAST:event_filterShowUntranslatedRadioButtonActionPerformed

    private void filterShowUncategorizedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterShowUncategorizedRadioButtonActionPerformed
        setupSegmentsList();
    }//GEN-LAST:event_filterShowUncategorizedRadioButtonActionPerformed

    private void filterShowSuspectRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterShowSuspectRadioButtonActionPerformed
        setupSegmentsList();
    }//GEN-LAST:event_filterShowSuspectRadioButtonActionPerformed

    private void filterShowTranslatableRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterShowTranslatableRadioButtonActionPerformed
        setupSegmentsList();
    }//GEN-LAST:event_filterShowTranslatableRadioButtonActionPerformed

    private void localeSettingsMachineTranslationClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localeSettingsMachineTranslationClearButtonActionPerformed
        currentMemory.setAutomaticTranslation("");
		updateCurrent();
		populateSegment(currentMemory);
    }//GEN-LAST:event_localeSettingsMachineTranslationClearButtonActionPerformed

    private void menuAuthorizeOnGithubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAuthorizeOnGithubActionPerformed
		authorizeGithub(true, (token) -> {
			UIUtils.alert(this, "Success", "Github authorization obtained!");
		});
    }//GEN-LAST:event_menuAuthorizeOnGithubActionPerformed

    private void menuUpdateRepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUpdateRepoActionPerformed
		if(storedLocation == null) {
			showError("You must first select a database to load or fork.");
			return;
		}
		if(unsavedChanges) {
			showError("You cannot update while having unsaved changes, please save and try again.");
		}
		new Thread(() -> {
			try {
				setStatus("Updating repo");
				File wd = new File(storedLocation);
				String repoStatus = CommandExecutor.Execute(wd, "git", "status", "--porcelain");
				if(!"".equals(repoStatus)) {
					repoStatus = CommandExecutor.Execute(wd, "git", "status");
					showError("<html><body>Cannot update repo, repository not clean!<br>"
							+ repoStatus.replace("\n", "<br>")
							+ "<br><br>Please manually clean up the repo, or commit, and try again.</body></html>");
					return;
				}
				// TODO This doesn't appear to work yet
				CommandExecutor pull = new CommandExecutor("git", "pull");
				pull.setWorkingDir(wd);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				pull.setSystemOut(out);
				ByteArrayOutputStream err = new ByteArrayOutputStream();
				pull.setSystemErr(err);
				int exit = pull.start().waitFor();
				if(err.size() > 0 || exit != 0) {
					showError("<html><body>Encountered an error while trying to update:<br>"
						+ err.toString("UTF-8").replace("\n", "<br>")
						+ "</body></html>");
				}
				System.out.println(out.toString("UTF-8"));
			} catch(InterruptedException | IOException ex) {
				showError("Could not update repo: " + ex.getMessage());
			}
		}, "UpdateRepo").start();
    }//GEN-LAST:event_menuUpdateRepoActionPerformed

    private void menuSaveAndCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAndCommitActionPerformed
		doSave(() -> {
			doCommit(() -> {});
		});
    }//GEN-LAST:event_menuSaveAndCommitActionPerformed

    private void menuSaveCommitAndPushActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveCommitAndPushActionPerformed
        doSave(() -> {
			doCommit(() -> {
				doPush(() -> {});
			});
		});
    }//GEN-LAST:event_menuSaveCommitAndPushActionPerformed

    private void menuShowLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowLogsActionPerformed
        logViewer.setVisible(true);
    }//GEN-LAST:event_menuShowLogsActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		launch(null);
	}

	public static void launch(String database) {
		Implementation.forceServerType(Implementation.Type.SHELL);
		ClassDiscovery.getDefaultInstance()
				.addDiscoveryLocation(ClassDiscovery.GetClassContainer(LocalizationUI.class));
		File f;
		if(database != null) {
			f = new File(database);
			if(!f.exists()) {
				System.err.println(f.getAbsolutePath() + " does not point to an existing directory!");
				System.exit(1);
			}
			if(!f.isDirectory()) {
				System.err.println(f.getAbsolutePath() + " does not point to a directory!");
				System.exit(1);
			}
		} else {
			f = null;
		}
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc="Look and feel setting code">
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
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException
				| javax.swing.UnsupportedLookAndFeelException ex) {
			Logger.getLogger(LocalizationUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		LocalizationUI ui = new LocalizationUI();
		ui.initialize();

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				UIUtils.centerWindow(ui);
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(ui.getGraphicsConfiguration());
				int taskBarSize = scnMax.bottom;
				ui.setSize((int) Math.min(screenSize.getWidth(), ui.getWidth()),
						(int) Math.min(screenSize.getHeight() - taskBarSize, ui.getHeight()));
				ui.setVisible(true);
				if(f != null) {
					ui.initializeTranslationDb(f);
				} else if(ui.storedLocation != null) {
					ui.initializeTranslationDb(new File(ui.storedLocation));
				}
			}
		});
	}

	private static PersistenceNetwork getPersistenceNetwork(File config) throws URISyntaxException, IOException,
			DataSourceException {
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(config.getParentFile().getParentFile());
		return new PersistenceNetworkImpl(config, new URI("sqlite://" + new File(config.getParentFile().getParentFile(),
				"persistence.db").toString().replace('\\', '/')), options);
	}

	private void initialize() {

		try {
			Static.GenerateStandaloneEnvironment(true);
			this.pn = getPersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig());
			if(this.pn != null) {
				storedLocation = this.pn.get(new String[]{"l10n", "lastLoadedDb"});
				azureKey = this.pn.get(new String[]{"l10n", "azureKey"});
			}
			this.gEnv = new GlobalEnv(new MethodScriptExecutionQueue("L10N-UI", "default"),
					MethodScriptFileLocations.getDefault().getTempDir(), EnumSet.of(RuntimeMode.CMDLINE));
			this.staticRuntimeEnv = new StaticRuntimeEnv(
					new Profiler(CommandHelperFileLocations.getDefault().getProfilerConfigFile()), this.pn,
					new ProfilesImpl(MethodScriptFileLocations.getDefault().getProfilesFile()), new TaskManagerImpl());
		} catch(URISyntaxException | IOException | DataSourceException | Profiles.InvalidProfileException ex) {
			showError("Could not load Persistence Database! " + ex.getMessage());
		}

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if(unsavedChanges) {
					if(UIUtils.confirm(LocalizationUI.this, "Unsaved Changes",
							"You have unsaved changes, are you sure you want to quit?")) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}

		});

		setInvalidMenus(false);
	}

	/**
	 * Sets menu options to the specified value. These are set to false initially, and then once a
	 * translation model is loaded, set to true.
	 * @param to
	 */
	private void setInvalidMenus(boolean to) {
		menuFindSegment.setEnabled(to);
		menuJumpToPage.setEnabled(to);
		menuMoveDownSegment.setEnabled(to);
		menuMoveUpSegment.setEnabled(to);
		menuCountUntranslatedChars.setEnabled(to);
	}

	private void setSummarySettingsEnabled(boolean enabled) {
		UIUtils.setEnabled(enabled, summaryGlobalCommentField,
				summaryIsUntranslatable, summaryIsSuspectSegment, summaryMachineTranslatableUncategorizedRadioButton,
				summaryMachineTranslatableYes, summaryMachineTranslatableNo, summaryAppearsOnPagesList);
	}

	private void setLocaleSettingsEnabled(boolean enabled) {
		UIUtils.setEnabled(enabled, localeSettingsLocaleCommentField, localeSettingsMachineTranslationField,
				localeSettingsMachineTranslationGenerateButton, localeSettingsMachineTranslationClearButton,
				localeSettingsManualTranslationField);
	}

	private void showError(String text) {
		logViewer.pushLog(text);
		UIUtils.alert(this, "Error", text, UIUtils.MessageType.ERROR);
	}

	private String getBranding() {
		try {
			return Implementation.GetServerType().getBranding();
		} catch(Exception ex) {
			return "MethodScript";
		}
	}

	void initializeTranslationDb(File path) {
		if(!path.exists()) {
			UIUtils.alert(this, "Error", "Tried to load database at " + path.getAbsolutePath()
					+ " but it could not be found.", UIUtils.MessageType.ERROR);
			return;
		}
		new Thread(() -> {
			try {
				translations = new TranslationMaster(path, (current, total) -> {
					setProgressStatus("Loading translations from " + path + ", please wait...",
							current, total);
				});
				if(pn != null) {
					try {
						pn.set(dm, new String[]{"l10n", "lastLoadedDb"}, path.getAbsolutePath());
					} catch(DataSourceException | ReadOnlyException | IllegalArgumentException ex) {
						Logger.getLogger(LocalizationUI.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} catch(IOException ex) {
				EventQueue.invokeLater(() -> {
					showError("Could not load database: " + ex.getMessage());
				});
				return;
			}
			EventQueue.invokeLater(this::initializeUIFromDatabase);
		}, "Initialize-Translations").start();
		setProgressStatus("Loading translations from " + path + ", please wait...", 0, 0);
	}

	private void initializeUIFromDatabase() {
		setStatus(FINISHED);

		localeList.setModel(new ListModel<String>(){
			@Override
			public int getSize() {
				return translations.getLocales().size() + 1;
			}

			@Override
			public String getElementAt(int index) {
				if(index == 0) {
					return "All Locales";
				}
				return translations.getLocales().get(index - 1).getLocale();
			}

			@Override
			public void addListDataListener(ListDataListener l) {}

			@Override
			public void removeListDataListener(ListDataListener l) {}

		});

		pagesList.setModel(new ListModel<String>(){
			@Override
			public int getSize() {
				return translations.getPages().size() + 1;
			}

			@Override
			public String getElementAt(int index) {
				if(index == 0) {
					return "All Pages";
				}
				return translations.getPages().get(index - 1);
			}

			@Override
			public void addListDataListener(ListDataListener l) {}

			@Override
			public void removeListDataListener(ListDataListener l) {}

		});

		localeList.setSelectedIndex(0);
		pagesList.setSelectedIndex(0);

		setInvalidMenus(true);
	}

	private void setupSegmentsList() {
		int index = pagesList.getSelectedIndex();
		if(index == -1) {
			return;
		}
		Locale locale;
		if(localeList.getSelectedIndex() == 0) {
			locale = Locale.getDummyLocale();
		} else {
			locale = Locale.fromLocale(localeList.getSelectedValue());
		}
		if(index == 0) {
			// All segments
			currentSegments = translations.getMemoriesForLocale(locale);
		} else {
			// Single page
			index = index - 1;
			String page = translations.getPages().get(index);
			currentSegments = translations.getMemoriesForPage(locale, page);
		}

		currentSegments = filterSegmentList(currentSegments);

		segmentCountLabel.setText("Segments: " + currentSegments.size());

		Collections.sort(currentSegments, (t, t1) -> {
			return t.getEnglishKey().compareTo(t1.getEnglishKey());
		});

		segmentsList.setModel(new ListModel<String>() {
			@Override
			public int getSize() {
				return currentSegments.size();
			}

			@Override
			public String getElementAt(int i) {
				TranslationMemory m = currentSegments.get(i);
				return m.getEnglishKey();
			}

			@Override
			public void addListDataListener(ListDataListener ll) {}

			@Override
			public void removeListDataListener(ListDataListener ll) {}
		});

		segmentsList.setSelectedIndex(0);
	}

	/**
	 * Given a list of segments, filters out the ones that don't match the filter criteria
	 * set in the GUI.
	 * @param list
	 * @return
	 */
	private List<TranslationMemory> filterSegmentList(List<TranslationMemory> list) {
		// Filter out values based on the checkbox filters
		boolean showOnlyUntranslated = filterShowUntranslatedRadioButton.isSelected();
		boolean showOnlyUncategorized = filterShowUncategorizedRadioButton.isSelected();
		boolean showOnlySuspect = filterShowSuspectRadioButton.isSelected();
		boolean showOnlyTranslatable = filterShowTranslatableRadioButton.isSelected();
		return list.stream()
			.filter((s) -> {
				TranslationSummary.TranslationSummaryEntry tse = translations.getSummaryForKey(s.getEnglishKey());
				if(showOnlyUntranslated
						&& (!s.getMachineTranslation().isEmpty() || !s.getTranslation().isEmpty()
							|| tse.isUntranslatable()
							|| tse.isSuspectSegment()
							|| tse.getEligibleForMachineTranslation() != null)) {
					return false;
				}
				// Marking them as untranslatable or suspect segments also removes this from this filter,
				// as that implies they are not machine translatable either.
				if(showOnlyUncategorized
						&& (tse.getEligibleForMachineTranslation() != null
							|| tse.isUntranslatable()
							|| tse.isSuspectSegment())) {
					return false;
				}
				if(showOnlySuspect && !tse.isSuspectSegment()) {
					return false;
				}
				if(showOnlyTranslatable && (tse.isUntranslatable()
							|| tse.isSuspectSegment()
							|| tse.getEligibleForMachineTranslation() != null)) {
					return false;
				}
				return true;
			})
			.collect(Collectors.toList());
	}

	private Font getFontForLocale(Locale locale) {
		List<String> fonts = locale.getUseFonts();
		Font useFont = null;
		if(fonts.isEmpty()) {
			useFont = new JLabel().getFont();
		}
		if(useFont == null) {
			GraphicsEnvironment ge =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
			for(Font font : ge.getAllFonts()) {
				if(fonts.contains(font.getFamily())) {
					useFont = font;
					break;
				}
			}
		}
		if(useFont == null) {
			useFont = new JLabel().getFont();
		}
		return new Font(useFont.getName(), 0, 11);
	}

	private void populateSegment(TranslationMemory tm) {
		Font localeFieldFont = getFontForLocale(tm.getLocale());
		TranslationSummary.TranslationSummaryEntry summary = translations.getSummaryForKey(tm.getEnglishKey());
		currentMemory = tm;
		currentSummary = summary;
		summaryIdField.setText(Integer.toString(summary.getId()));
		summaryEnglishKeyField.setText(summary.getEnglishKey());
		summaryGlobalCommentField.setText(summary.getComment());
		summaryIsUntranslatable.setSelected(summary.isUntranslatable());
		summaryIsSuspectSegment.setSelected(summary.isSuspectSegment());
		Boolean eFMT = summary.getEligibleForMachineTranslation();
		if(eFMT == null) {
			summaryMachineTranslatableUncategorizedRadioButton.setSelected(true);
		} else if(eFMT) {
			summaryMachineTranslatableYes.setSelected(true);
		} else {
			summaryMachineTranslatableNo.setSelected(true);
		}

		summaryAppearsOnPagesList.setModel(buildListModel(Arrays.asList("Discovering...")));
		summaryAppearsOnPagesList.setEnabled(false);

		new Thread(() -> {
			TranslationMemory myTM = tm;
			List<String> pages = new ArrayList<>();
			for(String page : translations.getPages()) {
				for(TranslationMemory tt : translations.getMemoriesForPage(Locale.getDummyLocale(), page)) {
					if(tt.getId() == tm.getId()) {
						pages.add(page);
					}
				}
			}
			EventQueue.invokeLater(() -> {
				if(tm == myTM) {
					summaryAppearsOnPagesList.setModel(buildListModel(pages));
					summaryAppearsOnPagesList.setEnabled(true);
				}
			});
		}, "FindPagesThread").start();

		setSummarySettingsEnabled(true);

		if(localeList.getSelectedIndex() != 0) {
			if(localeList.getSelectedValue().equals("art") || azureKey != null) {
				localeSettingsMachineTranslationGenerateButton.setEnabled(true);
			}
			localeSettingsLocaleIdField.setText(tm.getLocale().getLocale() + "-" + tm.getId());
			localeSettingsLocaleCommentField.setText(tm.getComment());
			localeSettingsMachineTranslationField.setText(tm.getMachineTranslation());
			localeSettingsManualTranslationField.setText(tm.getTranslation());
			setLocaleSettingsEnabled(!summary.isSuspectSegment() && !summary.isUntranslatable()
					&& summary.getEligibleForMachineTranslation() != null);
		} else {
			setLocaleSettingsEnabled(false);
			localeSettingsLocaleIdField.setText("");
			localeSettingsLocaleCommentField.setText("");
			localeSettingsMachineTranslationField.setText("");
			localeSettingsManualTranslationField.setText("");
		}

		localeSettingsLocaleCommentField.setFont(localeFieldFont);
		localeSettingsMachineTranslationField.setFont(localeFieldFont);
		localeSettingsManualTranslationField.setFont(localeFieldFont);

		ComponentOrientation co = tm.getLocale().getTextDirection() == TextDirection.LTR
				? ComponentOrientation.LEFT_TO_RIGHT : ComponentOrientation.RIGHT_TO_LEFT;
		localeSettingsLocaleCommentField.setComponentOrientation(co);
		localeSettingsMachineTranslationField.setComponentOrientation(co);
		localeSettingsManualTranslationField.setComponentOrientation(co);

		boolean isUntranslatable = summaryIsUntranslatable.isSelected();
		UIUtils.setEnabled(!isUntranslatable, summaryMachineTranslatableNo,
					summaryMachineTranslatableUncategorizedRadioButton, summaryMachineTranslatableYes);
	}

	private void updateCurrent() {
		setUnsavedChanges(true);
		{
			// Summary
			currentSummary.setComment(summaryGlobalCommentField.getText());
			currentSummary.setSuspectSegment(summaryIsSuspectSegment.isSelected());
			boolean isUntranslatable = summaryIsUntranslatable.isSelected();
			currentSummary.setUntranslatable(isUntranslatable);

			Boolean eFMT;
			if(summaryMachineTranslatableYes.isSelected()) {
				eFMT = true;
			} else if(summaryMachineTranslatableNo.isSelected()) {
				eFMT = false;
			} else {
				eFMT = null;
			}
			currentSummary.setEligibleForMachineTranslation(eFMT);
		}
		{
			// Locale
			if(localeList.getSelectedIndex() != 0) {
				currentMemory.setComment(localeSettingsLocaleCommentField.getText());
				currentMemory.setTranslation(localeSettingsManualTranslationField.getText());
				setLocaleSettingsEnabled(!currentSummary.isSuspectSegment() && !currentSummary.isUntranslatable());
			}
		}
	}

	private <T> ListModel<T> buildListModel(List<T> list) {
		return new ListModel<T>() {
			@Override
			public int getSize() {
				return list.size();
			}

			@Override
			public T getElementAt(int i) {
				return list.get(i);
			}

			@Override
			public void addListDataListener(ListDataListener ll) {}

			@Override
			public void removeListDataListener(ListDataListener ll) {}
		};
	}

	private void setProgressStatus(String status, double current, double total) {
		logViewer.pushLog(status);
		EventQueue.invokeLater(() -> {
			statusLabel.setText(status);
			progressBar.setVisible(true);
			if((current == 0 && total == 0) || current == total) {
				progressBar.setIndeterminate(true);
			} else {
				progressBar.setIndeterminate(false);
				progressBar.setMaximum((int) total);
				progressBar.setValue((int) current);
			}
		});
	}

	private void setStatus(String status) {
		logViewer.pushLog(status);
		EventQueue.invokeLater(() -> {
			progressBar.setVisible(false);
			statusLabel.setText(status);
		});
	}

	/**
	 * Logs the message to the log viewer, but not the UI
	 * @param status
	 */
	private void setSilentStatus(String status) {
		logViewer.pushLog(status);
	}

	private void setUnsavedChanges(boolean to) {
		unsavedChanges = to;
		String p = "";
		if(unsavedChanges) {
			p = "* ";
		}
		String title = p + "L10N Interface";
		EventQueue.invokeLater(() -> {
			setTitle(title);
			menuSave.setEnabled(unsavedChanges);
		});
	}

	public static interface GithubAuthSuccess {
		/**
		 * Provides the oauth token that was obtained from either the cache or the server.
		 * @param token
		 */
		void token(String token);
	}

	/**
	 *
	 * @param clearFirst If the settings should be cleared first. Only should be done on the explicit menu option.
	 * @param success If successful, the callback to run afterwards. This is NOT run on the main thread.
	 */
	public void authorizeGithub(boolean clearFirst, GithubAuthSuccess success) {
		String authorizationUrl = "https://github.com/login/oauth/authorize";
		String clientId = "9574af3fc79384fa690a";
		// Not actually a secret for "public apps"
		String clientSecret = "e767193ef18fd213dcde506f8f465e873c1a5927";
		String scope = "public_repo";
		String tokenUrl = "https://github.com/login/oauth/access_token";
		OAuth.x_get_oauth_token.OAuthOptions options
				= new OAuth.x_get_oauth_token.OAuthOptions(authorizationUrl, clientId, clientSecret, scope, tokenUrl);
		new Thread(() -> {
			try {
				if(clearFirst) {
					OAuth.clear_oauth_tokens.execute(gEnv, clientId);
				}
				options.forcePort = 5346;
				String githubOAuthToken = OAuth.x_get_oauth_token.execute(gEnv, options);
				success.token(githubOAuthToken);
			} catch (Exception ex) {
				showError(ex.getMessage());
			}
		}, "GithubAuth").start();
	}

	/**
	 *
	 * @param onSuccess Called if the operation is successful, or if no changes were needed to be saved.
	 */
	private void doSave(Runnable onSuccess) {
		if(unsavedChanges) {
			new Thread(() -> {
				try {
					translations.save((current, total) -> {
						setProgressStatus("Saving...", current, total);
					});
					setUnsavedChanges(false);
					setStatus(FINISHED);
					onSuccess.run();
				} catch (IOException ex) {
					UIUtils.alert(this, "Error while saving!", "Could not save the database: " + ex.getMessage(),
							UIUtils.MessageType.ERROR);
					setStatus("Error saving...");
				}
			}).start();
			setProgressStatus("Saving...", 0, 0);
		} else {
			onSuccess.run();
		}
	}

	private void doCommit(Runnable success) {
		new Thread(() -> {
			try {
				String gitStatus = CommandExecutor.Execute(translations.getTranslationDb(),
						"git", "status", "--porcelain");
				if("".equals(gitStatus)) {
					UIUtils.alert(this, "No changes", "No changes to commit!");
					success.run();
					return;
				}
				String commitMessage = UIUtils.prompt(this, "Commit Message", "Enter a descriptive commit message,"
						+ " summarizing your changes.");
				CommandExecutor.Execute(translations.getTranslationDb(), "git", "add", ".");
				CommandExecutor.Execute(translations.getTranslationDb(), "git", "commit", "-m", commitMessage);
				success.run();
			} catch (InterruptedException | IOException ex) {
				UIUtils.alert(this, "Error", ex.getMessage());
			} finally {
				setStatus(FINISHED);
			}
		}, "CommitChanges").start();
		setStatus("Committing...");
	}

	private void doPush(Runnable success) {
		new Thread(() -> {
			try {
				String diff = CommandExecutor
						.Execute(translations.getTranslationDb(), "git", "log", "origin/master..master");
				// If diff is empty, there are no local changes that need pushing
				if("".equals(diff)) {
					UIUtils.alert(this, "No changes", "No commits need to be pushed.");
					success.run();
					return;
				}
				CommandExecutor.Execute(translations.getTranslationDb(), "git", "push");
			} catch (InterruptedException | IOException ex) {
				UIUtils.alert(this, "Error", ex.getMessage());
			} finally {
				setStatus(FINISHED);
			}
		}, "PushChanges").start();
		setStatus("Pushing...");
	}



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JRadioButton filterShowAllRadioButton;
    private javax.swing.JRadioButton filterShowSuspectRadioButton;
    private javax.swing.JRadioButton filterShowTranslatableRadioButton;
    private javax.swing.JRadioButton filterShowUncategorizedRadioButton;
    private javax.swing.JRadioButton filterShowUntranslatedRadioButton;
    private javax.swing.ButtonGroup filtersButtonGroup;
    private javax.swing.JLabel filtersLabel;
    private javax.swing.JMenuItem helpMenu;
    private javax.swing.JMenu helpMenuTop;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel localeLabel;
    private javax.swing.JList<String> localeList;
    private javax.swing.JLabel localeSettingsLabel;
    private javax.swing.JTextField localeSettingsLocaleCommentField;
    private javax.swing.JLabel localeSettingsLocaleCommentLabel;
    private javax.swing.JTextField localeSettingsLocaleIdField;
    private javax.swing.JLabel localeSettingsLocaleIdLabel;
    private javax.swing.JButton localeSettingsMachineTranslationClearButton;
    private javax.swing.JTextArea localeSettingsMachineTranslationField;
    private javax.swing.JButton localeSettingsMachineTranslationGenerateButton;
    private javax.swing.JLabel localeSettingsMachineTranslationLabel;
    private javax.swing.JCheckBox localeSettingsMachineTranslationWordWrapCheckbox;
    private javax.swing.JTextArea localeSettingsManualTranslationField;
    private javax.swing.JLabel localeSettingsManualTranslationLabel;
    private javax.swing.JCheckBox localeSettingsManualTranslationWordWrapCheckbox;
    private javax.swing.JPanel localeSettingsPanel;
    private javax.swing.JMenuItem menuAuthorizeOnGithub;
    private javax.swing.JMenuItem menuAzureKey;
    private javax.swing.JMenuItem menuCountUntranslatedChars;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem menuFindSegment;
    private javax.swing.JMenuItem menuForkDatabase;
    private javax.swing.JMenuItem menuJumpToPage;
    private javax.swing.JMenuItem menuLoad;
    private javax.swing.JMenuItem menuMoveDownSegment;
    private javax.swing.JMenuItem menuMoveUpSegment;
    private javax.swing.JMenuItem menuPullRequest;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenuItem menuSaveAndCommit;
    private javax.swing.JMenuItem menuSaveCommitAndPush;
    private javax.swing.JMenuItem menuShowLogs;
    private javax.swing.JMenuItem menuUpdateRepo;
    private javax.swing.JMenu navigationMenu;
    private javax.swing.JLabel pagesLabel;
    private javax.swing.JList<String> pagesList;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel segmentCountLabel;
    private javax.swing.JLabel segmentDetailsLabel;
    private javax.swing.JPanel segmentDetailsPanel;
    private javax.swing.JLabel segmentsLabel;
    private javax.swing.JList<String> segmentsList;
    private java.awt.Label statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel summaryAppearsOnPagesLabel;
    private javax.swing.JList<String> summaryAppearsOnPagesList;
    private javax.swing.JLabel summaryDataLabel;
    private javax.swing.ButtonGroup summaryEligibleForMachineTranslationButtonGroup;
    private javax.swing.JTextArea summaryEnglishKeyField;
    private javax.swing.JLabel summaryEnglishKeyLabel;
    private javax.swing.JTextField summaryGlobalCommentField;
    private javax.swing.JLabel summaryGlobalCommentLabel;
    private javax.swing.JTextField summaryIdField;
    private javax.swing.JCheckBox summaryIsSuspectSegment;
    private javax.swing.JCheckBox summaryIsUntranslatable;
    private javax.swing.JRadioButton summaryMachineTranslatableNo;
    private javax.swing.JRadioButton summaryMachineTranslatableUncategorizedRadioButton;
    private javax.swing.JRadioButton summaryMachineTranslatableYes;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JCheckBox summaryWordWrapCheckbox;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuBar topMenu;
    private javax.swing.JButton viewPageInBrowserButton;
    // End of variables declaration//GEN-END:variables
}
