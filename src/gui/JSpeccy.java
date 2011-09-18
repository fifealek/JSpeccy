/*
 * JSpeccy.java
 *
 * Created on 21 de enero de 2008, 14:27
 */

package gui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import machine.MachineTypes;
import machine.Spectrum;
import configuration.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;

/**
 *
 * @author  jsanchez
 */
public class JSpeccy extends javax.swing.JFrame {
    Spectrum spectrum;
    JSpeccyScreen jscr;
    File currentDirSnapshot, currentDirSaveSnapshot,
        currentDirTape, currentDirSaveImage;
    JFileChooser openSnapshotDlg, saveSnapshotDlg, openTapeDlg, saveImageDlg;
    String lastSnapshotDir, lastTapeDir;
    File recentFile[] = new File[5];
    ListSelectionModel lsm;
    JSpeccySettingsType settings;
    SettingsDialog settingsDialog;

    /** Creates new form JSpeccy */
    public JSpeccy() {
        initComponents();
        initEmulator();
    }

    private void verifyConfigFile(boolean deleteFile) {
        File file = new File("JSpeccy.xml");
        if (file.exists() && !deleteFile) {
            return;
        }

        if (deleteFile) {
            file.delete();
        }

        // Si el archivo de configuración no existe, lo crea de nuevo en el
        // directorio actual copiándolo del bueno que hay siempre en el .jar
        try {
            InputStream input = Spectrum.class.getResourceAsStream("/schema/JSpeccy.xml");
            FileOutputStream output = new FileOutputStream("JSpeccy.xml");

            int value = input.read();
            while (value != -1) {
                output.write(value & 0xff);
                value = input.read();
            }

            input.close();
            output.close();
        } catch (FileNotFoundException notFoundExcpt) {
            Logger.getLogger(JSpeccy.class.getName()).log(Level.SEVERE, null, notFoundExcpt);
        } catch (IOException ioExcpt) {
            Logger.getLogger(JSpeccy.class.getName()).log(Level.SEVERE, null, ioExcpt);
        }
    }

    private void readSettingsFile() {
        verifyConfigFile(false);

        boolean readed = true;
        try {
            // create a JAXBContext capable of handling classes generated into
            // the configuration package
            JAXBContext jc = JAXBContext.newInstance("configuration");

            // create an Unmarshaller
            Unmarshaller unmsh = jc.createUnmarshaller();

            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the configuration package.
            JAXBElement<?> settingsElement =
                    (JAXBElement<?>) unmsh.unmarshal(new FileInputStream("JSpeccy.xml"));

            settings = (JSpeccySettingsType) settingsElement.getValue();
        } catch (JAXBException jexcpt) {
            System.out.println("Something during unmarshalling go very bad!");
            readed = false;
        } catch (IOException ioexcpt) {
            System.out.println("Can't open the JSpeccy.xml configuration file");
        }

        if (readed)
            return;

        System.out.println("Trying to create a new one JSpeccy.xml for you");

        verifyConfigFile(true);
        try {
            // create a JAXBContext capable of handling classes generated into
            // the configuration package
            JAXBContext jc = JAXBContext.newInstance("configuration");

            // create an Unmarshaller
            Unmarshaller unmsh = jc.createUnmarshaller();

            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the configuration package.
            JAXBElement<?> settingsElement =
                    (JAXBElement<?>) unmsh.unmarshal(new FileInputStream("JSpeccy.xml"));

            settings = (JSpeccySettingsType) settingsElement.getValue();
        } catch (JAXBException jexcpt) {
            System.out.println("Something during unmarshalling go very very bad!");
            readed = false;
        } catch (IOException ioexcpt) {
            System.out.println("Can't open the JSpeccy.xml configuration file anyway");
            System.exit(0);
        }

    }

