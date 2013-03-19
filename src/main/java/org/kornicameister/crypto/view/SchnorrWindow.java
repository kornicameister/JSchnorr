package org.kornicameister.crypto.view;

import org.kornicameister.crypto.schnorr.SAlgorithm;
import org.kornicameister.crypto.schnorr.SAlgorithmPQA;
import org.kornicameister.crypto.schnorr.SComplexity;
import org.kornicameister.crypto.sqlite.SQLiteController;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.NoSuchAlgorithmException;

public class SchnorrWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JButton signButton;
    private JButton verifyButton;
    private JTextArea textArea1;
    private JTextField textField1;
    private JPanel fileChooser;
    private JButton loadPropertiesButton;
    private JTextField filePathTextField;
    private JTextArea schnorrParamTA;
    private SAlgorithm sAlgorithm;
    private int selectedTab;
    private File toBeSignedFile;

    public SchnorrWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        signButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onSignAction();
            }
        });
        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onVerifyAction();
            }
        });
        loadPropertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onInitAction();
            }
        });
        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                selectedTab = tabbedPane1.getSelectedIndex();
            }
        });

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        fileChooser = new JPanel();
        fileChooser.setLayout(new BorderLayout());
        final JFileChooser chooser = new JFileChooser();

        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                toBeSignedFile = chooser.getSelectedFile();
                filePathTextField.setText(toBeSignedFile.getAbsolutePath());
            }
        });

        fileChooser.add(chooser, BorderLayout.CENTER);
    }

    private void onSignAction() {
        Integer id = null;
        switch (selectedTab) {
            case 1: {
                String message = this.textArea1.getText();
                try {
                    id = this.sAlgorithm.sign(message);
                } catch (NoSuchAlgorithmException e) {
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "Sign error", JOptionPane.ERROR_MESSAGE);
                }
                if (id != null) {
                    this.textField1.setText(String.format("%d", id));
                }
            }
            break;
            case 2: {
                try {
                    id = this.sAlgorithm.sign(new FileInputStream(this.toBeSignedFile));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "Sign error", JOptionPane.ERROR_MESSAGE);
                }
                if (id != null) {
                    this.textField1.setText(String.format("%d", id));
                }
            }
        }
    }

    private void onVerifyAction() {
        Integer id = Integer.valueOf(this.textField1.getText());

        switch (selectedTab) {
            case 1: {
                try {
                    String message = this.textArea1.getText();
                    if (this.sAlgorithm.verify(message, id)) {
                        JOptionPane.showMessageDialog(null,
                                "All is good, message is good...HERO",
                                "SUCCESS",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Are you trying to fool me ?",
                                "FAILURE",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NoSuchAlgorithmException e) {
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "Verify error", JOptionPane.ERROR_MESSAGE);
                }
            }
            break;
            case 2: {
                try {
                    if (this.sAlgorithm.verify(new FileInputStream(this.toBeSignedFile), id)) {
                        JOptionPane.showMessageDialog(null,
                                "All is good, message is good...HERO",
                                "SUCCESS",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Are you trying to fool me ?",
                                "FAILURE",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getStackTrace(), "Verify error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


    }

    private void onInitAction() {
        JFileChooser chooser = new JFileChooser("./");
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file == null) {
                    return false;
                }
                return file.getName().contains(".properties") || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Properties file allowed only";
            }
        });
        if (chooser.showOpenDialog(this.contentPane) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                this.sAlgorithm = new SAlgorithm(
                        SAlgorithmPQA.loadFromProperties(SComplexity.S_320, file.getAbsolutePath()),
                        SQLiteController.getConnection(file));
                StringBuilder builder = new StringBuilder();
                builder.append(this.sAlgorithm.getPQA());
                this.schnorrParamTA.setText(builder.toString());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getStackTrace(), "Bad error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
