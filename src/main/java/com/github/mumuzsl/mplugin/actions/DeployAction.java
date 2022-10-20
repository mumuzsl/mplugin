package com.github.mumuzsl.mplugin.actions;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.mumuzsl.mplugin.entity.SshInfo;
import com.github.mumuzsl.mplugin.entity.UploadInfo;
import com.github.mumuzsl.mplugin.service.DeployService;
import com.github.mumuzsl.mplugin.ui.DeployDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.DialogManager;
import jnr.ffi.annotations.In;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.TransferListener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * @auther zhaosenlin
 * @date 2022/9/23 20:59
 */
public class DeployAction extends AnAction {

    public static final String TEST_FILE_PATH
            = "D:\\code\\didaima_java\\jeecg-boot-module-system\\target\\jeecg-boot-module-system-3.2.0.jar";
    public static final File TEST_FILE = new File(TEST_FILE_PATH);
    public static final UploadInfo TEST_INFO;

    static {
        SshInfo sshInfo = new SshInfo();
        sshInfo.setIp("192.168.237.133");
        sshInfo.setUsername("root");
        sshInfo.setPassword("1");

        TEST_INFO = new UploadInfo(sshInfo);
        TEST_INFO.setDest("/home");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        FileChooserDescriptor descriptor   = new FileChooserDescriptor(true, false, true, true, false, false);
        VirtualFile[]         virtualFiles = FileChooser.chooseFiles(descriptor, event.getProject(), null);

        if (virtualFiles.length == 0) {
            return;
        }

        final DeployService deployService = DeployService.getInstance();
        final UploadInfo    uploadInfo    = deployService.getState();
        if (uploadInfo == null) {
            Messages.showErrorDialog("error", "error");
            return;
        }

        uploadInfo.setVirtualFiles(List.of(virtualFiles));
        final long totalSize = uploadInfo.getVirtualFiles().stream()
                .map(VirtualFile::getLength).flatMapToLong(LongStream::of).sum();

        DeployDialog deployDialog = new DeployDialog(event.getProject());
        deployDialog.show();

        if (!deployDialog.isOK()) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(event.getProject(), "Upload file") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                long curSize = 1;
                try {
                    do {
                        indicator.setFraction(curSize * 0.1 / (totalSize + 1));
                        System.out.println(String.format("%s %s %s", indicator.getFraction(), curSize, totalSize));
                        Thread.sleep(1000);
                    } while ((curSize = deployService.getUploadPress().get()) < totalSize && curSize >= 0);
                    indicator.setFraction(1.0);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        });

        new Thread(deployService::upload).start();
    }
}