    private void saveRecentFiles() {
        try {
            // create a JAXBContext capable of handling classes generated into
            // the configuration package
            JAXBContext jc = JAXBContext.newInstance("configuration");

            // create an Unmarshaller
            Unmarshaller unmsh = jc.createUnmarshaller();

            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the configuration package.
            JAXBElement<?> settingsElement =
                    (JAXBElement<?>) unmsh.unmarshal(new FileInputStream("JSpeccy.xml"));

            settings = (JSpeccySettingsType) settingsElement.getValue();
        } catch (JAXBException jexcpt) {
            System.out.println("Something during unmarshalling go very bad!");
        } catch (IOException ioexcpt) {
            System.out.println("Can't open the JSpeccy.xml configuration file");
        }

        if (recentFile[0] != null)
            settings.getRecentFilesSettings().setRecentFile0(recentFile[0].getAbsolutePath());
        if (recentFile[1] != null)
            settings.getRecentFilesSettings().setRecentFile1(recentFile[1].getAbsolutePath());
        if (recentFile[2] != null)
            settings.getRecentFilesSettings().setRecentFile2(recentFile[2].getAbsolutePath());
        if (recentFile[3] != null)
            settings.getRecentFilesSettings().setRecentFile3(recentFile[3].getAbsolutePath());
        if (recentFile[4] != null)
            settings.getRecentFilesSettings().setRecentFile4(recentFile[4].getAbsolutePath());
        settings.getRecentFilesSettings().setLastSnapshotDir(lastSnapshotDir);
        settings.getRecentFilesSettings().setLastTapeDir(lastTapeDir);

        try {
            BufferedOutputStream fOut =
                new BufferedOutputStream(new FileOutputStream("JSpeccy.xml"));
            // create an element for marshalling
            JAXBElement<JSpeccySettingsType> confElement =
                (new ObjectFactory()).createJSpeccySettings(settings);

            // create a Marshaller and marshal to conf. file
            JAXB.marshal(confElement, fOut);
            try {
                fOut.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initEmulator() {
        readSettingsFile();
        spectrum = new Spectrum(settings);
        jscr = new JSpeccyScreen();
        spectrum.setScreenComponent(jscr);
        jscr.setTvImage(spectrum.getTvImage());
        spectrum.setInfoLabels(modelLabel, speedLabel);
        spectrum.setHardwareMenuItems(spec16kHardware, spec48kHardware, spec128kHardware,
                specPlus2Hardware, specPlus2AHardware, specPlus3Hardware);
        spectrum.setJoystickMenuItems(noneJoystick, kempstonJoystick,
                sinclair1Joystick, sinclair2Joystick, cursorJoystick);
        spectrum.tape.setTapeIcon(tapeLabel);
        tapeCatalog.setModel(spectrum.tape.getTapeTableModel());
        tapeCatalog.getColumnModel().getColumn(0).setMaxWidth(150);
        lsm = tapeCatalog.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {

                if (!event.getValueIsAdjusting() && event.getLastIndex() != -1) {
                    spectrum.tape.setSelectedBlock(lsm.getLeadSelectionIndex());
                }
            }
        });
        spectrum.tape.setListSelectionModel(lsm);
        getContentPane().add(jscr, BorderLayout.CENTER);
        pack();
        addKeyListener(spectrum.getKeyboard());

        // Synchronize the file user settings with GUI settings
        switch (settings.getSpectrumSettings().getDefaultModel()) {
            case 0:
                spec16kHardware.setSelected(true);
                break;
            case 2:
                spec128kHardware.setSelected(true);
                break;
            case 3:
                specPlus2Hardware.setSelected(true);
                break;
            case 4:
                specPlus2AHardware.setSelected(true);
                break;
            case 5:
                specPlus3Hardware.setSelected(true);
                break;
            default:
                spec48kHardware.setSelected(true);
        }
        
        switch (settings.getKeyboardJoystickSettings().getJoystickModel()) {
            case 1:
                kempstonJoystick.setSelected(true);
                break;
            case 2:
                sinclair1Joystick.setSelected(true);
                break;
            case 3:
                sinclair2Joystick.setSelected(true);
                break;
            case 4:
                cursorJoystick.setSelected(true);
                break;
            default:
                noneJoystick.setSelected(true);
        }

        if (settings.getSpectrumSettings().isMutedSound()) {
            silenceMachineMenu.setSelected(true);
            silenceSoundToggleButton.setSelected(true);
        }

        if (settings.getSpectrumSettings().isDoubleSize()) {
            jscr.toggleDoubleSize();
            doubleSizeOption.setSelected(true);
            doubleSizeToggleButton.setSelected(true);
            pack();
        }

        if (settings.getRecentFilesSettings().getRecentFile0() != null &&
                !settings.getRecentFilesSettings().getRecentFile0().isEmpty()) {
            recentFile[0] = new File(settings.getRecentFilesSettings().getRecentFile0());
            recentFileMenu0.setText(recentFile[0].getName());
            recentFileMenu0.setToolTipText(recentFile[0].getAbsolutePath());
            recentFileMenu0.setEnabled(true);
        }

        if (settings.getRecentFilesSettings().getRecentFile1() != null &&
                !settings.getRecentFilesSettings().getRecentFile1().isEmpty()) {
            recentFile[1] = new File(settings.getRecentFilesSettings().getRecentFile1());
            recentFileMenu1.setText(recentFile[1].getName());
            recentFileMenu1.setToolTipText(recentFile[1].getAbsolutePath());
            recentFileMenu1.setEnabled(true);
        }

        if (settings.getRecentFilesSettings().getRecentFile2() != null &&
                !settings.getRecentFilesSettings().getRecentFile2().isEmpty()) {
            recentFile[2] = new File(settings.getRecentFilesSettings().getRecentFile2());
            recentFileMenu2.setText(recentFile[2].getName());
            recentFileMenu2.setToolTipText(recentFile[2].getAbsolutePath());
            recentFileMenu2.setEnabled(true);
        }

        if (settings.getRecentFilesSettings().getRecentFile3() != null &&
                !settings.getRecentFilesSettings().getRecentFile3().isEmpty()) {
            recentFile[3] = new File(settings.getRecentFilesSettings().getRecentFile3());
            recentFileMenu3.setText(recentFile[3].getName());
            recentFileMenu3.setToolTipText(recentFile[3].getAbsolutePath());
            recentFileMenu3.setEnabled(true);
        }

        if (settings.getRecentFilesSettings().getRecentFile4() != null &&
                !settings.getRecentFilesSettings().getRecentFile4().isEmpty()) {
            recentFile[4] = new File(settings.getRecentFilesSettings().getRecentFile4());
            recentFileMenu4.setText(recentFile[4].getName());
            recentFileMenu4.setToolTipText(recentFile[4].getAbsolutePath());
            recentFileMenu4.setEnabled(true);
        }

        if (!settings.getRecentFilesSettings().getLastSnapshotDir().isEmpty()) {
            lastSnapshotDir = settings.getRecentFilesSettings().getLastSnapshotDir();
        } else {
            lastSnapshotDir = "/home/jsanchez/Spectrum";
        }

        if (!settings.getRecentFilesSettings().getLastTapeDir().isEmpty()) {
            lastTapeDir = settings.getRecentFilesSettings().getLastTapeDir();
        } else {
            lastTapeDir = "/home/jsanchez/Spectrum";
        }

        settingsDialog = new SettingsDialog(settings);
        spectrum.start();
    }

    private void rotateRecentFile(File lastname) {
        recentFile[4] = recentFile[3];
        recentFile[3] = recentFile[2];
        recentFile[2] = recentFile[1];
        recentFile[1] = recentFile[0];
        recentFile[0] = lastname;

        if (recentFile[0] != null && !recentFile[0].getName().isEmpty()) {
            recentFileMenu0.setText(recentFile[0].getName());
            recentFileMenu0.setToolTipText(recentFile[0].getAbsolutePath());
            recentFileMenu0.setEnabled(true);
            settings.getRecentFilesSettings().setRecentFile0(recentFile[0].getAbsolutePath());
        }

        if (recentFile[1] != null && !recentFile[1].getName().isEmpty()) {
            recentFileMenu1.setText(recentFile[1].getName());
            recentFileMenu1.setToolTipText(recentFile[1].getAbsolutePath());
            recentFileMenu1.setEnabled(true);
            settings.getRecentFilesSettings().setRecentFile1(recentFile[1].getAbsolutePath());
        }

        if (recentFile[2] != null && !recentFile[2].getName().isEmpty()) {
            recentFileMenu2.setText(recentFile[2].getName());
            recentFileMenu2.setToolTipText(recentFile[2].getAbsolutePath());
            recentFileMenu2.setEnabled(true);
            settings.getRecentFilesSettings().setRecentFile2(recentFile[2].getAbsolutePath());
        }

        if (recentFile[3] != null && !recentFile[3].getName().isEmpty()) {
            recentFileMenu3.setText(recentFile[3].getName());
            recentFileMenu3.setToolTipText(recentFile[3].getAbsolutePath());
            recentFileMenu3.setEnabled(true);
            settings.getRecentFilesSettings().setRecentFile3(recentFile[3].getAbsolutePath());
        }

        if (recentFile[4] != null && !recentFile[4].getName().isEmpty()) {
            recentFileMenu4.setText(recentFile[4].getName());
            recentFileMenu4.setToolTipText(recentFile[4].getAbsolutePath());
            recentFileMenu4.setEnabled(true);
            settings.getRecentFilesSettings().setRecentFile4(recentFile[4].getAbsolutePath());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        keyboardHelper = new javax.swing.JDialog(this);
        keyboardImage = new javax.swing.JLabel();
        closeKeyboardHelper = new javax.swing.JButton();
        joystickButtonGroup = new javax.swing.ButtonGroup();
        hardwareButtonGroup = new javax.swing.ButtonGroup();
        tapeBrowserDialog = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        tapeCatalog = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        closeTapeBrowserButton = new javax.swing.JButton();
        tapeFilename = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        modelLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        tapeLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        speedLabel = new javax.swing.JLabel();
        toolbarMenu = new javax.swing.JToolBar();
        openSnapshotButton = new javax.swing.JButton();
        pauseToggleButton = new javax.swing.JToggleButton();
        fastEmulationToggleButton = new javax.swing.JToggleButton();
        doubleSizeToggleButton = new javax.swing.JToggleButton();
        silenceSoundToggleButton = new javax.swing.JToggleButton();
        resetSpectrumButton = new javax.swing.JButton();
        hardResetSpectrumButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openSnapshot = new javax.swing.JMenuItem();
        saveSnapshot = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        saveScreenShot = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        recentFilesMenu = new javax.swing.JMenu();
        recentFileMenu0 = new javax.swing.JMenuItem();
        recentFileMenu1 = new javax.swing.JMenuItem();
        recentFileMenu2 = new javax.swing.JMenuItem();
        recentFileMenu3 = new javax.swing.JMenuItem();
        recentFileMenu4 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        thisIsTheEndMyFriend = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        doubleSizeOption = new javax.swing.JCheckBoxMenuItem();
        joystickOptionMenu = new javax.swing.JMenu();
        noneJoystick = new javax.swing.JRadioButtonMenuItem();
        kempstonJoystick = new javax.swing.JRadioButtonMenuItem();
        sinclair1Joystick = new javax.swing.JRadioButtonMenuItem();
        sinclair2Joystick = new javax.swing.JRadioButtonMenuItem();
        cursorJoystick = new javax.swing.JRadioButtonMenuItem();
        settingsOptionsMenu = new javax.swing.JMenuItem();
        machineMenu = new javax.swing.JMenu();
        pauseMachineMenu = new javax.swing.JCheckBoxMenuItem();
        silenceMachineMenu = new javax.swing.JCheckBoxMenuItem();
        resetMachineMenu = new javax.swing.JMenuItem();
        hardResetMachineMenu = new javax.swing.JMenuItem();
        nmiMachineMenu = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        hardwareMachineMenu = new javax.swing.JMenu();
        spec16kHardware = new javax.swing.JRadioButtonMenuItem();
        spec48kHardware = new javax.swing.JRadioButtonMenuItem();
        spec128kHardware = new javax.swing.JRadioButtonMenuItem();
        specPlus2Hardware = new javax.swing.JRadioButtonMenuItem();
        specPlus2AHardware = new javax.swing.JRadioButtonMenuItem();
        specPlus3Hardware = new javax.swing.JRadioButtonMenuItem();
        mediaMenu = new javax.swing.JMenu();
        tapeMediaMenu = new javax.swing.JMenu();
        openTapeMediaMenu = new javax.swing.JMenuItem();
        playTapeMediaMenu = new javax.swing.JMenuItem();
        browserTapeMediaMenu = new javax.swing.JMenuItem();
        rewindTapeMediaMenu = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        createTapeMediaMenu = new javax.swing.JMenuItem();
        clearTapeMediaMenu = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        recordStartTapeMediaMenu = new javax.swing.JMenuItem();
        recordStopTapeMediaMenu = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        imageHelpMenu = new javax.swing.JMenuItem();
        aboutHelpMenu = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        keyboardHelper.setTitle(bundle.getString("JSpeccy.keyboardHelper.title")); // NOI18N

        keyboardImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Keyboard48k.png"))); // NOI18N
        keyboardImage.setText(bundle.getString("JSpeccy.keyboardImage.text")); // NOI18N
        keyboardHelper.getContentPane().add(keyboardImage, java.awt.BorderLayout.PAGE_START);

        closeKeyboardHelper.setText(bundle.getString("JSpeccy.closeKeyboardHelper.text")); // NOI18N
        closeKeyboardHelper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeKeyboardHelperActionPerformed(evt);
            }
        });
        keyboardHelper.getContentPane().add(closeKeyboardHelper, java.awt.BorderLayout.PAGE_END);

