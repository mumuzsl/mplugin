package com.github.mumuzsl.mplugin.ui;

import com.github.mumuzsl.mplugin.entity.SshInfo;
import com.github.mumuzsl.mplugin.entity.UploadInfo;
import com.github.mumuzsl.mplugin.service.DeployService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @auther zhaosenlin
 * @date 2022/9/24 21:43
 */
public class DeployDialog extends DialogWrapper {

    public DeployDialog(@Nullable Project project) {
        super(project);
        init();
        setTitle("deploy");
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        final DeployService deployService = DeployService.getInstance();
        final UploadInfo    uploadInfo    = deployService.getState();

        if (uploadInfo == null) return;

        final SshInfo sshInfo = uploadInfo.getSshInfo();

        sshInfo.setIp(ipTextArea.getText());
        sshInfo.setUsername(usernameTextArea.getText());
        sshInfo.setPassword(passwordTextArea.getText());
        uploadInfo.setSshInfo(sshInfo);
        uploadInfo.setDest(destTextArea.getText());
    }

    JTextArea ipTextArea = new JTextArea();
    JTextArea usernameTextArea = new JTextArea();
    JTextArea passwordTextArea = new JTextArea();
    JTextArea destTextArea = new JTextArea();

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // FlowLayout fl = new FlowLayout();
        GridLayout gl = new GridLayout(4, 4);
        gl.setVgap(3);

        ipTextArea.setEditable(true);
        ipTextArea.setSize(50, 10);
        usernameTextArea.setEditable(true);
        usernameTextArea.setSize(50, 10);
        passwordTextArea.setEditable(true);
        passwordTextArea.setSize(50, 10);
        destTextArea.setEditable(true);
        destTextArea.setSize(50, 10);

        JLabel ipLabel       = new JLabel("ip");
        JLabel usernameLabel = new JLabel("username");
        JLabel passwordLabel = new JLabel("password");
        JLabel destLabel     = new JLabel("dest");

        JPanel inputPanel = new JPanel();
        // inputPanel.setSize(60, 60);
        inputPanel.setLayout(gl);

        inputPanel.add(ipLabel);
        inputPanel.add(ipTextArea);
        inputPanel.add(usernameLabel);
        inputPanel.add(usernameTextArea);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordTextArea);
        inputPanel.add(destLabel);
        inputPanel.add(destTextArea);

        return inputPanel;
    }
}