        tapeBrowserDialog.setTitle(bundle.getString("JSpeccy.tapeBrowserDialog.title")); // NOI18N

        tapeCatalog.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Block Number", "Block Type", "Block information"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tapeCatalog.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        tapeCatalog.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tapeCatalog.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tapeCatalog);
        tapeCatalog.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tapeCatalog.getColumnModel().getColumn(0).setResizable(false);
        tapeCatalog.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("JSpeccy.tapeCatalog.columnModel.title0")); // NOI18N
        tapeCatalog.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("JSpeccy.tapeCatalog.columnModel.title1")); // NOI18N
        tapeCatalog.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("JSpeccy.tapeCatalog.columnModel.title2")); // NOI18N

        tapeBrowserDialog.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        closeTapeBrowserButton.setText(bundle.getString("JSpeccy.closeTapeBrowserButton.text")); // NOI18N
        closeTapeBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTapeBrowserButtonActionPerformed(evt);
            }
        });
        jPanel2.add(closeTapeBrowserButton);

        tapeBrowserDialog.getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        tapeFilename.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tapeFilename.setText(bundle.getString("JSpeccy.tapeFilename.text")); // NOI18N
        tapeBrowserDialog.getContentPane().add(tapeFilename, java.awt.BorderLayout.PAGE_START);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("JSpeccy.title")); // NOI18N
        setIconImage(Toolkit.getDefaultToolkit().getImage(
            JSpeccy.class.getResource("/icons/JSpeccy48x48.png")));
    setResizable(false);

    jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

    modelLabel.setText(bundle.getString("JSpeccy.modelLabel.text")); // NOI18N
    modelLabel.setToolTipText(bundle.getString("JSpeccy.modelLabel.toolTipText")); // NOI18N
    modelLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    jPanel1.add(modelLabel);

    jLabel1.setText(bundle.getString("JSpeccy.jLabel1.text")); // NOI18N
    jLabel1.setMaximumSize(new java.awt.Dimension(32767, 16));
    jLabel1.setPreferredSize(new java.awt.Dimension(100, 16));
    jPanel1.add(jLabel1);

    tapeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    tapeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Akai24x24.png"))); // NOI18N
    tapeLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    tapeLabel.setEnabled(false);
    tapeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    tapeLabel.setPreferredSize(new java.awt.Dimension(36, 26));
    tapeLabel.setRequestFocusEnabled(false);
    jPanel1.add(tapeLabel);

    jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
    jSeparator2.setMaximumSize(new java.awt.Dimension(5, 32767));
    jSeparator2.setMinimumSize(new java.awt.Dimension(3, 16));
    jSeparator2.setPreferredSize(new java.awt.Dimension(3, 16));
    jSeparator2.setRequestFocusEnabled(false);
    jPanel1.add(jSeparator2);

    speedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    speedLabel.setText(bundle.getString("JSpeccy.speedLabel.text")); // NOI18N
    speedLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    speedLabel.setMaximumSize(new java.awt.Dimension(50, 18));
    speedLabel.setMinimumSize(new java.awt.Dimension(40, 18));
    speedLabel.setPreferredSize(new java.awt.Dimension(45, 18));
    speedLabel.setRequestFocusEnabled(false);
    jPanel1.add(speedLabel);

    getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

    toolbarMenu.setRollover(true);

    openSnapshotButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/fileopen.png"))); // NOI18N
    openSnapshotButton.setToolTipText(bundle.getString("JSpeccy.openSnapshotButton.toolTipText")); // NOI18N
    openSnapshotButton.setFocusable(false);
    openSnapshotButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    openSnapshotButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    openSnapshotButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            openSnapshotActionPerformed(evt);
        }
    });
    toolbarMenu.add(openSnapshotButton);

    pauseToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/player_pause.png"))); // NOI18N
    pauseToggleButton.setToolTipText(bundle.getString("JSpeccy.pauseToggleButton.toolTipText")); // NOI18N
    pauseToggleButton.setFocusable(false);
    pauseToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    pauseToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/player_play.png"))); // NOI18N
    pauseToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    pauseToggleButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            pauseMachineMenuActionPerformed(evt);
        }
    });
    toolbarMenu.add(pauseToggleButton);

    fastEmulationToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/player_fwd.png"))); // NOI18N
    fastEmulationToggleButton.setText(bundle.getString("JSpeccy.fastEmulationToggleButton.text")); // NOI18N
    fastEmulationToggleButton.setToolTipText(bundle.getString("JSpeccy.fastEmulationToggleButton.toolTipText")); // NOI18N
    fastEmulationToggleButton.setFocusable(false);
    fastEmulationToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    fastEmulationToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    fastEmulationToggleButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            fastEmulationToggleButtonActionPerformed(evt);
        }
    });
    toolbarMenu.add(fastEmulationToggleButton);

    doubleSizeToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/viewmag+.png"))); // NOI18N
    doubleSizeToggleButton.setToolTipText(bundle.getString("JSpeccy.doubleSizeToggleButton.toolTipText")); // NOI18N
    doubleSizeToggleButton.setFocusable(false);
    doubleSizeToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    doubleSizeToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/viewmag-.png"))); // NOI18N
    doubleSizeToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    doubleSizeToggleButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            doubleSizeOptionActionPerformed(evt);
        }
    });
    toolbarMenu.add(doubleSizeToggleButton);

    silenceSoundToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sound-on-16x16.png"))); // NOI18N
    silenceSoundToggleButton.setToolTipText(bundle.getString("JSpeccy.silenceSoundToggleButton.toolTipText")); // NOI18N
    silenceSoundToggleButton.setFocusable(false);
    silenceSoundToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    silenceSoundToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sound-off-16x16.png"))); // NOI18N
    silenceSoundToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    silenceSoundToggleButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            silenceSoundToggleButtonActionPerformed(evt);
        }
    });
    toolbarMenu.add(silenceSoundToggleButton);

    resetSpectrumButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/shutdown.png"))); // NOI18N
    resetSpectrumButton.setToolTipText(bundle.getString("JSpeccy.resetSpectrumButton.toolTipText")); // NOI18N
    resetSpectrumButton.setFocusable(false);
    resetSpectrumButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    resetSpectrumButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    resetSpectrumButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            resetMachineMenuActionPerformed(evt);
        }
    });
    toolbarMenu.add(resetSpectrumButton);

    hardResetSpectrumButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/exit.png"))); // NOI18N
    hardResetSpectrumButton.setToolTipText(bundle.getString("JSpeccy.hardResetSpectrumButton.toolTipText")); // NOI18N
    hardResetSpectrumButton.setFocusable(false);
    hardResetSpectrumButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    hardResetSpectrumButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    hardResetSpectrumButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            hardResetSpectrumButtonActionPerformed(evt);
        }
    });
    toolbarMenu.add(hardResetSpectrumButton);

    getContentPane().add(toolbarMenu, java.awt.BorderLayout.PAGE_START);

    fileMenu.setText(bundle.getString("JSpeccy.fileMenu.text")); // NOI18N

    openSnapshot.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
    openSnapshot.setText(bundle.getString("JSpeccy.openSnapshot.text")); // NOI18N
    openSnapshot.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            openSnapshotActionPerformed(evt);
        }
    });
    fileMenu.add(openSnapshot);

    saveSnapshot.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
    saveSnapshot.setText(bundle.getString("JSpeccy.saveSnapshot.text")); // NOI18N
    saveSnapshot.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveSnapshotActionPerformed(evt);
        }
    });
    fileMenu.add(saveSnapshot);
    fileMenu.add(jSeparator4);

    saveScreenShot.setText(bundle.getString("JSpeccy.saveScreenShot.text")); // NOI18N
    saveScreenShot.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveScreenShotActionPerformed(evt);
        }
    });
    fileMenu.add(saveScreenShot);
    fileMenu.add(jSeparator1);

    recentFilesMenu.setText(bundle.getString("JSpeccy.recentFilesMenu.text")); // NOI18N

    recentFileMenu0.setText(bundle.getString("JSpeccy.recentFileMenu0.text")); // NOI18N
    recentFileMenu0.setEnabled(false);
    recentFileMenu0.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recentFileMenu0ActionPerformed(evt);
        }
    });
    recentFilesMenu.add(recentFileMenu0);

    recentFileMenu1.setText(bundle.getString("JSpeccy.recentFileMenu0.text")); // NOI18N
    recentFileMenu1.setEnabled(false);
    recentFileMenu1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recentFileMenu1ActionPerformed(evt);
        }
    });
    recentFilesMenu.add(recentFileMenu1);

    recentFileMenu2.setText(bundle.getString("JSpeccy.recentFileMenu0.text")); // NOI18N
    recentFileMenu2.setEnabled(false);
    recentFileMenu2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recentFileMenu2ActionPerformed(evt);
        }
    });
    recentFilesMenu.add(recentFileMenu2);

    recentFileMenu3.setText(bundle.getString("JSpeccy.recentFileMenu0.text")); // NOI18N
    recentFileMenu3.setEnabled(false);
    recentFileMenu3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recentFileMenu3ActionPerformed(evt);
        }
    });
    recentFilesMenu.add(recentFileMenu3);

    recentFileMenu4.setText(bundle.getString("JSpeccy.recentFileMenu0.text")); // NOI18N
    recentFileMenu4.setEnabled(false);
    recentFileMenu4.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recentFileMenu4ActionPerformed(evt);
        }
    });
    recentFilesMenu.add(recentFileMenu4);

    fileMenu.add(recentFilesMenu);
    fileMenu.add(jSeparator7);

    thisIsTheEndMyFriend.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK));
    thisIsTheEndMyFriend.setText(bundle.getString("JSpeccy.thisIsTheEndMyFriend.text")); // NOI18N
    thisIsTheEndMyFriend.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            thisIsTheEndMyFriendActionPerformed(evt);
        }
    });
    fileMenu.add(thisIsTheEndMyFriend);

    jMenuBar1.add(fileMenu);

    optionsMenu.setText(bundle.getString("JSpeccy.optionsMenu.text")); // NOI18N

    doubleSizeOption.setText(bundle.getString("JSpeccy.doubleSizeOption.text")); // NOI18N
    doubleSizeOption.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            doubleSizeOptionActionPerformed(evt);
        }
    });
    optionsMenu.add(doubleSizeOption);

    joystickOptionMenu.setText(bundle.getString("JSpeccy.joystickOptionMenu.text")); // NOI18N

    joystickButtonGroup.add(noneJoystick);
    noneJoystick.setSelected(true);
    noneJoystick.setText(bundle.getString("JSpeccy.noneJoystick.text")); // NOI18N
    noneJoystick.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            noneJoystickActionPerformed(evt);
        }
    });
    joystickOptionMenu.add(noneJoystick);

    joystickButtonGroup.add(kempstonJoystick);
    kempstonJoystick.setText(bundle.getString("JSpeccy.kempstonJoystick.text")); // NOI18N
    kempstonJoystick.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            kempstonJoystickActionPerformed(evt);
        }
    });
    joystickOptionMenu.add(kempstonJoystick);

    joystickButtonGroup.add(sinclair1Joystick);
    sinclair1Joystick.setText(bundle.getString("JSpeccy.sinclair1Joystick.text")); // NOI18N
    sinclair1Joystick.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            sinclair1JoystickActionPerformed(evt);
        }
    });
    joystickOptionMenu.add(sinclair1Joystick);

    joystickButtonGroup.add(sinclair2Joystick);
    sinclair2Joystick.setText(bundle.getString("JSpeccy.sinclair2Joystick.text")); // NOI18N
    sinclair2Joystick.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            sinclair2JoystickActionPerformed(evt);
        }
    });
    joystickOptionMenu.add(sinclair2Joystick);

    joystickButtonGroup.add(cursorJoystick);
    cursorJoystick.setText(bundle.getString("JSpeccy.cursorJoystick.text")); // NOI18N
    cursorJoystick.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            cursorJoystickActionPerformed(evt);
        }
    });
    joystickOptionMenu.add(cursorJoystick);

    optionsMenu.add(joystickOptionMenu);

    settingsOptionsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
    settingsOptionsMenu.setText(bundle.getString("JSpeccy.settings.text")); // NOI18N
    settingsOptionsMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            settingsOptionsMenuActionPerformed(evt);
        }
    });
    optionsMenu.add(settingsOptionsMenu);

    jMenuBar1.add(optionsMenu);

    machineMenu.setText(bundle.getString("JSpeccy.machineMenu.text")); // NOI18N
    machineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            silenceSoundToggleButtonActionPerformed(evt);
        }
    });

    pauseMachineMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAUSE, 0));
    pauseMachineMenu.setText(bundle.getString("JSpeccy.pauseMachineMenu.text")); // NOI18N
    pauseMachineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            pauseMachineMenuActionPerformed(evt);
        }
    });
    machineMenu.add(pauseMachineMenu);

    silenceMachineMenu.setText(bundle.getString("JSpeccy.silenceMachineMenu.text_1")); // NOI18N
    silenceMachineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            silenceSoundToggleButtonActionPerformed(evt);
        }
    });
    machineMenu.add(silenceMachineMenu);

    resetMachineMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
    resetMachineMenu.setText(bundle.getString("JSpeccy.resetMachineMenu.text")); // NOI18N
    resetMachineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            resetMachineMenuActionPerformed(evt);
        }
    });
    machineMenu.add(resetMachineMenu);

    hardResetMachineMenu.setText(bundle.getString("JSpeccy.hardResetMachineMenu.text")); // NOI18N
    hardResetMachineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            hardResetSpectrumButtonActionPerformed(evt);
        }
    });
    machineMenu.add(hardResetMachineMenu);

    nmiMachineMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_MASK));
    nmiMachineMenu.setText(bundle.getString("JSpeccy.nmiMachineMenu.text")); // NOI18N
    nmiMachineMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            nmiMachineMenuActionPerformed(evt);
        }
    });
    machineMenu.add(nmiMachineMenu);
    machineMenu.add(jSeparator3);

    hardwareMachineMenu.setText(bundle.getString("JSpeccy.hardwareMachineMenu.text")); // NOI18N

    hardwareButtonGroup.add(spec16kHardware);
    spec16kHardware.setText(bundle.getString("JSpeccy.spec16kHardware.text")); // NOI18N
    spec16kHardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            spec16kHardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(spec16kHardware);

    hardwareButtonGroup.add(spec48kHardware);
    spec48kHardware.setSelected(true);
    spec48kHardware.setText(bundle.getString("JSpeccy.spec48kHardware.text")); // NOI18N
    spec48kHardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            spec48kHardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(spec48kHardware);

    hardwareButtonGroup.add(spec128kHardware);
    spec128kHardware.setText(bundle.getString("JSpeccy.spec128kHardware.text")); // NOI18N
    spec128kHardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            spec128kHardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(spec128kHardware);

    hardwareButtonGroup.add(specPlus2Hardware);
    specPlus2Hardware.setText(bundle.getString("JSpeccy.specPlus2Hardware.text")); // NOI18N
    specPlus2Hardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            specPlus2HardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(specPlus2Hardware);

    hardwareButtonGroup.add(specPlus2AHardware);
    specPlus2AHardware.setText(bundle.getString("JSpeccy.specPlus2AHardware.text")); // NOI18N
    specPlus2AHardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            specPlus2AHardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(specPlus2AHardware);

    hardwareButtonGroup.add(specPlus3Hardware);
    specPlus3Hardware.setText(bundle.getString("JSpeccy.specPlus3Hardware.text")); // NOI18N
    specPlus3Hardware.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            specPlus3HardwareActionPerformed(evt);
        }
    });
    hardwareMachineMenu.add(specPlus3Hardware);

    machineMenu.add(hardwareMachineMenu);

    jMenuBar1.add(machineMenu);

    mediaMenu.setText(bundle.getString("JSpeccy.mediaMenu.text")); // NOI18N

    tapeMediaMenu.setText(bundle.getString("JSpeccy.tapeMediaMenu.text")); // NOI18N

    openTapeMediaMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
    openTapeMediaMenu.setText(bundle.getString("JSpeccy.openTapeMediaMenu.text")); // NOI18N
    openTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            openTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(openTapeMediaMenu);

    playTapeMediaMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
    playTapeMediaMenu.setText(bundle.getString("JSpeccy.playTapeMediaMenu.text")); // NOI18N
    playTapeMediaMenu.setEnabled(false);
    playTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            playTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(playTapeMediaMenu);

    browserTapeMediaMenu.setText(bundle.getString("JSpeccy.browserTapeMediaMenu.text")); // NOI18N
    browserTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            browserTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(browserTapeMediaMenu);

    rewindTapeMediaMenu.setText(bundle.getString("JSpeccy.rewindTapeMediaMenu.text")); // NOI18N
    rewindTapeMediaMenu.setEnabled(false);
    rewindTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            rewindTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(rewindTapeMediaMenu);
    tapeMediaMenu.add(jSeparator5);

    createTapeMediaMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
    createTapeMediaMenu.setText(bundle.getString("JSpeccy.createTapeMediaMenu.text")); // NOI18N
    createTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            createTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(createTapeMediaMenu);

    clearTapeMediaMenu.setText(bundle.getString("JSpeccy.clearTapeMediaMenu.text")); // NOI18N
    clearTapeMediaMenu.setEnabled(false);
    clearTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            clearTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(clearTapeMediaMenu);
    tapeMediaMenu.add(jSeparator6);

    recordStartTapeMediaMenu.setText(bundle.getString("JSpeccy.recordStartTapeMediaMenu.text")); // NOI18N
    recordStartTapeMediaMenu.setEnabled(false);
    recordStartTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recordStartTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(recordStartTapeMediaMenu);

    recordStopTapeMediaMenu.setText(bundle.getString("JSpeccy.recordStopTapeMediaMenu.text")); // NOI18N
    recordStopTapeMediaMenu.setEnabled(false);
    recordStopTapeMediaMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            recordStopTapeMediaMenuActionPerformed(evt);
        }
    });
    tapeMediaMenu.add(recordStopTapeMediaMenu);

    mediaMenu.add(tapeMediaMenu);

    jMenuBar1.add(mediaMenu);

    helpMenu.setText(bundle.getString("JSpeccy.helpMenu.text")); // NOI18N

    imageHelpMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
    imageHelpMenu.setText(bundle.getString("JSpeccy.imageHelpMenu.text")); // NOI18N
    imageHelpMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            imageHelpMenuActionPerformed(evt);
        }
    });
    helpMenu.add(imageHelpMenu);

    aboutHelpMenu.setText(bundle.getString("JSpeccy.aboutHelpMenu.text")); // NOI18N
    aboutHelpMenu.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            aboutHelpMenuActionPerformed(evt);
        }
    });
    helpMenu.add(aboutHelpMenu);

    jMenuBar1.add(helpMenu);

    setJMenuBar(jMenuBar1);

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openSnapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSnapshotActionPerformed
        boolean paused = spectrum.isPaused();
        if( openSnapshotDlg == null ) {
            openSnapshotDlg = new JFileChooser(lastSnapshotDir);
            openSnapshotDlg.setFileFilter(new FileFilterTapeSnapshot());
            currentDirSnapshot = openSnapshotDlg.getCurrentDirectory();
        }
        else
            openSnapshotDlg.setCurrentDirectory(currentDirSnapshot);

        if (!paused)
            spectrum.stopEmulation();

        int status = openSnapshotDlg.showOpenDialog(getContentPane());
        if (status == JFileChooser.APPROVE_OPTION) {
            File selectedFile = openSnapshotDlg.getSelectedFile();
            settings.getRecentFilesSettings().setLastSnapshotDir(lastSnapshotDir);
            if (selectedFile.getName().toLowerCase().endsWith(".sna")
                || selectedFile.getName().toLowerCase().endsWith(".z80")) {
                currentDirSnapshot = openSnapshotDlg.getCurrentDirectory();
                lastSnapshotDir = openSnapshotDlg.getCurrentDirectory().getAbsolutePath();
                rotateRecentFile(selectedFile);
                spectrum.loadSnapshot(selectedFile);
            } else {
                currentDirTape = openSnapshotDlg.getCurrentDirectory();
                lastTapeDir = openSnapshotDlg.getCurrentDirectory().getAbsolutePath();
                lastSnapshotDir = lastTapeDir;
                settings.getRecentFilesSettings().setLastTapeDir(lastTapeDir);
                currentDirSnapshot = currentDirTape;
                spectrum.tape.eject();
                if (spectrum.tape.insert(selectedFile)) {
                    rotateRecentFile(selectedFile);
                    tapeFilename.setText(selectedFile.getName());
                    playTapeMediaMenu.setEnabled(true);
                    clearTapeMediaMenu.setEnabled(true);
                    rewindTapeMediaMenu.setEnabled(true);
                    recordStartTapeMediaMenu.setEnabled(true);
                } else {
                    ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
                    JOptionPane.showMessageDialog(this, bundle.getString("LOAD_TAPE_ERROR"),
                        bundle.getString("LOAD_TAPE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_openSnapshotActionPerformed

    private void thisIsTheEndMyFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thisIsTheEndMyFriendActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        int ret = JOptionPane.showConfirmDialog(getContentPane(),
                  bundle.getString("ARE_YOU_SURE_QUESTION"), bundle.getString("QUIT_JSPECCY"),
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // NOI18N
        if( ret == JOptionPane.YES_OPTION ) {
            spectrum.stopEmulation();
            saveRecentFiles();
            System.exit(0);
        }
    }//GEN-LAST:event_thisIsTheEndMyFriendActionPerformed

    private void doubleSizeOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doubleSizeOptionActionPerformed
        Object source = evt.getSource();
        if( source instanceof javax.swing.JCheckBoxMenuItem )
            doubleSizeToggleButton.setSelected(doubleSizeOption.isSelected());
        else
            doubleSizeOption.setSelected(doubleSizeToggleButton.isSelected());

        jscr.toggleDoubleSize();
        pack();
    }//GEN-LAST:event_doubleSizeOptionActionPerformed

    private void pauseMachineMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseMachineMenuActionPerformed
        Object source = evt.getSource();
        if( source instanceof javax.swing.JCheckBoxMenuItem )
            pauseToggleButton.setSelected(pauseMachineMenu.isSelected());
        else
            pauseMachineMenu.setSelected(pauseToggleButton.isSelected());
        
        if( pauseMachineMenu.isSelected() )
            spectrum.stopEmulation();
        else
            spectrum.startEmulation();
    }//GEN-LAST:event_pauseMachineMenuActionPerformed

    private void resetMachineMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMachineMenuActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        int ret = JOptionPane.showConfirmDialog(getContentPane(),
                  bundle.getString("ARE_YOU_SURE_QUESTION"), bundle.getString("RESET_SPECTRUM"),
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // NOI18N

        if( ret == JOptionPane.YES_OPTION )
            spectrum.reset();
    }//GEN-LAST:event_resetMachineMenuActionPerformed

    private void silenceSoundToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_silenceSoundToggleButtonActionPerformed
        Object source = evt.getSource();
        if( source instanceof javax.swing.JToggleButton )
            silenceMachineMenu.setSelected(silenceSoundToggleButton.isSelected());
        else
            silenceSoundToggleButton.setSelected(silenceMachineMenu.isSelected());

        spectrum.muteSound(silenceSoundToggleButton.isSelected());
    }//GEN-LAST:event_silenceSoundToggleButtonActionPerformed

    private void playTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playTapeMediaMenuActionPerformed
        spectrum.toggleTape();
    }//GEN-LAST:event_playTapeMediaMenuActionPerformed

    private void openTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTapeMediaMenuActionPerformed
        boolean paused = spectrum.isPaused();
        if( openTapeDlg == null ) {
            openTapeDlg = new JFileChooser(lastTapeDir);
            openTapeDlg.setFileFilter(new FileFilterTape());
            currentDirTape = openTapeDlg.getCurrentDirectory();
        }
        else
            openTapeDlg.setCurrentDirectory(currentDirTape);

        if (!paused)
            spectrum.stopEmulation();

        int status = openTapeDlg.showOpenDialog(getContentPane());
        if (status == JFileChooser.APPROVE_OPTION) {
            currentDirTape = openTapeDlg.getCurrentDirectory();
            lastTapeDir = openTapeDlg.getCurrentDirectory().getAbsolutePath();
            settings.getRecentFilesSettings().setLastTapeDir(lastTapeDir);
            spectrum.tape.eject();
            if (spectrum.tape.insert(openTapeDlg.getSelectedFile())) {
                rotateRecentFile(openTapeDlg.getSelectedFile());
                tapeFilename.setText(openTapeDlg.getSelectedFile().getName());
                playTapeMediaMenu.setEnabled(true);
                clearTapeMediaMenu.setEnabled(true);
                rewindTapeMediaMenu.setEnabled(true);
                recordStartTapeMediaMenu.setEnabled(true);
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
                JOptionPane.showMessageDialog(this, bundle.getString("LOAD_TAPE_ERROR"),
                    bundle.getString("LOAD_TAPE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }

        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_openTapeMediaMenuActionPerformed

    private void rewindTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rewindTapeMediaMenuActionPerformed
        spectrum.tape.rewind();
    }//GEN-LAST:event_rewindTapeMediaMenuActionPerformed

    private void imageHelpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageHelpMenuActionPerformed
        keyboardHelper.setResizable(false);
        keyboardHelper.pack();
        keyboardHelper.setVisible(true);
    }//GEN-LAST:event_imageHelpMenuActionPerformed

    private void aboutHelpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutHelpMenuActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        JOptionPane.showMessageDialog(getContentPane(),
            bundle.getString("ABOUT_MESSAGE"), bundle.getString("ABOUT_TITLE"),
            JOptionPane.INFORMATION_MESSAGE,
            new javax.swing.ImageIcon(getClass().getResource("/icons/JSpeccy64x64.png")));
    }//GEN-LAST:event_aboutHelpMenuActionPerformed

    private void nmiMachineMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nmiMachineMenuActionPerformed
        spectrum.triggerNMI();
    }//GEN-LAST:event_nmiMachineMenuActionPerformed

    private void closeKeyboardHelperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeKeyboardHelperActionPerformed
        keyboardHelper.setVisible(false);
    }//GEN-LAST:event_closeKeyboardHelperActionPerformed

    private void saveSnapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSnapshotActionPerformed
        boolean paused = spectrum.isPaused();
        if( saveSnapshotDlg == null ) {
            saveSnapshotDlg = new JFileChooser("/home/jsanchez/Spectrum");
            saveSnapshotDlg.setFileFilter(new FileFilterSaveSnapshot());
            currentDirSaveSnapshot = saveSnapshotDlg.getCurrentDirectory();
        }
        else {
            saveSnapshotDlg.setCurrentDirectory(currentDirSaveSnapshot);
            BasicFileChooserUI chooserUI = (BasicFileChooserUI) saveSnapshotDlg.getUI();
            chooserUI.setFileName("");
        }

        if (!paused)
            spectrum.stopEmulation();

        int status = saveSnapshotDlg.showSaveDialog(getContentPane());
        if( status == JFileChooser.APPROVE_OPTION ) {
            //spectrum.stopEmulation();
            currentDirSaveSnapshot = saveSnapshotDlg.getCurrentDirectory();
            spectrum.saveSnapshot(saveSnapshotDlg.getSelectedFile());
        }
        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_saveSnapshotActionPerformed

    private void noneJoystickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneJoystickActionPerformed

        spectrum.setJoystick(Spectrum.Joystick.NONE);

}//GEN-LAST:event_noneJoystickActionPerformed

    private void kempstonJoystickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kempstonJoystickActionPerformed

        spectrum.setJoystick(Spectrum.Joystick.KEMPSTON);

    }//GEN-LAST:event_kempstonJoystickActionPerformed

    private void sinclair1JoystickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sinclair1JoystickActionPerformed

        spectrum.setJoystick(Spectrum.Joystick.SINCLAIR1);

    }//GEN-LAST:event_sinclair1JoystickActionPerformed

    private void sinclair2JoystickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sinclair2JoystickActionPerformed

        spectrum.setJoystick(Spectrum.Joystick.SINCLAIR2);

    }//GEN-LAST:event_sinclair2JoystickActionPerformed

    private void cursorJoystickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorJoystickActionPerformed

        spectrum.setJoystick(Spectrum.Joystick.CURSOR);
        
    }//GEN-LAST:event_cursorJoystickActionPerformed

    private void spec48kHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spec48kHardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUM48K, true);
        spectrum.reset();
    }//GEN-LAST:event_spec48kHardwareActionPerformed

    private void spec128kHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spec128kHardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUM128K, true);
        spectrum.reset();
    }//GEN-LAST:event_spec128kHardwareActionPerformed

    private void fastEmulationToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastEmulationToggleButtonActionPerformed
        if (fastEmulationToggleButton.isSelected())
            spectrum.changeSpeed(settings.getSpectrumSettings().getFramesInt());
        else
            spectrum.changeSpeed(1);
    }//GEN-LAST:event_fastEmulationToggleButtonActionPerformed

    private void browserTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browserTapeMediaMenuActionPerformed
        tapeBrowserDialog.setVisible(true);
        tapeBrowserDialog.pack();
        tapeCatalog.doLayout();
    }//GEN-LAST:event_browserTapeMediaMenuActionPerformed

    private void closeTapeBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTapeBrowserButtonActionPerformed
        tapeBrowserDialog.setVisible(false);
    }//GEN-LAST:event_closeTapeBrowserButtonActionPerformed

    private void specPlus2HardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specPlus2HardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUMPLUS2, true);
        spectrum.reset();
        
    }//GEN-LAST:event_specPlus2HardwareActionPerformed

    private void specPlus2AHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specPlus2AHardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUMPLUS2A, true);
        spectrum.reset();

    }//GEN-LAST:event_specPlus2AHardwareActionPerformed

    private void settingsOptionsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsOptionsMenuActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        int AYsoundMode = settings.getAY8912Settings().getSoundMode();
        settingsDialog.showDialog(this, bundle.getString("SETTINGS_DIALOG_TITLE"));
        spectrum.loadConfigVars();
        if (AYsoundMode !=  settings.getAY8912Settings().getSoundMode() &&
            !spectrum.isMuteSound()) {
            spectrum.muteSound(true);
            spectrum.muteSound(false);
        }
    }//GEN-LAST:event_settingsOptionsMenuActionPerformed

    private void saveScreenShotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveScreenShotActionPerformed
        boolean paused = spectrum.isPaused();
        if( saveImageDlg == null ) {
            saveImageDlg = new JFileChooser("/home/jsanchez/Spectrum");
            saveImageDlg.setFileFilter(new FileFilterImage());
            currentDirSaveImage = saveImageDlg.getCurrentDirectory();
        }
        else {
            saveImageDlg.setCurrentDirectory(currentDirSaveImage);
            BasicFileChooserUI chooserUI = (BasicFileChooserUI) saveImageDlg.getUI();
            chooserUI.setFileName("");
        }

        if (!paused)
            spectrum.stopEmulation();

        int status = saveImageDlg.showSaveDialog(getContentPane());
        if( status == JFileChooser.APPROVE_OPTION ) {
            //spectrum.stopEmulation();
            currentDirSaveImage = saveImageDlg.getCurrentDirectory();
            spectrum.saveImage(saveImageDlg.getSelectedFile());
        }
        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_saveScreenShotActionPerformed

    private void createTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createTapeMediaMenuActionPerformed
         boolean paused = spectrum.isPaused();
        if( openTapeDlg == null ) {
            openTapeDlg = new JFileChooser("/home/jsanchez/Spectrum");
            openTapeDlg.setFileFilter(new FileFilterTape());
            currentDirTape = openTapeDlg.getCurrentDirectory();
        }
        else
            openTapeDlg.setCurrentDirectory(currentDirTape);

        if (!paused)
            spectrum.stopEmulation();

        int status = openTapeDlg.showOpenDialog(this);
        if( status == JFileChooser.APPROVE_OPTION ) {
            currentDirTape = openTapeDlg.getCurrentDirectory();
            File filename = new File(openTapeDlg.getSelectedFile().getAbsolutePath());
            try {
                filename.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(JSpeccy.class.getName()).log(Level.SEVERE, null, ex);
            }
            spectrum.tape.eject();
            if (spectrum.tape.insert(filename)) {
                tapeFilename.setText(filename.getName());
                playTapeMediaMenu.setEnabled(true);
                clearTapeMediaMenu.setEnabled(true);
                rewindTapeMediaMenu.setEnabled(true);
                recordStartTapeMediaMenu.setEnabled(true);
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
                    JOptionPane.showMessageDialog(this, bundle.getString("LOAD_TAPE_ERROR"),
                        bundle.getString("LOAD_TAPE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_createTapeMediaMenuActionPerformed

    private void hardResetSpectrumButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hardResetSpectrumButtonActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        int ret = JOptionPane.showConfirmDialog(getContentPane(),
                  bundle.getString("ARE_YOU_SURE_QUESTION"), bundle.getString("HARD_RESET_SPECTRUM"),
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // NOI18N

        if( ret == JOptionPane.YES_OPTION )
            spectrum.hardReset();
    }//GEN-LAST:event_hardResetSpectrumButtonActionPerformed

    private void clearTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTapeMediaMenuActionPerformed
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        int ret = JOptionPane.showConfirmDialog(getContentPane(),
            bundle.getString("ARE_YOU_SURE_QUESTION"), bundle.getString("CLEAR_TAPE"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // NOI18N

        if (ret == JOptionPane.YES_OPTION && spectrum.tape.isTapeReady()) {
            File filename = new File(openTapeDlg.getSelectedFile().getAbsolutePath());
            try {
                filename.delete();
                filename.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(JSpeccy.class.getName()).log(Level.SEVERE, null, ex);
            }
            spectrum.tape.eject();
            if (!spectrum.tape.insert(filename)) {
                    JOptionPane.showMessageDialog(this, bundle.getString("LOAD_TAPE_ERROR"),
                        bundle.getString("LOAD_TAPE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_clearTapeMediaMenuActionPerformed

    private void spec16kHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spec16kHardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUM16K, true);
        spectrum.reset();
    }//GEN-LAST:event_spec16kHardwareActionPerformed

    private void specPlus3HardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specPlus3HardwareActionPerformed

        spectrum.selectHardwareModel(MachineTypes.SPECTRUMPLUS3, true);
        spectrum.reset();
    }//GEN-LAST:event_specPlus3HardwareActionPerformed

    private void recordStartTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordStartTapeMediaMenuActionPerformed
        boolean paused = spectrum.isPaused();

        if (!paused)
            spectrum.stopEmulation();

        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N
        if (!spectrum.tape.isTapeReady()) {
            JOptionPane.showMessageDialog(this,
                bundle.getString("RECORD_START_ERROR"), bundle.getString("RECORD_START_TITLE"),
                JOptionPane.ERROR_MESSAGE); // NOI18N
        } else {
            
            if (spectrum.startRecording()) {
                recordStartTapeMediaMenu.setEnabled(false);
                recordStopTapeMediaMenu.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this,
                bundle.getString("RECORD_START_FORMAT_ERROR"),
                    bundle.getString("RECORD_START_FORMAT_TITLE"),
                JOptionPane.ERROR_MESSAGE); // NOI18N
            }
        }

        playTapeMediaMenu.setSelected(false);
        
        if (!paused)
            spectrum.startEmulation();
    }//GEN-LAST:event_recordStartTapeMediaMenuActionPerformed

    private void recordStopTapeMediaMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordStopTapeMediaMenuActionPerformed
        spectrum.stopRecording();
        recordStartTapeMediaMenu.setEnabled(true);
        recordStopTapeMediaMenu.setEnabled(false);
        playTapeMediaMenu.setSelected(true);
    }//GEN-LAST:event_recordStopTapeMediaMenuActionPerformed

    private void loadRecentFile(int idx) {
        ResourceBundle bundle = ResourceBundle.getBundle("gui/Bundle"); // NOI18N

        if (!recentFile[idx].exists()) {
            JOptionPane.showMessageDialog(this, bundle.getString("RECENT_FILE_ERROR"),
                bundle.getString("RECENT_FILE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //NOI18N
        } else {
            if (recentFile[idx].getName().toLowerCase().endsWith(".sna")
                || recentFile[idx].getName().toLowerCase().endsWith(".z80")) {
                boolean paused = spectrum.isPaused();

                if (!paused) {
                    spectrum.stopEmulation();
                }

                spectrum.loadSnapshot(recentFile[idx]);

                if (!paused) {
                    spectrum.startEmulation();
                }
            } else {
                spectrum.tape.eject();
                if (spectrum.tape.insert(recentFile[idx])) {
                    playTapeMediaMenu.setEnabled(true);
                    clearTapeMediaMenu.setEnabled(true);
                    rewindTapeMediaMenu.setEnabled(true);
                    recordStartTapeMediaMenu.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(this, bundle.getString("LOAD_TAPE_ERROR"),
                        bundle.getString("LOAD_TAPE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void recentFileMenu0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFileMenu0ActionPerformed
        loadRecentFile(0);
    }//GEN-LAST:event_recentFileMenu0ActionPerformed

    private void recentFileMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFileMenu1ActionPerformed
        loadRecentFile(1);
    }//GEN-LAST:event_recentFileMenu1ActionPerformed

    private void recentFileMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFileMenu2ActionPerformed
        loadRecentFile(2);
    }//GEN-LAST:event_recentFileMenu2ActionPerformed

    private void recentFileMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFileMenu3ActionPerformed
        loadRecentFile(3);
    }//GEN-LAST:event_recentFileMenu3ActionPerformed

    private void recentFileMenu4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFileMenu4ActionPerformed
        loadRecentFile(4);
    }//GEN-LAST:event_recentFileMenu4ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JSpeccy().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutHelpMenu;
    private javax.swing.JMenuItem browserTapeMediaMenu;
    private javax.swing.JMenuItem clearTapeMediaMenu;
    private javax.swing.JButton closeKeyboardHelper;
    private javax.swing.JButton closeTapeBrowserButton;
    private javax.swing.JMenuItem createTapeMediaMenu;
    private javax.swing.JRadioButtonMenuItem cursorJoystick;
    private javax.swing.JCheckBoxMenuItem doubleSizeOption;
    private javax.swing.JToggleButton doubleSizeToggleButton;
    private javax.swing.JToggleButton fastEmulationToggleButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem hardResetMachineMenu;
    private javax.swing.JButton hardResetSpectrumButton;
    private javax.swing.ButtonGroup hardwareButtonGroup;
    private javax.swing.JMenu hardwareMachineMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem imageHelpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.ButtonGroup joystickButtonGroup;
    private javax.swing.JMenu joystickOptionMenu;
    private javax.swing.JRadioButtonMenuItem kempstonJoystick;
    private javax.swing.JDialog keyboardHelper;
    private javax.swing.JLabel keyboardImage;
    private javax.swing.JMenu machineMenu;
    private javax.swing.JMenu mediaMenu;
    private javax.swing.JLabel modelLabel;
    private javax.swing.JMenuItem nmiMachineMenu;
    private javax.swing.JRadioButtonMenuItem noneJoystick;
    private javax.swing.JMenuItem openSnapshot;
    private javax.swing.JButton openSnapshotButton;
    private javax.swing.JMenuItem openTapeMediaMenu;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JCheckBoxMenuItem pauseMachineMenu;
    private javax.swing.JToggleButton pauseToggleButton;
    private javax.swing.JMenuItem playTapeMediaMenu;
    private javax.swing.JMenuItem recentFileMenu0;
    private javax.swing.JMenuItem recentFileMenu1;
    private javax.swing.JMenuItem recentFileMenu2;
    private javax.swing.JMenuItem recentFileMenu3;
    private javax.swing.JMenuItem recentFileMenu4;
    private javax.swing.JMenu recentFilesMenu;
    private javax.swing.JMenuItem recordStartTapeMediaMenu;
    private javax.swing.JMenuItem recordStopTapeMediaMenu;
    private javax.swing.JMenuItem resetMachineMenu;
    private javax.swing.JButton resetSpectrumButton;
    private javax.swing.JMenuItem rewindTapeMediaMenu;
    private javax.swing.JMenuItem saveScreenShot;
    private javax.swing.JMenuItem saveSnapshot;
    private javax.swing.JMenuItem settingsOptionsMenu;
    private javax.swing.JCheckBoxMenuItem silenceMachineMenu;
    private javax.swing.JToggleButton silenceSoundToggleButton;
    private javax.swing.JRadioButtonMenuItem sinclair1Joystick;
    private javax.swing.JRadioButtonMenuItem sinclair2Joystick;
    private javax.swing.JRadioButtonMenuItem spec128kHardware;
    private javax.swing.JRadioButtonMenuItem spec16kHardware;
    private javax.swing.JRadioButtonMenuItem spec48kHardware;
    private javax.swing.JRadioButtonMenuItem specPlus2AHardware;
    private javax.swing.JRadioButtonMenuItem specPlus2Hardware;
    private javax.swing.JRadioButtonMenuItem specPlus3Hardware;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JDialog tapeBrowserDialog;
    private javax.swing.JTable tapeCatalog;
    private javax.swing.JLabel tapeFilename;
    private javax.swing.JLabel tapeLabel;
    private javax.swing.JMenu tapeMediaMenu;
    private javax.swing.JMenuItem thisIsTheEndMyFriend;
    private javax.swing.JToolBar toolbarMenu;
    // End of variables declaration//GEN-END:variables
    
}
